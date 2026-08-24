package com.toplms.web;

import com.toplms.core.context.TenantContext;
import com.toplms.core.context.UserContext;
import com.toplms.core.enumType.LoadingPageType;
import com.toplms.core.interceptor.TenantInterceptor;
import com.toplms.domain.base.Role;
import com.toplms.domain.base.Tenant;
import com.toplms.domain.base.User;
import com.toplms.domain.tenant.TenantUser;
import com.toplms.master.users.RoleService;
import com.toplms.master.users.UserService;
import com.toplms.tenant.user.TenantUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;
    @Autowired
    private TenantUserService tenantUserService;
    @Autowired
    private RoleService roleService;

    @GetMapping()
    public String dashboard(Model model) {
        // 1. Check if the request is executing under the Central SuperAdmin Core space
        if (TenantContext.getCurrentPageType().equals(LoadingPageType.MAIN)) {

            Optional<User> userOpt = this.userService.findByEmail(UserContext.getUserInfo().email());
            if (userOpt.isEmpty()) {
                return "redirect:/login?error=session_expired";
            }

            User user = userOpt.get();
            model.addAttribute("user", user);
            model.addAttribute("userType", user.getRole().getName());

            // SuperAdmins can be given a hardcoded or global configuration menu map
            return "dashboard/dashboard"; // Uses superadmin-dashboard layout
        }

        // 2. Tenant Workspace routing execution lane
        Tenant currentTenant = TenantContext.getCurrentTenant();
//        if (currentTenant == null) {
//            return "redirect:/login?error=invalid_tenant";
//        }

        Optional<TenantUser> tenantUserOpt = this.tenantUserService.findByEmail(UserContext.getUserInfo().email());

//        if (tenantUserOpt.isEmpty()) {
//            return "redirect:/login?error=unauthorized";
//        }

        TenantUser tenantUser = tenantUserOpt.get();

        // 3. Bind properties to the UI layout template
        model.addAttribute("user", tenantUser);
        model.addAttribute("currentTenant", currentTenant);
        model.addAttribute("currentTenantPlanId", currentTenant.getPlan().getId());
        model.addAttribute("userType", tenantUser.getRole().getName());

        if (tenantUser.getRole() != null && tenantUser.getRole().getMenuJson() != null) {
            model.addAttribute("sidebarMenus", tenantUser.getRole().getMenuJson());
        }

        return "tenant/dashboard/dashboard"; // Uses tenant-dashboard layout
    }
}
