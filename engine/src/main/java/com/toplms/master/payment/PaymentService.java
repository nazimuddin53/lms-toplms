package com.toplms.master.payment;

import com.toplms.domain.base.Payment;
import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import com.toplms.master.subscriptionPlan.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    // The statuses a superadmin can move a record between. There's no real gateway yet (see
    // Payment's javadoc), so these are bookkeeping values only — kept here rather than in the
    // template so the controller can reject anything a hand-crafted POST invents.
    public static final List<String> KNOWN_STATUSES = List.of("SUCCEEDED", "PENDING", "FAILED", "REFUNDED");

    private final PaymentRepository paymentRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public PaymentService(PaymentRepository paymentRepository, SubscriptionPlanRepository subscriptionPlanRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
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

    // --- SuperAdmin payment management ---

    public List<Payment> getAll() {
        return paymentRepository.findAllWithTenantAndPlan();
    }

    public Optional<Payment> getById(Long id) {
        return paymentRepository.findByIdWithTenantAndPlan(id);
    }

    /**
     * Correct an existing payment record. Only amount / currency / status / plan are editable:
     * the tenant a payment belongs to is part of its identity, and createdAt is marked
     * updatable = false on the entity so Hibernate never writes it again.
     *
     * <p>{@code @Transactional} makes Spring open a transaction around the whole method and
     * commit on normal return (rollback on a RuntimeException). Inside it the Payment we loaded
     * is a <em>managed</em> entity — Hibernate's dirty-checking flushes the setters below at
     * commit time, so the explicit save() is belt-and-braces rather than strictly required.
     * Without the annotation each repository call would be its own short transaction and the
     * entity would be detached between them.
     */
    @Transactional
    public Payment updatePayment(Long id, double amount, String currency, String status, String planId) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + id));

        if (!KNOWN_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Unknown payment status: " + status);
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Payment amount cannot be negative: " + amount);
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found: " + planId));

        existing.setAmount(amount);
        existing.setCurrency(currency);
        existing.setStatus(status);
        existing.setPlan(plan);
        return paymentRepository.save(existing);
    }

    /**
     * Hard-deletes the record. Nothing references Payment (it's the leaf of the billing graph),
     * so unlike SubscriptionPlanService.deletePlan() there's no in-use check to make. Note this
     * destroys billing history — the UI asks for confirmation before calling it.
     */
    @Transactional
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new IllegalArgumentException("Payment not found: " + id);
        }
        paymentRepository.deleteById(id);
    }
}