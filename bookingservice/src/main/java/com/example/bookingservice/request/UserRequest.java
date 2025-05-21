package com.example.bookingservice.request;

import java.time.LocalDate;
import java.util.Set;

public record UserRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String phoneNumber,
        String country,
        LocalDate birthday,
        Set<String> roles
) {}
