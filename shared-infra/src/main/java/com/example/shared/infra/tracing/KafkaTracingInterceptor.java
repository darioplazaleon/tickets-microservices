package com.example.shared.infra.tracing;

import com.example.shared.messaging.Tracing;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;

public class KafkaTracingInterceptor implements RecordInterceptor<Object, Object> {

    @Override
    @NonNull
    public ConsumerRecord<Object, Object> intercept(@NonNull ConsumerRecord<Object, Object> record,
                                                    @NonNull Consumer<Object, Object> consumer) {
        Header header = record.headers().lastHeader(Tracing.CORRELATION_ID_KAFKA_HEADER);
        if (header != null) {
            MDC.put(Tracing.CORRELATION_ID_MDC_KEY,
                    new String(header.value(), StandardCharsets.UTF_8));
        }
        return record;
    }

    @Override
    public void afterRecord(@NonNull ConsumerRecord<Object, Object> record,
                            @NonNull Consumer<Object, Object> consumer) {
        MDC.remove(Tracing.CORRELATION_ID_MDC_KEY);
    }
}
