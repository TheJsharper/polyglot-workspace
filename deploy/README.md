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
   `GOOGLE_FRONTEND_URL=https://<pages-domain>/sso/callback`. Copy the service's **Deploy Hook** URL.
3. **Cloudflare Pages**: create a project (Direct Upload). In its settings add the variable `API_ORIGIN`
   (the Render URL, no trailing slash). Register the Google redirect URI above in Google Cloud.
4. **GitHub** → Settings → Environments → `production` (optionally require a reviewer), then add:
   - Secrets: `CLOUDFLARE_API_TOKEN` (Pages edit permission), `CLOUDFLARE_ACCOUNT_ID`, `RENDER_DEPLOY_HOOK_URL`.
   - Variable: `CLOUDFLARE_PAGES_PROJECT` (the Pages project name).

## How requests flow

The browser only talks to the Pages domain. `deploy/cloudflare/functions` proxies `/api/*`, `/oauth2/*` and
`/login/oauth2/*` to Render, so cookies stay first-party and CORS is not needed.

## Notes

- Free Render and Neon instances sleep when idle; the first request can take up to a minute.
- Production has no demo-key or manager routes, so `project-01` is not deployed.
