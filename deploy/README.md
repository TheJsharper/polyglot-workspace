# Deploying (free tier)

Push to `main` runs `.github/workflows/ci-cd.yml`: tests both backends, lints/tests/builds the frontend, then
deploys. Pull requests only run the tests.

## One-time setup

1. **Neon** (free Postgres): create a project and copy the JDBC URL, user and password.
2. **Render** (free web service, Docker): new Web Service from this repo, root directory
   `backend/spring/fundamentals/project-00`, runtime Docker, and turn **Auto-Deploy off** (GitHub Actions triggers it).
   Environment variables (see `.env.example`): `DB_URL` (`jdbc:postgresql://…?sslmode=require`), `DB_USERNAME`,
   `DB_PASSWORD`, `JWT_SECRET` (32+ chars), `ADMIN_USERNAME`, `ADMIN_PASSWORD`, and for Google SSO
   `GOOGLE_SSO_ENABLED`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`,
   `GOOGLE_REDIRECT_URI=https://<pages-domain>/login/oauth2/code/google`,
   `GOOGLE_FRONTEND_URL=https://<pages-domain>/sso/callback`. Add `PROXY_SHARED_SECRET` as a secret environment
   variable; the same value must be configured for Cloudflare Pages Functions. Copy the service's **Deploy Hook** URL.
3. **Cloudflare Pages**: create a project (Direct Upload). In its settings add the variable `API_ORIGIN`
   (the Render URL, no trailing slash) and the secret `PROXY_SHARED_SECRET` (same value as Render). The Functions
   forward Cloudflare's `CF-Connecting-IP` through dedicated headers; Render trusts it only when the shared secret
   matches. Register the Google redirect URI above in Google Cloud.
4. **GitHub** → Settings → Environments → `production` (optionally require a reviewer), then add:
   - Secrets: `CLOUDFLARE_API_TOKEN` (Pages edit permission), `CLOUDFLARE_ACCOUNT_ID`, `RENDER_DEPLOY_HOOK_URL`.
   - Variable: `CLOUDFLARE_PAGES_PROJECT` (the Pages project name).

## How requests flow

The browser only talks to the Pages domain. `deploy/cloudflare/functions` proxies `/api/*`, `/oauth2/*` and
`/login/oauth2/*` to Render, so cookies stay first-party and CORS is not needed.

## Recruiter demo logins

Give each recruiter their own short-lived, read-only login. No dev mode or `project-01` is needed.

1. Sign in on `https://polyglot-university.pages.dev` as `university_admin` (or any ADMIN/MANAGER).
2. Open **Demo logins** in the sidebar.
3. Pick a persona (Student, Professor or Admin), a duration (1–30 days, default 7) and an optional label
   such as the recruiter's company. Click **Generate login**.
4. Copy the username and password. The password is shown only once. Send both to the recruiter.
5. The recruiter signs in on the normal login page. They see only the isolated `demo-*` sample data, every
   change is rejected with 403, and the login stops working when it expires.
6. Click **Revoke** to end access early. Expired logins are cleaned up automatically.

Optional Render variables: `APP_DEMOLOGINS_ENABLED` (default `true`), `APP_DEMOLOGINS_MAXACTIVE` (default
`25`) and `APP_DEMOLOGINS_MAXREQUESTSPERMINUTE` (default `120`).

## Notes

- Free Render and Neon instances sleep when idle; the first request can take up to a minute.
- Production has no demo-key or manager routes, so `project-01` is not deployed. Use demo logins instead.
