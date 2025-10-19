package com.example.hotel.service;

import com.example.hotel.domain.Room;
import com.example.hotel.domain.RoomHold;
import com.example.hotel.repo.RoomHoldRepository;
import com.example.hotel.repo.RoomRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HoldService {
  private final RoomHoldRepository holds;
  private final RoomRepository rooms;

  @Transactional
  public RoomHold confirm(Long roomId, String requestId, LocalDate start, LocalDate end) {
    var existing = holds.findByRequestId(requestId);
    if (existing.isPresent()) return existing.get();
    var locked = holds.findIntersectingForUpdate(roomId, start, end);
    if (!locked.isEmpty()) throw new IllegalStateException("ROOM_NOT_AVAILABLE");
    RoomHold saved =
        holds.save(
            RoomHold.builder()
                .roomId(roomId)
                .requestId(requestId)
                .startDate(start)
                .endDate(end)
                .status(RoomHold.Status.COMMITTED)
                .build());
    Room r = rooms.findById(roomId).orElseThrow();
    r.setTimesBooked(r.getTimesBooked() + 1);
    rooms.save(r);
    return saved;
  }

  @Transactional
  public void release(Long roomId, String requestId) {
    holds
        .findByRequestId(requestId)
        .ifPresent(
            h -> {
              if (h.getStatus() != RoomHold.Status.RELEASED) {
                holds.updateStatusByRequest(requestId, roomId, RoomHold.Status.RELEASED);
              }
            });
  }
}
