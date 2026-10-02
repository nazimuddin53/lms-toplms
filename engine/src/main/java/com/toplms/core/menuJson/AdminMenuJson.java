package com.toplms.core.menuJson;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Data
@Getter
@Setter
@NoArgsConstructor
public class AdminMenuJson {
   public Map<String, Object> menuJson = Map.of(
            "version", "1.0",
            "sidebar", List.of(
                    // 1. Core Dashboard Link
                    Map.of(
                            "title", "Dashboard",
                            "icon", "dashboard-icon",
                            "path", "/dashboard",
                            "roles", List.of("TENANT_ADMIN", "TEACHER", "STUDENT")
                    ),

                    // 2. Academic Management (Courses & Modules)
                    Map.of(
                            "title", "Courses",
                            "icon", "book-open-icon",
                            "roles", List.of("TENANT_ADMIN", "TEACHER", "STUDENT"),
                            "path", "/dashboard/courses"
                    ),

                    // 3. Examination & Assessment (Gated by 'quizzes' module configuration)
                    Map.of(
                            "title", "Students",
                            "icon", "users-icon",
                            "path", "/dashboard/students",
                            "roles", List.of("TENANT_ADMIN", "TEACHER", "STUDENT")
                    ),

                    // 3. Examination & Assessment (Gated by 'quizzes' module configuration)
                    Map.of(
                            "title", "Teachers",
                            "icon", "quiz-icon",
                            "path", "/dashboard/teachers",
                            "roles", List.of("TENANT_ADMIN", "TEACHER", "STUDENT")
                    ),

//                    // 4. User Workspace Management
//                    Map.of(
//                            "title", "Users",
//                            "icon", "users-icon",
//                            "roles", List.of("TENANT_ADMIN", "TEACHER")
//                    ),

                    // 5. Tenant Administration Panel. Without a "path" this rendered as a
                    // dead link (th:href="@{null}"), so it now points at the billing page.
                    Map.of(
                            "title", "Settings & Billing",
                            "icon", "settings-icon",
                            "path", "/dashboard/billing",
                            "roles", List.of("TENANT_ADMIN")
                    )
            )
    );
}
