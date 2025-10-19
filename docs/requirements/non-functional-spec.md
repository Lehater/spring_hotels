# Non-Functional Requirements (в рамках ТЗ)

## 1. Производительность и задержки (DEV)

- Целевые p95 задержки (локально, без сетевых hops):
    - POST /booking (happy-path): ≤ 300–500 ms
    - GET /rooms/recommend: ≤ 150 ms
- Тайм-ауты межсервисных вызовов (BS→HMS):
    - connect: ~1 s, read: ~2 s
    - retry: до 3 попыток с экспоненциальным backoff (200/500/1000 ms)

## 2. Надёжность и ошибки

- Повторы только для идемпотентных операций (HMS: confirm-availability, release).
- При исчерпании попыток: бронирование переводится в CANCELLED; вызывается компенсирующий release (best-effort).
- Конкурентные конфликты отражаются кодом 409.

## 3. Консистентность и идемпотентность

- X-Request-Id (UUID) обязателен для POST /booking и INTERNAL HMS-операций.
- Уникальные ключи: bookings.request_id, room_holds.request_id.
- Повторный запрос с тем же X-Request-Id возвращает прежний результат.

## 4. Безопасность

- JWT (TTL = 1 час), роли USER/ADMIN.
- Каждый сервис — Resource Server; Gateway — маршрутизатор (без собственной авторизации).
- INTERNAL-эндпойнты HMS не публикуются через Gateway.

## 5. Наблюдаемость и проверяемость

- Логи со сквозной корреляцией (requestId, bookingId, userId).
- В логах фиксируются переходы состояний и факты компенсации.
- Health endpoints включены; метрики — базовый минимум (см. observability-metrics.md).

## 6. Данные и даты

- В API: ISO-8601 `YYYY-MM-DD`, в БД: тип `DATE`.
- Интервалы инклюзивны по обоим краям.

## 7. Развёртывание (DEV)

- In-memory H2 для каждого сервиса.
- Порядок запуска: Eureka → HMS → BS → Gateway.
