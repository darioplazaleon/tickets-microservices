package com.example.apigateway.route.eventservice;

import com.example.apigateway.config.CustomHeaderFilter;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class EventServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> eventRoutes() {
        return GatewayRouterFunctions.route("event-service")
                .route(RequestPredicates.GET("/api/v1/event/{eventId}"),
                        request -> forwardWithPathVariable(request, "eventId", "http://localhost:8080/api/v1/event/"))
                .route(RequestPredicates.GET("/api/v1/events/all"),
                        HandlerFunctions.http("http://localhost:8080/api/v1/events/all"))
                .route(RequestPredicates.POST("/api/v1/events/create"),
                        HandlerFunctions.http("http://localhost:8080/api/v1/events/create"))
                .route(RequestPredicates.PUT("/api/v1/events/update/{eventId}"),
                        request -> forwardWithPathVariable(request, "eventId", "http://localhost:8080/api/v1/events/update/"))
                .route(RequestPredicates.DELETE("/api/v1/events/delete/{eventId}"),
                        request -> forwardWithPathVariable(request, "eventId", "http://localhost:8080/api/v1/events/delete/"))
                .filter(CustomHeaderFilter.addCustomHeaders())
                .build();
    }

    private static ServerResponse forwardWithPathVariable(ServerRequest request, String pathVariable, String baseURl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.http(baseURl + value).handle(request);
    }
}
