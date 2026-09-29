package com.toplms.tenant.course.repository;

public interface TenantCourseCountProjection {
    String getTenantId();
    long getCourseCount();
}
