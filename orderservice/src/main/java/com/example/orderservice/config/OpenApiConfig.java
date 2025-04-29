package com.example.orderservice.config;

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
    public OpenAPI OrderServiceOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Order Service API")
                                .version("1.0.0")
                                .description(
                                        """
                            This service manages customer orders, including order creation, order expiration, status updates, and communication with payment service.
                    
                            It supports full retrieval and summary access to orders for internal microservices and customers.
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
