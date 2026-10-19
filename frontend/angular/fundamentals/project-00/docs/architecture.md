# Angular workspace architecture

This workspace uses Nx 23 and Angular 22 for a browser application, reusable libraries, and an ng-native **iOS/Android application**. The browser app focuses on the University screens. The native app is a separate Expo/Metro application with its own Home and Dashboard screens.

## Web request path

1. [`apps/web-app/src/app/app.config.ts`](../apps/web-app/src/app/app.config.ts) calls `provideShell()`, which installs the router, University-only menu configuration and root NgRx providers.
2. [`libs/shells/main/src/lib/shell.component.ts`](../libs/shells/main/src/lib/shell.component.ts) connects the store to the header, sidenav and `<router-outlet>`. The shell owns wiring and menu items, not the header's markup.
3. [`libs/shells/main/src/lib/shell.routes.ts`](../libs/shells/main/src/lib/shell.routes.ts) lazy-loads login and registration, and maps each University sidebar item (`/university/students|professors|courses|lectures|notes`) to a **Native Federation remote** with `loadRemoteModule(<name>, './Routes')`. `/` and `/university` redirect to Students; the University route guard sends signed-out users to login. If a remote cannot be reached, the route renders `RemoteUnavailableComponent` instead of breaking the shell.

## Micro-frontends (Native Federation)

`web-app` is a dynamic Native Federation host. [`main.ts`](../apps/web-app/src/main.ts) calls `initFederation('federation.manifest.json')` before bootstrapping; [`public/federation.manifest.json`](../apps/web-app/public/federation.manifest.json) maps remote names to their `remoteEntry.json` URLs, so deployments can point at other origins without rebuilding the host. The `es-module-shims` polyfill is copied from `node_modules` as an asset and loaded by a deferred classic `<script>` (preceded by `<script type="esms-options">{"shimMode":true}</script>`) in each `index.html`, not via `polyfills`, which keeps the Vite dev server from re-analysing its intentional dynamic `import()` calls.

| Sidebar item | Remote project | Folder | Dev port |
| --- | --- | --- | --- |
| Students | `remote-students` | `apps/remotes/students` | 4301 |
| Professors | `remote-professors` | `apps/remotes/professors` | 4302 |
| Courses | `remote-courses` | `apps/remotes/courses` | 4303 |
| Lectures | `remote-lectures` | `apps/remotes/lectures` | 4304 |
| Notes & reviews | `remote-notes` | `apps/remotes/notes` | 4305 |

Each remote exposes `./Routes` (`src/app/remote.routes.ts`), which registers `provideUniversityState()` on its route and renders one page inside the shared `UniversityFrameComponent` from `libs/views/university`. Angular, NgRx and the workspace session/state/view libraries are shared singletons (`shareAll` + `tsconfig.federation.json`), so remotes use the host's Store and in-memory session. The host-only shell and layout libraries are deliberately skipped from sharing so loadRemoteModule runs in the Native Federation instance initialised by `main.ts`. Each remote also runs standalone (with its own login route) for isolated development.

## Native application

[`apps/mobiles/app`](../apps/mobiles/app) is the Nx project `app`, generated with `@ng-native/nx`. Angular components render native views through ng-native and React Native Fabric; Expo/Metro serves and bundles the app. Its native shell provides a left drawer, routed main content, and Home/Dashboard bottom tabs. It reuses the platform-neutral menu and summary data in `libs/services/core`; browser components and the Material/CDK layout are not native dependencies.

Run `nx run app:start` for Metro, `nx run app:test` for its Vitest tests, or `nx run app:typecheck` for Angular template/type checking. See the [mobile guide](../apps/mobiles/README.md) and app-specific [AGENTS.md](../apps/mobiles/app/AGENTS.md) for platform requirements and native coding boundaries.

### Browser, native and shared libraries

| Role | Project | How it runs |
| --- | --- | --- |
| Browser app | `apps/web-app` (`web-app`) | Standalone Angular application, served at port 4300. |
| Native app | `apps/mobiles/app` (`app`) | Expo/Metro app for iOS and Android with its own native routes and screens. |
| Shared libraries | `libs/` | Compiled into their consuming app; framework-neutral menu, stats, theme and API code can be shared where platform constraints allow. |

## Folder responsibilities

