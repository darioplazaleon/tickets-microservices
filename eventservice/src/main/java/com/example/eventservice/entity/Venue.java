package com.example.eventservice.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "venues")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Venue {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private int totalCapacity;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String city;

  @PrePersist
  public void prePersist() {
    if (this.id == null) this.id = UUID.randomUUID();
  }
}
