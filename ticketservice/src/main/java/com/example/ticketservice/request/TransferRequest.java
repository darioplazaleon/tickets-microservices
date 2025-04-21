package com.example.ticketservice.request;

import java.util.UUID;

public record TransferRequest(
        UUID newOwnerId
) {}
