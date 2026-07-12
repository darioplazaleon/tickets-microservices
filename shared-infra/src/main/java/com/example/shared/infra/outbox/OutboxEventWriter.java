package com.example.shared.infra.outbox;

import com.example.shared.messaging.Tracing;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 * Persiste eventos salientes en la tabla outbox_events. Debe invocarse dentro
 * de la transacción del cambio de negocio: así el evento y el write commitean
 * (o rollbackean) juntos. La publicación real la hace {@link OutboxRelay}.
 *
 * <p>Usa JDBC plano para no exigir JPA en los servicios consumidores; participa
 * igual en la transacción de JPA porque comparte el DataSource.</p>
 */
@RequiredArgsConstructor
public class OutboxEventWriter {

    private static final String INSERT = """
            INSERT INTO outbox_events (id, topic, message_key, event_type, payload, correlation_id, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void append(String topic, String messageKey, Object event) {
        append(topic, messageKey, event, MDC.get(Tracing.CORRELATION_ID_MDC_KEY));
    }

    public void append(String topic, String messageKey, Object event, String correlationId) {
        try {
            jdbcTemplate.update(INSERT,
                    UUID.randomUUID(),
                    topic,
                    messageKey,
                    event.getClass().getName(),
                    objectMapper.writeValueAsString(event),
                    correlationId,
                    Timestamp.from(Instant.now()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize outbox event for topic " + topic, e);
        }
    }
}
