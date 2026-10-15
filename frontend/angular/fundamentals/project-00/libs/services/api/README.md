# services-api

Platform-neutral typed client (`UniversityApi`) for the University REST API (`/v3/api-docs`).

```ts
const api = new UniversityApi('http://localhost:8080', () => token, fetch);
await api.login({ username, password });
```

Errors reject with `ApiError` (numeric `.status`, ProblemDetail in `.problem`); 401 rejects with `UnauthorizedError` (subclass). 204 operations resolve `undefined`.

`GET /api/lectures` and `/api/notes` are overloaded server-side (the published OpenAPI wrongly marks all params required); each filter has its own method.

Run `nx test services-api`.