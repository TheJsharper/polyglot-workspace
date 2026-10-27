# Trusted Client IP Specification

## Purpose
Trust proxy IPs.

## ADDED Requirements

### Requirement: Authenticated IP forwarding
Cloudflare Pages Function MUST forward `CF-Connecting-IP` in a dedicated header with a shared-secret header. Backend MUST trust the forwarded IP only after constant-time secret match; otherwise use socket remote address. Configure the secret by environment variable and never log it.

#### Scenario: Proxy identity
- GIVEN matching, missing, or mismatched secret
- WHEN backend resolves forwarded IP
- THEN trust it only on match; otherwise use socket address

### Requirement: Abuse and deployment limits
Failed redemption attempts MUST be limited per resolved client IP and return 429 above limit. In-memory limiting MAY serve single-instance Render free. Use only Cloudflare Pages, Render free, and Neon free; add no services.

#### Scenario: Rate and free-tier limits
- GIVEN client exceeds failed-redemption limit
- WHEN another redemption arrives
- THEN return 429 without paid tiers or added services



