package com.example.ticketservice.messaging.publisher;

import com.example.shared.events.TicketQrReadyEvent;
import com.example.ticketservice.messaging.outbox.OutboxEventWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TicketTransferEventPublisher {

    private final OutboxEventWriter outboxEventWriter;

    public void publishTransferTicketEvent(TicketQrReadyEvent event) {
        log.info("[Ticket Service] Appending TicketQrReadyEvent to outbox: {}", event);
        outboxEventWriter.append("tickets.qr.transfer", null, event);
    }
}
