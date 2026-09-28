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
                upsertRoleMenu(RoleEnum.SUPER_ADMIN, this.menuJson.getSuperadminMenuJson().getMenuJson());
                upsertRoleMenu(RoleEnum.TENANT_ADMIN, this.menuJson.getAdminMenuJson().getMenuJson());
                upsertRoleMenu(RoleEnum.STUDENT, this.menuJson.getStudentMenuJson().getMenuJson());
                upsertRoleMenu(RoleEnum.TEACHER, this.menuJson.getTeacherMenuJson().getMenuJson());
                log.info("Roles seed");
            } catch (Exception e) {
                log.error("Critical failure during roles space initialization context: ", e);
                throw new RuntimeException("Workspace bootstrapping failed", e);
            }
        }

        // The *MenuJson beans are the single source of truth for sidebar content, so re-sync
        // menu_json on every startup instead of only inserting it the first time the role is created.
        private void upsertRoleMenu(RoleEnum role, Map<String, Object> menuJson) {
            String name = String.valueOf(role);
            Role existing = roleRepository.findByRoleName(name).orElse(null);
            if (existing == null) {
                roleRepository.save(new Role(name, menuJson));
            } else if (!menuJson.equals(existing.getMenuJson())) {
                existing.setMenuJson(menuJson);
                roleRepository.save(existing);
            }
        }
}

