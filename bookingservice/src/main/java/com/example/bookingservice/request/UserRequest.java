package com.example.bookingservice.request;

import java.time.LocalDate;

public record UserRequest(
        String username,
        String email,
        String password,
        String fullName,
        String phoneNumber,
        String country,
        LocalDate birthday
) {}
