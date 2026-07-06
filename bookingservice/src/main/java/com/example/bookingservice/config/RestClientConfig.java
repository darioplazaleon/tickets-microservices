package com.example.bookingservice.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(tracingHeadersInterceptor());
        return restTemplate;
    }

    private ClientHttpRequestInterceptor tracingHeadersInterceptor() {
        return (request, body, execution) -> {
            String correlationId = MDC.get(RequestTracingFilter.CORRELATION_ID_MDC_KEY);
            if (correlationId != null) {
                request.getHeaders().set(RequestTracingFilter.CORRELATION_ID_HEADER, correlationId);
            }
            String userId = MDC.get(RequestTracingFilter.USER_ID_MDC_KEY);
            if (userId != null) {
                request.getHeaders().set(RequestTracingFilter.USER_ID_HEADER, userId);
            }
            return execution.execute(request, body);
        };
    }
}
