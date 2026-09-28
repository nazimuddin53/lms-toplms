package com.toplms.web;

import com.toplms.core.context.UserContext;
import com.toplms.core.menuJson.RoleEnum;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Replaces Spring Boot's default {@code BasicErrorController} (providing our own
 * {@link ErrorController} bean makes Boot's auto-configured one back off entirely) so a
 * 404/500 can render inside whichever shell the visitor was actually in — the plain public
 * page for an anonymous visitor, or the tenant/superadmin dashboard chrome (sidebar + header
 * intact) for a logged-in user who clicked a not-yet-built menu link.
 *
 * <p>{@code AuthenticationInterceptor}/{@code TenantInterceptor} both run on this "/error"
 * forward (each guards against blocking or redirecting it) and {@link DashboardModelAdvice}
 * fills in the dashboard model attributes from the {@code UserContext}/{@code TenantContext}
 * they set — this controller only needs to pick the right view name.
 */
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 500;
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        model.addAttribute("status", status);
        model.addAttribute("path", path);

        UserContext.UserContextInfo userInfo = UserContext.getUserInfo();
        if (userInfo == null) {
            return status == 404 ? "error/404" : "error/error";
        }

        return userInfo.role() == RoleEnum.SUPER_ADMIN ? "dashboard/error" : "tenant/dashboard/error";
    }
}
