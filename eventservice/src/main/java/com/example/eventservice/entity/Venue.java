package com.example.eventservice.entity;

import jakarta.persistence.*;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private int totalCapacity;

  @Column(nullable = false)
  private String address;

  @Column(nullable = false)
  private String city;

  //    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL)
  //    private List<Event> events;
}
