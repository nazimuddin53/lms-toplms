package com.toplms.master.subscriptionPlan;

import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.master.tenant.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SubscriptionPlanService {

    // The full set of module keys any plan can toggle — matches what's already seeded by
    // SubscriptionPlanDataInitializer and referenced as "moduleFlag" in the menu_json beans.
    private static final List<String> KNOWN_MODULE_KEYS = List.of("quizzes", "assignments", "certificates");

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TenantRepository tenantRepository;

    // Spring constructor injection automatically supplies our repository bean
    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository, TenantRepository tenantRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.tenantRepository = tenantRepository;
    }

    public Optional<SubscriptionPlan> getById(String id) {
        try {
            return subscriptionPlanRepository.findById(id);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public List<SubscriptionPlan> getAll() {
        try {
            return this.subscriptionPlanRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public SubscriptionPlan createPlan(SubscriptionPlan plan, List<String> enabledModules, Double usdPrice) {
        if (subscriptionPlanRepository.existsById(plan.getId())) {
            throw new IllegalArgumentException("Plan code already exists: " + plan.getId());
        }
        plan.setModulesJSON(buildModulesMap(enabledModules));
        plan.setPriceJSON(buildPriceMap(null, usdPrice));
        return subscriptionPlanRepository.save(plan);
    }

    public SubscriptionPlan updatePlan(String id, SubscriptionPlan updates, List<String> enabledModules, Double usdPrice) {
        SubscriptionPlan existing = getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + id));
        existing.setName(updates.getName());
        existing.setMaxCourses(updates.getMaxCourses());
        existing.setMaxStudents(updates.getMaxStudents());
        existing.setModulesJSON(buildModulesMap(enabledModules));
        // Merge (not replace) so currencies seeded outside the form, e.g. EUR/BDT on
        // GROWTH/ENTERPRISE, survive an edit that only ever collects a USD price.
        existing.setPriceJSON(buildPriceMap(existing.getPriceJSON(), usdPrice));
        return subscriptionPlanRepository.save(existing);
    }

    public void deletePlan(String id) {
        long tenantsOnPlan = tenantRepository.countByPlan_Id(id);
        if (tenantsOnPlan > 0) {
            throw new IllegalStateException(
                    "Cannot delete plan '" + id + "': " + tenantsOnPlan + " tenant(s) are currently on it.");
        }
        subscriptionPlanRepository.deleteById(id);
    }

    // Checkboxes for unset keys simply don't submit at all, so the only reliable way to
    // capture "this module is now off" is to rebuild the full known-key map every time,
    // rather than binding modulesJSON directly as a Map-indexed form property.
    private Map<String, Boolean> buildModulesMap(List<String> enabledModules) {
        Set<String> enabledSet = enabledModules == null ? Set.of() : new HashSet<>(enabledModules);
        Map<String, Boolean> map = new LinkedHashMap<>();
        KNOWN_MODULE_KEYS.forEach(key -> map.put(key, enabledSet.contains(key)));
        return map;
    }

    private Map<String, Double> buildPriceMap(Map<String, Double> existing, Double usdPrice) {
        Map<String, Double> map = existing != null ? new LinkedHashMap<>(existing) : new LinkedHashMap<>();
        map.put("USD", usdPrice != null ? usdPrice : 0.0);
        return map;
    }
}
