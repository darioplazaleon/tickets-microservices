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
import com.example.ticketservice.response.TicketResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketOwnershipService {

    private final TicketOwnershipRepository ticketOwnershipRepository;
    private final QrGenerator qrGenerator;
    private final TicketEventPublisher ticketEventPublisher;
    private final TicketTransferEventPublisher ticketTransferEventPublisher;

    public void processPayment(PaymentSucceededEvent event) {
        log.info("[Ticket Service] Processing payment for order {}", event.orderId());
        UUID orderId = event.orderId();
        UUID ownerId = event.userId();
        UUID eventId = event.eventId();

        for (TicketInfo info : event.tickets()) {
            for (int i = 0; i < info.quantity(); i++) {
                TicketOwnership ticket = TicketOwnership.builder()
                        .orderId(orderId)
                        .eventId(eventId)
                        .originalBuyerId(ownerId)
                        .currentOwnerId(ownerId)
                        .ticketType(info.ticketType())
                        .transferable(true)
                        .used(false)
                        .createdAt(Instant.now())
                        .build();

                ticketOwnershipRepository.save(ticket);
            }
        }

        String masterQr = generateMasterQr(orderId, ownerId);

        TicketMasterQrEvent masterEvent = new TicketMasterQrEvent(
                orderId,
                eventId,
                ownerId,
                masterQr,
                Instant.now()
        );

        ticketEventPublisher.publishTicketQrReadyEvent(masterEvent);
        log.info("[Ticket Service] Master QR generated and published for order {}.", orderId);
    }

    public String generateQr(UUID ticketId, UUID requesterId) {
        TicketOwnership ticket = ticketOwnershipRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (!ticket.getCurrentOwnerId().equals(requesterId)) {
            throw new IllegalArgumentException("You are not the current owner of this ticket");
        }

        if (ticket.isUsed()) {
            throw new IllegalArgumentException("Ticket has already been used");
        }

        log.info("[Ticket Service] Generating QR for ticket {} for user {}", ticketId, requesterId);
        return qrGenerator.generateForTicket(ticket.getId(), ticket.getCurrentOwnerId(), ticket.getEventId());
    }

    public String generateMasterQr(UUID order, UUID requesterId) {
        List<TicketOwnership> tickets = ticketOwnershipRepository.findByOrderId(order);

        if (tickets.isEmpty()) {
            throw new IllegalArgumentException("No tickets found for this order");
        }

        boolean allSameOwner = tickets.stream()
                .allMatch(t -> t.getCurrentOwnerId().equals(requesterId));

        boolean anyUsed = tickets.stream()
                .anyMatch(TicketOwnership::isUsed);

        if (!allSameOwner) {
            throw new IllegalArgumentException("You are not the owner of all tickets in this order");
        }

        if (anyUsed) {
            throw new IllegalArgumentException("One or more tickets have already been used");
        }

        List<UUID> ticketIds = tickets.stream().map(TicketOwnership::getId).toList();
        QrMasterPayload payload = qrGenerator.generateQrPayload(order, requesterId, ticketIds);
        return qrGenerator.generateMasterQrBase64(payload);
    }

    @Cacheable(value = "userTickets",key = "#userId")
    public List<TicketResponse> getTicketsForUser(UUID userId) {
        return ticketOwnershipRepository.findByCurrentOwnerId(userId).stream()
                .map(ticket -> {
                    String qrJson = createQrPayload(ticket);
                    String base64qr = generateBase64Qr(qrJson);

                    return new TicketResponse(
                            ticket.getId(),
                            ticket.getEventId(),
                            ticket.getTicketType(),
                            ticket.isUsed(),
                            ticket.isTransferable(),
                            base64qr
                    );
                })
                .toList();
    }

    @CacheEvict(key = "#userId", value = "userTickets")
    public void evictUserTickets(UUID userId) {
        log.info("[Ticket Service] Evicting cache for user {}", userId);
    }

    public void transferTicket(UUID ticketId, UUID fromUserId, UUID toUserId) {
        TicketOwnership ticket = ticketOwnershipRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (!ticket.getCurrentOwnerId().equals(fromUserId)) {
            throw new IllegalArgumentException("You are not the current owner of this ticket");
        }

        if (ticket.isUsed()) {
            throw new IllegalArgumentException("Ticket has already been used");
        }

        if (!ticket.isTransferable()) {
            throw new IllegalArgumentException("Ticket is not transferable");
        }

        ticket.setCurrentOwnerId(toUserId);
        ticket.setTransferredAt(Instant.now());

        ticketOwnershipRepository.save(ticket);

        String qrTransfer = generateQr(ticketId, toUserId);

        TicketQrReadyEvent transferEvent = new TicketQrReadyEvent(
                ticket.getId(),
                ticket.getOrderId(),
                ticket.getEventId(),
                fromUserId,
                toUserId,
                qrTransfer,
                Instant.now()
        );

        ticketTransferEventPublisher.publishTransferTicketEvent(transferEvent);

        log.info("[Ticket Service] Ticket {} transferred from user {} to user {}",
                ticketId, fromUserId, toUserId);
    }

    private String createQrPayload(TicketOwnership ticket) {
        return String.format(
                "{\"ticketId\":\"%s\",\"ownerId\":\"%s\",\"eventId\":\"%s\",\"timestamp\":\"%s\"}",
                ticket.getId(),
                ticket.getCurrentOwnerId(),
                ticket.getEventId(),
                Instant.now().toString()
        );
    }

    private String generateBase64Qr(String payload) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generating QR", e);
        }
    }

    
}
