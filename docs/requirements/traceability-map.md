# Traceability Map — Spring Hotels Booking System

Связь:  
**Требование (из ТЗ)** → **Артефакт (документ/диаграмма/API/DDL)** → **Реализация (сервис, слой, класс)** → **Тесты (ID
из test-matrix.md)**

---

## 1. Архитектура и сервисы

| Требование                                                                | Артефакт                                                            | Реализация                                                                   | Тесты             |
|---------------------------------------------------------------------------|---------------------------------------------------------------------|------------------------------------------------------------------------------|-------------------|
| Система состоит из Gateway, Booking Service, Hotel Service, Eureka Server | `architecture/c4-container.puml`, `README.md`                       | модули `gateway/`, `booking-service/`, `hotel-service/`, `discovery-eureka/` | Health: T1 sanity |
| Каждый сервис — самостоятельный Spring Boot с H2                          | `architecture/decisions/adr-0004-h2-in-memory.md`, `data/ddl/*.sql` | `application.yml` (каждого), H2 config                                       | T1 setup          |

---

## 2. Пользователи и роли

| Требование                                      | Артефакт                                                        | Реализация                            | Тесты         |
|-------------------------------------------------|-----------------------------------------------------------------|---------------------------------------|---------------|
| JWT-аутентификация и роли USER/ADMIN            | `booking-service-openapi.yaml`, `adr-0003-jwt-dev-algorithm.md` | `JwtService`, Spring Security config  | T11, T12, T14 |
| Регистрация/вход (JWT TTL=1h)                   | OpenAPI `/user/register`, `/user/auth`                          | `UserController`, `JwtService`        | T11, T14      |
| ADMIN управляет отелями/номерами/пользователями | `hotel-service-openapi.yaml` (ADMIN endpoints), NFR             | HMS Controllers (ADMIN @PreAuthorize) | T3, T4        |

---

## 3. Управление отелями и номерами

| Требование                                   | Артефакт                                                   | Реализация                                        | Тесты  |
|----------------------------------------------|------------------------------------------------------------|---------------------------------------------------|--------|
| CRUD отелей и номеров                        | `hotel-service-openapi.yaml` (`/api/hotels`, `/api/rooms`) | `HotelController`, `RoomController`               | T3, T4 |
| Статистика загрузки номеров (`times_booked`) | `adr-0005-recommendation-times-booked.md`                  | `RoomEntity.timesBooked`, `RecommendationService` | T13    |

---

## 4. Бронирования

| Требование                                    | Артефакт                                                                                   | Реализация                                                   | Тесты      |
|-----------------------------------------------|--------------------------------------------------------------------------------------------|--------------------------------------------------------------|------------|
| Создание бронирования (PENDING → CONFIRMED)   | `booking-service-openapi.yaml` (`POST /booking`), `adr-0001-saga-vs-2pc.md`                | `BookingController`, `BookingFacade`, `BookingDomainService` | T1, T2     |
| История и отмена бронирований                 | OpenAPI `/bookings`, `/booking/{id}`                                                       | `BookingController`, `BookingRepository`                     | T11, T12   |
| Статусы брони (PENDING, CONFIRMED, CANCELLED) | `domain/model/Booking.java`, `state/booking-state.puml`                                    | Enum `BookingStatus`                                         | T1, T5, T6 |
| Автоподбор по `times_booked`                  | `adr-0005-recommendation-times-booked.md`, `hotel-service-openapi.yaml` `/rooms/recommend` | `RecommendationService`, `HotelServiceClient`                | T2, T13    |

---

## 5. Алгоритмы и согласованность

| Требование                                 | Артефакт                                                           | Реализация                                                                      | Тесты          |
|--------------------------------------------|--------------------------------------------------------------------|---------------------------------------------------------------------------------|----------------|
| Двухшаговая согласованность (BS↔HMS)       | `adr-0001-saga-vs-2pc.md`, `sequence/booking-saga.puml`            | `BookingFacade` orchestrates → `HotelServiceClient` → `RoomAvailabilityService` | T1, T3, T5, T6 |
| Компенсация при сбое (`release`)           | `hotel-service-openapi.yaml` (`/release`), `idempotency-policy.md` | `BookingFacade`, `HotelServiceClient.release()`                                 | T5, T6         |
| Локальные транзакции (JPA @Transactional)  | `non-functional-spec.md`                                           | `BookingRepository`, `RoomHoldRepository`                                       | T1, T3         |
| Исключить дубликаты при повторной доставке | `idempotency-policy.md`, `DDL` (UNIQUE request_id)                 | @Transactional + UNIQUE(request_id)                                             | T7, T8, T9     |

