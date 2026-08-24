package com.toplms.master.tenant;

import com.toplms.core.menuJson.RoleEnum;
import com.toplms.domain.base.Role;
import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import com.toplms.domain.tenant.TenantUser;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import com.toplms.master.users.RoleService;
import com.toplms.master.users.UserRepository;

import com.toplms.tenant.user.TenantUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantRegistrationService {
    private final TenantService tenantService;
    private final TenantUserService tenantUserService;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionPlanService subscriptionPlanService;
    private final RoleService roleService;


    public TenantRegistrationService(TenantService tenantService, TenantUserService tenantUserService, PasswordEncoder passwordEncoder, SubscriptionPlanService subscriptionPlanService, RoleService roleService) {
        this.tenantService = tenantService;
        this.tenantUserService = tenantUserService;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionPlanService = subscriptionPlanService;
        this.roleService = roleService;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNewTenantAndAdmin(TenantRegistrationDto dto) {
        try {
            // 1. Check if subdomain is already taken
            if (tenantService.getTenantBySubdomain(dto.getSubdomain()).isPresent()) {
                throw new IllegalArgumentException("Subdomain is already in use.");
            }
            // 2. check subscription plan is exit or not
            Optional<SubscriptionPlan> subscriptionPlan = this.subscriptionPlanService.getById(dto.getPlanId());
            if (subscriptionPlan.isEmpty()) {
                throw new IllegalArgumentException("Subscription plan is not found.");
            }


            LocalDateTime subscriptionStartAt = LocalDateTime.now();
            LocalDateTime subscriptionEndAt = subscriptionStartAt.plusMonths(1);

            String randomUuid = UUID.randomUUID().toString();

            // 3. Save the Tenant
            Tenant tenant = new Tenant();
            tenant.setId(randomUuid);
            tenant.setName(dto.getName());
            tenant.setCompanyName(dto.getCompanyName());
            tenant.setSubdomain(dto.getSubdomain());
            tenant.setPlan(subscriptionPlan.get());
            tenant.setSubscriptionStartedAt(subscriptionStartAt);
            tenant.setSubscriptionEndsAt(subscriptionEndAt);

            tenant = tenantService.createTenant(tenant);
            // 2. check subscription plan is exit or not
            Optional<TenantUser> tenantUser = this.tenantUserService.findByEmailForDemo(dto.getAdminEmail(), tenant);

            if (tenantUser.isPresent()) {
                throw new IllegalArgumentException("Email is already exist.");
            }

            //roleRepository.findByRoleName(String.valueOf(RoleEnum.TENANT_ADMIN)).isEmpty()
            // 3. Check Exist Role and Get Role From Db
            Optional<Role> role = this.roleService.findByRoleName(String.valueOf(RoleEnum.TENANT_ADMIN));
            if (role.isEmpty()) {
                throw new IllegalArgumentException("Role is not found.");
            }
            // 4. Save the Admin User for that Tenant
            TenantUser admin = new TenantUser();
            admin.setEmail(dto.getAdminEmail());
            admin.setRole(role.get());
            admin.setName(dto.getName());
            admin.setPassword(passwordEncoder.encode(dto.getAdminPassword()));
            admin.setTenant(tenant); // Hard link user to this specific tenant workspace
            tenantUserService.create(admin, dto.getAdminPassword());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException (e.getMessage());
        }
    }
}
