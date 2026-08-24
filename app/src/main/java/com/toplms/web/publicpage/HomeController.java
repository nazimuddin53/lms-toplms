package com.toplms.web.publicpage;

import com.toplms.core.annotation.Public;
import com.toplms.core.context.TenantContext;
import com.toplms.core.interceptor.TenantInterceptor;
import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import com.toplms.master.subscriptionPlan.SubscriptionPlanRepository;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import com.toplms.master.tenant.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Year;
import java.util.List;
import java.util.Optional;


@Controller
public class HomeController {
    @Autowired
    private SubscriptionPlanService subscriptionPlanService;
    @Autowired
    private TenantService tenantService;

    @Public
    @GetMapping("/")
    public String index(Model model) {
        Tenant currentTenant = TenantContext.getCurrentTenant();

        if (currentTenant == null) {
            List<SubscriptionPlan> subscriptionPlans = subscriptionPlanService.getAll();
            if (!subscriptionPlans.isEmpty()) {

            }
            model.addAttribute("appName", "toplms");
            model.addAttribute("tagline", "Multi-tenant Learning Management System");

            model.addAttribute("plans", subscriptionPlans);
            model.addAttribute("currentYear", Year.now().getValue());
            return "public/index";
        }

        model.addAttribute("appName", currentTenant.getCompanyName());
        model.addAttribute("tagline", "Multi-tenant s Management System");
        model.addAttribute("currentYear", Year.now().getValue());
        return "tenant/public/tenant-index";
    }

}
