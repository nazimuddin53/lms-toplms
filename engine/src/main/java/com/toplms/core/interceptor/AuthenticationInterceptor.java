package com.toplms.core.interceptor;

import com.toplms.core.annotation.Public;
import com.toplms.core.context.TenantContext;
import com.toplms.core.context.UserContext;
import com.toplms.core.menuJson.RoleEnum;
import com.toplms.core.security.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;
    private static final String TOKEN_KEY = "AUTH_TOKEN";

    public AuthenticationInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. If it's not a controller method request (e.g., static assets), let it pass
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 2. Check if the Method or the entire Controller class is marked as @Public
        boolean isPublicMethod = handlerMethod.hasMethodAnnotation(Public.class);
        boolean isPublicClass = handlerMethod.getBeanType().isAnnotationPresent(Public.class);

        if (isPublicMethod || isPublicClass) {
            return true; // Dynamic Bypass! No authentication required.
        }

        String token = resolveToken(request);
        if (token == null) {
            return handleInvalidAuthentication(request, response, "Missing authentication context credentials.");
        }
        try {
            // 3. Extract claims using JwtProvider
            Claims claims = jwtProvider.parseToken(token);
            String email = claims.getSubject();
            String roleStr = claims.get("role", String.class);
            String tokenTenantId = claims.get("tenantId", String.class);

            // Safely parse into your strongly-typed Enum
            RoleEnum role = RoleEnum.valueOf(roleStr);

            // 4. Retrieve current tenant workspace resolving from domain (e.g., set by a TenantInterceptor or header)
            // Assuming you store resolved request tenant ID somewhere or pass it down

            String resolvedTenantId = TenantContext.getCurrentTenant() != null
                    ? TenantContext.getCurrentTenant().getId()
                    : request.getHeader("X-Tenant-ID");

            // CRITICAL RULE CONDITION: Global Platform Admin (SUPER_ADMIN)
            if (RoleEnum.SUPER_ADMIN.equals(role)) {
                // We deliberately use the overloaded constructor to skip adding tenant ID to the context info
                UserContext.setUserInfo(new UserContext.UserContextInfo(email, role));
                return true;
            }

            // Standard Workspace Users (TENANT_ADMIN, INSTRUCTOR, STUDENT)
            if (tokenTenantId == null) {
                return sendUnauthorizedResponse(response, "Access Denied: Non-admin token is missing tenant context.");
            }

            // Boundary Cross Check: Ensure user matches current tenant boundary environment
            if (resolvedTenantId != null && !tokenTenantId.equals(resolvedTenantId)) {
                return sendUnauthorizedResponse(response, "Access Denied: You do not belong to this tenant environment.");
            }

            // Valid Tenant Occupant Context Creation
            UserContext.setUserInfo(new UserContext.UserContextInfo(email, role, tokenTenantId));
            return true;

        } catch (IllegalArgumentException e) {
            return sendUnauthorizedResponse(response, "Access Denied: Invalid security clearance signature role.");
        } catch (Exception e) {
            return sendUnauthorizedResponse(response, "Access Denied: Security signature token expired or invalid.");
        }
    }

    private boolean handleInvalidAuthentication(HttpServletRequest request, HttpServletResponse response, String message) throws Exception {
        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");

        // Check if the request is an AJAX/Fetch call or an API endpoint route
        boolean isApiOrAjaxRequest = (acceptHeader != null && acceptHeader.contains("application/json"))
                || "XMLHttpRequest".equals(requestedWith)
                || request.getRequestURI().startsWith("/api/");

        if (isApiOrAjaxRequest) {
            // Return clean REST JSON metadata for front-end async clients
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
            return false;
        } else {
            // 🚀 BROWSER UI ROUTE: Send a 302 Redirect to your user-facing login page instead of printing raw text!
            // This completely breaks the raw text loop and moves the user back to the login screen.
            response.sendRedirect(request.getContextPath() + "/login?error=session_expired");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Crucial step: Wipe ThreadLocal variables at request completion to prevent thread leak pollution
        UserContext.clear();
    }

    private boolean sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));
        return false; // Blocks the request execution pipeline immediately
    }
    public String resolveToken(HttpServletRequest request) {
        // 1. First try to check the fallback Header container line
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. Fall back to reading the native browser Cookie wrapper
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (TOKEN_KEY.equals(cookie.getName())) {
                    return cookie.getValue(); // Clean JWT extracted effortlessly!
                }
            }
        }
        return null;
    }
    public void setTokenInCookie(HttpServletResponse response, String token) {
        Cookie authCookie = new Cookie(TOKEN_KEY, token);
        authCookie.setHttpOnly(true);       // Prevents XSS script theft
        authCookie.setSecure(false);        // Set to true in production with HTTPS
        authCookie.setPath("/");            // Valid across the entire domain
        authCookie.setMaxAge(24 * 60 * 60); // Expires in 1 day

        // Add the cookie to the browser response stream
        response.addCookie(authCookie);
    }
    public void invalidTokenCookie(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("AUTH_TOKEN".equals(cookie.getName())) {
                    // 🚀 THE WIPE MANEUVER: Instruct the browser to immediately evict the cookie file
                    cookie.setValue("");
                    cookie.setPath("/"); // Must mirror the original initialization path exactly
                    cookie.setMaxAge(0);  // 0 seconds tells the browser to purge it immediately
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
    }
}
