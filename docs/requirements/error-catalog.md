# Error Catalog

Формат:

```json
{
  "error": "CONFLICT",
  "message": "Room is not available for given dates",
  "details": {
    "roomId": 7,
    "startDate": "2025-11-01",
    "endDate": "2025-11-05"
  },
  "correlationId": "<X-Request-Id>"
}
```

Коды:

- VALIDATION_FAILED(400/422),
- UNAUTHORIZED(401),
- FORBIDDEN(403),
- NOT_FOUND(404),
- CONFLICT(409),
- USERNAME_TAKEN(409),
- ROOM_UNAVAILABLE(409),
- HOTEL_NOT_FOUND(404),
- ROOM_NOT_FOUND(404),
- TIMEOUT_UPSTREAM(504),
- UPSTREAM_ERROR(503),
- IDEMPOTENT_REPLAY(200/201).

Примечания:

- Идемпотентность:
    - при повторе с тем же X-Request-Id возвращается прежний результат с тем же кодом/телом, ошибка не генерируется.
- Конфликт занятости:
    - HMS возвращает 409 CONFLICT с details о периоде/roomId.
- Тайм-аут:
    - BS возвращает 504 TIMEOUT_UPSTREAM;
    - внутри заявка переводится в CANCELLED, вызывается release (best-effort).