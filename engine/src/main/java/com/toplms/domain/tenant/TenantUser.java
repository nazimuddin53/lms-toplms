package com.toplms.domain.tenant;

import com.toplms.domain.base.Role;
import com.toplms.domain.base.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tenant_users",
        uniqueConstraints = {
                // Enforces that an email is unique WITHIN a single tenant space
                @UniqueConstraint(name = "uq_tenant_user_email", columnNames = {"tenant_id", "email"})
        }
)
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TenantUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- THE MULTI-TENANT ROUTING LINK ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tenant_users_tenant"))
    private Tenant tenant;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255) // Length allows room for secure password hashes (BCrypt)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tenant_users_role"))
    private Role role;

    @Column(nullable = false)
    private boolean active = true; // For easily suspending a tenant account

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
