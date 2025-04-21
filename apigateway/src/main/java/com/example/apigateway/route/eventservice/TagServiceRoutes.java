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
public class TagServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> tagRoutes() {
        return GatewayRouterFunctions.route("tag-service")
                .route(RequestPredicates.GET("/api/v1/tags/{tagId}"),
                        request -> forwardWithPathVariable(request, "tagId", "http://localhost:8080/api/v1/tags/"))
                .route(RequestPredicates.GET("/api/v1/tags/all"),
                        HandlerFunctions.http("http://localhost:8080/api/v1/tags/all"))
                .route(RequestPredicates.POST("/api/v1/tags/create"),
                        HandlerFunctions.http("http://localhost:8080/api/v1/tags/create"))
                .route(RequestPredicates.PUT("/api/v1/tags/update/{tagId}"),
                        request -> forwardWithPathVariable(request, "tagId", "http://localhost:8080/api/v1/tags/update/"))
                .route(RequestPredicates.DELETE("/api/v1/tags/delete/{tagId}"),
                        request -> forwardWithPathVariable(request, "tagId", "http://localhost:8080/api/v1/tags/delete/"))
                .filter(CustomHeaderFilter.addCustomHeaders())
                .build();
    }

    private static ServerResponse forwardWithPathVariable(ServerRequest request, String pathVariable, String baseURl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.http(baseURl + value).handle(request);
    }
}
