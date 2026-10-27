# Demo Key Issuance Specification

## Purpose
Manage recruiter keys.

## ADDED Requirements

### Requirement: Manager-controlled keys
Only MANAGER MUST issue, list, add recruiter labels to, or revoke keys; ADMIN, PROFESSOR, STUDENT, and anonymous callers MUST be denied. Keys MUST be high-entropy random secrets, shown once, with only hashes stored in project-00 Postgres. Secrets MUST NOT appear in URLs or logs. Production MUST NOT require project-01.

#### Scenario: Manager and non-manager
- GIVEN demo enabled
- WHEN MANAGER issues, or others administer, a key
- THEN only MANAGER succeeds; secret is shown once and only hash stored

### Requirement: Atomic lifecycle and feature gate
Unredeemed keys MUST expire seven days after issuance. Consumption and revocation MUST be atomic, allowing at most one concurrent redemption. Revoked, expired, and consumed keys MUST return the same generic error. With `app.demo.enabled=false`, endpoints MUST be 404 or denied and no seed may run.

#### Scenario: Concurrent or unusable key
- GIVEN a key is concurrently redeemed, revoked, expired, or consumed
- WHEN redemption is attempted
- THEN at most one concurrent request succeeds; invalid states share one generic error

#### Scenario: Disabled feature
- GIVEN `app.demo.enabled=false`
- WHEN demo endpoints or seeding run
- THEN endpoints are unavailable and no data is seeded



