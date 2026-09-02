package com.toplms.core.interceptor;

import com.toplms.core.context.TenantContext;
import com.toplms.core.enumType.LoadingPageType;
import com.toplms.domain.base.Tenant;
import com.toplms.master.tenant.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;



@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private final TenantService tenantService ;
    public TenantInterceptor(TenantService tenantService) {
        this.tenantService = tenantService;
    }
    public static final LoadingPageType LANDING_PAGE_TYPE = LoadingPageType.MAIN;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String serverName = request.getServerName();
        System.out.println("serverName : " + serverName);
        if (serverName == null || serverName.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid request: Host domain cannot be resolved.");
            return false;
        }
        String host = serverName.toLowerCase().trim();

        // 1. Identify Root Domains (Main SaaS Platform Landing Pages)
        // Matches localhost, loopback, and any external/cloud/tunnel domain
        // that is NOT a registered tenant subdomain of localhost.
        if ( host.equals("localhost") || host.equals("127.0.0.1") || host.equals("0:0:0:0:0:0:0:1")
                || isExternalPlatformDomain(host)) {
            TenantContext.setCurrenPageType(LoadingPageType.MAIN);
            return true;
        }

        // 2. Identify Tenant Subdomains
        // 2. Resolve unique ID from domain lookup
        Optional<Tenant> tenantIdOpt = resolveTenantFromDomain(host);
        TenantContext.setCurrenPageType(LoadingPageType.TENANT);
        if (tenantIdOpt == null || tenantIdOpt.isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Could not map the domain '" + serverName + "' to a valid tenant.");
            return false;
        }

        TenantContext.setCurrentTenant(tenantIdOpt.get());
        return true;
    }

    private Optional<Tenant> resolveTenantFromDomain(String host) {
        String cleanHost = host.replace("www.", "");

        if (cleanHost.endsWith("localhost") ) {
            cleanHost = cleanHost.replace(".localhost", "");
        }
        if (cleanHost.endsWith("127.0.0.1")) {
            cleanHost = cleanHost.replace(".127.0.0.1", "");
        }
        System.out.println("Processing lookup for target host domain: " + cleanHost);
        TenantContext.setCurrenPageType(LoadingPageType.TENANT);
        // Fetch your tenant from the database using the unique host record
        Optional<Tenant> tenantOptional = this.tenantService.getTenantBySubdomain(cleanHost);

        if (tenantOptional.isPresent()) {
            Tenant tenant = tenantOptional.get();
            // Assuming your Tenant entity exposes an internal key likegetId() or getId()
            // Returning the internal database key isolates you if the client changes their URL later!
            return Optional.of(tenant);
        }
        // Return empty if no registered tenant owns this domain
        return Optional.empty();
    }
    /**
     * Returns true when the host is an external platform domain
     * (tunnel, cloud hosting, or custom domain) rather than a
     * tenant subdomain of localhost/127.0.0.1.
     *
     * Tenant subdomains always end with .localhost or .127.0.0.1
     * (e.g. demo.localhost). Everything else (lhr.life, onrender.com,
     * railway.app, fly.dev, serveousercontent.com, custom domains)
     * is treated as the main platform root.
     */
    private boolean isExternalPlatformDomain(String host) {
        // Tenant subdomains are always *.localhost or *.127.0.0.1
        // Any other domain is the platform root
        return !host.endsWith(".localhost")
                && !host.endsWith(".127.0.0.1")
                && !host.equals("localhost")
                && !host.equals("127.0.0.1");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
    }
}
