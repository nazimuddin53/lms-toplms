package com.toplms.core.menuJson;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@Getter
@Setter
public class MenuJson {

    private SuperAdminMenuJson superadminMenuJson;
    private AdminMenuJson adminMenuJson;
    private TeacherMenuJson teacherMenuJson;
    private StudentMenuJson studentMenuJson;

    public MenuJson(SuperAdminMenuJson superadminMenuJson, TeacherMenuJson teacherMenuJson, StudentMenuJson studentMenuJson, AdminMenuJson adminMenuJson) {
        this.superadminMenuJson = superadminMenuJson;
        this.studentMenuJson = studentMenuJson;
        this.adminMenuJson = adminMenuJson;
        this.teacherMenuJson = teacherMenuJson;

    }
}
