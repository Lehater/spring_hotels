package com.example.hotel.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHotelRequest(@NotBlank String name, String city) {}
