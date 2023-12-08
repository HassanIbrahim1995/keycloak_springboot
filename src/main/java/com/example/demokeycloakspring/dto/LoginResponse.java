package com.example.demokeycloakspring.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponse {
    private String access_token;
    @JsonIgnore
    private String refresh_token;
    private String expires_in;
    @JsonIgnore
    private String refresh_expires_in;
    @JsonIgnore
    private String token_type;
}
