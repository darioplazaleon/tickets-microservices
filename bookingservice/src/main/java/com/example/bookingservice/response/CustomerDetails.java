package com.example.bookingservice.response;

import com.example.bookingservice.entity.Customer;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerDetails(
    UUID id,
    String username,
    String email,
    String fullName,
    String phoneNumber,
    LocalDate birthday,
    String country) {
  public CustomerDetails(Customer customer) {
    this(
        customer.getId(),
        customer.getUsername(),
        customer.getEmail(),
        customer.getFullName(),
        customer.getPhoneNumber(),
        customer.getBirthday(),
        customer.getCountry());
  }
}
