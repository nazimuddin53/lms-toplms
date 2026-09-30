package com.toplms.tenant.course.repository;

import com.toplms.tenant.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    // Course carries Hibernate's @TenantId, which auto-injects "WHERE tenant_id = ?" (using
    // whatever CurrentTenantIdentifierResolverImpl currently resolves) into any HQL/Criteria
    // query against this entity. For a SuperAdmin request that resolves to the "__system__"
    // sentinel, so an ordinary JPQL count here would silently return 0 for every real tenant.
    // A native query that returns a scalar/projection (never Course/List<Course>) never goes
    // through the Course entity persister, so the @TenantId filter never engages — this is
    // the one safe way to count across all tenants. Do not change these to return Course.
    @Query(value = "SELECT COUNT(*) FROM course WHERE tenant_id = :tenantId", nativeQuery = true)
    long countByTenantIdAcrossAllTenants(@Param("tenantId") String tenantId);

    @Query(value = "SELECT tenant_id AS tenantId, COUNT(*) AS courseCount FROM course GROUP BY tenant_id", nativeQuery = true)
    List<TenantCourseCountProjection> countCoursesGroupedByTenant();
}
