package com.example.bookingservice.client;

import com.example.bookingservice.response.EventResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InventoryServiceClient {

    @Value("${event.service.url}")
    private String eventUri;

    public EventResponse getEvent(Long eventId) {
         RestTemplate restTemplate = new RestTemplate();
         return restTemplate.getForObject(eventUri + "/event/" + eventId, EventResponse.class);
    }
}
