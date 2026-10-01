package com.toplms.web.tenant;

import com.toplms.config.AppHostProperties;
import com.toplms.core.context.TenantContext;
import com.toplms.domain.base.Tenant;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Year;

/**
 * Feeds the shared tenant layout ({@code templates/tenant/layout/tenant-layout.html})
 * with the branding it needs on <em>every</em> tenant page — header wordmark, footer
 * copyright, the "powered by" backlink.
 *
 * <h2>Why a {@code @ControllerAdvice} instead of repeating this in each controller?</h2>
 * A {@code @ControllerAdvice} is a bean whose {@code @ModelAttribute} /
 * {@code @ExceptionHandler} / {@code @InitBinder} methods apply across controllers
 * rather than to a single one. A {@code @ModelAttribute} method declared here runs
 * <em>before</em> every handler method in the application and contributes to that
 * request's {@link Model}. So the layout can rely on {@code ${appName}} and friends
 * always being present — the tenant signup page, the landing page, and any future
 * tenant page get identical branding without one line of duplicated controller code.
 *
 * <h2>Ordering — why {@link TenantContext} is already populated here</h2>
 * Spring MVC's request lifecycle is:
 * <pre>
 *   HandlerInterceptor.preHandle()   ← TenantInterceptor resolves the subdomain
 *        ↓                              and stashes the Tenant in a ThreadLocal
 *   @ModelAttribute methods (this class)
 *        ↓
 *   @GetMapping handler method
 *        ↓
 *   view rendering (Thymeleaf)
 *        ↓
 *   HandlerInterceptor.afterCompletion()  ← TenantContext.clear()
 * </pre>
 * Interceptors run first, so by the time this method executes the tenant is resolved.
 * That ordering is the whole reason the {@code ThreadLocal} approach works.
 *
 * <h2>Platform (non-tenant) requests</h2>
 * On the platform root ({@code localhost}, custom domains) there is no current tenant,
 * so only the host-neutral attributes are contributed and the tenant-specific keys are
 * left absent. Handler methods run <em>after</em> this one, so a controller is always
 * free to override any value it sets (for example {@code HomeController} overriding
 * {@code appName} with "toplms" on the platform landing page).
 */
@ControllerAdvice
public class TenantLayoutAdvice {

    private final AppHostProperties appHostProperties;

    public TenantLayoutAdvice(AppHostProperties appHostProperties) {
        this.appHostProperties = appHostProperties;
    }

    /**
     * A void {@code @ModelAttribute} method taking {@link Model} is the idiomatic way
     * to contribute <em>several</em> attributes conditionally. (The more common
     * {@code @ModelAttribute("x") public X x()} form always adds exactly one attribute,
     * even when the value is null — which is not what we want off-tenant.)
     */
    @ModelAttribute
    public void tenantBranding(Model model) {
        int year = Year.now().getValue();

        // Always available, tenant or not — the footer uses both.
        model.addAttribute("currentYear", year);
        model.addAttribute("platformUrl", this.appHostProperties.getBaseUrl());

        Tenant tenant = TenantContext.getCurrentTenant();
        if (tenant == null) {
            // Platform root: leave the tenant keys unset so the layout's ?: fallbacks win.
            return;
        }

        String companyName = tenant.getCompanyName();

        model.addAttribute("appName", companyName);
        model.addAttribute("tenantName", tenant.getName());
        model.addAttribute("tenantSubdomain", tenant.getSubdomain());
        model.addAttribute("tenantInitial", initialOf(companyName));
        model.addAttribute("tagline",
                "Committed to academic excellence and student success.");

        // The academic session spans two calendar years ("2026-27"), which is how
        // institutions label an intake. Derived rather than stored so the portal
        // never shows a stale session; move this onto the Tenant entity once
        // institutions need to set their own session dates (a July-start academic
        // year, for instance, rolls over mid-calendar-year).
        model.addAttribute("academicSession", year + "-" + String.format("%02d", (year + 1) % 100));
    }

    /** First letter of the company name, uppercased, for the header monogram. */
    private String initialOf(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return "A";
        }
        return companyName.trim().substring(0, 1).toUpperCase();
    }
}
