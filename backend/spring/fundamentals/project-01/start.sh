#!/usr/bin/env bash
# Starts project-01 (demo-keys service) on 127.0.0.1:8081. Start it before project-00.
# SERVICE_AUTH_SECRET is read from the untracked .env in this folder (see .env.example).
set -euo pipefail
cd "$(dirname "$0")"

[ -f .env ] || { echo "Missing .env - copy .env.example to .env and fill in the values." >&2; exit 1; }

exec ./mvnw spring-boot:run
