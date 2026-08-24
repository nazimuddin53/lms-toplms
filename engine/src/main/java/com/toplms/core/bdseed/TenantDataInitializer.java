package com.toplms.core.bdseed;

import com.toplms.config.DemoTenantProperties;
import com.toplms.domain.base.SubscriptionPlan;
import com.toplms.domain.base.Tenant;
import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
import com.toplms.master.tenant.TenantRegistrationDto;
import com.toplms.master.tenant.TenantRegistrationService;
import com.toplms.master.tenant.TenantService;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@org.springframework.core.annotation.Order(4)
public class TenantDataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(TenantDataInitializer.class);
    private final SubscriptionPlanService subscriptionPlanService;
    private final DemoTenantProperties demoTenantProperties;
    private final TenantRegistrationService tenantRegistrationService;
    private final TenantService tenantService;
    public TenantDataInitializer(SubscriptionPlanService subscriptionPlanService, DemoTenantProperties demoTenantProperties, TenantRegistrationService tenantRegistrationService, TenantService tenantService) {

        this.subscriptionPlanService = subscriptionPlanService;
        this.tenantRegistrationService = tenantRegistrationService;
        this.demoTenantProperties = demoTenantProperties;
        this.tenantService = tenantService;
    }

    @Override
    public void run(String... args) throws Exception {
        try{
            System.out.println("subdomain : "+this.demoTenantProperties.getSubdomain());
            Optional<Tenant> tenant = this.tenantService.getTenantBySubdomain(this.demoTenantProperties.getSubdomain());
            if (tenant.isPresent()) {
                log.info("Demo tenant '{}' already exists. Skipping workspace seeding.", this.demoTenantProperties.getSubdomain());
                return;
            }

            // 2. Validate that the chosen pricing strategy plan exists in the database
            Optional<SubscriptionPlan> plan = this.subscriptionPlanService.getById(this.demoTenantProperties.getPlanId());
            if (plan.isEmpty()) {
                log.error("Aborting tenant seeding: Subscription Plan ID {} does not exist in the database.", this.demoTenantProperties.getPlanId());
                return; // Stop execution before causing a database foreign-key crash
            }
            // 3. Construct and map the data transfer record
            TenantRegistrationDto tenantRegistrationDto = getTenantRegistrationDto();

            // 4. Fire the database entry routine
            log.info("Seeding demo tenant workspace for subdomain: {}", this.demoTenantProperties.getSubdomain());
            this.tenantRegistrationService.createNewTenantAndAdmin(tenantRegistrationDto);

            System.out.println("✅ Default tenant workspace bootstrapped successfully.");
            log.info("Default tenant workspace bootstrapped successfully. Tenant Company: {} email: {} password: {}", tenantRegistrationDto.getCompanyName(), tenantRegistrationDto.getAdminEmail(), tenantRegistrationDto.getAdminPassword());
        } catch (IllegalArgumentException e) {
            // Catch expected validation exceptions (e.g., "Subdomain already in use")
            System.out.println("ℹ️ Skipping default tenant bootstrap: " + e.getMessage());
        } catch (Exception e) {
            // Log unexpected critical errors without throwing a hard RuntimeException
            System.err.println("❌ Critical failure during workspace bootstrap: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private @NonNull TenantRegistrationDto getTenantRegistrationDto() {
        TenantRegistrationDto  tenantRegistrationDto = new TenantRegistrationDto();
        tenantRegistrationDto.setCompanyName(this.demoTenantProperties.getCompanyName());
        tenantRegistrationDto.setSubdomain(this.demoTenantProperties.getSubdomain());
        tenantRegistrationDto.setPlanId(this.demoTenantProperties.getPlanId());
        tenantRegistrationDto.setName(this.demoTenantProperties.getName());
        tenantRegistrationDto.setAdminEmail(this.demoTenantProperties.getAdminEmail());
        tenantRegistrationDto.setAdminPassword(this.demoTenantProperties.getAdminPassword());
        return tenantRegistrationDto;
    }

}
