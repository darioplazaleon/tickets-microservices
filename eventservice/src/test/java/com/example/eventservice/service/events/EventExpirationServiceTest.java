package com.example.eventservice.service.events;

import com.example.eventservice.entity.TicketType;
import com.example.eventservice.repository.ProcessedEventRepository;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.shared.events.OrderExpiredEvent;
import com.example.shared.records.TicketInfoSimple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class EventExpirationServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private EventExpirationService eventExpirationService;

    private final UUID eventId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    private OrderExpiredEvent expiredEvent(int quantity) {
        return new OrderExpiredEvent(
                orderId,
                UUID.randomUUID(),
                eventId,
                List.of(new TicketInfoSimple("VIP", quantity)),
                UUID.randomUUID(),
                Instant.now());
    }

    @Test
    void orderExpirationReleasesReservedTickets() {
        TicketType type = TicketType.builder()
                .id(UUID.randomUUID()).name("VIP").capacity(10).reserved(5).sold(2)
                .price(BigDecimal.TEN).build();
        when(processedEventRepository.existsById("order-expired:" + orderId)).thenReturn(false);
        when(ticketTypeRepository.findByEventIdAndNameIgnoreCase(eventId, "VIP"))
                .thenReturn(Optional.of(type));

        eventExpirationService.processOrderExpiration(expiredEvent(3));

        assertThat(type.getReserved()).isEqualTo(2);
        assertThat(type.getSold()).isEqualTo(2);
        verify(ticketTypeRepository).save(type);
    }

    @Test
    void reservedNeverGoesNegative() {
        TicketType type = TicketType.builder()
                .id(UUID.randomUUID()).name("VIP").capacity(10).reserved(1).sold(0)
                .price(BigDecimal.TEN).build();
        when(processedEventRepository.existsById(any(String.class))).thenReturn(false);
        when(ticketTypeRepository.findByEventIdAndNameIgnoreCase(eventId, "VIP"))
                .thenReturn(Optional.of(type));

        eventExpirationService.processOrderExpiration(expiredEvent(5));

        assertThat(type.getReserved()).isZero();
    }

    @Test
    void orderExpirationSkipsDuplicateEvents() {
        when(processedEventRepository.existsById("order-expired:" + orderId)).thenReturn(true);

        eventExpirationService.processOrderExpiration(expiredEvent(3));

        verifyNoInteractions(ticketTypeRepository);
        verify(processedEventRepository, never()).save(any());
    }
}
