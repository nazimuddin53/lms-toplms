package com.toplms.master.tenant;

import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import com.toplms.master.subscriptionPlan.SubscriptionPlanRepository;
import com.toplms.tenant.course.repository.CourseRepository;
import com.toplms.tenant.course.repository.TenantCourseCountProjection;
import com.toplms.tenant.user.TenantUserCountProjection;
import com.toplms.tenant.user.TenantUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository planRepository;
    private final TenantUserRepository tenantUserRepository;
    private final CourseRepository courseRepository;

    // Spring constructor injection automatically supplies our repository bean
    public TenantService(TenantRepository tenantRepository, SubscriptionPlanRepository planRepository,
                          TenantUserRepository tenantUserRepository, CourseRepository courseRepository) {
        this.tenantRepository = tenantRepository;
        this.planRepository = planRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.courseRepository = courseRepository;
    }

//    @Transactional(readOnly = true)
    public Optional<Tenant> getTenantById(String id) {
        return tenantRepository.findById(id);
    }
    public Optional<Tenant> getTenantBySubdomain(String subdomain) {
        try {
            return tenantRepository.findBySubdomain(subdomain);
        }  catch (Exception e) {
            return Optional.empty();
        }
    }

    public Tenant createTenant(Tenant tenant) {
        if (tenantRepository.existsById(tenant.getId())) {
            throw new IllegalArgumentException("Tenant ID already exists.");
        }

        if (tenant.getPlan() == null) {
            // 1. Fetch the default target plan from the database (e.g., FREE tier)
            SubscriptionPlan defaultPlan = planRepository.findById("FREE")
                    .orElseThrow(() -> new IllegalStateException("System Error: Default FREE subscription plan not seeded."));

            // 2. Assign the plan entity to the tenant object
            tenant.setPlan(defaultPlan);

        }

        // 3. Save the tenant row with its new foreign key reference linked
        return tenantRepository.save(tenant);
    }

    // --- SuperAdmin tenant management ---

    public List<TenantSummaryDto> getAllTenantSummaries() {
        List<Tenant> tenants = tenantRepository.findAllWithPlan();
        Map<String, Long> userCounts = tenantUserRepository.countUsersGroupedByTenant().stream()
                .collect(Collectors.toMap(TenantUserCountProjection::getTenantId, TenantUserCountProjection::getUserCount));
        Map<String, Long> courseCounts = courseRepository.countCoursesGroupedByTenant().stream()
                .collect(Collectors.toMap(TenantCourseCountProjection::getTenantId, TenantCourseCountProjection::getCourseCount));

        return tenants.stream()
                .map(t -> new TenantSummaryDto(t, userCounts.getOrDefault(t.getId(), 0L), courseCounts.getOrDefault(t.getId(), 0L)))
                .toList();
    }

    public Optional<TenantSummaryDto> getTenantSummary(String tenantId) {
        return tenantRepository.findById(tenantId).map(t -> new TenantSummaryDto(
                t,
                tenantUserRepository.countByTenant_Id(tenantId),
                courseRepository.countByTenantIdAcrossAllTenants(tenantId)));
    }

    public Tenant setActive(String tenantId, boolean active) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        tenant.setActive(active);
        return tenantRepository.save(tenant);
    }

    public Tenant changePlan(String tenantId, String newPlanId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        SubscriptionPlan plan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found: " + newPlanId));

        // Deliberately not touching subscriptionStartedAt/EndsAt here — this is a superadmin
        // override, not a billing event. Renewal/proration semantics belong to the future
        // payment-integration phase.
        tenant.setPlan(plan);
        return tenantRepository.save(tenant);
    }
}
