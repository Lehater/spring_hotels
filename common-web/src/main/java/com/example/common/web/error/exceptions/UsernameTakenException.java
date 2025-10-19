package com.example.common.web.error.exceptions;

import com.example.common.web.error.BusinessException;
import org.springframework.http.HttpStatus;

public class UsernameTakenException extends BusinessException {
  public UsernameTakenException() {
    super("USERNAME_TAKEN", "Username already exists", HttpStatus.CONFLICT);
  }
}
