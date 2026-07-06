package com.example.eventservice.service.events;

import com.example.eventservice.entity.ProcessedEvent;
import com.example.eventservice.repository.ProcessedEventRepository;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.shared.events.PaymentSucceededEvent;
import com.example.shared.records.TicketInfo;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventPaymentService {

    private final TicketTypeRepository ticketTypeRepository;
    private final ProcessedEventRepository processedEventRepository;

    // Transaccional para que un retry por choque de @Version re-ejecute el lote
    // completo desde cero en vez de re-aplicar tickets ya guardados.
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50))
    @Transactional
    public void processPaymentSuccess(PaymentSucceededEvent event) {
        // Kafka entrega at-least-once y estos contadores no son idempotentes:
        // un duplicado volvería a sumar sold / restar reserved.
        String eventKey = "payment-success:" + event.orderId();
        if (processedEventRepository.existsById(eventKey)) {
            log.warn("[Event Service] Event {} already processed, skipping duplicate", eventKey);
            return;
        }

        for (TicketInfo ticket : event.tickets()) {
            ticketTypeRepository
                    .findByEventIdAndNameIgnoreCase(event.eventId(), ticket.ticketType())
                    .ifPresentOrElse(ticketType -> {
                        ticketType.setReserved(ticketType.getReserved() - ticket.quantity());
                        ticketType.setSold(ticketType.getSold() + ticket.quantity());
                        ticketTypeRepository.save(ticketType);
                        log.info("[Event Service] TicketType '{}' updated: reserved={}, sold={}",
                                ticketType.getName(),
                                ticketType.getReserved(),
                                ticketType.getSold());
                    }, () -> {
                        log.warn("[Event Service] TicketType '{}' not found for eventId: {}. Ignoring ticket info.",
                                ticket.ticketType(),
                                event.eventId());
                    });
        }

        processedEventRepository.save(
                ProcessedEvent.builder().eventKey(eventKey).processedAt(Instant.now()).build());
    }
}
