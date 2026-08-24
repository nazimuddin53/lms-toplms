package com.toplms.tenant.course.domain;


//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A course — the first piece of the actual LMS domain.
 *
 * <p>This lives in {@code com.toplms.tenant} (the tenant-plane package), as
 * opposed to {@code com.toplms.master} (control-plane: tenants, users, roles).
 * Tenant scoping (a {@code tenant_id} column populated via Hibernate's
 * {@code @TenantId}) will be reintroduced once the master/tenant domain is
 * back in place.
 */
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//public class Course {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, length = 160)
//    private String title;
//
//    @Column(columnDefinition = "text")
//    private String description;
//
//    @CreationTimestamp
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private Instant createdAt;
//}
