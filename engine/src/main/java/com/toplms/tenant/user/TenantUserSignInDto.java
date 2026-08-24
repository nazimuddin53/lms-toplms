package com.toplms.tenant.user;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class TenantUserSignInDto {
    private String email;
    private String password;
}
