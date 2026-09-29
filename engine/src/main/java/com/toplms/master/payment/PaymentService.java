package com.toplms.master.payment;

import com.toplms.domain.base.Payment;
import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment recordPayment(Tenant tenant, SubscriptionPlan plan, double amount, String currency) {
        Payment payment = new Payment();
        payment.setTenant(tenant);
        payment.setPlan(plan);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus("SUCCEEDED");
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsForTenant(String tenantId) {
        return paymentRepository.findByTenant_IdOrderByCreatedAtDesc(tenantId);
    }
}
