package com.example.ticketservice.messaging.publisher;

import com.example.shared.events.TicketQrReadyEvent;
import com.example.ticketservice.config.KafkaTracingInterceptor;
import com.example.ticketservice.config.RequestTracingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketTransferEventPublisher {

    private final KafkaTemplate<String, TicketQrReadyEvent> kafkaTemplate;

    public void publishTransferTicketEvent(TicketQrReadyEvent event) {
        log.info("[Ticket Service] Publishing TicketQrReadyEvent: {}", event);

        ProducerRecord<String, TicketQrReadyEvent> record =
                new ProducerRecord<>("tickets.qr.transfer", event);

        String correlationId = MDC.get(RequestTracingFilter.CORRELATION_ID_MDC_KEY);
        if (correlationId != null) {
            record.headers().add(KafkaTracingInterceptor.CORRELATION_ID_KAFKA_HEADER,
                    correlationId.getBytes(StandardCharsets.UTF_8));
        }

        kafkaTemplate.send(record);
        log.info("[Ticket Service] TicketQrReadyEvent published:, ownerId={}", event.currentOwnerId());
    }
}
