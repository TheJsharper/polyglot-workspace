# Mobile applications

[`app`](app/) is the workspace's ng-native application for iOS and Android. It is an Nx application generated with the official `@ng-native/nx` plugin, using Angular 22, Expo 57 and React Native 0.86. Its bundle identifier is `com.polyglotworkspace.app`.

## Run and validate

Run commands from the workspace root with Node **22.22.3 or newer**:

```sh
nx run app:start
nx run app:test
nx run app:typecheck
nx run app:run-android
nx run app:run-ios
```

`start` runs Expo/Metro; scan its QR code with Expo Go or use a simulator. Tests run in Node against a fake native host and do not require a device. `run-android` requires a configured Android SDK/emulator; `run-ios` requires macOS and an iOS simulator. The test and typecheck targets have been verified in this workspace. Device and simulator behavior has not been verified here.

The University API screens (login, register, role-aware CRUD and relationships) and the `EXPO_PUBLIC_API_URL` setting are documented in [the app README](app/README.md).

The app root is [`src/main.ts`](app/src/main.ts); the native shell combines a left drawer, main content outlet and native Home/Dashboard bottom tabs. Shared menu entries and Home/Dashboard stats come from `libs/services/core`; native screens render those values with ng-native components. The web `MainLayoutComponent` depends on Material/CDK, and the web dashboard is loaded by Module Federation, so neither can be imported into the native runtime. The app's own [`AGENTS.md`](app/AGENTS.md) documents native component, event, styling, routing and testing conventions.

## Native boundary

ng-native renders Angular components as native views through its renderer and React Native Fabric, not HTML in a WebView. Import each native component from `@ng-native/components` and use lowercase tags such as `<view>` and `<text>`. Expo/Metro handles native development and bundling; the browser host's Angular/Webpack build does not.

- Do not use browser `document`/`window`, Angular Material, DOM-dependent effects from `libs/services/state`, or browser-only image and animation APIs. Reuse code only after checking that its imports are platform-neutral.
- The web host's Webpack Module Federation is **not** native navigation. Do not import `dashboard_remote/Routes` into this app; follow the ng-native router guidance if adding native navigation.
- Expo Go does not include every possible native module. Use a development build or release build when a feature requires modules that Expo Go does not contain.

## Official references

| Topic | Source |
| --- | --- |
| Overview and native APIs | [ng-native](https://ng-native.com/) |
| App setup, native elements, tests and Expo Go | [Getting started](https://ng-native.com/guide/getting-started) |
| Nx integration | [Nx package guide](https://ng-native.com/packages/nx) |
| Native router, headers, stacks and tab navigation | [Router package](https://ng-native.com/packages/router) and [tabs](https://ng-native.com/packages/router/tabs) |
| Renderer and native navigation | [Architecture](https://ng-native.com/guide/architecture) |
| DOM and feature limitations | [Known limitations](https://ng-native.com/guide/limitations) |

The app uses `@ng-native/router` with `react-native-screens` for native route stacks and bottom tabs. The left drawer and routed feature pages are native UI; the same `MAIN_MENU`, Home stats and Dashboard stats are shared with the web application from `libs/services/core`.

For project-specific mobile work, load the [ng-native mobile skill](../../.github/skills/ng-native-mobile/SKILL.md).
