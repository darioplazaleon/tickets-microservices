package com.example.eventservice.response;

import com.example.eventservice.entity.Event;

import java.util.List;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String event,
        CategoryResponse category,
        List<TagResponse> tags) {
    public EventResponse(Event event) {
        this(
                event.getId(),
                event.getName(),
                new CategoryResponse(event.getCategory().getId(), event.getCategory().getName()),
                event.getTags().stream()
                        .map(t -> new TagResponse(t.getId(), t.getName()))
                        .toList()
        );
    }
}
