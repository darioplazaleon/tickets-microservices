package com.example.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class EventServiceClient {
    @Value("${event.service.url}")
    private String eventServiceUrl;

    public ResponseEntity<Void> updateEventCapacity(UUID eventId, int ticketCount) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(eventServiceUrl + "/" + eventId + "/capacity/" + ticketCount, null);

        return ResponseEntity.ok().build();
    }
}
