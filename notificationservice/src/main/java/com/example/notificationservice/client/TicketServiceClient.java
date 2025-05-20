package com.example.notificationservice.client;

import com.example.shared.data.TicketData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.ticket-service.url}")
    private String ticketServiceUrl;

    @Cacheable(value = "ticketDetails", key = "#ticketId")
    public TicketData getTicketServiceUrl(UUID ticketId) {
        String url = ticketServiceUrl + "/data/" + ticketId;

        return restTemplate.getForObject(url, TicketData.class);
    }
}
