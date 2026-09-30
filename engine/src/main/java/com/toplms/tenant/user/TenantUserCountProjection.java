package com.toplms.tenant.user;

public interface TenantUserCountProjection {
    String getTenantId();
    long getUserCount();
}
