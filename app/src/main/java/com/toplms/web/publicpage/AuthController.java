package com.toplms.web.publicpage;

import com.toplms.config.AppHostProperties;
import com.toplms.core.annotation.Public;
import com.toplms.core.context.TenantContext;
import com.toplms.core.context.UserContext;
import com.toplms.core.enumType.LoadingPageType;
import com.toplms.core.interceptor.AuthenticationInterceptor;
import com.toplms.core.interceptor.TenantInterceptor;
import com.toplms.core.menuJson.RoleEnum;
import com.toplms.core.security.JwtProvider;
import com.toplms.domain.base.Role;
import com.toplms.domain.base.Tenant;
import com.toplms.domain.base.User;
import com.toplms.domain.tenant.TenantUser;
import com.toplms.master.tenant.TenantService;
import com.toplms.master.users.RoleService;
import com.toplms.master.users.UserService;
import com.toplms.tenant.user.TenantUserService;
import com.toplms.tenant.user.TenantUserSignInDto;
import com.toplms.tenant.user.TenantUserSignupDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class AuthController {
    @Autowired
    private TenantService tenantService;
    @Autowired
    private AppHostProperties appHostProperties;
    @Autowired
    private TenantUserService tenantUserService;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    @Autowired
    private RoleService roleService;

    @Public
    @GetMapping("login")
    public String login(Model model) {
        TenantUserSignInDto tenantUserSignInDto = new TenantUserSignInDto();

        model.addAttribute("signInDto", tenantUserSignInDto);
        if(TenantContext.getCurrentPageType().equals(LoadingPageType.MAIN)) {
            model.addAttribute("userType", "Administrator Profile");
        } else {
            model.addAttribute("userType", "User Profile");
        }

        return "public/login";
    }

    @Public
    @PostMapping("login")
    public String postLogin(@Valid @ModelAttribute("signInDto") TenantUserSignInDto dto,
                            BindingResult bindingResult, HttpServletResponse response, Model model) {

        Tenant currentTenant = TenantContext.getCurrentTenant();
        if (TenantContext.getCurrentPageType().equals(LoadingPageType.MAIN)) {
            Optional<User> userOpt = this.userService.findByEmail(dto.getEmail());

            if (userOpt.isPresent()) {
                model.addAttribute("errorMessage", "Invalid email or password");
            }

            User user = userOpt.get();

            if(userOpt.isPresent() && !this.passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                model.addAttribute("errorMessage", "Invalid password");
            }


            if (bindingResult.hasErrors()) {
                return "public/login";
            }
            try {
                String token = this.jwtProvider.generateToken(user.getEmail(), user.getRole().getName());

                this.authenticationInterceptor.setTokenInCookie(response, token);
                return "redirect:" + this.appHostProperties.getBaseUrl() +"/dashboard";
            } catch (IllegalArgumentException e) {
                // 4. CRITICAL ERROR CATCH: Display custom exceptions (e.g., "Subdomain already taken!")
                model.addAttribute("errorMessage", e.getMessage());

                return "public/login";
            }
        } else {
            Optional<TenantUser> tenantUser = this.tenantUserService.findByEmail(dto.getEmail());

            if (tenantUser.isEmpty()) {
                model.addAttribute("errorMessage", "Invalid email or password");
            }

            if(tenantUser.isPresent() && !this.passwordEncoder.matches(dto.getPassword(), tenantUser.get().getPassword())) {
                model.addAttribute("errorMessage", "Invalid password");
            }


            if (bindingResult.hasErrors()) {
                return "public/login";
            }

            try {
                String token = this.jwtProvider.generateToken(tenantUser.get().getEmail(), tenantUser.get().getRole().getName(), currentTenant.getId());

                this.authenticationInterceptor.setTokenInCookie(response, token);
                return "redirect:/dashboard";
            } catch (IllegalArgumentException e) {
                // 4. CRITICAL ERROR CATCH: Display custom exceptions (e.g., "Subdomain already taken!")
                model.addAttribute("errorMessage", e.getMessage());

                return "public/login";
            }
        }


    }

    @Public
    @GetMapping("signup")
    public String signup( Model model) {
        Tenant tenant = TenantContext.getCurrentTenant();
        if (tenant == null) {
            return "redirect:" + this.appHostProperties.getBaseUrl() + "/register";
        }
        TenantUserSignupDto tenantUserSignupDto = new TenantUserSignupDto();

        model.addAttribute("signupDto", tenantUserSignupDto);
        return "tenant/public/signup";
    }

    @Public
    @PostMapping("signup")
    public String signup(@Valid @ModelAttribute("signInDto") TenantUserSignupDto dto,
                         BindingResult bindingResult, HttpServletResponse response, Model model) {
        Tenant currentTenant = TenantContext.getCurrentTenant();
        if (currentTenant == null) {
            return "redirect:" + this.appHostProperties.getBaseUrl() + "/register";
        }
        Optional<TenantUser> tenantUser = this.tenantUserService.findByEmail(dto.getEmail());
        if (tenantUser.isPresent()) {
            model.addAttribute("errorMessage", "Email already in use");
        }

        if (bindingResult.hasErrors()) {
            return "tenant/public/signup";
        }

        try {
            Optional<Role> role = this.roleService.findByRoleName(String.valueOf(RoleEnum.STUDENT));

            TenantUser newUser = new TenantUser();
            newUser.setEmail(dto.getEmail());
            newUser.setName(dto.getName());
            newUser.setRole(role.get());

            this.tenantUserService.create(newUser, dto.getPassword());
            String token = this.jwtProvider.generateToken(tenantUser.get().getEmail(), tenantUser.get().getPassword(), currentTenant.getId());

            this.authenticationInterceptor.setTokenInCookie(response, token);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());

            return "tenant/public/signup";
        }

    }

    @GetMapping("logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. Locate the authentication cookie wrapper container
        this.authenticationInterceptor.invalidTokenCookie(request, response);

        UserContext.clear();

        Tenant tenant = TenantContext.getCurrentTenant();

        if (tenant == null) {
            // Option A: Running under the Main Platform Root Hub Core
            TenantContext.clear();
            return "redirect:/login?logout=success";
        }

        TenantContext.clear();
        return "redirect:/login?logout=success";
    }
}
