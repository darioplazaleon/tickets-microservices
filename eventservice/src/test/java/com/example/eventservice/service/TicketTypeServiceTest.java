package com.example.eventservice.service;

import com.example.eventservice.entity.TicketType;
import com.example.eventservice.exception.InsufficientCapacityException;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.eventservice.request.ReserveTicketRequest;
import com.example.eventservice.response.ReserveTicketResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private TicketTypeService ticketTypeService;

    private final UUID eventId = UUID.randomUUID();

    private TicketType ticketType(int capacity, int reserved, int sold) {
        return TicketType.builder()
                .id(UUID.randomUUID())
                .name("VIP")
                .capacity(capacity)
                .reserved(reserved)
                .sold(sold)
                .price(BigDecimal.TEN)
                .build();
    }

    @Test
    void reserveTicketsIncrementsReservedAndReturnsResponse() {
        TicketType type = ticketType(10, 2, 3);
        when(ticketTypeRepository.findByEventIdAndNameIgnoreCase(eventId, "VIP"))
                .thenReturn(Optional.of(type));

        ReserveTicketResponse response =
                ticketTypeService.reserveTickets(eventId, new ReserveTicketRequest("VIP", 4));

        assertThat(type.getReserved()).isEqualTo(6);
        assertThat(response.reservedQuantity()).isEqualTo(4);
        assertThat(response.ticketType()).isEqualTo("VIP");
        assertThat(response.unitPrice()).isEqualByComparingTo(BigDecimal.TEN);
        verify(ticketTypeRepository).save(type);
    }

    @Test
    void reserveTicketsFailsWhenCapacityIsInsufficient() {
        // capacidad 10, 5 reservadas + 3 vendidas => solo quedan 2 disponibles
        TicketType type = ticketType(10, 5, 3);
        when(ticketTypeRepository.findByEventIdAndNameIgnoreCase(eventId, "VIP"))
                .thenReturn(Optional.of(type));

        assertThatThrownBy(() ->
                ticketTypeService.reserveTickets(eventId, new ReserveTicketRequest("VIP", 3)))
                .isInstanceOf(InsufficientCapacityException.class);

        assertThat(type.getReserved()).isEqualTo(5);
        verify(ticketTypeRepository, never()).save(any());
    }

    @Test
    void reserveTicketsRejectsNonPositiveQuantity() {
        assertThatThrownBy(() ->
                ticketTypeService.reserveTickets(eventId, new ReserveTicketRequest("VIP", 0)))
                .isInstanceOf(IllegalArgumentException.class);

        verify(ticketTypeRepository, never()).save(any());
    }

    @Test
    void reserveTicketsFailsWhenTicketTypeDoesNotExist() {
        when(ticketTypeRepository.findByEventIdAndNameIgnoreCase(eventId, "GOLD"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                ticketTypeService.reserveTickets(eventId, new ReserveTicketRequest("GOLD", 1)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
