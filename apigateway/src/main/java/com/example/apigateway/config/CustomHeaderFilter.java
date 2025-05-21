package com.example.apigateway.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.UUID;

@Component
public class CustomHeaderFilter {
    public static HandlerFilterFunction<ServerResponse, ServerResponse> addCustomHeaders() {
        return (request, next) -> {

            String correlationId = request.headers().firstHeader("X-Correlation-Id");
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            String userId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                userId = jwtAuth.getToken().getSubject();
            }

            System.out.println("CustomHeaderFilter: X-User-Id: " + userId + ", X-Correlation-Id: " + correlationId);

            ServerRequest modifiedRequest = ServerRequest.from(request)
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-Correlation-Id", correlationId)
                    .build();

            ServerResponse response = next.handle(modifiedRequest);

            response.headers().add("X-User-Id", userId != null ? userId : "");
            response.headers().add("X-Correlation-Id", correlationId);

            return response;
        };
    }
}