package com.toplms.tenant.billing.controller;

import com.toplms.core.context.TenantContext;
import com.toplms.core.context.UserContext;
import com.toplms.core.menuJson.RoleEnum;
import com.toplms.domain.base.Tenant;
import com.toplms.master.payment.PaymentService;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import com.toplms.master.tenant.TenantService;
import com.toplms.master.tenant.TenantSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * The tenant-facing half of billing: "what plan am I on, how much of it am I using, and what
 * have I been charged?" The SuperAdmin equivalents (SuperAdminTenantController /
 * SuperAdminPaymentController) can edit these records across every tenant; this page is
 * strictly read-only and strictly scoped to the caller's own workspace.
 *
 * <h2>Where the tenant comes from — and why never from the request</h2>
 * The workspace is read from {@link TenantContext}, which {@code TenantInterceptor} populated
 * from the request's subdomain before this handler ran. It is deliberately NOT a path variable
 * or request parameter: accepting a tenant id from the client would let any logged-in user of
 * one workspace read another workspace's invoices by editing the URL.
 */
@Controller
@RequestMapping("/dashboard/billing")
public class TenantBillingController {

    private final TenantService tenantService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final PaymentService paymentService;

    public TenantBillingController(TenantService tenantService,
                                    SubscriptionPlanService subscriptionPlanService,
                                    PaymentService paymentService) {
        this.tenantService = tenantService;
        this.subscriptionPlanService = subscriptionPlanService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String billing(Model model) {
        Tenant tenant = TenantContext.getCurrentTenant();
        if (tenant == null) {
            // Reached on the platform apex, where there is no workspace to bill.
            return "redirect:/dashboard";
        }

        // AdminMenuJson only shows this link to TENANT_ADMIN, but a menu controls rendering,
        // not access — a TEACHER or STUDENT could still type the URL. Invoice amounts are not
        // staff-wide information, so the role is enforced here as well.
        if (UserContext.getCurrentUserRole() != RoleEnum.TENANT_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Billing is restricted to workspace administrators.");
        }

        // Carries the tenant plus its course/user counts, so the page can show usage against
        // the plan's ceilings rather than just quoting the limits.
        TenantSummaryDto summary = tenantService.getTenantSummary(tenant.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Workspace not found: " + tenant.getId()));

        // tenant.getPlan() is a lazy proxy left over from the session TenantInterceptor used,
        // so re-fetch the plan through this request's own session before the view reads
        // modulesJSON/priceJSON off it — the same reason DashboardModelAdvice re-fetches.
        model.addAttribute("summary", summary);
        model.addAttribute("plan", subscriptionPlanService.getById(summary.tenant().getPlan().getId())
                .orElse(null));
        model.addAttribute("payments", paymentService.getPaymentsForTenant(tenant.getId()));

        return "tenant/billing/billing";
    }
}
