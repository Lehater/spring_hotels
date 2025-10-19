package com.example.booking.web.dto;

import java.time.LocalDate;

public record BookingRequest(
    Long roomId, Long hotelId, LocalDate start, LocalDate end, boolean autoSelect) {}
