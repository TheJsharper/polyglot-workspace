# Angular workspace architecture

This workspace uses Nx 23 and Angular 22 for a browser application, reusable libraries, and an ng-native **iOS/Android application**. The browser app focuses on the University screens. The native app is a separate Expo/Metro application with its own Home and Dashboard screens.

## Web request path

1. [`apps/web-app/src/app/app.config.ts`](../apps/web-app/src/app/app.config.ts) calls `provideShell()`, which installs the router, University-only menu configuration and root NgRx providers.
2. [`libs/shells/main/src/lib/shell.component.ts`](../libs/shells/main/src/lib/shell.component.ts) connects the store to the header, sidenav and `<router-outlet>`. The shell owns wiring and menu items, not the header's markup.
3. [`libs/shells/main/src/lib/shell.routes.ts`](../libs/shells/main/src/lib/shell.routes.ts) lazy-loads login, registration and University routes. `/` redirects to `/university`; the University route guard sends signed-out users to login.

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
| `apps/mobiles/app/` | Native ng-native/Expo application; [mobile guide](../apps/mobiles/README.md) documents commands, platform requirements and boundaries. |
| `libs/shells/` | Composition root: browser menu entries, lazy routes, root providers, connection to layout. |
| `libs/layouts/` | Responsive toolbar/sidenav presentation; receives state via inputs, emits user actions via outputs. |
| `libs/views/university/` | Browser University screens and lazy route definitions. |
| `libs/shared/` | Shared UI primitives and the Material 3 theme. |
| `libs/services/state/` | Angular NgRx features/providers/effects for global application state. |
| `libs/services/core/` | Framework-free menu/theme models and helpers, including Home/Dashboard summary data retained for native screens. |

Projects carry `type:*` tags; [`eslint.config.mjs`](../eslint.config.mjs) enforces allowed imports with `@nx/enforce-module-boundaries`. Add new projects with an appropriate tag and a matching constraint rather than introducing back-edges.

## University API

The Spring service in `backend/spring/fundamentals/project-00` serves the University API at `http://localhost:8080`; its live contract is `/v3/api-docs` and interactive reference is `/swagger-ui/index.html`. The browser app provides University screens for students, professors, courses, lectures and notes; native Home and Dashboard screens remain separate. Login and registration are available without a session; resource screens require one. Public registration creates a student or professor account; admin registration needs an existing admin token. The server enforces roles and resource ownership even when a UI action is offered.

`libs/services/api` holds typed, platform-neutral contracts and requests. `libs/services/session` holds a per-app, in-memory bearer session: logout, expiry or an unauthorized API response clears it; there is no token persistence or refresh endpoint. `libs/views/university` owns the browser screens. The native app uses native views and the same API/session libraries but not web UI, Material or browser-only NgRx effects.

For local web development, [`proxy.conf.json`](../apps/web-app/proxy.conf.json) forwards `/api` from the web host to `http://localhost:8080`, avoiding browser CORS configuration. Production must serve `/api` on the web origin via reverse proxy; the development proxy is not baked into the production bundle. The native app needs a configurable host-reachable API origin (see the [mobile guide](../apps/mobiles/README.md)); `localhost` on a physical phone is not the development machine.

## State, UI and checks

[`provideAppState()`](../libs/services/state/src/lib/provide-app-state.ts) registers NgRx Store (`layout`, `navigation`, router reducer), functional Effects, Router-Store and DevTools in dev mode. The layout feature owns sidenav/theme; the navigation feature loads the shell's injected menu config. Effects restore the theme from `localStorage`, apply the `dark-theme` class on `<html>` and persist toggles. [`_theme.scss`](../libs/shared/ui/src/styles/_theme.scss) defines Material 3 light/dark tokens used by the browser app. The layout uses Angular Material toolbar, sidenav, list, icons and buttons; CDK breakpoints switch the sidenav to an overlay on handsets.

From the workspace directory, use Node >= 22.22.3 and run:

```text
npx nx run-many -t lint test -p web-app shells-main layouts-main views-university
npx nx run web-app:build
npx nx serve web-app
```

Use narrower project targets when editing a single library. The browser build depends on lint; Angular library tests use Vitest/Analog, and `libs/services/core` uses Vitest. The root `npm test` script is a placeholder and is not the project test runner. The browser app serves at `http://localhost:4300/`; port 4200 is occupied by Docker on this machine, so the browser app is pinned to 4300. Stop old dev servers before reusing the port.
