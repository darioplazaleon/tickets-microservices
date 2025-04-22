package com.example.ticketservice.response;

public record TicketValidationResponse(String status, String message) {

    public static TicketValidationResponse accepted() {
        return new TicketValidationResponse("accepted", "Ticket is valid and accepted.");
    }

    public static TicketValidationResponse rejected(String reason) {
        return new TicketValidationResponse("rejected", reason);
    }
}
