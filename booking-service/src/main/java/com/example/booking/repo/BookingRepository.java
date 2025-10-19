package com.example.booking.repo;

import com.example.booking.domain.Booking;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
  Optional<Booking> findByRequestId(String requestId);

  List<Booking> findByUserId(Long userId);
}
