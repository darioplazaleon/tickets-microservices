package com.example.apigateway.route.ticketservice;

import com.example.apigateway.config.CustomHeaderFilter;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.*;

import java.net.URI;

@Configuration
public class TicketRoutesService {

    @Bean
    public RouterFunction<ServerResponse> ticketRoutes() {
        return GatewayRouterFunctions.route("ticket-service")
                .route(RequestPredicates.POST("/api/v1/tickets/validate"),
                        HandlerFunctions.http("http://localhost:8086/api/v1/tickets/validate"))
                .route(RequestPredicates.POST("/api/v1/tickets/transfer/{ticketId}"),
                        request -> forwardWithPathVariable(request, "ticketId", "http://localhost:8086/api/v1/tickets/transfer/"))
                .filter(CustomHeaderFilter.addCustomHeaders())
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("ticketServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    private static ServerResponse forwardWithPathVariable(ServerRequest request, String pathVariable, String baseURl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.http(baseURl + value).handle(request);
    }

}
