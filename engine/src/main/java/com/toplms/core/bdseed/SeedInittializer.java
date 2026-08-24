//package com.toplms.core.bdseed;
//
//import com.toplms.config.AppHostProperties;
//import com.toplms.config.DemoTenantProperties;
//import com.toplms.core.menuJson.MenuJson;
//import com.toplms.master.subscriptionPlan.SubscriptionPlanRepository;
//import com.toplms.master.subscriptionPlan.SubscriptionPlanService;
//import com.toplms.master.tenant.TenantRegistrationService;
//import com.toplms.master.tenant.TenantService;
//import com.toplms.master.users.RoleRepository;
//import com.toplms.master.users.RoleService;
//import com.toplms.master.users.UserService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SeedInittializer implements CommandLineRunner {
//    private final RoleRepository roleRepository;
//    private final MenuJson menuJson;
//    private final SubscriptionPlanRepository subscriptionPlanRepository;
//    private final SubscriptionPlanService subscriptionPlanService;
//    private final DemoTenantProperties demoTenantProperties;
//    private final TenantRegistrationService tenantRegistrationService;
//    private final TenantService tenantService;
//    private final RoleService roleService;
//    private final UserService userService;
//    private final AppHostProperties appHostProperties;
//    public SeedInittializer(RoleRepository roleRepository, MenuJson menuJson, SubscriptionPlanRepository subscriptionPlanRepository, SubscriptionPlanService subscriptionPlanService, DemoTenantProperties demoTenantProperties, TenantRegistrationService tenantRegistrationService, TenantService tenantService, RoleService roleService, UserService userService, AppHostProperties appHostProperties) {
//
//        this.roleRepository = roleRepository;
//        this.menuJson = menuJson;
//        this.subscriptionPlanRepository = subscriptionPlanRepository;
//        this.subscriptionPlanService = subscriptionPlanService;
//        this.demoTenantProperties = demoTenantProperties;
//        this.tenantRegistrationService = tenantRegistrationService;
//        this.tenantService = tenantService;
//        this.roleService = roleService;
//        this.userService = userService;
//        this.appHostProperties = appHostProperties;
//    }
//    @Override
//    public void run(String... args) throws Exception {
//        RoleDataInitializer roleDataInitializer = new RoleDataInitializer(roleRepository, menuJson);
//        UserDataIntializer userDataIntializer = new UserDataIntializer(roleService, userService, appHostProperties);
//        SubscriptionPlanDataInitializer subscriptionPlanDataInitializer = new SubscriptionPlanDataInitializer(subscriptionPlanRepository);
//        TenantDataInitializer tenantDataInitializer = new TenantDataInitializer(subscriptionPlanService, demoTenantProperties, tenantRegistrationService, tenantService);
//
////        roleDataInitializer.run();
////        userDataIntializer.run();
////        subscriptionPlanDataInitializer.run();
////        tenantDataInitializer.run();
//    }
//}
