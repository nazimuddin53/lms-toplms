package com.toplms.core.multitenancy;

import com.toplms.core.context.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Tells Hibernate which tenant is "current" for the request being processed, so that any
 * entity carrying a Hibernate {@code @TenantId} field gets {@code WHERE tenant_id = ?}
 * appended to every query and auto-filled on every insert.
 *
 * TenantInterceptor populates {@link TenantContext} per-request; this class is just the
 * bridge Hibernate calls into to read that value. Requests with no tenant in context
 * (the SuperAdmin/platform space) fall back to a fixed sentinel identifier, since Hibernate
 * requires resolveCurrentTenantIdentifier() to never return null.
 */
@Component
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {

    public static final String SYSTEM_TENANT_ID = "__system__";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getCurrentTenantId();
        return tenantId != null ? tenantId : SYSTEM_TENANT_ID;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
