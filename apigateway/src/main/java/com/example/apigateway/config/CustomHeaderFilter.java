package com.example.apigateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.UUID;

@Slf4j
public class CustomHeaderFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    public static HandlerFilterFunction<ServerResponse, ServerResponse> addCustomHeaders() {
        return (request, next) -> {

            String correlationId = request.headers().firstHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            String userId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                userId = jwtAuth.getToken().getSubject();
            }

            log.debug("Routing request {} with {}: {}, {}: {}",
                    request.path(), USER_ID_HEADER, userId, CORRELATION_ID_HEADER, correlationId);

            String finalCorrelationId = correlationId;
            String finalUserId = userId;
            ServerRequest modifiedRequest = ServerRequest.from(request)
                    .headers(headers -> {
                        // El identity header siempre lo decide el gateway a partir del JWT
                        // validado: se descarta cualquier valor enviado por el cliente.
                        headers.remove(USER_ID_HEADER);
                        if (finalUserId != null) {
                            headers.set(USER_ID_HEADER, finalUserId);
                        }
                        headers.set(CORRELATION_ID_HEADER, finalCorrelationId);
                    })
                    .build();

            ServerResponse response = next.handle(modifiedRequest);

            response.headers().set(CORRELATION_ID_HEADER, correlationId);

            return response;
        };
    }
}
