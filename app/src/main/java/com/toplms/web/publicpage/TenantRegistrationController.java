package com.toplms.web.publicpage;

import com.toplms.config.AppHostProperties;
import com.toplms.core.enumType.LoadingPageType;
import com.toplms.domain.base.Tenant;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import org.springframework.ui.Model;
import com.toplms.core.annotation.Public;
import com.toplms.core.context.TenantContext;
import com.toplms.core.interceptor.TenantInterceptor;
import com.toplms.master.tenant.TenantRegistrationDto;
import com.toplms.master.tenant.TenantRegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller

@RequestMapping("/register")
public class TenantRegistrationController {

    private final TenantRegistrationService registrationService;
    private final AppHostProperties appHostProperties;
    private final SubscriptionPlanService subscriptionPlanService;

    public TenantRegistrationController(TenantRegistrationService registrationService, AppHostProperties appHostProperties, SubscriptionPlanService subscriptionPlanService) {
        this.registrationService = registrationService;
        this.appHostProperties = appHostProperties;
        this.subscriptionPlanService = subscriptionPlanService;
    }

    @Public
    @GetMapping("")
    public String showRegistrationForm(@RequestParam(value = "planId", required = false) String planId, Model model) {
        // 1. Ensure registration only happens on the central landing page (toplms)
        Tenant currentTenant = TenantContext.getCurrentTenant();
        if (!TenantInterceptor.LANDING_PAGE_TYPE.equals(LoadingPageType.MAIN)) {
            String redirectUrl = "redirect:" + this.appHostProperties.getBaseUrl() + "/register";
            if (planId != null) {
                redirectUrl += "?planId=" + planId;
            }
            return redirectUrl;
        }

        TenantRegistrationDto registrationDto = new TenantRegistrationDto();

        // 2. Handle Plan Selector Fallback Toggle
        if (planId == null || planId.trim().isEmpty()) {
            model.addAttribute("showPlanSelector", true);
            // Fetch plans from DB and send them to Thymeleaf
            System.out.println(subscriptionPlanService.getAll());
             model.addAttribute("availablePlans", subscriptionPlanService.getAll());
        } else {
            registrationDto.setPlanId(planId);
            model.addAttribute("showPlanSelector", false);
            // Optionally fetch specific plan details to display as text info
             model.addAttribute("selectedPlan", subscriptionPlanService.getById(planId));
        }

        model.addAttribute("registrationDto", registrationDto);
        return "public/tenantRegister";
    }

    @Public
    @PostMapping("")
    public String registerTenant(@Valid @ModelAttribute("registrationDto") TenantRegistrationDto dto,
                                 BindingResult bindingResult, Model model) {
        // Ensure this logic cannot execute under isolated subdomains
        Tenant currentTenant = TenantContext.getCurrentTenant();
        if (!TenantInterceptor.LANDING_PAGE_TYPE.equals(LoadingPageType.MAIN)) {
            return "redirect:" + this.appHostProperties.getBaseUrl() + "/register";
        }

        // 3. CRITICAL ERROR CATCH: Re-populate dynamic dropdown data if validation fails!
        if (bindingResult.hasErrors()) {
            populateFallbackDataIfNeeded(dto, model);
            return "public/tenantRegister";
        }

        try {
            registrationService.createNewTenantAndAdmin(dto);
            return "redirect:http://" + dto.getSubdomain() + "."+ this.appHostProperties.getHost() + ":"+ this.appHostProperties.getPort() + "/login";
        } catch (IllegalArgumentException e) {
            // 4. CRITICAL ERROR CATCH: Display custom exceptions (e.g., "Subdomain already taken!")
            model.addAttribute("errorMessage", e.getMessage());
            populateFallbackDataIfNeeded(dto, model);
            return "public/tenantRegister";
        }
    }
    /**
     * Helper method to keep UI state consistent across page reload refreshes
     */
    private void populateFallbackDataIfNeeded(TenantRegistrationDto dto, Model model) {
        if (dto.getPlanId() == null || dto.getPlanId().trim().isEmpty()) {
            model.addAttribute("showPlanSelector", true);
             model.addAttribute("availablePlans", subscriptionPlanService.getAll());
        } else {
            // User had submitted a dynamic plan from selection or URL parameter
            model.addAttribute("showPlanSelector", true);
            // We set true so the dropdown stays visible and retains their chosen selection
             model.addAttribute("availablePlans", subscriptionPlanService.getById(dto.getPlanId()));
        }
    }
}