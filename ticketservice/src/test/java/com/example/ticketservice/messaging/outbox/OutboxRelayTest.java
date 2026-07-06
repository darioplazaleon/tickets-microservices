package com.example.ticketservice.messaging.outbox;

import com.example.shared.events.TicketMasterQrEvent;
import com.example.ticketservice.entity.OutboxEvent;
import com.example.ticketservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private OutboxRelay outboxRelay;

    @BeforeEach
    void setUp() {
        outboxRelay = new OutboxRelay(outboxEventRepository, kafkaTemplate, objectMapper);
    }

    private OutboxEvent pendingEvent(String correlationId) throws Exception {
        TicketMasterQrEvent event = new TicketMasterQrEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "qr", Instant.now());
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .topic("tickets.qr.ready")
                .messageKey("key-1")
                .eventType(TicketMasterQrEvent.class.getName())
                .payload(objectMapper.writeValueAsString(event))
                .correlationId(correlationId)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void publishesPendingEventsWithOriginalTypeAndMarksThemPublished() throws Exception {
        OutboxEvent pending = pendingEvent("cid-123");
        when(outboxEventRepository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(pending));
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

        assertThat(pending.getPublishedAt()).isNotNull();
        verify(outboxEventRepository).save(pending);
    }

    @Test
    @SuppressWarnings("unchecked")
    void stopsTheBatchOnFailureToPreserveOrdering() throws Exception {
        OutboxEvent first = pendingEvent(null);
        OutboxEvent second = pendingEvent(null);
        when(outboxEventRepository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc())
                .thenReturn(List.of(first, second));
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        outboxRelay.publishPending();

        // Solo se intentó el primero; ninguno quedó marcado como publicado.
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
        assertThat(first.getPublishedAt()).isNull();
        assertThat(second.getPublishedAt()).isNull();
    }
}
