package com.toplms.config;

//import com.toplms.master.tenant.TenantService;
import com.toplms.core.interceptor.AuthenticationInterceptor;
import com.toplms.core.interceptor.SuperAdminAccessInterceptor;
import com.toplms.core.interceptor.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
        private final TenantInterceptor tenantInterceptor;
        private final AuthenticationInterceptor authInterceptor;
        private final SuperAdminAccessInterceptor superAdminAccessInterceptor;

        public WebMvcConfig(TenantInterceptor tenantInterceptor, AuthenticationInterceptor authInterceptor,
                             SuperAdminAccessInterceptor superAdminAccessInterceptor) {
                this.tenantInterceptor = tenantInterceptor;
                this.authInterceptor = authInterceptor;
                this.superAdminAccessInterceptor = superAdminAccessInterceptor;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
                // Order matters! Resolve tenant context first, then handle auth check.
                // Both interceptors run on "/error" too (Spring's internal forward target for
                // CustomErrorController) — each has its own internal guard against blocking or
                // redirecting that specific path, so an anonymous 404/500 still renders normally
                // while a logged-in visitor's context (tenant, role) is still populated for it.
                registry.addInterceptor(tenantInterceptor).addPathPatterns("/**");
                registry.addInterceptor(authInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico");
                // Registered after authInterceptor so UserContext is already populated by the
                // time this runs — it only needs to check the role, not re-validate the token.
                registry.addInterceptor(superAdminAccessInterceptor).addPathPatterns("/superadmin/**");
        }
}
