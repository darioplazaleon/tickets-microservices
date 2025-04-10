package com.example.eventservice.repository;

import com.example.eventservice.entity.Event;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
  Optional<Event> findByName(String eventName);
}
