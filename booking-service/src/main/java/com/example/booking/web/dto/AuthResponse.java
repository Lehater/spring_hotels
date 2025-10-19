package com.example.booking.web.dto;

public record AuthResponse(String token, long expiresInSeconds) {}
