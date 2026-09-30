package com.toplms.master.payment;

import com.toplms.domain.base.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByTenant_IdOrderByCreatedAtDesc(String tenantId);
}
