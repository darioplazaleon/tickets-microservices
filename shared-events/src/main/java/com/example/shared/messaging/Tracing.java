package com.example.shared.messaging;

/** Nombres de headers HTTP/Kafka y claves MDC usados para propagar el correlation-id. */
public final class Tracing {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String CORRELATION_ID_KAFKA_HEADER = "correlation-id";
    public static final String USER_ID_KAFKA_HEADER = "user-id";

    private Tracing() {
    }
}
