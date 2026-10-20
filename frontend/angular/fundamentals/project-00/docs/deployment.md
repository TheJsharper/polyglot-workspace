# University app deployment

## Backend

Deploy the Spring API with `SPRING_PROFILES_ACTIVE=prod`. The production profile disables the H2 console and Swagger/OpenAPI endpoints, and the Docker image uses it by default. Configure the database and secrets through environment variables:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `ADMIN_USERNAME`, `ADMIN_PASSWORD`
- `JWT_SECRET` (at least 32 bytes)

The `prod` profile does not create sample users or enable demo-token endpoints. Create accounts through the normal registration flow or the configured admin account. Never deploy the `dev` profile: it binds to loopback for local work, enables development tooling, seeds shared-password accounts, and enables demo-token access.

Development-only recruiter demo access is not supported in production. Its local workflow and seed-account details are intentionally kept in untracked `docs/dev/` notes.

## Frontend

The browser host must serve `/api/*` on its own origin, usually by proxying those requests to the backend. The local Angular development proxy is not included in production builds.

The Native Federation host also needs reachable URLs for all five remotes in `apps/web-app/public/federation.manifest.json`. The checked-in development manifest points to `localhost:4301-4305`; replace those values with deployed remote URLs before building the public host.

## Existing deployments

This deployment does not modify database contents. Before deploying it over a database previously used for demo-token access:

1. Rotate `JWT_SECRET` to invalidate previously issued access tokens.
2. Remove any local sample accounts or reset their passwords to unique, strong values before exposing the service. Development sample passwords must not be used on a public deployment.
3. Inspect the database and remove or reset any other demo-only records as appropriate.

The application intentionally does not perform destructive cleanup automatically. Production does not read `DEMO_PUBLIC_KEY` or `DEMO_REVOKED_JTIS`.
