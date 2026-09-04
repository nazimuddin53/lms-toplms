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
import org.springframework.web.bind.annotation.RequestParam;

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
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        TenantUserSignInDto tenantUserSignInDto = new TenantUserSignInDto();

        model.addAttribute("signInDto", tenantUserSignInDto);
        if(TenantContext.getCurrentPageType().equals(LoadingPageType.MAIN)) {
            model.addAttribute("userType", "Administrator Profile");
        } else {
            model.addAttribute("userType", "User Profile");
        }

        if ("session_expired".equals(error)) {
            model.addAttribute("errorMessage", "Your session has expired. Please sign in again.");
        } else if ("invalid_tenant".equals(error)) {
            model.addAttribute("errorMessage", "Invalid workspace or tenant not found.");
        } else if ("unauthorized".equals(error)) {
            model.addAttribute("errorMessage", "You are not authorized to access this resource.");
        }

        if ("success".equals(logout)) {
            model.addAttribute("successMessage", "You have been signed out successfully.");
        }

        return "public/login";
    }

    @Public
    @PostMapping("login")
    public String postLogin(@Valid @ModelAttribute("signInDto") TenantUserSignInDto dto,
                            BindingResult bindingResult, HttpServletResponse response, Model model) {

        Tenant currentTenant = TenantContext.getCurrentTenant();
        if (TenantContext.getCurrentPageType().equals(LoadingPageType.MAIN)) {
            model.addAttribute("userType", "Administrator Profile");

            if (bindingResult.hasErrors()) {
                return "public/login";
            }

            Optional<User> userOpt = this.userService.findByEmail(dto.getEmail());

            if (userOpt.isEmpty() || !this.passwordEncoder.matches(dto.getPassword(), userOpt.get().getPassword())) {
                model.addAttribute("errorMessage", "Invalid email or password");
                return "public/login";
            }

            User user = userOpt.get();

            try {
                String token = this.jwtProvider.generateToken(user.getEmail(), user.getRole().getName());
                this.authenticationInterceptor.setTokenInCookie(response, token);
                return "redirect:" + this.appHostProperties.getBaseUrl() + "/dashboard";
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", e.getMessage());
                return "public/login";
            }
        } else {
            model.addAttribute("userType", "User Profile");

            if (bindingResult.hasErrors()) {
                return "public/login";
            }

            Optional<TenantUser> tenantUserOpt = this.tenantUserService.findByEmail(dto.getEmail());

            if (tenantUserOpt.isEmpty() || !this.passwordEncoder.matches(dto.getPassword(), tenantUserOpt.get().getPassword())) {
                model.addAttribute("errorMessage", "Invalid email or password");
                return "public/login";
            }

            TenantUser tenantUser = tenantUserOpt.get();

            try {
                String token = this.jwtProvider.generateToken(tenantUser.getEmail(), tenantUser.getRole().getName(), currentTenant.getId());
                this.authenticationInterceptor.setTokenInCookie(response, token);
                return "redirect:/dashboard";
            } catch (IllegalArgumentException e) {
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
        Optional<TenantUser> existingUser = this.tenantUserService.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            model.addAttribute("errorMessage", "Email already in use");
            return "tenant/public/signup";
        }

        if (bindingResult.hasErrors()) {
            return "tenant/public/signup";
        }

        try {
            Optional<Role> role = this.roleService.findByRoleName(String.valueOf(RoleEnum.STUDENT));

            TenantUser newUser = new TenantUser();
            newUser.setEmail(dto.getEmail());
            newUser.setName(dto.getName());
            role.ifPresent(newUser::setRole);

            this.tenantUserService.create(newUser, dto.getPassword());
            String roleName = newUser.getRole() != null ? newUser.getRole().getName() : RoleEnum.STUDENT.name();
            String token = this.jwtProvider.generateToken(newUser.getEmail(), roleName, currentTenant.getId());

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
