package com.example.eventservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TicketType {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int reserved = 0;

    @Column(nullable = false)
    private int sold = 0;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @PrePersist
    public void prePersist() {
        if (this.id == null) this.id = UUID.randomUUID();
    }
}
