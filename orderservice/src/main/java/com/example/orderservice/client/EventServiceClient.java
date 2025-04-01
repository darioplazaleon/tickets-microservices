package com.example.orderservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EventServiceClient {
    @Value("${event.service.url}")
    private String eventServiceUrl;

    public ResponseEntity<Void> updateEventCapacity(Long eventId, Long ticketCount) {

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(eventServiceUrl + "/event/" + eventId + "/capacity/" + ticketCount, null);

        return ResponseEntity.ok().build();
    }
}
