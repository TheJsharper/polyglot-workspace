# Demo Data Isolation Specification

## Purpose
Isolate demo data and reads.

## ADDED Requirements

### Requirement: Isolated, bounded dataset
DEMO sessions MUST see only demo-flagged records, at most 10 per resource: students, professors, courses, lectures, and notes. Enabled production MUST seed idempotently. DEMO MUST never receive real users or data, including Google `g-<hash>` accounts. Real-user lists SHOULD exclude demo records because synthetic fixtures are not university records; inclusion is allowed only when deliberately intended.

#### Scenario: Demo and real-user reads
- GIVEN real and demo records coexist
- WHEN DEMO or real users read listed resources
- THEN DEMO sees only flagged records (max 10); real lists exclude demos by default

#### Scenario: Production seed repeats
- GIVEN demo is enabled and data seeded
- WHEN production restarts
- THEN no duplicate demo data is added

### Requirement: Read-only demo access
Every DEMO request except GET, HEAD, or OPTIONS MUST return 403 without changing data, including manager/admin endpoints.

#### Scenario: Demo attempts mutation
- GIVEN authenticated DEMO session
- WHEN it sends any other method
- THEN 403 and no data changes


