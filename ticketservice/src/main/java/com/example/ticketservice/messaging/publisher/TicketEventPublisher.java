package com.example.ticketservice.messaging.publisher;

import com.example.shared.events.TicketMasterQrEvent;
import com.example.ticketservice.messaging.outbox.OutboxEventWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketEventPublisher {

    private final OutboxEventWriter outboxEventWriter;

    public void publishTicketQrReadyEvent(TicketMasterQrEvent event) {
        log.info("[Ticket Service] Appending TicketQrReadyEvent to outbox: {}", event);
        outboxEventWriter.append("tickets.qr.ready", event.eventId().toString(), event);
    }
}
