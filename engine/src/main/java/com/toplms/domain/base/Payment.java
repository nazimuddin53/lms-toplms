package com.toplms.domain.base;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A record of a (simulated) payment made when a tenant is provisioned on a paid plan. No real
 * payment gateway is involved — see TenantRegistrationController's fake payment step — this
 * exists purely so SuperAdmin has a billing history to look at per tenant.
 */
@Entity
@Table(name = "payment")
@Data
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_tenant"))
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_plan"))
    private SubscriptionPlan plan;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false, length = 10)
    private String currency;

    // Always "SUCCEEDED" today — there's no real gateway to fail against — but kept as a
    // column rather than hardcoded in the view so a real gateway can slot in later.
    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
