# Proposal: Recruiter Demo Login

## Intent
Let managers give recruiters realistic access without exposing real university records or requiring self-registration.

## Scope

### In Scope
- Manager-labelled, revocable, single-use keys stored hashed in project-00 Postgres; atomic consume and seven-day unredeemed expiry.
- Recruiter-selected STUDENT or PROFESSOR view; dedicated DEMO authority and 60-minute access-only session.
- Production-seeded isolated dataset capped at 10 students, professors, courses, lectures, and notes; strictly read-only.
- Cloudflare Pages Functions forward client IP with a shared-secret header; backend trusts it only after secret validation.

### Out of Scope
- Public recruiter self-service, demo writes, production project-01, paid tiers, or refresh cookies.

## Capabilities

### New Capabilities
- `demo-key-issuance`: Manager key issue, label, expiry, revoke, and one-time use.
- `demo-session`: Persona selection and 60-minute DEMO authorization.
- `demo-data-isolation`: Bounded seeded data separated from real records and writes.
- `trusted-client-ip`: Secret-authenticated proxy IP forwarding and abuse limits.

### Modified Capabilities
- None; no existing OpenSpec specs exist.

## Approach
Move key lifecycle and atomic consumption into project-00 Postgres; retain project-01 for development only. Issue DEMO-scoped tokens and route reads to isolated seed data. Keep production gated by `app.demo.enabled`; trust forwarded IP only with the Pages Function secret. Add no services; accept Render free-tier cold starts of 30 to 60 seconds.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `backend/spring/fundamentals/project-00/src/main/java/com/example/project00/web/{DemoController,DemoKeyController}.java`, `backend/spring/fundamentals/project-00/src/main/java/com/example/project00/service/{DemoTokenService,DemoKeysClient}.java` | Modified | In-process key lifecycle and redemption. |
| `backend/spring/fundamentals/project-00/src/main/java/com/example/project00/security/{DemoSessionFilter,AuthorizationRules,SecurityConfig,TokenService}.java`, `backend/spring/fundamentals/project-00/src/main/java/com/example/project00/config/DemoDataInitializer.java`, `backend/spring/fundamentals/project-00/src/main/resources/application*.properties`, `src/main/resources/db/migration/` (additive schema) | Modified | DEMO isolation, read-only enforcement, seed data, gating, additive persistence. |
| `frontend/angular/fundamentals/project-00/libs/views/university/src/lib/{demo-keys.component.ts,auth.component.ts}`, `frontend/angular/fundamentals/project-00/libs/services/api/src/lib/university-api.ts` | Modified | Manager and recruiter flows. |
| `deploy/cloudflare/functions/_proxy.js`, `deploy/cloudflare/functions/api/[[path]].js`; `backend/spring/fundamentals/project-01` | Modified | Authenticated IP forwarding; development-only service. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Demo leaks records or permits writes | Medium | Dedicated authority, isolated queries, endpoint enforcement, integration tests. |
| Spoofed proxy header or secret exposure | Medium | Trust only secret-validated forwarding; store secret in deployment configuration. |
| Cold start delays first visit | High | Set expectations; remain within free tiers. |

## Rollback Plan
Disable production with `app.demo.enabled=false`; revert the feature merge with `--no-ff`. Keep schema changes additive and preserve data.

## Dependencies
- Cloudflare Pages, Render, and Neon free-tier deployment secrets; no new services.

## Success Criteria
- [ ] Managers issue/revoke labelled keys; concurrent redemption succeeds at most once.
- [ ] Sessions expire at 60 minutes, set no refresh cookie, and cannot write.
- [ ] Personas see only isolated seeded data capped at 10 records per resource.
- [ ] Only secret-authenticated Pages forwarding supplies the production client IP.
- [ ] Production gating works; JUnit/MockMvc tests pass.