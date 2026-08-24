package com.toplms.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("demo")
@Validated
@Component
@Data
@Getter
@Setter
public class DemoTenantProperties {
    private String companyName;
    private String subdomain; // e.g. "mit" -> becomes mit.localhost
    private String planId;
    private String name;
    private String adminEmail;
    private String adminPassword;
}

