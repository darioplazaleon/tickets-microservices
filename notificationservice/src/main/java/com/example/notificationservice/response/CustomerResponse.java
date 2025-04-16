package com.example.notificationservice.response;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String username,
        String email,
        String fullName
) {
}
