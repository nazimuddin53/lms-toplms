package com.toplms.master.tenant;

import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import com.toplms.master.subscriptionPlan.SubscriptionPlanRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository planRepository;

    // Spring constructor injection automatically supplies our repository bean
    public TenantService(TenantRepository tenantRepository, SubscriptionPlanRepository planRepository) {
        this.tenantRepository = tenantRepository;
        this.planRepository = planRepository;
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
}
