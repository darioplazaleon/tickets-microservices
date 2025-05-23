package com.example.eventservice.service;

import com.example.eventservice.config.RestPage;
import com.example.eventservice.entity.Venue;
import com.example.eventservice.repository.VenueRepository;
import com.example.eventservice.request.LocationAddRequest;
import com.example.eventservice.response.LocationResponse;
import com.example.eventservice.response.VenueSimple;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueService {

  private final VenueRepository venueRepository;

  @Cacheable(value = "venues", key = "#venueId")
  public LocationResponse getVenueById(UUID venueId) {
    return venueRepository
        .findById(venueId)
        .map(LocationResponse::new)
        .orElseThrow(() -> new EntityNotFoundException("Venue not found"));
  }

  @Cacheable("Venue_Response_Page ")
  public RestPage<LocationResponse> getAllVenues(Pageable pageable) {
    Page<LocationResponse> page = venueRepository.findAll(pageable).map(LocationResponse::new);
    return new RestPage<>(page);
  }

  @Cacheable("allVenuesSimple")
  public List<VenueSimple> getAllVenuesSimple() {
    return venueRepository.findAll().stream()
        .map(venue -> new VenueSimple(venue.getId(), venue.getName()))
        .toList();
  }

  @CacheEvict(value = {"venues", "allVenues"}, allEntries = true)
  public LocationResponse createVenue(LocationAddRequest newLocationRequest) {
    if (venueRepository.findByName(newLocationRequest.name()).isPresent()) {
      throw new EntityExistsException("Venue with this name already exists");
    }

    var venue =
        Venue.builder()
            .name(newLocationRequest.name())
            .city(newLocationRequest.city())
            .totalCapacity(newLocationRequest.totalCapacity())
            .address(newLocationRequest.address())
            .build();

    var savedVenue = venueRepository.save(venue);

    return new LocationResponse(savedVenue);
  }

  @CacheEvict(value = {"venues", "allVenues"}, allEntries = true)
  public LocationResponse updateVenue(UUID venueId, LocationAddRequest newLocationRequest) {
    var venue =
        venueRepository
            .findById(venueId)
            .orElseThrow(() -> new EntityNotFoundException("Venue not found"));

    if (!venue.getName().equals(newLocationRequest.name()) &&
            venueRepository.findByName(newLocationRequest.name()).isPresent()) {
      throw new EntityExistsException("Venue with this name already exists");
    }

    venue.setName(newLocationRequest.name());
    venue.setCity(newLocationRequest.city());
    venue.setTotalCapacity(newLocationRequest.totalCapacity());
    venue.setAddress(newLocationRequest.address());

    var updatedVenue = venueRepository.save(venue);

    return new LocationResponse(updatedVenue);
  }

  @CacheEvict(value = {"venues", "allVenues"}, allEntries = true)
  public void deleteVenue(UUID venueId) {
    var venue =
        venueRepository
            .findById(venueId)
            .orElseThrow(() -> new EntityNotFoundException("Venue not found"));

    venueRepository.delete(venue);
  }
}
