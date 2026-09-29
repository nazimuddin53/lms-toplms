package com.toplms.master.tenant;

import com.toplms.master.payment.PaymentService;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/superadmin/tenants")
public class SuperAdminTenantController {

    private final TenantService tenantService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final PaymentService paymentService;

    public SuperAdminTenantController(TenantService tenantService, SubscriptionPlanService subscriptionPlanService,
                                       PaymentService paymentService) {
        this.tenantService = tenantService;
        this.subscriptionPlanService = subscriptionPlanService;
        this.paymentService = paymentService;
    }

    // =========================
    // READ - Tenant List
    // =========================
    @GetMapping
    public String tenants(Model model) {
        model.addAttribute("tenantSummaries", tenantService.getAllTenantSummaries());
        return "superadmin/tenants/list";
    }

    // =========================
    // READ - Tenant Detail
    // =========================
    @GetMapping("/{id}")
    public String tenantDetail(@PathVariable String id, Model model) {
        TenantSummaryDto summary = tenantService.getTenantSummary(id)
                .orElseThrow(() -> new NoSuchElementException("Tenant not found: " + id));

        model.addAttribute("summary", summary);
        model.addAttribute("plans", subscriptionPlanService.getAll());
        model.addAttribute("payments", paymentService.getPaymentsForTenant(id));
        return "superadmin/tenants/detail";
    }

    // =========================
    // SUSPEND / ACTIVATE
    // =========================
    @PostMapping("/{id}/suspend")
    public String suspend(@PathVariable String id) {
        tenantService.setActive(id, false);
        return "redirect:/superadmin/tenants/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable String id) {
        tenantService.setActive(id, true);
        return "redirect:/superadmin/tenants/" + id;
    }

    // =========================
    // CHANGE PLAN (superadmin override — no payment)
    // =========================
    @PostMapping("/{id}/plan")
    public String changePlan(@PathVariable String id, @RequestParam String planId) {
        tenantService.changePlan(id, planId);
        return "redirect:/superadmin/tenants/" + id;
    }
}
