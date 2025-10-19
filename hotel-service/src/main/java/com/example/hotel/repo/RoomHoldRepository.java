package com.example.hotel.repo;

import com.example.hotel.domain.RoomHold;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface RoomHoldRepository extends JpaRepository<RoomHold, Long> {
  Optional<RoomHold> findByRequestId(String requestId);

  @Query(
      "select h from RoomHold h where h.roomId = :roomId and h.status <> 'RELEASED' and (:start <= h.endDate and :end >= h.startDate)")
  List<RoomHold> findIntersecting(Long roomId, LocalDate start, LocalDate end);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select h from RoomHold h where h.roomId = :roomId and h.status <> 'RELEASED' and (:start <= h.endDate and :end >= h.startDate)")
  List<RoomHold> findIntersectingForUpdate(Long roomId, LocalDate start, LocalDate end);

  @Modifying
  @Query(
      "update RoomHold h set h.status = :status where h.requestId = :requestId and h.roomId = :roomId")
  int updateStatusByRequest(String requestId, Long roomId, RoomHold.Status status);
}
