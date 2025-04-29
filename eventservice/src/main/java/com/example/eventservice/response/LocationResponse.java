package com.example.eventservice.response;

import com.example.eventservice.entity.Venue;
import java.util.UUID;

public record LocationResponse(
    UUID venueId, String venueName, String address, String city, int totalCapacity) {
  public LocationResponse(Venue venue) {
    this(
        venue.getId(),
        venue.getName(),
        venue.getAddress(),
        venue.getCity(),
        venue.getTotalCapacity());
  }
}
