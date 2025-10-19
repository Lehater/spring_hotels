package com.example.common.web.error.exceptions;

import com.example.common.web.error.BusinessException;
import org.springframework.http.HttpStatus;

public class RoomNotAvailableException extends BusinessException {
  public RoomNotAvailableException() {
    super("ROOM_NOT_AVAILABLE", "Room is not available for these dates", HttpStatus.CONFLICT);
  }
}
