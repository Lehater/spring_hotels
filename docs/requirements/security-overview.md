# Security Overview (DEV)

- JWT HS256 (TTL=1h);
- общий секрет у BS/HMS/Gateway.
- Claims:
    - sub,
    - roles,
    - exp,
    - iss=booking-service.
- Роли:
    - USER (личные операции),
    - ADMIN (CRUD отелей/номеров/пользователей).
- Каждый сервис — Resource Server, Gateway — маршрутизация.
