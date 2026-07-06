package com.example.notificationservice.config;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class KafkaTracingInterceptor implements RecordInterceptor<Object, Object> {

    public static final String CORRELATION_ID_KAFKA_HEADER = "correlation-id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    @NonNull
    public ConsumerRecord<Object, Object> intercept(@NonNull ConsumerRecord<Object, Object> record,
                                                    @NonNull Consumer<Object, Object> consumer) {
        Header header = record.headers().lastHeader(CORRELATION_ID_KAFKA_HEADER);
        if (header != null) {
            MDC.put(CORRELATION_ID_MDC_KEY,
                    new String(header.value(), StandardCharsets.UTF_8));
        }
        return record;
    }

    @Override
    public void afterRecord(@NonNull ConsumerRecord<Object, Object> record,
                            @NonNull Consumer<Object, Object> consumer) {
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }
}
