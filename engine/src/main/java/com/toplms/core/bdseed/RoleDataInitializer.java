package com.toplms.core.bdseed;

import com.toplms.core.menuJson.MenuJson;
import com.toplms.core.menuJson.RoleEnum;
import com.toplms.domain.base.Role;
import com.toplms.master.subscriptionPlan.SubscriptionPlanRepository;
import com.toplms.master.users.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@org.springframework.core.annotation.Order(2)
public class RoleDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RoleDataInitializer.class);
        private final RoleRepository roleRepository;
        private final MenuJson menuJson;


    public RoleDataInitializer(RoleRepository roleRepository, MenuJson menuJson) {
            this.roleRepository = roleRepository;
            this.menuJson = menuJson;
        }

        @Override
        public void run(String... args) throws Exception {
            try {
                String name ;

                // 1. SuperAdmin Role
                name = String.valueOf(RoleEnum.SUPER_ADMIN);
                if (roleRepository.findByRoleName(name).isEmpty()) {
                    Map<String, Object> menuJson = this.menuJson.getSuperadminMenuJson().getMenuJson();
                    roleRepository.save(new Role(name, menuJson));
                }

                // 2. Admin Role
                name = String.valueOf(RoleEnum.TENANT_ADMIN);
                if (roleRepository.findByRoleName(String.valueOf(RoleEnum.TENANT_ADMIN)).isEmpty()) {
                    Map<String, Object> menuJson = this.menuJson.getAdminMenuJson().getMenuJson();
                    roleRepository.save(new Role(name, menuJson));
                }

                // 3. Student Role
                name = String.valueOf(RoleEnum.STUDENT);
                if (roleRepository.findByRoleName(name).isEmpty()) {
                    Map<String, Object> menuJson = this.menuJson.getStudentMenuJson().getMenuJson();
                    roleRepository.save(new Role(name, menuJson));
                }

                // 4. Teacher Role
                name = String.valueOf(RoleEnum.TEACHER);
                if (roleRepository.findByRoleName(name).isEmpty()) {
                    Map<String, Object> menuJson = this.menuJson.getTeacherMenuJson().getMenuJson();
                    roleRepository.save(new Role(name, menuJson));
                }
                log.info("Roles seed");
            } catch (Exception e) {
                log.error("Critical failure during roles space initialization context: ", e);
                throw new RuntimeException("Workspace bootstrapping failed", e);
            }
        }
}

