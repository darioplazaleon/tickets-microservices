package com.example.eventservice.service.events;

import com.example.eventservice.repository.TicketTypeRepository;
import com.example.shared.events.PaymentSucceededEvent;
import com.example.shared.records.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventPaymentService {

    private final TicketTypeRepository ticketTypeRepository;

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
