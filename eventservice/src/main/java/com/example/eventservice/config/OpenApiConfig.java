package com.example.eventservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventServiceApi() {
        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")))
                .info(
                        new Info()
                                .title("Event Service API")
                                .description(
                                        """
                                                TThis service manages events, venues, categories, tags, and ticket types.
                                                
                                                It provides public endpoints for listing events and categories, and secured administrative endpoints for creating, updating, and deleting event-related entities.
                                                
                                                Event capacity management, ticket types availability, and venue assignments are handled within this service.
                                                """)
                                .version("1.0.0")
                                .contact(
                                        new Contact()
                                                .name("Dario A. Plaza Leon")
                                                .email("contact@plazaleon.tech")
                                                .url("https://plazaleon.tech")));
    }
}
