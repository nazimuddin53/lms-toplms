package com.toplms.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@ConfigurationProperties("app")
@Validated
@Component
@Data
@Getter
@Setter
public class AppHostProperties {
    private int port;
    private String host = "localhost";
    private String contextPath = "/";
    private String baseUrl ;
    private String email;
    private String password;
    private String token;
    private String secretKey;
}
