package com.example.common.web.error.exceptions;

import com.example.common.web.error.BusinessException;
import org.springframework.http.HttpStatus;

public class ServiceErrorException extends BusinessException {
  public ServiceErrorException(String msg) {
    super("SERVICE_ERROR", msg, HttpStatus.SERVICE_UNAVAILABLE);
  }
}
