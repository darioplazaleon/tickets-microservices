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
public class VenueServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> venueRoutes() {
        return GatewayRouterFunctions.route("venue-service")
                .route(RequestPredicates.GET("/api/v1/venues/{venueId}"),
                        request -> forwardWithPathVariable(request, "venueId", "http://localhost:8080/api/v1/venues/"))
                .route(RequestPredicates.GET("/api/v1/venues/all"),
                        HandlerFunctions.http("http://localhost:8080/api/v1/venues/all"))
                .route(RequestPredicates.POST("/api/v1/venues/create"),
                        HandlerFunctions.http("http://localhost:8080/api/v1/venues/create"))
                .route(RequestPredicates.PUT("/api/v1/venues/update/{venueId}"),
                        request -> forwardWithPathVariable(request, "venueId", "http://localhost:8080/api/v1/venues/update/"))
                .route(RequestPredicates.DELETE("/api/v1/venues/delete/{venueId}"),
                        request -> forwardWithPathVariable(request, "venueId", "http://localhost:8080/api/v1/venues/delete/"))
                .filter(CustomHeaderFilter.addCustomHeaders())
                .build();
    }

    private static ServerResponse forwardWithPathVariable(ServerRequest request, String pathVariable, String baseURl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.http(baseURl + value).handle(request);
    }

}