---

## 6. Безопасность

| Требование                   | Артефакт                              | Реализация                        | Тесты    |
|------------------------------|---------------------------------------|-----------------------------------|----------|
| Проверка JWT каждым сервисом | NFR, OpenAPI securitySchemes          | Spring Security (Resource Server) | T14      |
| Ограничение ролей            | OpenAPI (`security: bearerAuth`), NFR | `@PreAuthorize` по ролям          | T12, T14 |

---

## 7. Ошибки и надёжность

| Требование                    | Артефакт                        | Реализация                                       | Тесты                        |
|-------------------------------|---------------------------------|--------------------------------------------------|------------------------------|
| Формат error envelope         | `requirements/error-catalog.md` | `GlobalExceptionHandler`                         | Все ошибки (T3–T6, T12, T14) |
| Ретраи при тайм-аутах         | `reliability-retry-policy.md`   | `HotelServiceClient` (WebClient + retry/backoff) | T5, T6                       |
| 504/503 при недоступности HMS | Error catalog                   | `BookingFacade` error mapping                    | T5, T6                       |
| 409 при конфликте             | Error catalog                   | `RoomAvailabilityService` (пересечения)          | T3                           |

---

## 8. Данные и даты

| Требование                      | Артефакт               | Реализация                   | Тесты   |
|---------------------------------|------------------------|------------------------------|---------|
| Проверка пересечения интервалов | `time-date-policy.md`  | `DateRangePolicy.overlaps()` | T3, T10 |
| Формат ISO-8601 (`YYYY-MM-DD`)  | OpenAPI (format: date) | DTO поля LocalDate           | T1, T2  |
| Inclusive границы               | `time-date-policy.md`  | Domain logic (HMS)           | T3, T10 |

---

## 9. Идемпотентность

| Требование                    | Артефакт                                           | Реализация                      | Тесты |
|-------------------------------|----------------------------------------------------|---------------------------------|-------|
| Повторы без побочных эффектов | `idempotency-policy.md`, `DDL (UNIQUE request_id)` | Controllers + Repository lookup | T7–T9 |
| `X-Request-Id` обязателен     | `idempotency-policy.md`                            | Controller filter/validation    | T7    |

---

## 10. Observability / Проверяемость

| Требование        | Артефакт                   | Реализация                               | Тесты                         |
|-------------------|----------------------------|------------------------------------------|-------------------------------|
| Корреляция логов  | `logging-audit.md`         | MDC (`requestId`, `bookingId`, `userId`) | Проверка по логам после T1–T6 |
| Health endpoints  | Spring Boot Actuator       | `/actuator/health`                       | Manual                        |
| Метрики (минимум) | `observability-metrics.md` | Micrometer counters/histograms           | Manual check                  |

---

## 11. Тестирование и проверка

| Требование                                | Артефакт                                             | Реализация                  | Тесты             |
|-------------------------------------------|------------------------------------------------------|-----------------------------|-------------------|
| Сценарии: успех, ошибка, тайм-аут, повтор | `testing/test-matrix.md`, `testing/test-strategy.md` | Тестовые классы по сервисам | T1–T14            |
| Отражение переходов состояний в логах     | `logging-audit.md`                                   | INFO-level события          | Проверка журналов |
| JWT и права доступа                       | OpenAPI, Security Config                             | Security тесты              | T11–T14           |

---

## 12. Развёртывание / DEV

| Требование                   | Артефакт                                   | Реализация                       | Тесты                      |
|------------------------------|--------------------------------------------|----------------------------------|----------------------------|
| Локальный запуск (in-memory) | `operations/runbook.md`, `application.yml` | Spring Boot auto-config          | Smoke test                 |
| Gateway маршрутизация        | `api/gateway-routing-policy.md`            | `application.yml` gateway.routes | Manual check via `/api/*`  |
| Eureka discovery             | `application.yml (Eureka)`, C4             | Spring Cloud config              | Manual check via dashboard |


