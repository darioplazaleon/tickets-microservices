package com.example.ticketservice.repository;

import com.example.ticketservice.entity.TicketOwnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketOwnershipRepository extends JpaRepository<TicketOwnership, UUID> {
    List<TicketOwnership> findByCurrentOwnerId(UUID ownerId);
    Optional<TicketOwnership> findByIdAndCurrentOwnerId(UUID id, UUID ownerId);

    List<TicketOwnership> findByOrderId(UUID orderId);
}
