package com.example.eventservice.response;

import com.example.eventservice.entity.Venue;

public record LocationResponse(Long venueId, String venueName, int totalCapacity) {
  public LocationResponse(Venue venue) {
    this(venue.getId(), venue.getName(), venue.getTotalCapacity());
  }
}
