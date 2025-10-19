package com.example.booking.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hotel.client")
public record HotelClientProps(
    int connectTimeoutMs, int readTimeoutMs, int retries, int backoffMs) {}
