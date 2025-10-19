# Idempotency Policy

- `X-Request-Id` (UUID) обязателен для: 
  - BS POST /booking; 
  - HMS POST confirm-availability/release.
- UNIQUE: 
  - bookings.request_id; 
  - room_holds.request_id.
- Повтор запроса возвращает прежний результат; release — idempotent no-op.
- 400 при отсутствии X-Request-Id; MDC содержит requestId/bookingId/userId.
