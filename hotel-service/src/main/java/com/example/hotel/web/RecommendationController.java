package com.example.hotel.web;

import com.example.hotel.domain.Room;
import com.example.hotel.repo.RoomRepository;
import com.example.hotel.service.AvailabilityService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RecommendationController {
  private final RoomRepository rooms;
  private final AvailabilityService availability;

  @GetMapping("/recommend")
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public List<Room> recommend(
      @RequestParam Long hotelId,
      @RequestParam LocalDate start,
      @RequestParam LocalDate end,
      @RequestParam(defaultValue = "5") int limit) {
    return rooms.findByHotelId(hotelId).stream()
        .filter(Room::isAvailable)
        .filter(r -> availability.isAvailable(r.getId(), start, end))
        .sorted(Comparator.comparingInt(Room::getTimesBooked).thenComparing(Room::getId))
        .limit(Math.max(1, limit))
        .collect(Collectors.toList());
  }
}
