# Angular workspace guidance

Scope: this `project-00` Nx/Angular workspace only. Start with [the architecture guide](docs/architecture.md) for the full folder map and data flow.

## Where changes belong

| Path | Responsibility |
| --- | --- |
| `apps/web-app/` | Browser host and application bootstrap. |
| `apps/mobiles/app/` | ng-native/Expo application for native iOS and Android UI. Shares menu/content data and has native drawer/tab UI. See [the mobile guide](apps/mobiles/README.md) and app-specific [AGENTS.md](apps/mobiles/app/AGENTS.md). |
| `libs/remotes/dashboard-remote/` | Independently built and served Angular **application** exposing federated routes. Location under `libs/` does not make it an Angular library. |
| `libs/shells/main/` | Menu configuration, router setup, lazy route wiring and host shell. |
| `libs/layouts/main-layout/` | Material header and responsive left navigation UI. |
| `libs/views/home/`, `libs/dashboards/main/` | Local feature pages and route definitions. |
| `libs/widgets/ui/`, `libs/shared/ui/` | Reusable stat card, page header and shared Material theme. |
| `libs/services/state/`, `libs/services/core/` | NgRx application state and framework-free TypeScript types/helpers, respectively. |

Follow the `type:*` tags and `@nx/enforce-module-boundaries` rules in `eslint.config.mjs`; do not bypass them by importing implementation files across layers. Keep new local and federated feature routes lazy. The host consumes `dashboard_remote/Routes`; the remote exposes `./Routes`. Do not load remote source directly into the host or assume the remote is a second host.

## Validate changes

Use Node compatible with Angular 22 (Node >= 22.22.3); from this directory run `npx nx run-many -t lint test build -p web-app dashboard_remote` for relevant web changes, or select only the affected projects/targets. `build` depends on lint for both apps. The root npm `test` script is a placeholder, not the Nx project tests. Nx's Angular Webpack Module Federation executors are deprecated in Nx 23; do not substitute `@angular/build:dev-server` without migrating the federation setup.

## On-demand mobile workflow

For ng-native, Expo, mobile app setup, native UI, or mobile documentation work, load [the ng-native mobile skill](.github/skills/ng-native-mobile/SKILL.md) and read [the mobile area guide](apps/mobiles/README.md). Browser Material components, DOM APIs and Webpack Module Federation are **not** assumed to work in native apps.
