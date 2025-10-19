package com.example.hotel.web;

import com.example.hotel.domain.RoomHold;
import com.example.hotel.service.HoldService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

record ConfirmRequest(
    @NotBlank String requestId, @NotNull LocalDate start, @NotNull LocalDate end) {}

@RestController
@RequestMapping("/internal/rooms")
@RequiredArgsConstructor
public class InternalRoomController {
  private final HoldService holds;

  @PostMapping("/{id}/confirm-availability")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<RoomHold> confirm(
      @RequestHeader("X-Request-Id") String requestId,
      @PathVariable Long id,
      @RequestBody ConfirmRequest req) {
    RoomHold h = holds.confirm(id, requestId, req.start(), req.end());
    return ResponseEntity.ok(h);
  }

  @PostMapping("/{id}/release")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> release(
      @RequestHeader("X-Request-Id") String requestId, @PathVariable Long id) {
    holds.release(id, requestId);
    return ResponseEntity.accepted().build();
  }
}
