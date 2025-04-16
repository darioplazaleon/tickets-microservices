package com.example.notificationservice.client;

import com.example.notificationservice.response.EventDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceClient {

    @Value("${services.event-service-url}")
    private String eventServiceUrl;

    private final RestTemplate restTemplate;

    public EventDetailsResponse getEventDetails(UUID eventId) {
        String url = eventServiceUrl + "/api/v1/events/" + eventId;

        return restTemplate.getForObject(url, EventDetailsResponse.class);
    }
}
