# University app deployment

## Backend

Deploy the Spring API with `SPRING_PROFILES_ACTIVE=prod`. The production profile disables the H2 console and Swagger/OpenAPI endpoints, and the Docker image uses it by default. Production requires a persistent PostgreSQL database; it intentionally has no in-memory fallback. Configure the database and secrets through environment variables:

- `DB_URL` (PostgreSQL JDBC URL with TLS, e.g. `jdbc:postgresql://db.example.com:5432/university?sslmode=require`), `DB_USERNAME`, `DB_PASSWORD`
- `ADMIN_USERNAME`, `ADMIN_PASSWORD`
- `JWT_SECRET` (at least 32 bytes)
- `GOOGLE_SSO_ENABLED=true`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- `GOOGLE_REDIRECT_URI=https://<api-origin>/login/oauth2/code/google`
- `GOOGLE_FRONTEND_URL=https://<web-origin>/sso/callback`

Create a Google OAuth Web application client. Register the exact backend callback URI and web origin in Google Cloud; keep the client secret in the deployment secret manager. Google SSO is disabled until explicitly enabled. Production refuses to enable it unless both callback and frontend URLs use HTTPS.

The backend uses Google authorization-code login and validates Google's OIDC response. First-time users choose Student or Professor in the web app; the API rejects Admin self-assignment. Account identity is keyed by Google's subject identifier, not email matching. New Google-linked accounts are capped at 10 successful record creations per user for each resource type the user's role is allowed to create. The backend enforces these limits transactionally.

Access tokens last at most 15 minutes. The browser receives a `Secure`, `HttpOnly`, `SameSite=Lax` refresh cookie, rotated on refresh and capped at an absolute 10-day session age. The host and reverse proxy must route `/api/*`, `/oauth2/*`, and `/login/oauth2/*` to the Spring API on the same site; the OAuth callback and refresh cookie must remain HTTPS in production.

The production profile currently uses Hibernate `ddl-auto=update`; this release does not include versioned database migrations. Back up PostgreSQL and validate schema changes against a staging copy before deployment. Add a versioned migration process before relying on automatic schema evolution for long-lived production data. Do not schedule app restarts as a substitute for session expiry; sessions expire and rotate independently of process lifetime.

The `prod` profile does not create sample users or enable demo-token endpoints. Create accounts through the normal registration flow or the configured admin account. Never deploy the `dev` profile: it binds to loopback for local work, enables development tooling, seeds shared-password accounts, and enables demo-token access.

Development-only recruiter demo access is not supported in production. Its local workflow and seed-account details are intentionally kept in untracked `docs/dev/` notes.

## Frontend

The browser host must serve `/api/*`, `/oauth2/*`, and `/login/oauth2/*` on its own origin, usually by proxying those paths to the backend. The local Angular development proxy is not included in production builds.

The Native Federation host also needs reachable URLs for all five remotes in `apps/web-app/public/federation.manifest.json`. The checked-in development manifest points to `localhost:4301-4305`; replace those values with deployed remote URLs before building the public host.

## Existing deployments

This deployment does not modify database contents. Before deploying it over a database previously used for demo-token access:

1. Rotate `JWT_SECRET` to invalidate previously issued access tokens.
2. Remove any local sample accounts or reset their passwords to unique, strong values before exposing the service. Development sample passwords must not be used on a public deployment.
3. Inspect the database and remove or reset any other demo-only records as appropriate.

The application intentionally does not perform destructive cleanup automatically. Production does not read `DEMO_PUBLIC_KEY` or `DEMO_REVOKED_JTIS`.
