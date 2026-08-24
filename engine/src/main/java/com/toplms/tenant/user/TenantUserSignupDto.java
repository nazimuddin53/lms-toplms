package com.toplms.tenant.user;

import lombok.*;
@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class TenantUserSignupDto {
    private String name;
    private String email;
    private String password;

}
