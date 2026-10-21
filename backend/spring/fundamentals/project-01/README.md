# project-01 – demo-keys service

Spring Boot microservice that generates, stores and verifies recruiter demo keys for `../project-00`.

- Port `8081`, bound to `127.0.0.1` (override with `PORT`).
- Every route needs a short-lived HS256 service token signed with `SERVICE_AUTH_SECRET` (at least 32 characters, the same value as `DEMO_KEYS_SERVICE_SECRET` in `project-00`). The service refuses to start without it. See `.env.example`.
- Keys live in an in-memory H2 database, so they are lost on restart.

## API (`/internal/demo-keys`)

| Method and path | Result |
| --- | --- |
| `POST /jobs` `{count 1-10, days 1-30, label?}` | `202`, `Location` header and a job (`PENDING`, `RUNNING`, `DONE`, `FAILED`) |
| `GET /jobs/{id}` | job status and, once `DONE`, the keys |
| `GET /` | list keys (private keys are never returned) |
| `DELETE /{id}` | `204`; `409` if already redeemed; `404` if unknown |
| `POST /redeem` `{token}` | `{valid, jti, expiresAt}` and records the redemption |

Run: `./mvnw spring-boot:run` · Test: `./mvnw clean test`
