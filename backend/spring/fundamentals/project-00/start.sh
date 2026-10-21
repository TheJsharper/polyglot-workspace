#!/usr/bin/env bash
# Starts project-00 (University API) with the dev profile. Start project-01 first (demo keys).
# Settings are read from the untracked .env in this folder (see .env.example).
set -euo pipefail
cd "$(dirname "$0")"

[ -f .env ] || { echo "Missing .env - copy .env.example to .env and fill in the values." >&2; exit 1; }

exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
