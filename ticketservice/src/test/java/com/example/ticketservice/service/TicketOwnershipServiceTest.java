package com.example.ticketservice.service;

import com.example.shared.events.PaymentSucceededEvent;
import com.example.shared.events.TicketMasterQrEvent;
import com.example.shared.events.TicketQrReadyEvent;
import com.example.shared.records.TicketInfo;
import com.example.ticketservice.entity.TicketOwnership;
import com.example.ticketservice.messaging.publisher.TicketEventPublisher;
import com.example.ticketservice.messaging.publisher.TicketTransferEventPublisher;
import com.example.ticketservice.repository.TicketOwnershipRepository;
import com.example.ticketservice.request.QrMasterPayload;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketOwnershipServiceTest {

    @Mock
    private TicketOwnershipRepository ticketOwnershipRepository;

    @Mock
    private QrGenerator qrGenerator;

    @Mock
    private TicketEventPublisher ticketEventPublisher;

    @Mock
    private TicketTransferEventPublisher ticketTransferEventPublisher;

    @InjectMocks
    private TicketOwnershipService ticketOwnershipService;

    private final UUID orderId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();

    private PaymentSucceededEvent paymentEvent(int quantity) {
        return new PaymentSucceededEvent(
                orderId,
                UUID.randomUUID(),
                eventId,
                ownerId,
                List.of(new TicketInfo("VIP", quantity, BigDecimal.TEN)),
                BigDecimal.TEN,
                UUID.randomUUID(),
                Instant.now());
    }

    private TicketOwnership ticket(UUID currentOwner, boolean used, boolean transferable) {
        return TicketOwnership.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .eventId(eventId)
                .originalBuyerId(ownerId)
                .currentOwnerId(currentOwner)
                .ticketType("VIP")
                .transferable(transferable)
                .used(used)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void processPaymentCreatesOneTicketPerUnitAndPublishesMasterQr() {
        when(ticketOwnershipRepository.existsByOrderId(orderId)).thenReturn(false);
        when(ticketOwnershipRepository.findByOrderId(orderId))
                .thenReturn(List.of(ticket(ownerId, false, true)));
        when(qrGenerator.generateQrPayload(eq(orderId), eq(ownerId), any()))
                .thenReturn(new QrMasterPayload(orderId, ownerId, List.of(), "ts", "sig"));
        when(qrGenerator.generateMasterQrBase64(any())).thenReturn("qr-base64");

        ticketOwnershipService.processPayment(paymentEvent(3));

        verify(ticketOwnershipRepository, times(3)).save(any(TicketOwnership.class));

        ArgumentCaptor<TicketMasterQrEvent> published = ArgumentCaptor.forClass(TicketMasterQrEvent.class);
        verify(ticketEventPublisher).publishTicketQrReadyEvent(published.capture());
        assertThat(published.getValue().orderId()).isEqualTo(orderId);
        assertThat(published.getValue().qrBase64()).isEqualTo("qr-base64");
    }

    @Test
    void processPaymentSkipsDuplicateEvents() {
        when(ticketOwnershipRepository.existsByOrderId(orderId)).thenReturn(true);

        ticketOwnershipService.processPayment(paymentEvent(3));

        verify(ticketOwnershipRepository, never()).save(any());
        verifyNoInteractions(ticketEventPublisher);
    }

    @Test
    void transferTicketChangesOwnerAndPublishesEvent() {
        TicketOwnership ticket = ticket(ownerId, false, true);
        UUID newOwner = UUID.randomUUID();
        when(ticketOwnershipRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(qrGenerator.generateForTicket(ticket.getId(), newOwner, eventId)).thenReturn("qr-transfer");

        ticketOwnershipService.transferTicket(ticket.getId(), ownerId, newOwner);

        assertThat(ticket.getCurrentOwnerId()).isEqualTo(newOwner);
        assertThat(ticket.getTransferredAt()).isNotNull();
        verify(ticketOwnershipRepository).save(ticket);

        ArgumentCaptor<TicketQrReadyEvent> published = ArgumentCaptor.forClass(TicketQrReadyEvent.class);
        verify(ticketTransferEventPublisher).publishTransferTicketEvent(published.capture());
        assertThat(published.getValue().currentOwnerId()).isEqualTo(newOwner);
    }

    @Test
    void transferTicketFailsWhenRequesterIsNotTheOwner() {
        TicketOwnership ticket = ticket(ownerId, false, true);
        when(ticketOwnershipRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                ticketOwnershipService.transferTicket(ticket.getId(), UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);

        verify(ticketOwnershipRepository, never()).save(any());
        verifyNoInteractions(ticketTransferEventPublisher);
    }

    @Test
    void transferTicketFailsWhenTicketWasUsed() {
        TicketOwnership ticket = ticket(ownerId, true, true);
        when(ticketOwnershipRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                ticketOwnershipService.transferTicket(ticket.getId(), ownerId, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void transferTicketFailsWhenTicketIsNotTransferable() {
        TicketOwnership ticket = ticket(ownerId, false, false);
        when(ticketOwnershipRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                ticketOwnershipService.transferTicket(ticket.getId(), ownerId, UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
