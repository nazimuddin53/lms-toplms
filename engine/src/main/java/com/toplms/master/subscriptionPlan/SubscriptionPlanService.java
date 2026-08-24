package com.toplms.master.subscriptionPlan;

import com.toplms.domain.base.SubscriptionPlan;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    // Spring constructor injection automatically supplies our repository bean
    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
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
}
