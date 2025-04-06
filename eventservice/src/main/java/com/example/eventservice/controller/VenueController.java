package com.example.eventservice.controller;

import com.example.eventservice.request.LocationAddRequest;
import com.example.eventservice.response.LocationResponse;
import com.example.eventservice.service.VenueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

  private final VenueService venueService;

  @GetMapping("/{venueId}")
  public ResponseEntity<LocationResponse> getVenueById(@PathVariable Long venueId) {
    try {
      LocationResponse locationResponse = venueService.getVenueById(venueId);
      return ResponseEntity.ok(locationResponse);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/all")
  public Page<LocationResponse> getAllVenues(Pageable pageable) {
    return venueService.getAllVenues(pageable);
  }

  @PostMapping("/add")
  public ResponseEntity<LocationResponse> createVenue(@RequestBody LocationAddRequest newLocation) {
    LocationResponse locationResponse = venueService.createVenue(newLocation);
    return ResponseEntity.status(HttpStatus.CREATED).body(locationResponse);
  }

  @PutMapping("/{venueId}/update")
  public ResponseEntity<LocationResponse> updateVenue(
      @PathVariable Long venueId, @RequestBody LocationAddRequest newLocation) {
    try {
      LocationResponse locationResponse = venueService.updateVenue(venueId, newLocation);
      return ResponseEntity.ok(locationResponse);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{venueId}/delete")
  public ResponseEntity<Void> deleteVenue(@PathVariable Long venueId) {
    try {
      venueService.deleteVenue(venueId);
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
