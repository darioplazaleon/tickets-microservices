package com.example.paymentservice.client;

import com.example.paymentservice.response.OrderResponse;
import com.stripe.model.climate.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceClient {

    @Value("${order.service.url}")
    private String orderServiceUrl;

    private final RestTemplate restTemplate;

    public OrderResponse getOrder(UUID orderId) {

        HttpEntity<Void> entity = new HttpEntity<>(null);
        String url = orderServiceUrl + "/order/" + orderId;
        log.info("Attempting to GET order from URL: {}", url);

        try {
            ResponseEntity<OrderResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    OrderResponse.class
            );
            return responseEntity.getBody();
        } catch (HttpClientErrorException e) {
            log.error("HttpClientErrorException while fetching order {} from {}: Status Code: {}, Response Body: {}",
                    orderId, url, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        } catch (Exception e) {
            log.error("Exception while fetching order {} from {}: {}", orderId, url, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch order", e);
        }
    }
}
