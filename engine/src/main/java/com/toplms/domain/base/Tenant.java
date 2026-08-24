package com.toplms.domain.base;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tenant")
@Data
@Getter
@Setter
@NoArgsConstructor
public class Tenant {

    @Id
    @Column(nullable = false, unique = true, length = 150)
    private String id; // e.g., "alpha-university", "mit-academy" (used in X-Tenant-ID headers)

    @Column(nullable = false, length = 100)
    private String name; // Elegant display name, e.g., "Alpha University Campus"

    @Column(nullable = false, name = "business_name")
    private String companyName; // e.g., "MIT University"

    @Column(name = "subdomain")
    private String subdomain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "plan_id",             // Name of foreign key column in 'tenants' table
            nullable = false,             // Every tenant must have a subscription plan
            foreignKey = @ForeignKey(name = "fk_tenants_subscription_plan") // Explicit constraint name
    )
    private SubscriptionPlan plan;

    @Column(name = "subscription_started_at")
    private LocalDateTime subscriptionStartedAt;

    @Column(name = "subscription_ends_at")
    private LocalDateTime subscriptionEndsAt;

    @Column(nullable = false)
    private boolean active = true; // For easily suspending a tenant account

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Lifecycle hooks to handle dates automatically
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
