package com.example.notificationservice.client;

import com.example.notificationservice.response.OrderSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceClient {
    @Value("${services.order-service.url}")
    private String orderServiceUrl;

    private final RestTemplate restTemplate;

    @Cacheable(value = "orderSummaries", key = "#orderId")
    public OrderSummary getOrderDetails(UUID orderId) {
        String url = orderServiceUrl + "/" + orderId + "/summary";

        return restTemplate.getForObject(url, OrderSummary.class);
    }
}
