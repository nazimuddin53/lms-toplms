package com.toplms.master.payment;

import com.toplms.domain.base.Payment;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * Platform-wide billing history for SuperAdmin. Per-tenant payments are already shown inside
 * SuperAdminTenantController's detail page; this is the cross-tenant view plus the edit/delete
 * controls, so a bad record can be corrected without touching the database by hand.
 *
 * <p>Access control is not repeated here — WebMvcConfig registers SuperAdminAccessInterceptor on
 * "/superadmin/**", so any request reaching these handlers is already a verified SUPER_ADMIN.
 */
@Controller
@RequestMapping("/superadmin/payments")
public class SuperAdminPaymentController {

    private final PaymentService paymentService;
    private final SubscriptionPlanService subscriptionPlanService;

    public SuperAdminPaymentController(PaymentService paymentService,
                                        SubscriptionPlanService subscriptionPlanService) {
        this.paymentService = paymentService;
        this.subscriptionPlanService = subscriptionPlanService;
    }

    // =========================
    // READ - Payment List
    // =========================
    @GetMapping
    public String payments(Model model) {
        model.addAttribute("payments", paymentService.getAll());
        return "superadmin/payments/list";
    }

    // =========================
    // READ - Payment Detail
    // =========================
    // "/{id}" is a single path segment, so it never collides with "/edit/{id}" or
    // "/delete/{id}" — Spring matches on segment count first.
    @GetMapping("/{id}")
    public String paymentDetail(@PathVariable Long id, Model model) {
        model.addAttribute("payment", findOrThrow(id));
        return "superadmin/payments/view";
    }

    // =========================
    // UPDATE - Show Form
    // =========================
    @GetMapping("/edit/{id}")
    public String editPaymentForm(@PathVariable Long id, Model model) {
        model.addAttribute("payment", findOrThrow(id));
        model.addAttribute("plans", subscriptionPlanService.getAll());
        model.addAttribute("statuses", PaymentService.KNOWN_STATUSES);
        return "superadmin/payments/edit";
    }

    // =========================
    // UPDATE - Save Changes
    // =========================
    // Deliberately taking individual @RequestParams instead of @ModelAttribute Payment: binding
    // the whole entity would let a hand-crafted POST also rewrite id/tenant/createdAt. Naming
    // only the four editable fields makes the write surface obvious.
    @PostMapping("/edit/{id}")
    public String updatePayment(@PathVariable Long id,
                                 @RequestParam double amount,
                                 @RequestParam String currency,
                                 @RequestParam String status,
                                 @RequestParam String planId) {
        try {
            paymentService.updatePayment(id, amount, currency, status, planId);
        } catch (IllegalArgumentException e) {
            return "redirect:/superadmin/payments/edit/" + id + "?error=invalid";
        }
        return "redirect:/superadmin/payments/" + id;
    }

    // =========================
    // DELETE
    // =========================
    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id) {
        try {
            paymentService.deletePayment(id);
        } catch (IllegalArgumentException e) {
            return "redirect:/superadmin/payments?error=not_found";
        }
        return "redirect:/superadmin/payments";
    }

    // A stale or mistyped URL is a client error, not a server fault. Throwing
    // ResponseStatusException lets Spring forward to "/error" with a real 404, which
    // CustomErrorController then renders inside the superadmin dashboard shell. A bare
    // NoSuchElementException would surface as an unhandled 500 instead.
    private Payment findOrThrow(Long id) {
        return paymentService.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found: " + id));
    }
}
