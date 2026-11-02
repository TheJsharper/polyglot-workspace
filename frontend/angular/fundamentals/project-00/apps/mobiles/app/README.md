# Mobile app (ng-native)

Native iOS/Android client for the live University API (`/v3/api-docs`), built with ng-native, Expo and `@angular/router`. It shares `UniversitySession`/`UniversityApi` (`@project-00/services-session`, `@project-00/services-api`) with the web app; no DOM, Material, NgRx or Module Federation is used.

## API base URL

`EXPO_PUBLIC_API_URL` selects the server. Without it the app uses `http://localhost:8080` on both simulators (`src/app/api-config.ts`). On the Android emulator, or an Android phone over USB, forward the port to the computer first:

```sh
adb reverse tcp:8080 tcp:8080   # API (Metro's 8081 is forwarded by `expo run:android`)
```

Using `localhost` (not `10.0.2.2`) is required for Google SSO: the browser must start and finish the OAuth flow on the same host as the registered `http://localhost:8080/login/oauth2/code/google` callback, and Google rejects private IP redirect hosts.

```sh
# physical device without USB: use the computer's LAN address, with the device on the same network
EXPO_PUBLIC_API_URL=http://192.168.1.20:8080 nx run app:start
```

The server must be reachable from the device (listen on `0.0.0.0`, allow port 8080 through the firewall). The dev API profile binds `127.0.0.1`, which works with `adb reverse` and the iOS simulator. Plain `http` works in debug builds; a release build needs HTTPS or Android cleartext-traffic configuration. Start the API with `backend/spring/fundamentals/project-00/start.ps1` (it selects JDK 25 when `JAVA_HOME` is older).

Native requests are not subject to browser CORS, so the API needs no CORS rules for the app; only an Expo web build would.

## Sign-in

The sign-in screen supports the configured University username/password and Google SSO when the API reports Google OAuth as enabled. Password fields on the sign-in and registration screens have an eye icon button (accessibility label "Show/Hide Password") to reveal what was typed. Native Google sign-in opens the system browser, uses a one-time state and PKCE-bound callback, and exchanges the login ticket for a mobile session. New Google users may create Student or Professor accounts; Admin accounts must already be provisioned and linked by the University API. Admin usernames/passwords are controlled by that API, so a failed `university_admin` login must be checked against the configured dev account rather than changed in the app.

Google sign-in needs a real browser: use an emulator image **with Google Play** (it ships Chrome). Plain `google_apis` images only have the WebView test browser, which shows a blank page and Google blocks OAuth inside WebViews.

Mobile access and refresh credentials are kept in memory only. Closing or restarting the app requires signing in again. Production Google OAuth should use HTTPS for the API and the registered native app scheme (`app://sso/callback`); do not use an HTTP API URL over an untrusted network.

## Navigation

| Route | Screen |
| --- | --- |
| `/` | Redirects to the University experience |
| `/login`, `/register` | Sign in; create a STUDENT or PROFESSOR account (then signs in) |
| `/university` | Guarded entry point; redirects to the first resource allowed for the signed-in role. Anonymous users are sent to `/login?returnUrl=…`, and ending the session (logout/401) returns to `/login` |
| `/university/{students,professors,courses,lectures,notes}` | List screens |
| `…/new`, `…/:id`, `…/:id/edit` | Create, detail (with relationships and actions) and edit |
| `/university/demo-logins` | Admin/manager demo-login creation, one-time credentials, list and revoke |

## Role-aware behavior

The username is the configured admin username, student id or employee id. Admin usernames and passwords are configured by the University API and are not created by the mobile app. Buttons follow the API's documented rules; the server remains the authority and its problem detail is shown inline.
After sign-in, administrators land on Students. The University drawer follows the browser role-filtered menu and keeps account identity and sign-out available on every University screen.

- **ADMIN**: create/edit/delete students and professors; create/edit/delete courses; assign/remove course teachers. Admins may reassign lecture delivery to a professor already assigned to that course.
- **PROFESSOR**: schedule, reschedule and cancel lectures for courses they teach; record/remove attendance; reassign delivery for their course's lectures to another assigned professor; review and grade notes; pending-review list.
- **STUDENT**: own profile; enroll/unenroll themselves; write notes for attended lectures; edit/delete own notes.

Every screen shows explicit loading (a pulsing skeleton of a profile block and two post cards, colored by the same System/Light/Dark theme tokens as the web startup skeleton), error (with Retry on loads) and success states, and every delete or cancel asks for inline confirmation first.

## Light and dark mode

By default the app follows the device's system appearance (`userInterfaceStyle: automatic` in `app.json`) and switches live. The theme icon button at the right of every screen header (including sign-in) cycles **System → Light → Dark**. Its icons are sun-moon, sun and moon, and its accessibility label is "Theme: X. Change theme". `ThemePreference` in `src/app/theme-preference.ts` applies the choice with `ColorScheme.set()` and saves it on the device with `Storage` from `@ng-native/expo/async-storage` (`@react-native-async-storage/async-storage`), so it survives restarts. The Android system navigation bar keeps its own colour. Colours are CSS custom properties (`--bg`, `--surface`, `--raised`, `--border`, `--text`, `--muted`, `--primary`, `--danger`, …) defined once on the root in `src/app/app.ts`, with a `@media (prefers-color-scheme: dark)` override. Components read them with `var(--token)`; do not add hard-coded hex colours to screens. Native props that cannot read CSS (header colours, the input placeholder) use the matching `PALETTE` in `src/app/theme.ts`, so keep both in step. On an Android emulator, `adb shell cmd uimode night yes|no` flips the scheme.

## Icons

Icons use `@ng-native/icons` with `@ng-icons/core` and the Lucide set (`@ng-icons/lucide`). Examples:

- The password eye/eye-off toggle.
- The University brand mark (sign-in screen, header and drawer).
- Header menu and sign-out icon buttons.
- One icon per drawer item: Students graduation cap, Professors presentation, Courses book, Lectures calendar-clock, Notes notebook-pen, Demo logins key. The current item is highlighted and reported as selected.

`AppButton` takes an `iconSvg` input for raw multi-colour SVG, used for the official Google "G" on "Continue with Google". Icons draw through `react-native-svg`, a native module, so after changing native dependencies rebuild the dev client (`npx expo run:android --no-bundler` from this folder) before reloading the bundle.

## Keyboard

The soft keyboard closes when there is nothing left to type into:

- tapping empty space;
- dragging a screen;
- pressing any `AppButton`;
- opening the drawer;
- navigating.

React Native's `Keyboard.dismiss()` does not blur ng-native `<text-input>` on Android. Instead, every `app-field` registers with `SoftKeyboard` in `src/app/university/ui.ts`, which blurs the focused field. Use `SoftKeyboard.dismiss()` rather than `Keyboard.dismiss()` in new code.

## Validate

```sh
nx run app:test        # Vitest against a fake native layer and a fake session
nx run app:typecheck
nx run app:eslint:lint
```

An Android 11 (API 30) emulator smoke test verified the app launch and native navigation. API-backed University workflows are covered by tests with a fake session; a live API is required to exercise them on a device.