# Reliability & Retry Policy

- Ретраи только для HMS:
    - confirm-availability,
    - release.
- Попыток: 3;
- backoff: ~200ms → 500ms → 1000ms;
- timeouts:
    - connect ~1s,
    - read ~2s.
- На провале:
    - Booking=CANCELLED;
    - release (best-effort);
    - наружу 504/503.
