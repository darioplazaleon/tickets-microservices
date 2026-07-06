package com.example.eventservice.service.events;

import com.example.eventservice.repository.TicketTypeRepository;
import com.example.shared.events.PaymentSucceededEvent;
import com.example.shared.records.TicketInfo;
import jakarta.transaction.Transactional;
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

    // Transaccional para que un retry por choque de @Version re-ejecute el lote
    // completo desde cero en vez de re-aplicar tickets ya guardados.
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50))
    @Transactional
    public void processPaymentSuccess(PaymentSucceededEvent event) {
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
    }
}
