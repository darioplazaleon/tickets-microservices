package com.example.notificationservice.client;

import com.example.notificationservice.response.CustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.customer-service-url}")
    private String customerServiceUrl;

    public CustomerResponse getCustomer(UUID customerId) {
        String url = customerServiceUrl + "/" + customerId;

        log.info("[Notification Service] Fetching customer details from: {}, for customer: {}", url, customerId);

        CustomerResponse customerResponse = restTemplate.getForObject(url, CustomerResponse.class);

        if (customerResponse == null) {
            log.error("Customer not found for ID: {}", customerId);
            throw new RuntimeException("Customer not found");
        }

        return customerResponse;
    }
}
