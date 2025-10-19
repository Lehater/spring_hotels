package com.example.hotel.web.dto;

import jakarta.validation.constraints.NotNull;

public record CreateRoomRequest(@NotNull Long hotelId, String number, Boolean available) {}
