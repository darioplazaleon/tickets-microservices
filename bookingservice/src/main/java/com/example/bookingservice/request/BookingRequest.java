package com.example.bookingservice.request;


import java.util.List;
import java.util.UUID;

public record BookingRequest(UUID eventId, List<TicketRequest> tickets) {}
