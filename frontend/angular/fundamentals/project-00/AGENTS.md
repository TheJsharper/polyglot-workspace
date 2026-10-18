# Angular workspace guidance

Scope: this `project-00` Nx/Angular workspace only. Start with [the architecture guide](docs/architecture.md) for the full folder map and data flow.

## Where changes belong

| Path | Responsibility |
| --- | --- |
| `apps/web-app/` | Browser host and application bootstrap. |
| `apps/mobiles/app/` | ng-native/Expo application for native iOS and Android UI. Shares menu/content data and has native drawer/tab UI. See [the mobile guide](apps/mobiles/README.md) and app-specific [AGENTS.md](apps/mobiles/app/AGENTS.md). |
| `libs/shells/main/` | University-only web menu, router setup, lazy route wiring and host shell. |
| `libs/layouts/main-layout/` | Material header and responsive left navigation UI. |
| `libs/views/university/` | Browser University screens and route definitions. |
| `libs/shared/ui/` | Shared page header and Material theme. |
| `libs/services/state/`, `libs/services/core/` | NgRx application state and framework-free TypeScript types/helpers, respectively. |

Follow the `type:*` tags and `@nx/enforce-module-boundaries` rules in `eslint.config.mjs`; do not bypass them by importing implementation files across layers. Keep new feature routes lazy and preserve the separation between the browser UI and the native app.

## Validate changes

Use Node compatible with Angular 22 (Node >= 22.22.3); from this directory run `npx nx run-many -t lint test -p web-app shells-main layouts-main views-university` and `npx nx run web-app:build` for relevant web changes, or select only the affected projects/targets. The root npm `test` script is a placeholder, not the Nx project tests.

## On-demand mobile workflow

For ng-native, Expo, mobile app setup, native UI, or mobile documentation work, load [the ng-native mobile skill](.github/skills/ng-native-mobile/SKILL.md) and read [the mobile area guide](apps/mobiles/README.md). Browser Material components, DOM APIs and browser-only NgRx effects are **not** assumed to work in native apps.
