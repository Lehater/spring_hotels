package com.example.hotel.service;

import com.example.hotel.domain.RoomHold;
import com.example.hotel.repo.RoomHoldRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
  private final RoomHoldRepository holds;

  public boolean isAvailable(Long roomId, LocalDate start, LocalDate end) {
    List<RoomHold> xs = holds.findIntersecting(roomId, start, end);
    return xs.isEmpty();
  }
}
