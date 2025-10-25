# Spring Hotels - учебный монорепозиторий (Microservices)

Многомодульный пример распределённой системы бронирования отелей на **Java 17 / Spring Boot 3.5 / Spring Cloud**.
Все сервисы используют **H2 (in-memory)**, взаимодействие - через HTTP, открытые API описаны в **OpenAPI**.

## Модули

- **discovery-eureka** - Service Registry (Netflix Eureka).
- **gateway** - API Gateway (Spring Cloud Gateway, WebFlux). Маршрутизация, CORS, скрытие `/internal/**`.
- **hotel-service (HMS)** - CRUD отелей/комнат, рекомендации, удержания/подтверждения слотов.
- **booking-service (BS)** - пользователи и JWT, создание/идемпотентность бронирований, сага с HMS.
- **common-web** - общие фильтры/ошибки/логирование (для сервисов на servlet-стеке).

## Ключевые возможности

- Регистрация и вход пользователей (JWT **HS256**, TTL = 1h).
- Создание бронирования с двухшаговой согласованностью:
  `PENDING → (confirm в HMS) → CONFIRMED` / при сбое → `release + CANCELLED`.
- **Идемпотентность** критических POST по заголовку **`X-Request-Id`** (UUID).
- Ретрии и таймауты клиентских вызовов BS → HMS.
- Подсказки по выбору номера: фильтр свободных, сортировка `(timesBooked ASC, id ASC)`.
- Единый формат ошибок (error envelope) и сквозная корреляция по `X-Request-Id`.

## Архитектура и порты

- **Eureka** - `8761` (UI: `http://localhost:8761`)
- **Gateway** - `8080`
- **Hotel-Service** - `8081`
- **Booking-Service** - `8082`

Gateway маршрутизирует к сервисам по serviceId через Eureka и проксирует `Authorization`/`X-Request-Id`.
Пути `/internal/**` **недоступны через Gateway** (возвращается 404).

## Требования

- Java **17+**
- Maven **3.9+**
- Свободны порты 8761/8080/8081/8082

## Быстрый запуск

```bash
# 1) Клонирование / распаковка архива
cd spring-hotels

# 2) Общие переменные окружения (DEV)
export AUTH_JWT_SECRET=dev-secret-0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCD
export SPRING_PROFILES_ACTIVE=dev-test

# 3) Запуск по модулям (в отдельных терминалах)
make run-eureka
make run-hms
make run-bs
make run-gw

```

## Конфигурация JWT

Во всех сервисах используется один симметричный секрет **HS256**:

```yaml
auth:
  jwt:
  secret: ${AUTH_JWT_SECRET:...}  # задаётся через ENV
```

- **booking-service** выпускает токен с claim `role` (`ROLE_USER`/`ROLE_ADMIN`).
- **gateway / hotel-service / booking-service** валидируют токены одним и тем же секретом.

> Для прод-окружения используйте отдельный Identity Provider (Keycloak/OAuth2) и асимметричную криптографию.

## Быстрый e2e через Gateway (8080)

1. **Регистрация админа и получение JWT**

```bash
TOKEN=$(curl -s http://localhost:8080/user/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin","admin":true}' | jq -r .token)
```

1. **Создание отеля и комнаты (требует ROLE_ADMIN)**

```bash
RID=$(uuidgen)

curl -s http://localhost:8080/api/hotels \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Request-Id: $RID" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo","city":"Berlin"}'

curl -s http://localhost:8080/api/rooms \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Request-Id: $RID" \
  -H 'Content-Type: application/json' \
  -d '{"hotelId":1,"number":"101"}'
```

1. **Подсказка номера**

```bash
curl -s "http://localhost:8080/api/rooms/recommend?hotelId=1&start=2025-10-25&end=2025-10-27&limit=1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Request-Id: $(uuidgen)"
```

1. **Бронирование (идемпотентно по `X-Request-Id`)**

```bash
RID2=$(uuidgen)
curl -s http://localhost:8080/booking \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Request-Id: $RID2" \
  -H 'Content-Type: application/json' \
  -d '{"hotelId":1,"autoSelect":true,"start":"2025-10-25","end":"2025-10-27"}'
```

1. **Список своих бронирований**

```bash
curl -s http://localhost:8080/bookings \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Request-Id: $(uuidgen)"
```

## Основные эндпоинты (через Gateway)

### Booking-Service

- `POST /user/register` - регистрация (для админа `{"admin":true}`).
- `POST /user/auth` - логин (выдаёт JWT).
- `POST /booking` - создать бронирование (под капотом: сага + confirm в HMS).
- `GET /bookings` - свои бронирования.
- `GET /booking/{id}`, `DELETE /booking/{id}` - доступ только к своим.

### Hotel-Service

- `POST /api/hotels` (ADMIN), `GET /api/hotels`
- `POST /api/rooms` (ADMIN), `GET /api/rooms?hotelId=...`
- `GET /api/rooms/recommend?hotelId&start&end&limit`
- `POST /internal/rooms/{id}/confirm-availability` (только внутри периметра; ADMIN)
- `POST /internal/rooms/{id}/release?requestId=...` (ADMIN)

> Пути `/internal/**` недоступны через Gateway (404). Они вызываются только сервис-клиентами.

## Надёжность и согласованность

- Локальные транзакции в пределах сервиса (`@Transactional`).
- Сага в Booking: при ошибках **release** в HMS (best-effort) и перевод в `CANCELLED`.
- Ретрии клиентских вызовов BS→HMS, тайм-ауты вынесены в конфиг профиля.
- **Идемпотентность**: повтор критического запроса с тем же `X-Request-Id` возвращает прежний результат.

## Обсервабилити

- Логи (MDC): `requestId`, `userId`, `bookingId` (если применимо).
- Actuator: `/actuator/health`, Prometheus-метрики (`/actuator/prometheus`) в HMS/BS.
- Все ошибки - единый **error envelope** (код, сообщение, timestamp, requestId).

## OpenAPI / Swagger

- Спеки: `docs/api/hotel-service-openapi.yaml`, `docs/api/booking-service-openapi.yaml`.
- Swagger-UI на сервисах (если включён) доступен по прямым портам.

## Тесты

- Учебные интеграционные тесты:
    - **Booking-Service**: регистрация/логин; happy booking (WireMock HMS); идемпотентность; конфликт/тайм-аут; доступ к
      своим; expired JWT.
    - **Hotel-Service**: CRUD и списки; рекомендации (limit/порядок); `confirm/release` (успех/409/идемпотентность);
      проверка ролей на `/internal/**`; валидации + error envelope.
    - **Gateway**: маршрутизация 200/404; deny `/internal/**`; проброс/генерация `X-Request-Id`; CORS; JWT-защита.
- Запуск:
  ```bash
  make test
  ```

---
