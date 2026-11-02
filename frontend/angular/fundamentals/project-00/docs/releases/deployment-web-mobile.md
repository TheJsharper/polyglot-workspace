# Releasing web and mobile independently

Status: phases 1 and 2 are implemented. Phases 3 to 5 need store accounts and are not started.

## Goal

Web, API and mobile ship to different places on different schedules. A change to one must not release, or block, the others.

| Unit | Artifact | Target | Release trigger |
| --- | --- | --- | --- |
| Web | static `site/` | Cloudflare Pages | every merge to `main` that touches web code |
| API | Docker image | Render | every merge to `main` that touches `backend/spring` |
| Mobile | AAB (Android), IPA (iOS) | Google Play, App Store | a deliberate, versioned release (phase 3 onward) |

## Why one workflow was not enough

The old `ci-cd.yml` ran on every push to `main` and deployed web and API each time. A mobile-only commit redeployed the website. The mobile app was not tested in CI at all. The `frontend-test` job also skipped `services-core`, `services-state`, `services-session` and `shared-ui`.

## What changed (phases 1 and 2)

`ci-cd.yml` is split into three workflows. Job ids are unchanged (`backend-test`, `frontend-test`, `deploy-frontend`, `deploy-api`) so any existing references keep working. `mobile-test` is new.

| Workflow | Runs when these paths change | Jobs |
| --- | --- | --- |
| `api.yml` | `backend/spring/**` | `backend-test`, then `deploy-api` on `main` |
| `web.yml` | everything under `frontend/.../project-00` except `apps/mobiles`, `docs` and `*.md`; `deploy/cloudflare/**` | `frontend-test`, then `deploy-frontend` on `main` |
| `mobile.yml` | `apps/mobiles/**`, `libs/services/api/**`, `libs/services/session/**`, root Nx and npm files | `mobile-test` |

The mobile paths come from the Nx dependency graph: `app` depends only on `services-api` and `services-session`. `services-core` is web-only. A change to a shared library runs web and mobile CI. A change to `apps/mobiles` runs mobile only.

### Rules that keep the three apart

- **Concurrency is per workflow.** Each file has its own group. With one shared group, a web run would cancel an API run on the same ref.
- **Deploys are never cancelled mid-run.** `cancel-in-progress` is on for pull requests only. A second push to `main` waits instead of killing an in-flight deploy.
- **Deploy jobs only run on `push` to `main`**, after their own tests pass, in the `production` environment.
- **Web no longer waits for backend tests.** The two are independent. Deploy the API first when a web change needs a new endpoint.

## Compatibility between releases

Web and API deploy within minutes. Installed mobile apps stay on old versions for months. So:

- API changes must stay backward compatible, or be versioned (`/api/v1`). Remove a field only after the old clients are gone.
- Add a minimum-supported-version check to the app before the first store release, so an old build can show "update required".
- Order for a breaking change: API first (compatible), then web and mobile, then clean up the old path.

## Remaining phases

### Phase 3: Android internal testing (needs a Google Play Console account)

- Add `eas.json` and an Expo project (`EXPO_TOKEN`), or build with Gradle on `ubuntu-latest`.
- New `mobile-release.yml`, triggered by a `mobile-v*` tag or `workflow_dispatch`, never by a push to `main`.
- Run in a protected `production-mobile` environment with a required reviewer.
- Secrets live only in that environment: upload keystore, Play service-account JSON.
- Ship to the internal testing track first. Promote to closed, then production by hand.

### Phase 4: iOS and TestFlight (needs an Apple Developer account)

- Needs a macOS runner or EAS cloud builds.
- Secrets: App Store Connect API key, signing certificate and provisioning profile (or EAS-managed).
- TestFlight first, then App Store review.

### Phase 5: over-the-air JS fixes (optional)

- EAS Update can ship JavaScript-only fixes without store review.
- Native changes (new native modules, `app.json` plugins) always need a full store build.
- Pin each update to a runtime version so it never reaches an incompatible binary.

### Versioning

- Web and API: no version number, the commit is the release.
- Mobile: semver in `app.json` plus store build numbers (`versionCode`, `buildNumber`) that only increase. Tag releases as `mobile-vX.Y.Z`.

## Known risks

- **Required status checks.** If branch protection requires `backend-test` or `frontend-test`, a path-filtered workflow that does not run leaves the check pending forever. Today direct pushes to `main` work, so this is not an issue. Revisit it if protection is added.
- **Path filters are a hand-kept list.** A new shared library that mobile starts using must be added to `mobile.yml`. Check `npx nx graph` when mobile gains a dependency.
- **`workflow_dispatch` is not wired up** for web or API yet. To redeploy without a code change, push a commit that touches the workflow file.

## Verification

- Clean clone: `npm ci` in the project root and in `apps/mobiles/app`, then `npx nx run-many -t typecheck test -p app`.
- Every workflow file linted with `actionlint`.
- After the push: the three workflows run once (each includes its own file in `paths`). A later docs-only commit starts none of them.
