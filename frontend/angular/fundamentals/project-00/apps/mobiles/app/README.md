# Mobile app (ng-native)

Native iOS/Android client for the live University API (`/v3/api-docs`), built with ng-native, Expo and `@angular/router`. It shares `UniversitySession`/`UniversityApi` (`@project-00/services-session`, `@project-00/services-api`) with the web app; no DOM, Material, NgRx or Module Federation is used.

## API base URL

`EXPO_PUBLIC_API_URL` selects the server. Without it the app uses `http://10.0.2.2:8080` on the Android emulator and `http://localhost:8080` on the iOS simulator (`src/app/api-config.ts`).

```sh
# physical device: use the computer's LAN address, with the device on the same network
EXPO_PUBLIC_API_URL=http://192.168.1.20:8080 nx run app:start
```

The server must be reachable from the device (listen on `0.0.0.0`, allow port 8080 through the firewall). Plain `http` works in Expo Go; a release build needs HTTPS or Android cleartext-traffic configuration.

## Navigation

| Route | Screen |
| --- | --- |
| `/`, `/dashboard` | Sample Home and Dashboard tabs (public) |
| `/login`, `/register` | Sign in; create a STUDENT or PROFESSOR account (then signs in) |
| `/university` | Guarded hub; the drawer's **University** entry opens it. Anonymous users are sent to `/login?returnUrl=…`, and ending the session (logout/401) returns to `/login` |
| `/university/{students,professors,courses,lectures,notes}` | List screens |
| `…/new`, `…/:id`, `…/:id/edit` | Create, detail (with relationships and actions) and edit |

## Role-aware behavior

The username is the student id or employee id. Buttons follow the API's documented rules; the server remains the authority and its problem detail is shown inline.

- **ADMIN**: create/edit/delete students and professors; create/edit/delete courses; assign/remove course teachers. Admins may reassign lecture delivery to a professor already assigned to that course.
- **PROFESSOR**: schedule, reschedule and cancel lectures for courses they teach; record/remove attendance; reassign delivery for their course's lectures to another assigned professor; review and grade notes; pending-review list.
- **STUDENT**: own profile; enroll/unenroll themselves; write notes for attended lectures; edit/delete own notes.

Every screen shows explicit loading, error (with Retry on loads) and success states, and every delete or cancel asks for inline confirmation first.

## Validate

```sh
nx run app:test        # Vitest against a fake native layer and a fake session
nx run app:typecheck
nx run app:eslint:lint
```

Device and simulator behavior has not been verified.