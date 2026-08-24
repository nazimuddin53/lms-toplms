package com.toplms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security for the apex (marketing + signup) process. Intentionally
 * minimal — there are no authenticated routes in this module today.
 *
 * <p><strong>What Spring Security is still doing for us here:</strong>
 *
 * <ul>
 *   <li><strong>CSRF protection.</strong> Left ON (the default). Thymeleaf
 *       auto-injects the token into form POSTs — {@code /signup} relies on it.
 *       This is the main reason apex pulls in {@code spring-boot-starter-security}
 *       at all.</li>
 *   <li><strong>Sensible security headers</strong> (X-Frame-Options, etc.)
 *       that come with the default filter chain.</li>
 *   <li><strong>Permit-all on every URL.</strong> No login wall — apex pages
 *       are public.</li>
 * </ul>
 *
 * <p><strong>What it deliberately does NOT do:</strong> no {@code formLogin},
 * no {@code UserDetailsService}, no tenant resolution. Login + tenant auth
 * live in :tenant-app's {@code TenantSecurityConfig}. A future superadmin
 * portal at apex {@code /admin/**} would add a second {@link SecurityFilterChain}
 * bean here, scoped by {@code securityMatcher("/admin/**")}.
 *
 * <p>The {@link org.springframework.security.crypto.password.PasswordEncoder}
 * bean comes from {@link com.toplms.config.SecurityCryptoConfig} in :engine,
 * shared with :tenant-app — {@link com.toplms.master.service.TenantProvisioningService}
 * uses it to hash the founding admin password during signup.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Every apex URL is public — marketing pages, signup form, error pages.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // No form login on apex. Disabling avoids Spring Security's
                // default /login endpoint that would otherwise be registered.
                .formLogin(FormLoginConfigurer::disable)
                // No HTTP basic either.
                .httpBasic(AbstractHttpConfigurer::disable);
        // CSRF stays ON (default) — Thymeleaf form helpers add the token to /signup POST.
        return http.build();
    }
}
