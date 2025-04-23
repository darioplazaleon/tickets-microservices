package com.example.ticketservice.service;

import com.example.shared.data.TicketData;
import com.example.ticketservice.entity.TicketOwnership;
import com.example.ticketservice.repository.TicketOwnershipRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketOwnershipRepository ticketOwnershipRepository;

    public TicketData getTicketById(UUID ticketId) {
        TicketOwnership ticket = ticketOwnershipRepository.findById(ticketId)
                .orElseThrow();

        return new TicketData(
                ticket.getId(),
                ticket.getEventId(),
                ticket.getTicketType()
        );
    }
}
