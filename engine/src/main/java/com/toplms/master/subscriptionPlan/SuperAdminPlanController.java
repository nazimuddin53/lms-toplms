package com.toplms.master.subscriptionPlan;

import com.toplms.domain.base.SubscriptionPlan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/superadmin/plans")
public class SuperAdminPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    public SuperAdminPlanController(SubscriptionPlanService subscriptionPlanService) {
        this.subscriptionPlanService = subscriptionPlanService;
    }

    // =========================
    // READ - Plan List
    // =========================
    @GetMapping
    public String plans(Model model) {
        model.addAttribute("plans", subscriptionPlanService.getAll());
        return "superadmin/plans/list";
    }

    // =========================
    // CREATE - Show Form
    // =========================
    @GetMapping("/create")
    public String createPlanForm(Model model) {
        model.addAttribute("plan", new SubscriptionPlan());
        return "superadmin/plans/create";
    }

    // =========================
    // CREATE - Save Plan
    // =========================
    @PostMapping
    public String createPlan(@ModelAttribute SubscriptionPlan plan,
                              @RequestParam(name = "modules", required = false) List<String> modules,
                              @RequestParam(required = false) Double usdPrice) {
        try {
            subscriptionPlanService.createPlan(plan, modules, usdPrice);
        } catch (IllegalArgumentException e) {
            return "redirect:/superadmin/plans/create?error=duplicate_id";
        }
        return "redirect:/superadmin/plans";
    }

    // =========================
    // UPDATE - Show Form
    // =========================
    @GetMapping("/edit/{id}")
    public String editPlanForm(@PathVariable String id, Model model) {
        model.addAttribute("plan", subscriptionPlanService.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Plan not found: " + id)));
        return "superadmin/plans/edit";
    }

    // =========================
    // UPDATE - Save Changes
    // =========================
    @PostMapping("/edit/{id}")
    public String updatePlan(@PathVariable String id,
                              @ModelAttribute SubscriptionPlan plan,
                              @RequestParam(name = "modules", required = false) List<String> modules,
                              @RequestParam(required = false) Double usdPrice) {
        subscriptionPlanService.updatePlan(id, plan, modules, usdPrice);
        return "redirect:/superadmin/plans";
    }

    // =========================
    // DELETE
    // =========================
    @PostMapping("/delete/{id}")
    public String deletePlan(@PathVariable String id) {
        try {
            subscriptionPlanService.deletePlan(id);
        } catch (IllegalStateException e) {
            return "redirect:/superadmin/plans?error=plan_in_use";
        }
        return "redirect:/superadmin/plans";
    }
}
