package com.example.ticketservice.controller;

import com.example.shared.data.TicketData;
import com.example.ticketservice.request.TicketValidationRequest;
import com.example.ticketservice.request.TransferRequest;
import com.example.ticketservice.response.TicketResponse;
import com.example.ticketservice.response.TicketValidationResponse;
import com.example.ticketservice.service.TicketOwnershipService;
import com.example.ticketservice.service.TicketService;
import com.example.ticketservice.service.TicketValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketOwnershipController {

    private final TicketOwnershipService ticketOwnershipService;
    private final TicketValidationService ticketValidationService;
    private final TicketService ticketService;

    @GetMapping("/mine")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            @RequestHeader("X-User-Id") UUID userId) {
        List<TicketResponse> tickets = ticketOwnershipService.getTicketsForUser(userId);
        return ResponseEntity.ok(tickets);
    }


    @PostMapping("/transfer/{ticketId}")
    public ResponseEntity<Void> transferTicket(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID ticketId,
            @RequestBody TransferRequest transferRequest) {
        ticketOwnershipService.transferTicket(ticketId, userId, transferRequest.newOwnerId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<TicketValidationResponse> validateQr(
            @RequestBody TicketValidationRequest request) {

        String qrInput = request.qr();
        String json;

        try {
            json = new String(java.util.Base64.getDecoder().decode(qrInput));
        } catch (IllegalArgumentException e) {
            json = qrInput;
        }

        TicketValidationResponse response = ticketValidationService.validateQrPayload(json);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/data/{ticketId}")
    public ResponseEntity<TicketData> getTicketById(
            @PathVariable UUID ticketId) {
        TicketData ticket = ticketService.getTicketById(ticketId);
        return ResponseEntity.ok(ticket);
    }
}
