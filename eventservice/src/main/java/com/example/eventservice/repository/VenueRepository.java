package com.example.eventservice.repository;

import com.example.eventservice.entity.Venue;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {
  Optional<Venue> findByName(String name);
}
