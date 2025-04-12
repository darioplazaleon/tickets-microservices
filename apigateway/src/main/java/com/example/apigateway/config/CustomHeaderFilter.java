package com.example.apigateway.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.UUID;

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

            // Modificar la ServerRequest para agregar headers
            ServerRequest modifiedRequest = ServerRequest.from(request)
                    .header("X-User-Id", userId != null ? userId : "")
                    .header("X-Correlation-Id", correlationId)
                    .build();

            // Llamar al siguiente handler con la request modificada
            ServerResponse response = next.handle(modifiedRequest);

            // Opcional: agregar headers en la respuesta si es necesario
            response.headers().add("X-User-Id", userId != null ? userId : "");
            response.headers().add("X-Correlation-Id", correlationId);

            return response;
        };
    }
}