package com.example.ticketservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_ownerships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketOwnership {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID eventId;

    @Column(nullable = false)
    private UUID originalBuyerId;

    @Column(nullable = false)
    private UUID currentOwnerId;

    @Column(nullable = false)
    private String ticketType;

    private boolean used;
    private boolean transferable;

    private Instant transferredAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) this.createdAt = Instant.now();
        if (id == null) this.id = UUID.randomUUID();
    }
}
