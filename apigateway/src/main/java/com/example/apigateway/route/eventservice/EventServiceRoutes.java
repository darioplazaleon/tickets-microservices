package com.example.apigateway.route.eventservice;

import com.example.apigateway.config.CustomHeaderFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;

@Configuration
public class EventServiceRoutes {

    @Value("${services.event-service.url}")
    private String eventServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> eventRoutes() {
        return GatewayRouterFunctions.route("event-service")
                .route(RequestPredicates.GET("/api/v1/event/{eventId}"),
                        request -> forwardWithPathVariable(request, "eventId", eventServiceUrl + "/api/v1/event/"))
                .route(RequestPredicates.GET("/api/v1/events/all"),
                        HandlerFunctions.http(eventServiceUrl + "/api/v1/events/all"))
                .route(RequestPredicates.POST("/api/v1/events/create"),
                        HandlerFunctions.http(eventServiceUrl + "/api/v1/events/create"))
                .route(RequestPredicates.PUT("/api/v1/events/update/{eventId}"),
                        request -> forwardWithPathVariable(request, "eventId", eventServiceUrl + "/api/v1/events/update/"))
                .route(RequestPredicates.DELETE("/api/v1/events/delete/{eventId}"),
                        request -> forwardWithPathVariable(request, "eventId", eventServiceUrl + "/api/v1/events/delete/"))
                .filter(CustomHeaderFilter.addCustomHeaders())
                .build();
    }

    private static ServerResponse forwardWithPathVariable(ServerRequest request, String pathVariable, String baseURl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.http(baseURl + value).handle(request);
    }

    @Bean
    public RouterFunction<ServerResponse> eventServiceApiDocs() {
        return GatewayRouterFunctions.route("event-service-api-docs")
                .route(RequestPredicates.path("/docs/eventservice/v3/api-docs"),
                        HandlerFunctions.http(eventServiceUrl))
                .filter(setPath("/v3/api-docs"))
                .build();
    }
}
