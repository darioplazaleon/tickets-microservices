package com.example.ticketservice.controller;

import com.example.ticketservice.request.TransferRequest;
import com.example.ticketservice.response.TicketResponse;
import com.example.ticketservice.service.TicketOwnershipService;
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

    @GetMapping("/mine")
    public ResponseEntity<List<TicketResponse>> getMyTickets(@RequestHeader("X-User-Id")UUID userId) {
        List<TicketResponse> tickets = ticketOwnershipService.getTicketsForUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/transfer/{ticketId}")
    public ResponseEntity<Void> transferTicket(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID ticketId,
            @RequestBody TransferRequest transferRequest
    ) {
        ticketOwnershipService.transferTicket(userId, ticketId, transferRequest.newOwnerId());
        return ResponseEntity.ok().build();
    }

}
