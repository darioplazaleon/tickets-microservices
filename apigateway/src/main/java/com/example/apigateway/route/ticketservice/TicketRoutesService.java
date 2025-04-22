package com.example.apigateway.route.ticketservice;

import com.example.apigateway.config.CustomHeaderFilter;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

@Configuration
public class TicketRoutesService {

    @Bean
    public RouterFunction<ServerResponse> ticketRoutes() {
        return GatewayRouterFunctions.route("ticket-service")
                .route(RequestPredicates.POST("/api/v1/tickets/validate"),
                        HandlerFunctions.http("http://localhost:8086/api/v1/tickets/validate"))
                .filter(CustomHeaderFilter.addCustomHeaders())
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("ticketServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

}
