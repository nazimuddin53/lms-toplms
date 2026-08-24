package com.toplms.core.bdseed;

import com.toplms.config.AppHostProperties;
import com.toplms.core.menuJson.RoleEnum;
import com.toplms.domain.base.Role;
import com.toplms.domain.base.User;
import com.toplms.master.users.RoleService;
import com.toplms.master.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@org.springframework.core.annotation.Order(3)
public class UserDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserDataInitializer.class);
    private final RoleService roleService;
    private final UserService userService;
    private final AppHostProperties appHostProperties;

    public UserDataInitializer(RoleService roleService, UserService userService,  AppHostProperties appHostProperties) {
        this.roleService = roleService;
        this.userService = userService;
        this.appHostProperties = appHostProperties;
    }

    @Override
    public void run(String... args) {
        try {
            Optional<Role> role = this.roleService.findByRoleName(String.valueOf(RoleEnum.SUPER_ADMIN));
            if (role.isEmpty()) {
                log.error("Aborting tenant seeding: Role {} does not exist in the database.", role.get().getName());
                return; // Stop execution before causing a database foreign-key crash
            }
            Optional<User> userOpt = this.userService.findByEmail(this.appHostProperties.getEmail());
            if (userOpt.isPresent()) {
                log.info("User {} has been seeded.", userOpt.get().getName());
                return;
            }
            User newUser = new User();
            newUser.setEmail(this.appHostProperties.getEmail());
            newUser.setName("Admin");
            newUser.setRole(role.get());

            log.info("Seeding admin user workspace for superadmin email: {} password: {}", this.appHostProperties.getEmail(), this.appHostProperties.getPassword());
            this.userService.create(newUser, this.appHostProperties.getPassword());

        }  catch (Exception e) {
            log.error("Critical failure during admin user space initialization context: ", e);
            throw new RuntimeException("Workspace bootstrapping failed", e);
        }
    }
}
