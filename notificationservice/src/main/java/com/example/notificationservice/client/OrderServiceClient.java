package com.example.notificationservice.client;

import com.example.notificationservice.response.EventDetailsResponse;
import com.example.notificationservice.response.OrderSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceClient {
    @Value("${services.order-service.url}")
    private String orderServiceUrl;

    private final RestTemplate restTemplate;

    public OrderSummary getOrderDetails(UUID eventId) {
        String url = orderServiceUrl + "/" + eventId + "/summary";

        return restTemplate.getForObject(url, OrderSummary.class);
    }
}
