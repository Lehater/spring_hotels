package com.example.common.web.error.exceptions;

import com.example.common.web.error.BusinessException;
import org.springframework.http.HttpStatus;

public class BadCredentialsAppException extends BusinessException {
  public BadCredentialsAppException() {
    super("BAD_CREDENTIALS", "Bad credentials", HttpStatus.UNAUTHORIZED);
  }
}
