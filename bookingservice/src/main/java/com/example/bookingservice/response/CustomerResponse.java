package com.example.bookingservice.response;

import com.example.bookingservice.entity.Customer;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String username,
        String email,
        String fullName
) {
    public CustomerResponse(Customer customer) {
        this(
                customer.getId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getFullName()
        );
    }
}
