package com.toplms.master.tenant;

import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class TenantRegistrationDto {
    private String name;
    private String companyName;
    private String subdomain; // e.g. "mit" -> becomes mit.localhost
    private String planId;
    private String adminEmail;
    private String adminPassword;
}
