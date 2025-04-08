package com.example.eventservice.request;

public record LocationAddRequest(
    String venueName, int totalCapacity, String address, String city) {}
