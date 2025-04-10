package com.example.eventservice.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column private String name;

  @ManyToOne
  @JoinColumn(name = "venue_id")
  private Venue venue;

  @Column(nullable = false)
  private int leftCapacity;

  @Column(nullable = false)
  private LocalDateTime startDate;

  @Column(nullable = false)
  private LocalDateTime endDate;

  @Enumerated(EnumType.STRING)
  private EventStatus status;

  @Column(name = "ticket_price", nullable = false)
  private BigDecimal ticketPrice;

  @Column(name = "capacity",nullable = false)
  private int capacity;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
  
  @Column(name = "updated_at")
  private Instant updatedAt;

  @ManyToMany
  @JoinTable(
      name = "event_tags",
      joinColumns = @JoinColumn(name = "event_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags = new ArrayList<>();

  @PrePersist
  public void prePersist() {
    if (this.id == null) this.id = UUID.randomUUID();
    if (this.createdAt == null) this.createdAt = Instant.now();
    if (capacity == 0 && venue != null) this.capacity = venue.getTotalCapacity();
  }
}
