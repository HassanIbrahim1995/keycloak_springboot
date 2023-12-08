package com.example.demokeycloakspring.dto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public record KeycloakProperties(
        @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuerUrl,
        @Value("${spring.security.oauth2.client.registration.keycloak.client-id}") String clientId,
        @Value("${spring.security.oauth2.client.registration.keycloak.authorization-grant-type}") String grantType,
        @Value("${token.url}") String tokeUrl
) {
}
