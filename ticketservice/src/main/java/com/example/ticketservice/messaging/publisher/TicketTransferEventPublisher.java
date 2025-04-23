package com.example.ticketservice.messaging.publisher;

import com.example.shared.events.TicketQrReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketTransferEventPublisher {

    private final KafkaTemplate<String, TicketQrReadyEvent> kafkaTemplate;

    public void publishTransferTicketEvent(TicketQrReadyEvent event) {
        log.info("[Ticket Service] Publishing TicketQrReadyEvent: {}", event);
        kafkaTemplate.send("tickets.qr.transfer", event);
        log.info("[Ticket Service] TicketQrReadyEvent published:, ownerId={}", event.currentOwnerId());
    }
}
