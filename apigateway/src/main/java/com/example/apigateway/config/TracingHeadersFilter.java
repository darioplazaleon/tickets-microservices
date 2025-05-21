package com.example.apigateway.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TracingHeadersFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = generateCorrelationId();
        }

        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isEmpty()) {
            userId = extractUserIdFromToken(request);
        }

        HttpServletRequest requestWrapper = new CustomHeadersWrapper(request, correlationId, userId);

        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        filterChain.doFilter(requestWrapper, response);

        System.out.println(response.getStatus());
        System.out.println(response.getHeader(CORRELATION_ID_HEADER));
        System.out.println(requestWrapper.getHeader(USER_ID_HEADER));
        System.out.println(requestWrapper);
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    private String extractUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                DecodedJWT decodedJWT = JWT.decode(token);
                return decodedJWT.getSubject();
            } catch (JWTDecodeException e) {
                // Handle exception
            }
        }

        return "anonymous";
    }

    private static class CustomHeadersWrapper extends HttpServletRequestWrapper {
        private final String correlationId;
        private final String userId;

        public CustomHeadersWrapper(HttpServletRequest request, String correlationId, String userId) {
            super(request);
            this.correlationId = correlationId;
            this.userId = userId;
        }

        @Override
        public String getHeader(String name) {
            if (CORRELATION_ID_HEADER.equalsIgnoreCase(name)) {
                return correlationId;
            } else if (USER_ID_HEADER.equalsIgnoreCase(name)) {
                return userId;
            }
            return super.getHeader(name);
        }
    }
}

