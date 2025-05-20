package com.example.bookingservice.client;

import com.example.bookingservice.request.UserRequest;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class KeycloakAdminClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    public UUID createUserAngGetId(UserRequest userRequest) {
        String token = getAdminAccessToken();

        Map<String, Object> user = Map.of(
                "username", userRequest.username(),
                "email", userRequest.email(),
                "enabled", true,
                "credentials", new Object[]{
                        Map.of(
                                "type", "password",
                                "value", userRequest.password(),
                                "temporary", false
                        )
                }
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);

        String registerUserUrl = keycloakUrl + "/admin/realms/" + realm + "/users";

        ResponseEntity<String> response = restTemplate.exchange(registerUserUrl, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create user: " + response.getBody());
        }

        String location = Objects.requireNonNull(response.getHeaders().getLocation()).toString();
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }

    private String getAdminAccessToken() {
        String tokenUrl = keycloakUrl + "/admin/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=password" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&username=" + adminUsername +
                "&password=" + adminPassword;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get admin access token");
        }

        return response.getBody().get("access_token").toString();
    }
}
