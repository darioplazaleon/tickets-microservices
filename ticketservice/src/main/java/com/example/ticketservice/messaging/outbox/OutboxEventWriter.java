package com.example.ticketservice.messaging.outbox;

import com.example.ticketservice.config.RequestTracingFilter;
import com.example.ticketservice.entity.OutboxEvent;
import com.example.ticketservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Persiste eventos salientes en la tabla outbox. Debe invocarse dentro de la
 * transacción del cambio de negocio: así el evento y el write commitean (o
 * rollbackean) juntos. La publicación real la hace {@link OutboxRelay}.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventWriter {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void append(String topic, String messageKey, Object event) {
        append(topic, messageKey, event, MDC.get(RequestTracingFilter.CORRELATION_ID_MDC_KEY));
    }

    public void append(String topic, String messageKey, Object event, String correlationId) {
        try {
            outboxEventRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .topic(topic)
                    .messageKey(messageKey)
                    .eventType(event.getClass().getName())
                    .payload(objectMapper.writeValueAsString(event))
                    .correlationId(correlationId)
                    .createdAt(Instant.now())
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize outbox event for topic " + topic, e);
        }
    }
}
