package com.toplms.web;

import com.toplms.core.context.TenantContext;
import com.toplms.core.context.UserContext;
import com.toplms.core.enumType.LoadingPageType;
import com.toplms.domain.base.User;
import com.toplms.master.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

/**
 * {@link DashboardModelAdvice} already supplies the dashboard chrome (sidebarMenus, user,
 * currentTenant, plan info) for every request — this controller only decides which layout to
 * render, plus the one piece of behavior that's genuinely specific to this page: bouncing a
 * SuperAdmin whose account has vanished mid-session back to the login screen.
 */
@Controller
@RequestMapping("dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public String dashboard() {
        if (TenantContext.getCurrentPageType().equals(LoadingPageType.MAIN)) {
            Optional<User> userOpt = userService.findByEmail(UserContext.getUserInfo().email());
            if (userOpt.isEmpty()) {
                return "redirect:/login?error=session_expired";
            }
            return "dashboard/dashboard"; // Uses superadmin-dashboard layout
        }

        return "tenant/dashboard/dashboard"; // Uses tenant-dashboard layout
    }
}
