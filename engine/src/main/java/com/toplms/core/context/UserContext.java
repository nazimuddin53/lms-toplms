package com.toplms.core.context;


import org.springframework.stereotype.Component;
import com.toplms.core.menuJson.RoleEnum;


public class UserContext {

    private static final ThreadLocal<UserContextInfo> currentInfo = new ThreadLocal<>();

    // The data carrier record
    public record UserContextInfo(String email, RoleEnum role, String tenantId) {

        /**
         * Overloaded constructor specifically for Global Platform Admins
         * who do not belong to any tenant.
         */
        public UserContextInfo(String email, RoleEnum role) {
            this(email, role, null);
        }
    }

    public static void setUserInfo(UserContextInfo info) {
        currentInfo.set(info);
    }

    public static UserContextInfo getUserInfo() {
        return currentInfo.get();
    }

    public static String getCurrentUserEmail() {
        UserContextInfo info = currentInfo.get();
        return (info != null) ? info.email() : null;
    }

    public static RoleEnum getCurrentUserRole() {
        UserContextInfo info = currentInfo.get();
        return (info != null) ? info.role() : null;
    }

    /**
     * Cleanly returns null for users that do not have a tenant identity (like platform admins)
     */
    public static String getCurrentUserTenantId() {
        UserContextInfo info = currentInfo.get();
        return (info != null) ? info.tenantId() : null;
    }

    public static boolean isPlatformAdmin() {
        UserContextInfo info = currentInfo.get();
        return info != null && RoleEnum.SUPER_ADMIN.equals(info.role());
    }

    public static void clear() {
        currentInfo.remove();
    }
}