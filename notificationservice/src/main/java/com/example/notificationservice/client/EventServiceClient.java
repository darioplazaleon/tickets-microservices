package com.example.notificationservice.client;

import com.example.shared.data.EventSimpleData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceClient {

    @Value("${services.event-service.url}")
    private String eventServiceUrl;

    private final RestTemplate restTemplate;

    @Cacheable(value = "eventDetails", key = "#eventId")
    public EventSimpleData getEventDetails(UUID eventId) {
        String url = eventServiceUrl + "/notification/" + eventId;
        return restTemplate.getForObject(url, EventSimpleData.class);
    }
}
