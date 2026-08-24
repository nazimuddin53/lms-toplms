package com.toplms.core.context;

import com.toplms.core.enumType.LoadingPageType;
import com.toplms.domain.base.Tenant;

import java.util.Optional;

public final class TenantContext {
    private static final ThreadLocal<Tenant> contextHolder = new ThreadLocal<>();
    private static final ThreadLocal<LoadingPageType> contextType = new ThreadLocal<>();

    private TenantContext() {}
    public static void setCurrentTenant(Tenant tenant) {
        contextHolder.set(tenant);
    }

    public static Tenant getCurrentTenant() {
        return contextHolder.get();
    }
    public static void setCurrenPageType(LoadingPageType pageType) {
        contextType.set(pageType);
    }

    public static LoadingPageType getCurrentPageType() {
        return contextType.get();
    }
    public static Optional<Tenant> getTenantOptional() {
        return Optional.ofNullable(contextHolder.get());
    }

    public static void clear() {
        contextHolder.remove();
    }
}
