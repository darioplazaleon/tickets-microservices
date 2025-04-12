package com.example.apigateway.route;

import com.example.apigateway.config.CustomHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;
import java.util.UUID;

@Configuration
public class BookingServiceRoutes {

    String userId = "4818";
    String correlationId = "1234";

    @Bean
    public RouterFunction<ServerResponse> bookingRoutes() {
        return GatewayRouterFunctions.route("booking-service")
                .route(RequestPredicates.POST("/api/v1/bookings/create"),
                        HandlerFunctions.http("http://localhost:8081/api/v1/bookings/create"))
                .route(RequestPredicates.POST("/api/v1/customers/register"),
                        HandlerFunctions.http("http://localhost:8081/api/v1/customers/register"))
                .filter(CustomHeaderFilter.addCustomHeaders())
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("bookingServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return GatewayRouterFunctions.route("fallbackRoute")
                .POST("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("Booking service is down. Please try again later."))
                .build();
    }

}
