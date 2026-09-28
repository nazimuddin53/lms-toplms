package com.toplms.config;

//import com.toplms.master.tenant.TenantService;
import com.toplms.core.interceptor.AuthenticationInterceptor;
import com.toplms.core.interceptor.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
        private final TenantInterceptor tenantInterceptor;
        private final AuthenticationInterceptor authInterceptor;

        public WebMvcConfig(TenantInterceptor tenantInterceptor, AuthenticationInterceptor authInterceptor) {
                this.tenantInterceptor = tenantInterceptor;
                this.authInterceptor = authInterceptor;
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
        }
}
