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
public class CategoryServiceRoutes {

    @Bean
    public RouterFunction<ServerResponse> categoryRoutes() {
        return GatewayRouterFunctions.route("category-service")
                .route(RequestPredicates.GET("/api/v1/categories/{categoryId}"),
                        request -> forwardWithPathVariable(request, "categoryId", "http://localhost:8080/api/v1/categories/"))
                .route(RequestPredicates.GET("/api/v1/categories/all"),
                        HandlerFunctions.http("http://localhost:8080/api/v1/categories/all"))
                .route(RequestPredicates.POST("/api/v1/categories/create"),
                        HandlerFunctions.http("http://localhost:8080/api/v1/categories/create"))
                .route(RequestPredicates.PUT("/api/v1/categories/update/{categoryId}"),
                        request -> forwardWithPathVariable(request, "categoryId", "http://localhost:8080/api/v1/categories/update/"))
                .route(RequestPredicates.DELETE("/api/v1/categories/delete/{categoryId}"),
                        request -> forwardWithPathVariable(request, "categoryId", "http://localhost:8080/api/v1/categories/delete/"))
                .filter(CustomHeaderFilter.addCustomHeaders())
                .build();
    }

    private static ServerResponse forwardWithPathVariable(ServerRequest request, String pathVariable, String baseURl) throws Exception {
        String value = request.pathVariable(pathVariable);
        return HandlerFunctions.http(baseURl + value).handle(request);
    }

}
