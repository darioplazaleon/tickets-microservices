package com.example.paymentservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;

public class OpenApiConfig {
    @Bean
    public OpenAPI paymentServiceOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Payment Service API")
                                .version("1.0.0")
                                .description(
                                        """
                                              This service manages Stripe payment sessions, payment confirmation, and webhook handling.
                                                
                                              It exposes endpoints for initiating customer checkout flows and receiving payment event notifications.
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
