package com.example.ticketservice.config;

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
    public OpenAPI ticketServiceOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Ticket Service API")
                                .version("1.0.0")
                                .description(
                                        """
                                                This service manages ticket ownership, validation, transfer, and provides ticket data for other microservices.
                                                
                                                It exposes endpoints for users to view and transfer their tickets, validate QR codes, and allows internal services to fetch ticket information.
                                                """)
                                .contact(
                                        new Contact()
                                                .name("Dario A. Plaza Leon")
                                                .email("contact@plazaleon.tech")
                                                .url("https://plazaleon.tech")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                ));
    }
}
