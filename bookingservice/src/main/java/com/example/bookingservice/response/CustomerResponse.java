package com.example.bookingservice.response;

import com.example.bookingservice.entity.Customer;

public record CustomerResponse(
        Long id,
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
