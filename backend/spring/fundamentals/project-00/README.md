# project-00 — University API

A layered Spring Boot 4 application (Java 25, Spring Data JPA, H2 locally/PostgreSQL in production, Spring Security with JWT) that manages courses, lectures,
professors, students and lecture notes.

## Configuration

Secrets and database settings are not stored in the repository. Copy [.env.example](.env.example) to `.env`
(git-ignored) and fill in the values; the app loads it from the working directory, and real environment variables
override it. `ADMIN_USERNAME`, `ADMIN_PASSWORD` and `JWT_SECRET` (at least 32 bytes) are required, with no defaults.
Tests use their own throwaway values from the Surefire configuration in `pom.xml`.

## Run

```powershell
$env:JAVA_HOME = 'C:\Users\devloop\.jdks\openjdk-25.0.1'
.\mvnw.cmd spring-boot:run
```

| What | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI document (JSON) | http://localhost:8080/v3/api-docs |
| OpenAPI document (YAML) | http://localhost:8080/v3/api-docs.yaml |
| H2 console | http://localhost:8080/h2-console |

For the H2 console, use JDBC URL `jdbc:h2:mem:project00`, username `sa`, and leave the password blank. The console
is enabled for local development and is publicly accessible; do not expose it in production. The database is in
memory, so data is lost on restart. DevTools restarts the app when classes change.

## Profiles and demo data

| Profile | Activation | Behaviour |
| --- | --- | --- |
| `dev` (default) | no `SPRING_PROFILES_ACTIVE` | H2 console and Swagger UI on; seeds local sample data on an empty database |
| `prod` | `SPRING_PROFILES_ACTIVE=prod` | H2 console and Swagger UI off; no sample data |

The `dev` profile seeds local sample accounts, creates the Google-only MANAGER account (`app.manager.email`) and enables recruiter demo-key testing; never expose it publicly. Docker uses `prod` by default, which has no demo-key or manager routes and seeds no sample accounts. Tests run with the `test` profile, so nothing is seeded.

### Demo-keys microservice

Demo keys are generated, stored and verified by the sibling service `../project-01` (port 8081, loopback only). In the `dev` profile `project-00` calls it asynchronously: `POST /api/manager/demo-keys` answers `202 Accepted` with a `Location` of `/api/manager/demo-keys/jobs/{id}` to poll until `DONE` or `FAILED`. If `project-01` is down the API answers `503`. Both services must share a secret of at least 32 characters: `DEMO_KEYS_SERVICE_SECRET` in `project-00` and `SERVICE_AUTH_SECRET` in `project-01` (see each `.env.example`). Start `project-01` first (`./mvnw spring-boot:run`), then `project-00` with the `dev` profile. `prod` never creates the client. Local development instructions and sample credentials are kept in untracked `frontend/angular/fundamentals/project-00/docs/dev/` notes.

The `prod` profile requires a persistent PostgreSQL database through `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`; it has no in-memory fallback. Google sign-in is off until `GOOGLE_SSO_ENABLED=true` and Google OAuth credentials and HTTPS callback URLs are configured. See the workspace [deployment guide](../../../../frontend/angular/fundamentals/project-00/docs/deployment.md) for Google Cloud and production proxy setup.

The application does not delete existing database contents automatically. For a deployment previously used with demos, rotate `JWT_SECRET` and remove local sample accounts before exposing it publicly.

## Architecture

Dependencies only point downwards.

| Layer | Package | Responsibility |
|---|---|---|
| Web | `web`, `web.dto` | REST controllers, request validation, DTO mapping, error mapping, OpenAPI docs |
| Security | `security` | JWT issuing/verification, user lookup, `@PreAuthorize` ownership rules (`@authz`) |
| Service | `service` | Transactional use cases and business rules |
| Repository | `repository` | Spring Data JPA interfaces with derived finders |
| Domain | `domain` | JPA entities that guard their own invariants |

### Domain model

- **Course** (`courseCode`, `title`, `credits`) has many **Lectures**, is taught by many **Professors** and has
  many enrolled **Students**.
- **Lecture** (`date`, `time`, `topic`, `room`) is delivered by one **Professor** who teaches the course and
  attended by many **Students** enrolled in it.
- **Professor** (`employeeId`, `name`, `department`) delivers many lectures and may review notes.
- **Student** (`studentId`, `name`, `enrollmentYear`) attends many lectures and writes notes.
- **UserAccount** (`username`, BCrypt `passwordHash`, `role`) is the login of a student or professor.
- **Notes** (`content`, `timestamp`, `isPrivate`) are written by a student for an attended lecture; a
  professor can grade them (0–100).

