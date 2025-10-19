# Gateway Routing Policy (final)

| Public Path(s)                                        | Target                 | Примечание                |
|-------------------------------------------------------|------------------------|---------------------------|
| `/api/user/**`, `/api/booking/**`, `/api/bookings/**` | `lb://booking-service` | Проброс JWT, X-Request-Id |
| `/api/hotels/**`, `/api/rooms/**`                     | `lb://hotel-service`   | Без INTERNAL              |

INTERNAL (не публикуется):

- POST /api/rooms/{id}/confirm-availability
- POST /api/rooms/{id}/release

Фильтры: удалить Cookie; пробрасывать Authorization/X-Request-Id/X-Booking-Id; генерить X-Request-Id при отсутствии.
