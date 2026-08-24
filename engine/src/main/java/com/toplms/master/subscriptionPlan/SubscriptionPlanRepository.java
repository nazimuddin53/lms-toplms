package com.toplms.master.subscriptionPlan;

import com.toplms.domain.base.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan,String> {

    @Query("SELECT t FROM SubscriptionPlan t WHERE LOWER(t.id) = LOWER(id)")
    Optional<SubscriptionPlan> findByHost(String host);

}
