package com.example.booking.web;

import com.example.booking.client.HotelClient;
import com.example.booking.domain.Booking;
import com.example.booking.repo.BookingRepository;
import com.example.booking.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {
  private final BookingRepository bookings;
  private final HotelClient hms;

  @PostMapping
  @PreAuthorize("hasAnyRole('USER','ADMIN')")
  public ResponseEntity<BookingResponse> create(
      @RequestHeader("Authorization") String bearer,
      @RequestHeader("X-Request-Id") String requestId,
      @RequestBody BookingRequest req) {
    var existing = bookings.findByRequestId(requestId);
    if (existing.isPresent()) {
      var b = existing.get();
      return ResponseEntity.ok(new BookingResponse(b.getId(), b.getStatus().name(), b.getRoomId()));
    }
    MDC.put("requestId", requestId);
    var b =
        bookings.save(
            Booking.builder()
                .userId(0L)
                .status(Booking.Status.PENDING)
                .startDate(req.start())
                .endDate(req.end())
                .requestId(requestId)
                .build());
    Long roomId = req.roomId();
    try {
      if (req.autoSelect()) {
        var rec =
            hms.recommend(bearer, req.hotelId(), req.start().toString(), req.end().toString(), 1);
        if (rec.isEmpty()) throw new IllegalStateException("NO_RECOMMENDATION");
        roomId = Long.valueOf(((java.util.Map) rec.get(0)).get("id").toString());
      }
      if (roomId == null) throw new IllegalArgumentException("ROOM_ID_REQUIRED");
      hms.confirm(bearer, roomId, requestId, req.start().toString(), req.end().toString());
      b.setRoomId(roomId);
      b.setStatus(Booking.Status.CONFIRMED);
      bookings.save(b);
      return ResponseEntity.ok(new BookingResponse(b.getId(), b.getStatus().name(), b.getRoomId()));
    } catch (Exception e) {
      if (roomId != null) {
        try {
          hms.release(bearer, roomId, requestId);
        } catch (Exception ignored) {
        }
      }
      b.setStatus(Booking.Status.CANCELLED);
      bookings.save(b);
      return ResponseEntity.status(503)
          .body(new BookingResponse(b.getId(), b.getStatus().name(), roomId));
    } finally {
      MDC.clear();
    }
  }
}
