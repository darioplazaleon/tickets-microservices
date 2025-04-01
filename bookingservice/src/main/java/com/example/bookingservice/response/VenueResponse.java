package com.example.bookingservice.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueResponse {
    private Long id;
    private String name;
    private int totalCapacity;
    private String address;
    private String city;
}
