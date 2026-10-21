# Local development demos

Recruiter demo keys, the `/demo` page, the manager account that creates the keys, and the ten-account sample dataset are available only when the backend runs in the local `dev` profile. Docker and the `prod` profile do not enable demo-key endpoints, create the manager or seed sample accounts.

Keys are created, listed and deleted by the manager in the app (**Demo keys**) and stored in the dev database; no scripts or environment variables are involved. The startup, test, and dataset details are kept in a local, Git-ignored `docs/dev/` guide. See [deployment.md](./deployment.md) for production requirements.
