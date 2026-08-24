package com.toplms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Shared password-hashing bean. Lives in :engine so that both bootable
 * modules (:app and :tenant-app) get the same one from a single definition —
 * no duplication, no risk of the two processes accidentally using different
 * hash algorithms and being unable to verify each other's passwords.
 *
 * <p><strong>Why not in a {@code SecurityConfig}?</strong> Putting the
 * {@link PasswordEncoder} alongside the {@code SecurityFilterChain} couples
 * a small, stateless crypto helper to a much heavier web-security config.
 * Splitting it out also means any future non-web hasher consumer (e.g. a
 * provisioning service that hashes a founding admin password) can rely on
 * the bean existing without dragging the servlet stack into their tests.
 *
 * <p>{@link BCryptPasswordEncoder} is Spring's recommended default: adaptive
 * cost factor, built-in salt per hash. The two-process split means the
 * <em>algorithm</em> must stay consistent — bcrypt-hashed rows written by one
 * process MUST be verifiable by the other.
 */
@Configuration
public class SecurityCryptoConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
