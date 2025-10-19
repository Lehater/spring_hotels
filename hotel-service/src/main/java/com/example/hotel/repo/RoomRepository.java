package com.example.hotel.repo;

import com.example.hotel.domain.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
  List<Room> findByHotelId(Long hotelId);
}
