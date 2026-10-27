# Demo Session Specification

## Purpose
Issue temporary recruiter demo access.

## ADDED Requirements

### Requirement: Anonymous, persona-scoped redemption
Anonymous callers MUST POST a valid key and STUDENT or PROFESSOR persona when enabled; other personas MUST be rejected. A session MUST carry dedicated DEMO authority and selected persona view, be valid for 60 minutes, and have no refresh cookie or refresh capability. It MUST NOT grant real STUDENT, PROFESSOR, ADMIN, or MANAGER permissions. Manager/admin endpoints MUST deny it. Unusable key states MUST share one generic error.

#### Scenario: Valid redemption
- GIVEN an enabled demo and valid key
- WHEN anonymous caller POSTs STUDENT or PROFESSOR
- THEN key is consumed; 60-minute access-only DEMO token, no refresh

#### Scenario: Invalid redemption or escalation
- GIVEN invalid persona, unusable key, or DEMO session
- WHEN redeemed, refreshed, or used on manager/admin endpoint
- THEN no invalid session or refresh; access denied and key error generic


