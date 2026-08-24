package com.toplms.tenant.user;

import com.toplms.core.context.TenantContext;
import com.toplms.core.exception.UserNotFoundException;
import com.toplms.domain.base.Tenant;
import com.toplms.domain.base.User;
import com.toplms.domain.tenant.TenantUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.OptionalDataException;
import java.util.Optional;

@Service
public class TenantUserService {
    private final TenantUserRepository tenantUserRepository;
    private final PasswordEncoder passwordEncoder;
    public TenantUserService(TenantUserRepository tenantUserRepository, PasswordEncoder passwordEncoder) {
        this.tenantUserRepository = tenantUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<TenantUser> findByEmail(String email) {
//        try {
            Tenant tenant = TenantContext.getCurrentTenant();
            return this.tenantUserRepository.findByEmail(email, tenant.getId());
//        }  catch (UserNotFoundException e) {
//            throw e;
//        }
    }
    public Optional<TenantUser> findByEmailForDemo(String email, Tenant tenant) {
//        try {

            return this.tenantUserRepository.findByEmail(email, tenant.getId());
//        }  catch (UserNotFoundException e) {
//            throw e;
//        }
    }

    public TenantUser create(TenantUser newUser, String rawPassword) {
        try {
            // 3. Hash the raw password before saving
            String hashedPassword = passwordEncoder.encode(rawPassword);
            newUser.setPassword(hashedPassword);

            return tenantUserRepository.save(newUser);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);

        }

    }
}
