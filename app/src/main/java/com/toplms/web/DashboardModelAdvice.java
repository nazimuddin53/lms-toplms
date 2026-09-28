package com.toplms.web;

import com.toplms.core.context.TenantContext;
import com.toplms.core.context.UserContext;
import com.toplms.core.menuJson.RoleEnum;
import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import com.toplms.domain.base.User;
import com.toplms.domain.tenant.TenantUser;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import com.toplms.master.users.UserService;
import com.toplms.tenant.user.TenantUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

/**
 * The "one point" for dashboard chrome data (sidebar menu, tenant/user info, plan modules).
 *
 * <p>A {@code @ControllerAdvice}'s {@code @ModelAttribute} methods run before EVERY controller
 * method in the app, for every request — Spring calls them as part of resolving the handler's
 * arguments, right after {@code HandlerInterceptor.preHandle()} and right before the controller
 * method itself runs. That ordering matters here: by the time this runs, {@code TenantInterceptor}
 * and {@code AuthenticationInterceptor} have already set {@code TenantContext}/{@code UserContext}
 * for this request, so this method just reads them — it never authenticates anything itself.
 *
 * <p>Because it's global, any current or future controller that decorates the dashboard layout
 * (tenant or superadmin) gets {@code sidebarMenus} automatically, without adding a single
 * {@code model.addAttribute(...)} call of its own. Controllers keep only what's actually
 * page-specific (e.g. the course list, a redirect when the account no longer exists).
 */
@ControllerAdvice
public class DashboardModelAdvice {

    @Autowired
    private UserService userService;
    @Autowired
    private TenantUserService tenantUserService;
    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

    @ModelAttribute
    public void populateDashboardChrome(Model model) {
        UserContext.UserContextInfo userInfo = UserContext.getUserInfo();
        if (userInfo == null) {
            return; // anonymous request (public marketing pages, login, an unauthenticated 404) — nothing to add
        }

        if (userInfo.role() == RoleEnum.SUPER_ADMIN) {
            populateSuperAdminChrome(userInfo, model);
        } else {
            populateTenantChrome(userInfo, model);
        }
    }

    private void populateSuperAdminChrome(UserContext.UserContextInfo userInfo, Model model) {
        Optional<User> userOpt = userService.findByEmail(userInfo.email());
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("userType", user.getRole().getName());
        if (user.getRole() != null && user.getRole().getMenuJson() != null) {
            model.addAttribute("sidebarMenus", user.getRole().getMenuJson());
        }
    }

    private void populateTenantChrome(UserContext.UserContextInfo userInfo, Model model) {
        Tenant currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return;
        }

        // TenantUserService.findByEmail() reads TenantContext internally; findByEmailForDemo()
        // takes the tenant explicitly instead, which is what we already have here.
        Optional<TenantUser> tenantUserOpt = tenantUserService.findByEmailForDemo(userInfo.email(), currentTenant);
        if (tenantUserOpt.isEmpty()) {
            return;
        }
        TenantUser tenantUser = tenantUserOpt.get();

        model.addAttribute("user", tenantUser);
        model.addAttribute("currentTenant", currentTenant);
        model.addAttribute("userType", tenantUser.getRole().getName());

        // currentTenant.getPlan() is a Hibernate proxy bound to the (already-closed) session
        // TenantInterceptor used to resolve the tenant, so re-fetch the plan here — through
        // this request's own live session — before reading its modulesJSON, to avoid a
        // LazyInitializationException in the view.
        String planId = currentTenant.getPlan().getId();
        model.addAttribute("currentTenantPlanId", planId);
        Optional<SubscriptionPlan> planOpt = subscriptionPlanService.getById(planId);
        planOpt.ifPresent(plan -> model.addAttribute("planModules", plan.getModulesJSON()));

        if (tenantUser.getRole() != null && tenantUser.getRole().getMenuJson() != null) {
            model.addAttribute("sidebarMenus", tenantUser.getRole().getMenuJson());
        }
    }
}
