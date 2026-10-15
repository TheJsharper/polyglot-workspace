# Angular workspace architecture

This workspace uses Nx 23 and Angular 22 for a web **host**, a separately served **remote application**, reusable libraries, and an ng-native **iOS/Android application**. The web host fetches the dashboard through Module Federation when its route is visited. The mobile app is a separate Expo/Metro application and is not part of the web federation.

## Web request path

1. [`apps/web-app/src/app/app.config.ts`](../apps/web-app/src/app/app.config.ts) calls `provideShell()`, which installs the router, menu configuration and root NgRx providers.
2. [`libs/shells/main/src/lib/shell.component.ts`](../libs/shells/main/src/lib/shell.component.ts) connects the store to the header, sidenav and `<router-outlet>`. The shell owns wiring and menu items, not the header's markup.
3. [`libs/shells/main/src/lib/shell.routes.ts`](../libs/shells/main/src/lib/shell.routes.ts) lazy-loads the local `views-home` route for `/` and the federated `dashboard_remote/Routes` for `/dashboard`.
4. [`libs/remotes/dashboard-remote/module-federation.config.ts`](../libs/remotes/dashboard-remote/module-federation.config.ts) exposes `./Routes`, which returns the routes from `libs/dashboards/main`.

## Native application

[`apps/mobiles/app`](../apps/mobiles/app) is the Nx project `app`, generated with `@ng-native/nx`. Angular components render native views through ng-native and React Native Fabric; Expo/Metro serves and bundles the app. Its native shell provides a left drawer, routed main content, and Home/Dashboard bottom tabs. It reuses the platform-neutral menu and summary data in `libs/services/core`; the Material/CDK web layout and federated dashboard remote are not native dependencies.

Run `nx run app:start` for Metro, `nx run app:test` for its Vitest tests, or `nx run app:typecheck` for Angular template/type checking. See the [mobile guide](../apps/mobiles/README.md) and app-specific [AGENTS.md](../apps/mobiles/app/AGENTS.md) for platform requirements and native coding boundaries.

### Host, remote and local libraries

| Role | Project | How it runs |
| --- | --- | --- |
| Host | `apps/web-app` (`web-app`) | Entry point, port 4200; [`module-federation.config.ts`](../apps/web-app/module-federation.config.ts) registers `dashboard_remote`. |
| Remote | `libs/remotes/dashboard-remote` (`dashboard_remote`) | An Nx `projectType: application`, independently built/served on port 4201; exports `./Routes` via a remote entry. |
| Native app | `apps/mobiles/app` (`app`) | Expo/Metro app for iOS and Android; separate from browser federation. |
| Local libraries | `libs/views`, `libs/dashboards`, etc. | Compiled into an application build, not served independently. A local lazy import splits code but does not create a federated remote. |

Module Federation is a Webpack runtime boundary: the host requests an exposed module from the remote server. Its name (`dashboard_remote`) and exposed key (`./Routes`) must match the host import (`dashboard_remote/Routes`). Both servers are part of one composed web application, not two hosts. `dashboard_remote` lives under `libs/remotes` for organization; it is **not** a conventional Angular library. See [`project.json`](../libs/remotes/dashboard-remote/project.json) for its build, serve, lint and test targets. Nx 23's Angular Webpack federation executors are deprecated; replacing only the Angular dev-server builder would break this setup.

## Folder responsibilities

| Folder | Ownership and dependency direction |
| --- | --- |
| `apps/web-app/` | Web bootstrap, federation host configuration, shared global styles. |
| `apps/mobiles/app/` | Native ng-native/Expo application; [mobile guide](../apps/mobiles/README.md) documents commands, platform requirements and boundaries. |
| `libs/remotes/` | Federated applications. The dashboard remote wraps `libs/dashboards/main` routes; it retains its own build output and HTTP server. |
| `libs/shells/` | Composition root: menu entries, lazy local and remote routes, root providers, connection to layout. |
| `libs/layouts/` | Responsive toolbar/sidenav presentation; receives state via inputs, emits user actions via outputs. |
| `libs/views/` | Route-level local pages such as Home. |
| `libs/dashboards/` | Dashboard feature components/routes, exposed by the remote. |
| `libs/widgets/` | Reusable UI such as stat cards. |
| `libs/shared/` | Shared UI primitives and the Material 3 theme. |
| `libs/services/state/` | Angular NgRx features/providers/effects for global application state. |
| `libs/services/core/` | Framework-free menu/theme models and helpers, including menu entries and Home/Dashboard summary data shared by web and native screens. |

Projects carry `type:*` tags; [`eslint.config.mjs`](../eslint.config.mjs) enforces allowed imports with `@nx/enforce-module-boundaries`. Notably, the shell may depend on `type:remote`, but the remote consumes dashboard/shared/service libraries instead of the shell. Add new projects with an appropriate tag and a matching constraint rather than introducing back-edges.

## State, UI and checks

[`provideAppState()`](../libs/services/state/src/lib/provide-app-state.ts) registers NgRx Store (`layout`, `navigation`, router reducer), functional Effects, Router-Store and DevTools in dev mode. The layout feature owns sidenav/theme; the navigation feature loads the shell's injected menu config. Effects restore the theme from `localStorage`, apply the `dark-theme` class on `<html>` and persist toggles. [`_theme.scss`](../libs/shared/ui/src/styles/_theme.scss) defines Material 3 light/dark tokens, used by the host and remote. The layout uses Angular Material toolbar, sidenav, list, icons and buttons; CDK breakpoints switch the sidenav to an overlay on handsets.

From the workspace directory, use Node >= 22.22.3 and run:

```text
npx nx run-many -t lint test build -p web-app dashboard_remote
npx nx serve web-app --devRemotes=dashboard_remote
```

Use narrower project targets when editing a single library. Both app build targets depend on lint; Angular library tests use Vitest/Analog, and `libs/services/core` uses Vitest. The root `npm test` script is a placeholder and is not the project test runner. The host normally serves at `http://localhost:4200/`, with its remote at `http://localhost:4201/`; visit `/dashboard` through the host to verify federation. Stop old dev servers before reusing their ports.
