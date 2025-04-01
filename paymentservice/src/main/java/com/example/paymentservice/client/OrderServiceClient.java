package com.example.paymentservice.client;

import com.example.paymentservice.response.OrderResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderServiceClient {

    @Value("${order.service.url}")
    private String orderServiceUrl;

    public OrderResponse getOrder(Long orderId) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(orderServiceUrl + "/" + orderId, OrderResponse.class);
    }

    public ResponseEntity<Void> updateOrderStatus(Long orderId, String status) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(orderServiceUrl + "/" + orderId + "/update/" + status, null);
        return ResponseEntity.ok().build();
    }
}
