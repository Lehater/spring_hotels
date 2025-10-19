package com.example.common.web.error.servlet;

import com.example.common.web.error.BusinessException;
import com.example.common.web.error.ErrorEnvelope;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /* ===== helpers ===== */

  private String rid(WebRequest req) {
    return req.getHeader("X-Request-Id");
  }

  private ResponseEntity<ErrorEnvelope> body(
      HttpStatus status, String code, String message, String rid, Map<String, Object> details) {
    return ResponseEntity.status(status).body(ErrorEnvelope.of(code, message, rid, details));
  }

  /* ===== catalog-driven mappings ===== */

  /** Бизнес-исключения уже содержат code + status. */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorEnvelope> onBusiness(BusinessException ex, WebRequest req) {
    return body(ex.status(), ex.code(), ex.getMessage(), rid(req), Map.of());
  }

  /** Валидация тела/параметров: 400 VALIDATION_ERROR. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorEnvelope> onBeanValidation(
      MethodArgumentNotValidException ex, WebRequest req) {
    List<Map<String, String>> errors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
            .toList();
    return body(
        HttpStatus.BAD_REQUEST,
        "VALIDATION_ERROR",
        "Validation failed",
        rid(req),
        Map.of("errors", errors));
  }

  @ExceptionHandler({
    ConstraintViolationException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<ErrorEnvelope> onBadRequest(Exception ex, WebRequest req) {
    return body(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), rid(req), Map.of());
  }

  /** Security: 401/403. */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorEnvelope> onAuth(AuthenticationException ex, WebRequest req) {
    return body(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), rid(req), Map.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorEnvelope> onAccess(AccessDeniedException ex, WebRequest req) {
    return body(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), rid(req), Map.of());
  }

  /** Не найдено: 404. */
  @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
  public ResponseEntity<ErrorEnvelope> onNotFound(RuntimeException ex, WebRequest req) {
    return body(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), rid(req), Map.of());
  }

  /** Конфликты / уникальные ключи: 409. */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorEnvelope> onIntegrity(
      DataIntegrityViolationException ex, WebRequest req) {
    String detail =
        ex.getMostSpecificCause() != null
            ? ex.getMostSpecificCause().getMessage()
            : ex.getMessage();
    return body(
        HttpStatus.CONFLICT,
        "CONFLICT",
        "Data integrity violation",
        rid(req),
        Map.of("detail", detail));
  }

  /** Тайм-ауты: 504 GATEWAY_TIMEOUT. */
  @ExceptionHandler({TimeoutException.class, SocketTimeoutException.class})
  public ResponseEntity<ErrorEnvelope> onTimeout(Exception ex, WebRequest req) {
    return body(
        HttpStatus.GATEWAY_TIMEOUT, "GATEWAY_TIMEOUT", "Upstream timeout", rid(req), Map.of());
  }

  /** Сбои интеграций: маппим 4xx/5xx партнёра в единый envelope. */
  @ExceptionHandler({HttpStatusCodeException.class, WebClientResponseException.class})
  public ResponseEntity<ErrorEnvelope> onUpstreamStatus(RuntimeException ex, WebRequest req) {
    int upstream;
    String text;
    if (ex instanceof HttpStatusCodeException h) {
      upstream = h.getStatusCode().value();
      text = h.getResponseBodyAsString();
    } else {
      WebClientResponseException w = (WebClientResponseException) ex;
      upstream = w.getStatusCode().value();
      text = w.getResponseBodyAsString();
    }
    HttpStatus out = (upstream >= 500) ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_GATEWAY;
    return body(
        out,
        "SERVICE_ERROR",
        "Upstream error",
        rid(req),
        Map.of("upstreamStatus", upstream, "body", text));
  }

  /** Фолбэк: 503 SERVICE_ERROR. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorEnvelope> onGeneric(Exception ex, WebRequest req) {
    return body(
        HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_ERROR", ex.getMessage(), rid(req), Map.of());
  }
}