## REST API

| Resource | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/me` |
| Courses | `GET/POST /api/courses`, `GET/PUT/DELETE /api/courses/{courseCode}`, `PUT/DELETE /api/courses/{courseCode}/students/{studentId}`, `PUT/DELETE /api/courses/{courseCode}/professors/{employeeId}` |
| Professors | `GET/POST /api/professors`, `GET/PUT/DELETE /api/professors/{employeeId}` |
| Students | `GET/POST /api/students`, `GET/PUT/DELETE /api/students/{studentId}` |
| Lectures | `POST /api/lectures`, `GET /api/lectures?courseCode=` or `?employeeId=`, `GET/DELETE /api/lectures/{id}`, `PUT /api/lectures/{id}/schedule`, `PUT /api/lectures/{id}/professor`, `PUT/DELETE /api/lectures/{id}/attendees/{studentId}` |
| Notes | `POST /api/notes`, `GET /api/notes?lectureId=` or `?studentId=`, `GET /api/notes/pending-review`, `GET/PUT/DELETE /api/notes/{id}`, `PUT /api/notes/{id}/review` |

Errors use `application/problem+json` (RFC 9457):

| Status | Meaning |
|---|---|
| 400 | Validation failed or invalid input (e.g. notes for a lecture the student did not attend) |
| 401 | Missing, invalid or expired token, or wrong login credentials |
| 403 | The caller's role or identity is not allowed |
| 404 | Resource not found |
| 409 | Duplicate key, or the operation is not allowed in the current state (e.g. deleting a professor who still delivers lectures) |

## Security

Every endpoint except `/api/auth/register`, `/api/auth/login`, Google sign-in setup/completion, `/hello` and the Swagger/OpenAPI pages needs an
`Authorization: Bearer <token>` header. Tokens are HS256-signed JWTs (OAuth2 resource server) carrying the user
name as `sub` and the role in a `roles` claim; access tokens last no longer than 15 minutes. Login also issues hashed, rotating refresh credentials with an absolute ten-day maximum age. Google login validates OIDC server-side, keys accounts by Google's subject, and permits first-login Student or Professor choices but never Admin. Google-linked users have a backend-enforced limit of 10 creations per user per supported resource type.

`GET /api/auth/me` returns `username`, `role`, `displayName` and `pictureUrl`. `displayName` comes from the Google name refreshed at each Google sign-in, otherwise the student/professor name, otherwise the username. `pictureUrl` is the Google `picture` claim, kept only when it is an HTTPS URL on `googleusercontent.com`, and otherwise `null`. Browsers load it directly from Google with no referrer.

| Role | How the account is created | May |
|---|---|---|
| ADMIN ("superior") | Hard-defined by `ADMIN_USERNAME` / `ADMIN_PASSWORD` in `.env` | Manage courses and professors, register professors, assign teachers, delete students |
| PROFESSOR | Registered by the admin: `POST /api/professors` with a `password` | Schedule, reschedule, cancel lectures and take attendance for courses they teach; review notes of those courses |
| STUDENT | Registers themselves: `POST /api/auth/register` | Enroll in / leave courses (`PUT/DELETE /api/courses/{code}/students/{ownId}`), write and manage their own notes |

Usernames are the student id, the employee id or the admin name. Private notes are visible only to their author
and the professors teaching the course.

Try it in Swagger UI: call `POST /api/auth/login` with the admin credentials from your `.env`, copy
`accessToken`, click **Authorize** and paste it. Or with curl:

```powershell
$token = (curl.exe -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" `
  -d "{\"username\":\"admin\",\"password\":\"$env:ADMIN_PASSWORD\"}" | ConvertFrom-Json).accessToken
curl.exe localhost:8080/api/courses -H "Authorization: Bearer $token"
```

> Set `ADMIN_PASSWORD` in your shell (or paste it) for the snippet above; never commit real values.

## Tests

```powershell
.\mvnw.cmd test
```

| Layer | Tests | Style |
|---|---|---|
| Domain | `domain/*Test` | Plain unit tests |
| Repository | `repository/*RepositoryTest` | `@DataJpaTest` against H2 |
| Service | `service/*ServiceTest` | Mockito unit tests |
| Security | `security/*Test` | Unit tests for token issuing, user lookup and the `@authz` rules |
| Web | `web/*E2ETest` | Full app on a random port, real HTTP via `RestTestClient` with real tokens per role (includes the OpenAPI docs and `AuthE2ETest` for 401/403 rules) |
