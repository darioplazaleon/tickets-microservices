package com.example.eventservice.repository;

import com.example.eventservice.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {
    Optional<TicketType> findByEventIdAndNameIgnoreCase(UUID eventId, String name);
}
