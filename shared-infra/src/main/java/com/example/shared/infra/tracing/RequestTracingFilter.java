package com.example.shared.infra.tracing;

import com.example.shared.messaging.Tracing;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestTracingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = request.getHeader(Tracing.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(Tracing.CORRELATION_ID_MDC_KEY, correlationId);
        String userId = request.getHeader(Tracing.USER_ID_HEADER);
        if (userId != null && !userId.isEmpty()) {
            MDC.put(Tracing.USER_ID_MDC_KEY, userId);
        }

        response.setHeader(Tracing.CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(Tracing.CORRELATION_ID_MDC_KEY);
            MDC.remove(Tracing.USER_ID_MDC_KEY);
        }
    }
}
