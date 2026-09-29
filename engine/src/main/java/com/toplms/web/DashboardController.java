package com.toplms.web;

import com.toplms.core.context.TenantContext;
import com.toplms.core.context.UserContext;
import com.toplms.core.enumType.LoadingPageType;
import com.toplms.domain.base.Tenant;
import com.toplms.domain.base.User;
import com.toplms.domain.tenant.TenantUser;
import com.toplms.master.users.RoleService;
import com.toplms.master.users.UserService;
import com.toplms.tenant.course.service.CourseService;
import com.toplms.tenant.student.service.StudentService;
import com.toplms.tenant.teacher.service.TeacherService;
import com.toplms.tenant.user.TenantUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("dashboard")
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private TenantUserService tenantUserService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @GetMapping()
    public String dashboard(Model model) {

        // 1. Check if the request is executing under the Central SuperAdmin Core space
        if (TenantContext.getCurrentPageType().equals(LoadingPageType.MAIN)) {

            Optional<User> userOpt =
                    this.userService.findByEmail(UserContext.getUserInfo().email());

            if (userOpt.isEmpty()) {
                return "redirect:/login?error=session_expired";
            }

            User user = userOpt.get();

            model.addAttribute("user", user);
            model.addAttribute("userType", user.getRole().getName());

            // SuperAdmin dashboard
            return "dashboard/dashboard";
        }

        // 2. Tenant Workspace routing execution lane
        Tenant currentTenant = TenantContext.getCurrentTenant();

        Optional<TenantUser> tenantUserOpt =
                this.tenantUserService.findByEmail(UserContext.getUserInfo().email());

        TenantUser tenantUser = tenantUserOpt.get();

        // 3. Bind properties to the UI layout template
        model.addAttribute("user", tenantUser);
        model.addAttribute("currentTenant", currentTenant);
        model.addAttribute("currentTenantPlanId", currentTenant.getPlan().getId());
        model.addAttribute("userType", tenantUser.getRole().getName());

        // 4. Sidebar menu
        if (tenantUser.getRole() != null &&
                tenantUser.getRole().getMenuJson() != null) {

            model.addAttribute(
                    "sidebarMenus",
                    tenantUser.getRole().getMenuJson()
            );
        }

        // 5. Course information
        model.addAttribute(
                "totalCourses",
                courseService.getTotalCourses()
        );

        // 6. Student information
        model.addAttribute(
                "totalStudents",
                studentService.getTotalStudents()
        );

        // 7. Teacher information
        model.addAttribute(
                "totalTeachers",
                teacherService.getTotalTeachers()
        );

        // 8. Tenant dashboard
        return "tenant/dashboard/dashboard";
    }
}