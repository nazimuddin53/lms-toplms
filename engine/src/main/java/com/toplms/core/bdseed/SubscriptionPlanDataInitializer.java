package com.toplms.core.bdseed;

import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.master.subscriptionPlan.SubscriptionPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@org.springframework.core.annotation.Order(1)
public class SubscriptionPlanDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPlanDataInitializer.class);
    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanDataInitializer(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(String... args) {
        try {

            // 1. Seed Free Plan
            if (!planRepository.existsById("FREE")) {
                Map<String, Boolean> freeModules = Map.of(
                        "quizzes", true,
                        "assignments", false,
                        "certificates", false
                );
                Map<String, Double> freePricing = Map.of("USD", 0.0, "EUR", 0.0);

                planRepository.save(new SubscriptionPlan("FREE", "FREE", 10, 300, freeModules, freePricing));
            }

            // 2. Seed Growth Plan
            if (!planRepository.existsById("GROWTH")) {
                Map<String, Boolean> growthModules = Map.of(
                        "quizzes", true,
                        "assignments", true,
                        "certificates", true
                );
                Map<String, Double> growthPricing = Map.of("USD", 49.00, "EUR", 45.00, "BDT", 5500.0);

                planRepository.save(new SubscriptionPlan("GROWTH", "Growth Tier", 20, 500, growthModules, growthPricing));
            }
            log.info("Done initializing subscription plans.");
        } catch (Exception e) {
            log.error("Critical failure during subscription plan space initialization context: ", e);
            throw new RuntimeException("Workspace bootstrapping failed", e);
        }
    }
}
