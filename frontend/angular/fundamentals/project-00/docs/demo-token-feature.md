# Demo-token feature log

Recruiter demo access for the University app. Operational how-to: [deployment.md](./deployment.md).

## Requirements
- Demo tokens exist only in the `demo` environment; `dev` and `prod` use normal login and have no token endpoints.
- Tokens expire (default 10 days), are rate limited, and are redeemed on a validation page (`/demo`) by HR.
- Free public hosting plan (Cloudflare Pages, Render, Neon).

## Evolution
1. **DB-backed tokens (rejected):** admin generate/revoke endpoints, `demo_tokens` table, in-memory H2 wiped on restart, so tokens became "invalid or expired".
2. **Reset to public/private key (final):** stateless ES256 JWTs signed offline by `backend/scripts/demo-token.mjs`; the server holds only the public key (`DEMO_PUBLIC_KEY`) so it can verify but never mint.
3. **Simplification:** `GET /api/demo/status` lets the frontend detect demo mode (no config file); `backend/scripts/demo-local.ps1` runs keys, token and backend in one command.

## Design
- Claims: `jti`, `iat`, `nbf`, `exp`, optional `label`. Expiry is fixed at issue time ("10 days after first use" needs server state).
- `POST /api/demo/redeem {token, persona}` -> read-only session for seeded `stud-anna` / `prof-ada`, capped at the token expiry.
- Read-only: `DemoSessionFilter` returns 403 for non-GET/HEAD/OPTIONS; 60 req/min per demo user (429).
- Failed redeems: 5 per IP per 15 min, then 429 (in-memory `SlidingWindowLimiter`).
- Revocation: add `jti` to `DEMO_REVOKED_JTIS` and restart; rotate keys to revoke all.
- Gating: `@ConditionalOnProperty(app.demo.enabled=true)` set only by `application-demo.properties`.

## Frontend
`DEMO_ENABLED` token set in `bootstrap.ts` from `/api/demo/status`; `demoGuard` on the `/demo` route; `DemoComponent` (token textarea + persona); `UniversitySession.redeemDemo`.

## Verification
Backend `clean test` (incl. `DemoApiE2ETest`, `DemoDisabledE2ETest`), frontend lint/test, `web-app:build`, and a manual redeem (read OK, write 403, tampered token 401).

## Known limits
- Public deployment still needs the remotes hosted and `federation.manifest.json` URLs updated (currently `localhost:4301-4305`).
- Rate limiters are per instance.
- A stale process on port 8080 causes confusing 401s; stop it or use `-Port`.
