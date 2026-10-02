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
public class SuperAdminMenuJson {
    public Map<String, Object> menuJson = Map.of(
            "version", "1.0",
            "sidebar", List.of(
                    // 1. Core Dashboard Link
                    Map.of(
                            "title", "System Control Center",
                            "icon", "dashboard-icon",
                            "path", "/dashboard",
                            "roles", List.of("SUPER_ADMIN")
                    ),

                    // 2. Tenant registry
                    Map.of(
                            "title", "Client Workspaces",
                            "icon", "tenants-icon",
                            "path", "/superadmin/tenants",
                            "roles", List.of("SUPER_ADMIN")
                    ),

                    // 3. Subscription plan catalog
                    Map.of(
                            "title", "Subscription Packages",
                            "icon", "plans-icon",
                            "path", "/superadmin/plans",
                            "roles", List.of("SUPER_ADMIN")
                    ),

                    // 4. Cross-tenant billing history. Per-tenant payments also appear on the
                    // tenant detail page; this is the platform-wide ledger plus edit/delete.
                    Map.of(
                            "title", "Payment Records",
                            "icon", "payments-icon",
                            "path", "/superadmin/payments",
                            "roles", List.of("SUPER_ADMIN")
                    )

                    // 5. Platform-wide metrics
//                    Map.of(
//                            "title", "Hardware Diagnostics",
//                            "icon", "metrics-icon",
//                            "path", "/superadmin/metrics",
//                            "roles", List.of("SUPER_ADMIN")
//                    )
            )
    );

}
