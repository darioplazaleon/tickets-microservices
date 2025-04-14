package com.example.eventservice.service;

import com.example.eventservice.entity.Event;
import com.example.eventservice.entity.TicketType;
import com.example.eventservice.repository.TicketTypeRepository;
import com.example.eventservice.request.TicketTypeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;

    public TicketType saveTicketType(TicketTypeRequest ticketTypeRequest, Event event) {

        return TicketType.builder()
                .name(ticketTypeRequest.name())
                .capacity(ticketTypeRequest.capacity())
                .price(ticketTypeRequest.price())
                .event(event)
                .build();
    }
}
