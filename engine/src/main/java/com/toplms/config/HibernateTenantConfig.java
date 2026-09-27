package com.toplms.config;

import com.toplms.core.multitenancy.CurrentTenantIdentifierResolverImpl;
import org.hibernate.cfg.MultiTenancySettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers {@link CurrentTenantIdentifierResolverImpl} with Hibernate so that entities
 * carrying a {@code @TenantId} field (see {@code Course}) get their {@code tenant_id}
 * predicate and auto-fill handled by the ORM instead of by hand in every query.
 *
 * <p>This project uses the "discriminator column" flavor of Hibernate multi-tenancy —
 * one shared database/schema, isolation enforced by a {@code tenant_id} column — not the
 * separate-database-per-tenant flavor, so no {@code MultiTenantConnectionProvider} is
 * needed here; a {@code CurrentTenantIdentifierResolver} is the whole story.
 */
@Configuration
public class HibernateTenantConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernateTenantIdentifierResolverCustomizer(
            CurrentTenantIdentifierResolverImpl resolver) {
        return properties -> properties.put(MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }
}
