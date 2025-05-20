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

  @Column(nullable = false)
  private String name;

  @ManyToOne
  @JoinColumn(name = "venue_id")
  private Venue venue;

  @Column(nullable = false)
  private LocalDateTime startDate;

  @Column(nullable = false)
  private LocalDateTime endDate;

  @Enumerated(EnumType.STRING)
  private EventStatus status;

  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "created_by_user_id", nullable = false)
  private UUID createdByUserId;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "event_tags",
      joinColumns = @JoinColumn(name = "event_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  private List<Tag> tags = new ArrayList<>();

  @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TicketType> ticketTypes = new ArrayList<>();

  @PrePersist
  public void prePersist() {
    if (this.id == null) this.id = UUID.randomUUID();
    if (this.createdAt == null) this.createdAt = Instant.now();
  }

  public int getTotalCapacity() {
    return ticketTypes.stream().mapToInt(TicketType::getCapacity).sum();
  }

  public int getRemainingCapacity() {
    return ticketTypes.stream()
        .mapToInt(t -> t.getCapacity() - t.getReserved() - t.getSold())
        .sum();
  }
}
