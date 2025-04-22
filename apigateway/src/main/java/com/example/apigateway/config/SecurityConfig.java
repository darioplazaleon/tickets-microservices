package com.example.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${keycloak.auth-server-uri}")
    private String jwtSetUri;

    @Autowired
    private JwtAuthenticationConverter jwtConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/events/{eventId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tags/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tags/{tagId}").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.POST, "/api/v1/tags/create").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tags/update/{tagId}").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/tags/delete/{tagId}").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/all").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories/{categoryId}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/categories/create").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/categories/update/{categoryId}").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/delete/{categoryId}").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.POST, "/api/v1/events/create").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/events/update/{eventId}").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/events/delete/{eventId}").hasRole("admin_client_role")
                        .requestMatchers(HttpMethod.POST, "/api/v1/payment/checkout/{orderId}").hasRole("user_client_role")
                        .requestMatchers(HttpMethod.POST, "/api/v1/bookings/create").hasRole("user_client_role")
                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets/validate").hasRole("user_client_role")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> {
                    oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter));
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwtSetUri).build();
    }
}
