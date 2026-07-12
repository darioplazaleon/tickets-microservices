package com.example.ticketservice.messaging.publisher;

import com.example.shared.events.TicketQrReadyEvent;
import com.example.shared.infra.outbox.OutboxEventWriter;
import com.example.shared.messaging.Topics;
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
        outboxEventWriter.append(Topics.QR_TRANSFER, null, event);
    }
}
