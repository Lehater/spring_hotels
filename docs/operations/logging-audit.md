# Logging & Audit

## MDC (обязательные поля)

- `requestId` — значение X-Request-Id
- `bookingId` — X-Booking-Id (если известен/назначен)
- `userId` — из JWT (subject)

## Ключевые бизнес-события (уровень INFO)

- BS:
    - `booking.created` — статус PENDING, payload: {bookingId, userId, roomId?, hotelId, startDate, endDate}
    - `booking.confirm.request` — запрос в HMS (attempt, timeoutMs)
    - `booking.confirm.success` — переход CONFIRMED
    - `booking.confirm.failure` — код/ошибка, инициируем компенсацию
    - `booking.release.request` — компенсирующий вызов
    - `booking.release.done` — компенсация завершена
    - `booking.cancelled` — финальный статус CANCELLED
- HMS:
    - `hold.confirm.request` — roomId, dates
    - `hold.confirm.held` — создан HELD (или найден по идемпотентности)
    - `hold.release.done` — RELEASED (идемпотентный no-op допустим)
    - `availability.conflict` — 409, details: {roomId, startDate, endDate}
    - `room.timesBooked.changed` — +1 / -1 по requestId (однократно)

## Формат логов

- Структурированный (JSON) — **опционально**; допустим обычный текст с MDC.

## Маскирование

- Пароли и JWT не логируются; токен — только длина/префикс, если нужно.
