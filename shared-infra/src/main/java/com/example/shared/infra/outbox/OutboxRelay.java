package com.example.shared.infra.outbox;

import com.example.shared.messaging.Tracing;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class OutboxRelay {

    private static final String SELECT_PENDING = """
            SELECT id, topic, message_key, event_type, payload, correlation_id
            FROM outbox_events
            WHERE published_at IS NULL
            ORDER BY created_at ASC
            LIMIT 100
            """;

    private static final String MARK_PUBLISHED =
            "UPDATE outbox_events SET published_at = ? WHERE id = ?";

    record PendingEvent(UUID id, String topic, String messageKey, String eventType,
                        String payload, String correlationId) {}

    static final RowMapper<PendingEvent> ROW_MAPPER = (rs, rowNum) -> new PendingEvent(
            rs.getObject("id", UUID.class),
            rs.getString("topic"),
            rs.getString("message_key"),
            rs.getString("event_type"),
            rs.getString("payload"),
            rs.getString("correlation_id"));

    private final JdbcTemplate jdbcTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 2_000)
    public void publishPending() {
        List<PendingEvent> pending = jdbcTemplate.query(SELECT_PENDING, ROW_MAPPER);

        for (PendingEvent outbox : pending) {
            try {
                // Se reconstruye el objeto tipado para que JsonSerializer agregue el
                // header __TypeId__ que los consumidores usan para deserializar.
                Object event = objectMapper.readValue(outbox.payload(), Class.forName(outbox.eventType()));

                ProducerRecord<String, Object> record =
                        new ProducerRecord<>(outbox.topic(), outbox.messageKey(), event);
                if (outbox.correlationId() != null) {
                    record.headers().add(Tracing.CORRELATION_ID_KAFKA_HEADER,
                            outbox.correlationId().getBytes(StandardCharsets.UTF_8));
                }

                kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);

                jdbcTemplate.update(MARK_PUBLISHED, Timestamp.from(Instant.now()), outbox.id());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                // Se corta el batch para no publicar fuera de orden; el próximo tick reintenta.
                log.error("Failed to publish outbox event {} to topic {}, will retry",
                        outbox.id(), outbox.topic(), e);
                return;
            }
        }
    }
}
