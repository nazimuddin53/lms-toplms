package com.toplms.core.interceptor;

import com.toplms.core.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Closes a real gap: AuthenticationInterceptor only validates the JWT and populates
 * UserContext — it never checked that the caller's role is actually SUPER_ADMIN. Without this,
 * any authenticated tenant user hitting the root domain could reach a "/superadmin/**"
 * controller unguarded. Registered in WebMvcConfig after AuthenticationInterceptor, so
 * UserContext is already populated by the time this runs.
 */
@Component
public class SuperAdminAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // An unmapped "/superadmin/**" path (e.g. a typo) resolves to Spring Boot's default
        // static-resource handler, not a HandlerMethod — AuthenticationInterceptor already
        // skips those without touching UserContext, so it would still be empty here even for a
        // logged-in SuperAdmin. Skip the same way and let it fall through to a normal 404
        // instead of a false "access denied" redirect.
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        if (UserContext.isPlatformAdmin()) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + "/login?error=access_denied");
        return false;
    }
}
