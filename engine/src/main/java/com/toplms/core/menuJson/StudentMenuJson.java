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
public class StudentMenuJson {
    public  Map<String, Object> menuJson = Map.of(
            "version", "1.0",
            "sidebar", List.of(
                    // 1. Core Dashboard Link
                    Map.of(
                            "title", "Dashboard",
                            "icon", "dashboard-icon",
                            "path", "/dashboard",
                            "roles", List.of("TENANT_ADMIN", "INSTRUCTOR", "STUDENT")
                    ),

                    // 2. Academic Management (Courses & Modules)
                    Map.of(
                            "title", "Academics",
                            "icon", "book-open-icon",
                            "roles", List.of("TENANT_ADMIN", "INSTRUCTOR", "STUDENT"),
                            "children", List.of(
                                    Map.of("title", "All Courses", "path", "/academics/courses"),
                                    Map.of("title", "My Enrollments", "path", "/academics/my-learning", "roles", List.of("STUDENT")),
                                    Map.of("title", "Assignments", "path", "/academics/assignments", "moduleFlag", "assignments")
                            )
                    ),

                    // 3. Examination & Assessment (Gated by 'quizzes' module configuration)
                    Map.of(
                            "title", "Assessments",
                            "icon", "quiz-icon",
                            "moduleFlag", "quizzes",
                            "roles", List.of("TENANT_ADMIN", "INSTRUCTOR", "STUDENT"),
                            "children", List.of(
                                    Map.of("title", "Quizzes", "path", "/assessments/quizzes"),
                                    Map.of("title", "AI Smart Grading", "path", "/assessments/ai-grading", "roles", List.of("TENANT_ADMIN", "INSTRUCTOR"))
                            )
                    ),

                    // 4. User Workspace Management
                    Map.of(
                            "title", "Users & Groups",
                            "icon", "users-icon",
                            "roles", List.of("TENANT_ADMIN", "INSTRUCTOR"),
                            "children", List.of(
                                    Map.of("title", "Students Directory", "path", "/users/students"),
                                    Map.of("title", "Instructors Staff", "path", "/users/instructors", "roles", List.of("TENANT_ADMIN"))
                            )
                    ),

                    // 5. Tenant Administration Panel
                    Map.of(
                            "title", "Settings & Billing",
                            "icon", "settings-icon",
                            "roles", List.of("TENANT_ADMIN"),
                            "children", List.of(
                                    Map.of("title", "Workspace Customization", "path", "/settings/branding"),
                                    Map.of("title", "Subscription & Invoices", "path", "/settings/billing")
                            )
                    )
            )
    );
}
