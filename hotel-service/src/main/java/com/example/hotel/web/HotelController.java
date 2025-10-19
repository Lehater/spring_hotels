package com.example.hotel.web;

import com.example.hotel.domain.Hotel;
import com.example.hotel.repo.HotelRepository;
import com.example.hotel.web.dto.CreateHotelRequest;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {
  private final HotelRepository hotels;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Hotel> create(@RequestBody CreateHotelRequest req) {
    Hotel h = hotels.save(Hotel.builder().name(req.name()).city(req.city()).build());
    return ResponseEntity.created(URI.create("/api/hotels/" + h.getId())).body(h);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public List<Hotel> list() {
    return hotels.findAll();
  }
}
