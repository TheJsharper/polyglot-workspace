# services-university-state

NgRx state for the browser University screens, shared as a singleton by the host and every remote.

- `auth` feature (`provideUniversityAuth()`): token-free mirror of `UniversitySession.user()` (username, role). The bearer token never enters the store.
- `university` feature (`provideUniversityState()`): students, professors, courses, lectures and notes collections, per-resource view state (filter, create mode, loading, error, message, lookup errors), selections and the notes lookup mode. Each remote registers it in its route providers; registering it more than once is harmless.
- Effects call `UniversitySession.api` through `session.run()` so a 401 ends the session. Mutations are serializable `UniversityOperation` payloads dispatched with `UniversityActions.execute`.

Run `nx test services-university-state` to execute the unit tests.
