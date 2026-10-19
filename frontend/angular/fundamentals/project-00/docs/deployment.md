# Recruiter demo: signed tokens and free deployment

## Demo-token flow (public/private key)

Demo tokens exist **only in the `demo` environment**. Backend: `SPRING_PROFILES_ACTIVE=demo` (sets `app.demo.enabled=true`); in `dev` and `prod` the `/api/demo/*` endpoints and the read-only filter do not exist. The frontend needs no flag: at startup it calls `GET /api/demo/status`, which only answers in the demo profile; when it does, the `/demo` page and the "Recruiter demo" login link appear, otherwise `/demo` redirects to `/login`. Dev and prod use normal login.

**Local test in two commands**

```powershell
# terminal 1 (backend, JDK 25): creates keys on first run, copies a fresh token to the clipboard, starts the demo backend
cd backend\spring\fundamentals\project-00
.\scripts\demo-local.ps1
# terminal 2 (frontend)
cd frontend\angular\fundamentals\project-00
npm start
```
Then open http://localhost:4300/demo and paste the token (Ctrl+V).

Tokens are **ES256 JWTs signed offline**. The private key stays with you; the server only holds the public key, so it can verify tokens but can never mint them. There is no database table and no admin endpoint, so tokens survive restarts.

```powershell
cd backend\spring\fundamentals\project-00
node scripts\demo-token.mjs keys                                  # once: writes .demo-keys\private.pem + public.pem (git-ignored)
node scripts\demo-token.mjs issue --label "ACME HR" --days 10     # prints a token (and its jti on stderr)
```

1. Give the server the public key: `DEMO_PUBLIC_KEY` = contents of `.demo-keys\public.pem` (PEM; `\n` escapes are accepted).
2. HR opens `/demo`, pastes the token and picks **Student** or **Professor**.
3. `POST /api/demo/redeem` verifies the signature, `nbf` and `exp`, then returns a **read-only** session for the seeded persona (`stud-anna` / `prof-ada`) that never outlives the token.
4. Validity is fixed at issue time (`--days`, default 10; `--start-in-days` delays the start). "10 days after first use" is not possible without server state.
5. Revoke one token without rotating keys: add its `jti` to `DEMO_REVOKED_JTIS` (comma-separated) and restart. To invalidate everything, rotate the key pair.

Local test: `scripts\demo-local.ps1` does all of this for you (see above).

Rate limits: 5 failed redeems per IP per 15 minutes (then 429) and 60 requests per minute per demo user (in memory).

## Free hosting (subdomains: `*.onrender.com`, `*.pages.dev`)

| Part | Service | Notes |
| --- | --- | --- |
| Database | Neon (free Postgres) | Holds the seeded demo data; tokens themselves need no database. |
| Backend | Render web service (Docker) | Uses `Dockerfile`; free tier sleeps when idle. |
| Frontend | Cloudflare Pages | Proxy `/api/*` to the backend (see `apps/web-app/public/_redirects.example`, copy to `_redirects`). |

Backend environment on Render (profile `demo` is the Docker default):
`DB_URL` (`jdbc:postgresql://HOST/DB?sslmode=require`), `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `JWT_SECRET` (long random value), `DEMO_PUBLIC_KEY`, optional `DEMO_REVOKED_JTIS`.

The `demo` profile seeds the demo courses, professors and students (password `Password-123`) when the database is empty.

## Known gap

`apps/web-app/public/federation.manifest.json` points at `localhost:4301-4305`. For a public site, build and host each remote (e.g. as extra Cloudflare Pages projects or under one site) and update the manifest URLs before building the host.
