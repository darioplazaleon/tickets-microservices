package com.example.bookingservice.controller;

import com.example.bookingservice.request.UserRequest;
import com.example.bookingservice.response.CustomerResponse;
import com.example.bookingservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> registerCustomer(@RequestBody UserRequest userRequest) {
        var customerResponse = customerService.registerCustomer(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerResponse);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable UUID customerId) {
        var customerResponse = customerService.getById(customerId);
        return ResponseEntity.ok(customerResponse);
    }
}
