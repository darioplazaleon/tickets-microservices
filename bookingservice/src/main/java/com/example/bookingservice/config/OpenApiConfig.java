package com.example.bookingservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI bookingServiceOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Booking Service API")
                .version("1.0.0")
                .description(
                    """
        This service manages customer accounts, handles bookings, and processes ticket reservations.

        It provides endpoints for customer registration, booking management, and ticket reservation workflows.
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
