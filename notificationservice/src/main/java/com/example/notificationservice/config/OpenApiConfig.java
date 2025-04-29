package com.example.notificationservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .version("1.0.0")
                        .description(
                                """
                                This service handles sending email notifications based on Kafka events.
            
                                ⚡ **Note:** This service does not expose public HTTP endpoints.
            
                                It listens to Kafka events such as `payment.succeeded` and `ticket.qr.generated`, and sends appropriate email notifications to users.
                                """
                        )
                        .contact(new Contact()
                                .name("Dario A. Plaza Leon")
                                .email("contact@plazaleon.tech")
                                .url("https://plazaleon.tech"))
                );
    }
}
