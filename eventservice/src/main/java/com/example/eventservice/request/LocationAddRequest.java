package com.example.eventservice.request;

public record LocationAddRequest(
        String name, int totalCapacity, String address, String city) {}
