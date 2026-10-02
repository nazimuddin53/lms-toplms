package com.toplms.master.payment;

import com.toplms.domain.base.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByTenant_IdOrderByCreatedAtDesc(String tenantId);

    // Payment.tenant and Payment.plan are both FetchType.LAZY, so rendering a 50-row table
    // would otherwise fire 1 + 50 + 50 queries. JOIN FETCH pulls all three in one statement —
    // same trick TenantRepository.findAllWithPlan() uses for the tenant list.
    @Query("SELECT p FROM Payment p JOIN FETCH p.tenant JOIN FETCH p.plan ORDER BY p.createdAt DESC")
    List<Payment> findAllWithTenantAndPlan();

    @Query("SELECT p FROM Payment p JOIN FETCH p.tenant JOIN FETCH p.plan WHERE p.id = :id")
    Optional<Payment> findByIdWithTenantAndPlan(@Param("id") Long id);
}
