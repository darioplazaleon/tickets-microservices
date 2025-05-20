package com.example.bookingservice.client;

import com.example.bookingservice.request.ReserveTicketRequest;
import com.example.bookingservice.response.EventResponse;
import com.example.bookingservice.response.ReserveTicketResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class EventServiceClient {

    @Value("${ticket.service.url}")
    private String ticketUri;

    public BigDecimal reserveTicket(UUID eventId, String ticketType, int quantity) {
        RestTemplate restTemplate = new RestTemplate();
        String url = ticketUri + "/reserve/" + eventId;

        ReserveTicketRequest reserveTicketRequest = new ReserveTicketRequest(ticketType, quantity);

        ResponseEntity<ReserveTicketResponse> response = restTemplate
                .postForEntity(url, reserveTicketRequest, ReserveTicketResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to reserve ticket");
        }

        return response.getBody().unitPrice();
    }
}
