package com.example.paymentservice.client;

import com.example.paymentservice.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceClient {

    @Value("${order.service.url}")
    private String orderServiceUrl;

    private final RestTemplate restTemplate;

    public OrderResponse getOrder(UUID orderId) {

        HttpEntity<Void> entity = new HttpEntity<>(null);

        return restTemplate.exchange(
                orderServiceUrl + "/order/" + orderId,
                HttpMethod.GET,
                entity,
                OrderResponse.class
        ).getBody();
    }
}
