package com.example.shared.infra.outbox;

import com.example.shared.events.TicketMasterQrEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private OutboxRelay outboxRelay;

    @BeforeEach
    void setUp() {
        outboxRelay = new OutboxRelay(jdbcTemplate, kafkaTemplate, objectMapper);
    }

    private OutboxRelay.PendingEvent pendingEvent(String correlationId) throws Exception {
        TicketMasterQrEvent event = new TicketMasterQrEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "qr", Instant.now());
        return new OutboxRelay.PendingEvent(
                UUID.randomUUID(),
                "tickets.qr.ready",
                "key-1",
                TicketMasterQrEvent.class.getName(),
                objectMapper.writeValueAsString(event),
                correlationId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishesPendingEventsWithOriginalTypeAndMarksThemPublished() throws Exception {
        OutboxRelay.PendingEvent pending = pendingEvent("cid-123");
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of(pending));
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        outboxRelay.publishPending();

        ArgumentCaptor<ProducerRecord<String, Object>> record = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(record.capture());
        assertThat(record.getValue().topic()).isEqualTo("tickets.qr.ready");
        assertThat(record.getValue().key()).isEqualTo("key-1");
        assertThat(record.getValue().value()).isInstanceOf(TicketMasterQrEvent.class);
        assertThat(record.getValue().headers().lastHeader("correlation-id").value())
                .isEqualTo("cid-123".getBytes(StandardCharsets.UTF_8));

        verify(jdbcTemplate).update(anyString(), any(Timestamp.class), eq(pending.id()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void stopsTheBatchOnFailureToPreserveOrdering() throws Exception {
        OutboxRelay.PendingEvent first = pendingEvent(null);
        OutboxRelay.PendingEvent second = pendingEvent(null);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(List.of(first, second));
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        outboxRelay.publishPending();

        // Solo se intentó el primero; ninguno quedó marcado como publicado.
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
        verify(jdbcTemplate, never()).update(anyString(), any(Timestamp.class), any(UUID.class));
    }
}
