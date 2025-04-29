package com.example.eventservice.controller;

import com.example.eventservice.request.LocationAddRequest;
import com.example.eventservice.response.LocationResponse;
import com.example.eventservice.response.VenueSimple;
import com.example.eventservice.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
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
    public ResponseEntity<LocationResponse> getVenueById(@PathVariable UUID venueId) {
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

    @GetMapping("/all/simple")
    public ResponseEntity<List<VenueSimple>> getAllVenuesSimple() {
        List<VenueSimple> venues = venueService.getAllVenuesSimple();
        return ResponseEntity.ok(venues);
    }

    @PostMapping("/create")
    public ResponseEntity<LocationResponse> createVenue(
            @RequestBody LocationAddRequest newLocation) {
        LocationResponse locationResponse = venueService.createVenue(newLocation);
        return ResponseEntity.status(HttpStatus.CREATED).body(locationResponse);
    }


    @PutMapping("/update/{venueId}")
    public ResponseEntity<LocationResponse> updateVenue(
            @PathVariable UUID venueId,
            @RequestBody LocationAddRequest newLocation) {
        try {
            LocationResponse locationResponse = venueService.updateVenue(venueId, newLocation);
            return ResponseEntity.ok(locationResponse);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{venueId}")
    public ResponseEntity<Void> deleteVenue(@PathVariable UUID venueId) {
        try {
            venueService.deleteVenue(venueId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
