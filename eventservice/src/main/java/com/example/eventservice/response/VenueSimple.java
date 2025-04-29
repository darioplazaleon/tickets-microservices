package com.example.eventservice.response;

import java.util.UUID;

public record VenueSimple(
        UUID id,
        String name
) {}
