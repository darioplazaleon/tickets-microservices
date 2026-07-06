package com.example.eventservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "processed_events")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProcessedEvent {

    @Id
    @Column(name = "event_key", length = 120)
    private String eventKey;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}