| Folder | Ownership and dependency direction |
| --- | --- |
| `apps/web-app/` | Browser bootstrap, application build/serve configuration and shared global styles. |
| `apps/remotes/*/` | One Native Federation remote per University sidebar item (`type:remote`); exposes `./Routes` and owns that page component. |
| `apps/mobiles/app/` | Native ng-native/Expo application; [mobile guide](../apps/mobiles/README.md) documents commands, platform requirements and boundaries. |
| `libs/shells/` | Composition root: the five University sidebar entries, remote routes, root providers, connection to layout. |
| `libs/layouts/` | Responsive toolbar/sidenav presentation; receives state via inputs, emits user actions via outputs. |
| `libs/views/university/` | Browser login/registration screens and the shared `UniversityFrameComponent` (heading, account, notices, filter) used by every remote. |
| `libs/shared/` | Shared UI primitives and the Material 3 theme. |
| `libs/services/state/` | Angular NgRx features/providers/effects for global application state. |
| `libs/services/university-state/` | NgRx `auth` mirror of `UniversitySession` and the `university` feature (collections, selection, per-resource view state, filters, messages) with effects that call the API. |
| `libs/services/core/` | Framework-free menu/theme models and helpers, including Home/Dashboard summary data retained for native screens. |

Projects carry `type:*` tags; [`eslint.config.mjs`](../eslint.config.mjs) enforces allowed imports with `@nx/enforce-module-boundaries`. Add new projects with an appropriate tag and a matching constraint rather than introducing back-edges.

## Role-based access

`libs/shells/main/src/lib/shell.menu.ts` filters the sidebar by role and `roleGuard` in `shell.routes.ts` redirects forbidden routes to the first allowed item.

| Role | Sidebar items |
| --- | --- |
| Student | Courses, Lectures, Notes (own notes plus other students' public notes) |
| Professor | My Students (only students enrolled in courses they teach), Courses, Lectures, Notes (review) |
| Admin | All items |

## University API

The Spring service in `backend/spring/fundamentals/project-00` serves the University API at `http://localhost:8080`; its live contract is `/v3/api-docs` and interactive reference is `/swagger-ui/index.html`. The browser app provides University screens for students, professors, courses, lectures and notes; native Home and Dashboard screens remain separate. Login and registration are available without a session; resource screens require one. Public registration creates a student or professor account; admin registration needs an existing admin token. The server enforces roles and resource ownership even when a UI action is offered.

`libs/services/api` holds typed, platform-neutral contracts and requests. `libs/services/session` holds a per-app, in-memory bearer session: logout, expiry or an unauthorized API response clears it; there is no token persistence or refresh endpoint. `libs/views/university` owns the browser login/registration screens and shared frame; the resource pages live in the remotes. All web University data (records, selections, choices, notes, filters, loading/messages and the signed-in user) is held in NgRx via `libs/services/university-state`; components only select and dispatch. The native app uses native views and the same API/session libraries but not web UI, Material or browser-only NgRx effects.

For local web development, [`proxy.conf.json`](../apps/web-app/proxy.conf.json) forwards `/api` from the web host to `http://localhost:8080`, avoiding browser CORS configuration. Production must serve `/api` on the web origin via reverse proxy; the development proxy is not baked into the production bundle. The native app needs a configurable host-reachable API origin (see the [mobile guide](../apps/mobiles/README.md)); `localhost` on a physical phone is not the development machine.

## State, UI and checks

[`provideAppState()`](../libs/services/state/src/lib/provide-app-state.ts) registers NgRx Store (`layout`, `navigation`, router reducer), functional Effects, Router-Store and DevTools in dev mode. The layout feature owns sidenav/theme; the navigation feature loads the shell's injected menu config. Effects restore the theme from `localStorage`, apply the `dark-theme` class on `<html>` and persist toggles. [`_theme.scss`](../libs/shared/ui/src/styles/_theme.scss) defines Material 3 light/dark tokens used by the browser app. The layout uses Angular Material toolbar, sidenav, list, icons and buttons; CDK breakpoints switch the sidenav to an overlay on handsets.

From the workspace directory, use Node >= 22.22.3 and run:

```text
npx nx run-many -t lint test -p web-app shells-main layouts-main views-university
npx nx run web-app:build
npm start
```

Use narrower project targets when editing a single library. The browser build depends on lint; Angular library tests use Vitest/Analog, and `libs/services/core` uses Vitest. The root `npm test` script is a placeholder and is not the project test runner. The browser app serves at `http://localhost:4300/`; port 4200 is occupied by Docker on this machine, so the browser app is pinned to 4300 and the remotes use 4301-4305. A remote that is not running shows an "unavailable" notice in the content area. Stop old dev servers before reusing the port. Start the host with `serve` (or `npm start`, which serves the host and all five remotes), never `serve-original`: only `serve` wraps the dev server with the Native Federation builder that publishes the host `remoteEntry.json` and its shared Angular chunks; with `serve-original` the remotes load a second Angular copy and show "unavailable".
