package com.example.eventservice.service.events;

import com.example.eventservice.entity.ProcessedEvent;
import com.example.eventservice.entity.TicketType;
import com.example.eventservice.repository.ProcessedEventRepository;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.shared.events.PaymentSucceededEvent;
import com.example.shared.records.TicketInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventPaymentServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private EventPaymentService eventPaymentService;

    private final UUID eventId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    private PaymentSucceededEvent paymentEvent(int quantity) {
        return new PaymentSucceededEvent(
                orderId,
                UUID.randomUUID(),
                eventId,
                UUID.randomUUID(),
                List.of(new TicketInfo("VIP", quantity, BigDecimal.TEN)),
                BigDecimal.TEN,
                UUID.randomUUID(),
                Instant.now());
    }

    @Test
    void processPaymentMovesTicketsFromReservedToSold() {
        TicketType type = TicketType.builder()
                .id(UUID.randomUUID()).name("VIP").capacity(10).reserved(4).sold(1)
                .price(BigDecimal.TEN).build();
        when(processedEventRepository.existsById("payment-success:" + orderId)).thenReturn(false);
        when(ticketTypeRepository.findByEventIdAndNameIgnoreCase(eventId, "VIP"))
                .thenReturn(Optional.of(type));

        eventPaymentService.processPaymentSuccess(paymentEvent(3));

        assertThat(type.getReserved()).isEqualTo(1);
        assertThat(type.getSold()).isEqualTo(4);
        verify(ticketTypeRepository).save(type);

        ArgumentCaptor<ProcessedEvent> marker = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository).save(marker.capture());
        assertThat(marker.getValue().getEventKey()).isEqualTo("payment-success:" + orderId);
    }

    @Test
    void processPaymentSkipsDuplicateEvents() {
        when(processedEventRepository.existsById("payment-success:" + orderId)).thenReturn(true);

        eventPaymentService.processPaymentSuccess(paymentEvent(3));

        verifyNoInteractions(ticketTypeRepository);
        verify(processedEventRepository, never()).save(any());
    }
}
