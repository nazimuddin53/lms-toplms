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
                // "/error" is Spring's internal forward target for BasicErrorController — excluded
                // from both so an anonymous 404/500 renders the error page instead of being
                // redirected to /login by AuthenticationInterceptor.
                registry.addInterceptor(tenantInterceptor).addPathPatterns("/**").excludePathPatterns("/error");
                registry.addInterceptor(authInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error");
        }
}
