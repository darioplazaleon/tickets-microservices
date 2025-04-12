package com.example.bookingservice.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false)
  private UUID customerId;

  @Column(nullable = false)
  private UUID eventId;

  @Column(nullable = false)
  private int quantity;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookingStatus status;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  private Instant updatedAt;

  @PrePersist
  public void prePersist() {
    if (this.id == null) this.id = UUID.randomUUID();
    if (this.createdAt == null) this.createdAt = Instant.now();
  }
}
