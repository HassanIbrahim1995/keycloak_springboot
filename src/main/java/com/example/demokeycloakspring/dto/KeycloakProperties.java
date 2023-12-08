package com.example.demokeycloakspring.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "keycloak")
@Getter
@Setter
public class KeycloakProperties {
    private String issuerUrl;
    private String clientId;
    private String grantType;
    private String tokenUrl;
    private String logoutUrl;
}
