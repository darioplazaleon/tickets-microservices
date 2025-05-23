package com.example.eventservice.controller;

import com.example.eventservice.request.ReserveTicketRequest;
import com.example.eventservice.response.ReserveTicketResponse;
import com.example.eventservice.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

  private final TicketTypeService ticketTypeService;

  @PostMapping("/reserve/{eventId}")
  public ResponseEntity<ReserveTicketResponse> reserveTicket(
          @PathVariable UUID eventId,
          @RequestBody ReserveTicketRequest request) {
    ReserveTicketResponse response = ticketTypeService.reserveTickets(eventId, request);
    return ResponseEntity.ok(response);
  }
}
