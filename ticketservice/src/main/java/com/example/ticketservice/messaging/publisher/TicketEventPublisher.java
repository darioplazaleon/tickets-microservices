package com.example.ticketservice.messaging.publisher;

import com.example.shared.events.TicketMasterQrEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {

    private final KafkaTemplate<String, TicketMasterQrEvent> kafkaTemplate;

    public void publishTicketQrReadyEvent(TicketMasterQrEvent event) {
        log.info("[Ticket Service] Publishing TicketQrReadyEvent: {}", event);
        kafkaTemplate.send("tickets.qr.ready", event.eventId().toString(), event);
        log.info("[Ticket Service] TicketQrReadyEvent published:, ownerId={}", event.ownerId());
    }
}
