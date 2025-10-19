# Runbook — Local DEV

## Порядок запуска

1) Discovery: `discovery-eureka` (порт 8761)
2) HMS: `hotel-service` (порт 0 — случайный) — регистрируется в Eureka
3) BS:  `booking-service` (порт 0) — регистрируется в Eureka
4) Gateway: `api-gateway` (порт 8080)

## Конфигурация (DEV)

- JWT: HS256, общий `security.jwt.secret` одинаков в BS/HMS/Gateway Resource Server.
- H2: in-memory, авто-DDL `update`.
- Заголовок `X-Request-Id` обязателен (UUID). Если отсутствует — gateway генерирует.

## Первичная инициализация

- (Опционально) выполнить seed-скрипты:
    - HMS: `docs/data/seed/hotel-seed.sql`
    - BS:  `docs/data/seed/booking-seed.sql`
- Либо через API (ADMIN) создать отели/комнаты.

## Swagger UI

- BS:  `http://localhost:<bs-port>/swagger-ui/index.html`
- HMS: `http://localhost:<hms-port>/swagger-ui/index.html`
- Вызывать через Gateway для внешних API: `http://localhost:8080/...`

## Быстрый e2e (через Gateway)

1. Регистрация пользователя (или используйте сидированного):
   ```bash
   curl -s http://localhost:8080/api/user/register \
     -H "Content-Type: application/json" \
     -d '{"username":"u1","password":"p@ssw0rd"}'
   # => {"token":"<JWT>"}
