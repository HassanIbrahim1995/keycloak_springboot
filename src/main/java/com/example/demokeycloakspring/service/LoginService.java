package com.example.demokeycloakspring.service;

import com.example.demokeycloakspring.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service class for handling login and logout functionalities with Keycloak.
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private static final String CLIENT_ID = "client_id";
    private static final String TOKEN = "token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_PASSWORD = "password";

    private final WebClient webClient;

    private final KeycloakProperties keycloakProperties;

    /**
     * Logs in a user using the provided login request details.
     *
     * @param loginRequest the login request containing username and password
     * @return ResponseEntity with LoginResponse and HTTP status code
     */
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID, keycloakProperties.getClientId());
        map.add(GRANT_TYPE, GRANT_TYPE_PASSWORD);
        map.add("username", loginRequest.getUsername());
        map.add("password", loginRequest.getPassword());

        return webClient.post()
                .uri(keycloakProperties.getTokenUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(map)
                .retrieve()
                .toEntity(LoginResponse.class)
                .block();
    }

    /**
     * Logs out a user using the provided token request.
     *
     * @param request the token request containing the refresh token
     * @return ResponseEntity with Response message and HTTP status code
     */
    public ResponseEntity<Response> logout(TokenRequest request) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID, keycloakProperties.getClientId());
        map.add("refresh_token", request.getToken());
        ResponseEntity<Response> response = webClient.post()
                .uri(keycloakProperties.getLogoutUrl())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(map)
                .retrieve()
                .toEntity(Response.class)
                .block();

        if (response.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.ok(new Response("Logged out successfully"));
        }
        return ResponseEntity.status(response.getStatusCode()).body(null);
    }
}
