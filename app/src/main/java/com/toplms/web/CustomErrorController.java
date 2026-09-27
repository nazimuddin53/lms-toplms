package com.toplms.web;

import com.toplms.core.menuJson.RoleEnum;
import com.toplms.core.security.JwtProvider;
import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import com.toplms.domain.base.User;
import com.toplms.domain.tenant.TenantUser;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import com.toplms.master.tenant.TenantService;
import com.toplms.master.users.UserService;
import com.toplms.tenant.user.TenantUserService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

/**
 * Replaces Spring Boot's default {@code BasicErrorController} (providing our own
 * {@link ErrorController} bean makes Boot's auto-configured one back off entirely) so a
 * 404/500 can render inside whichever shell the visitor was actually in — the plain public
 * page for an anonymous visitor, or the tenant/superadmin dashboard chrome (sidebar + header
 * intact) for a logged-in user who clicked a not-yet-built menu link.
 *
 * <p>Note: a truly unmapped route never reaches a {@code HandlerMethod}, so
 * {@code TenantInterceptor}/{@code AuthenticationInterceptor} never ran on the original
 * request and {@code TenantContext}/{@code UserContext} are empty here. This class reads the
 * {@code AUTH_TOKEN} cookie's JWT directly instead, mirroring what those interceptors do.
 */
@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);
    private static final String TOKEN_COOKIE = "AUTH_TOKEN";

    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private TenantUserService tenantUserService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 500;
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        model.addAttribute("status", status);
        model.addAttribute("path", path);

        String anonymousView = status == 404 ? "error/404" : "error/error";

        String token = resolveAuthToken(request);
        if (token == null) {
            return anonymousView;
        }

        try {
            String email = jwtProvider.getEmail(token);
            RoleEnum role = RoleEnum.valueOf(jwtProvider.getRole(token));

            if (role == RoleEnum.SUPER_ADMIN) {
                Optional<User> userOpt = userService.findByEmail(email);
                if (userOpt.isEmpty()) {
                    return anonymousView;
                }
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("userType", user.getRole().getName());
                if (user.getRole() != null && user.getRole().getMenuJson() != null) {
                    model.addAttribute("sidebarMenus", user.getRole().getMenuJson());
                }
                return "dashboard/error";
            }

            // TenantUserService.findByEmail() reads TenantContext, which is empty here
            // (TenantInterceptor never runs on the internal /error forward) — so the tenant
            // must be resolved explicitly first and passed in directly instead.
            String tenantId = jwtProvider.getTenantId(token);
            Optional<Tenant> tenantOpt = tenantId != null ? tenantService.getTenantById(tenantId) : Optional.empty();
            if (tenantOpt.isEmpty()) {
                return anonymousView;
            }
            Tenant tenant = tenantOpt.get();

            Optional<TenantUser> tenantUserOpt = tenantUserService.findByEmailForDemo(email, tenant);
            if (tenantUserOpt.isEmpty()) {
                return anonymousView;
            }
            TenantUser tenantUser = tenantUserOpt.get();

            model.addAttribute("user", tenantUser);
            model.addAttribute("currentTenant", tenant);
            model.addAttribute("userType", tenantUser.getRole().getName());

            String planId = tenant.getPlan().getId();
            model.addAttribute("currentTenantPlanId", planId);
            Optional<SubscriptionPlan> planOpt = subscriptionPlanService.getById(planId);
            planOpt.ifPresent(plan -> model.addAttribute("planModules", plan.getModulesJSON()));

            if (tenantUser.getRole() != null && tenantUser.getRole().getMenuJson() != null) {
                model.addAttribute("sidebarMenus", tenantUser.getRole().getMenuJson());
            }
            return "tenant/dashboard/error";
        } catch (Exception e) {
            log.warn("Falling back to the anonymous error page — could not resolve dashboard context for this request", e);
            return anonymousView;
        }
    }

    private String resolveAuthToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
