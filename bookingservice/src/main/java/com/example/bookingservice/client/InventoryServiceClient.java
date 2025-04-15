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
public class InventoryServiceClient {

    @Value("${event.service.url}")
    private String eventUri;

    public EventResponse getEvent(UUID eventId) {
         RestTemplate restTemplate = new RestTemplate();
         return restTemplate.getForObject(eventUri + "/events/" + eventId, EventResponse.class);
    }

    public BigDecimal reserveTicket(UUID eventId, String ticketType, int quantity) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/api/v1/ticket-types/reserve/" + eventId;

        ReserveTicketRequest reserveTicketRequest = new ReserveTicketRequest(ticketType, quantity);

        ResponseEntity<ReserveTicketResponse> response = restTemplate
                .postForEntity(url, reserveTicketRequest, ReserveTicketResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Failed to reserve ticket");
        }

        System.out.println("✅ Reserva exitosa: " + response.getBody());

        return response.getBody().unitPrice();
    }
}
