#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

mvn -q -pl seckill-order -am test -DskipTests=false

HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8082/actuator/health}"

if curl -fsS "$HEALTH_URL" >/dev/null; then
  echo "OK: actuator health reachable"
else
  echo "ERROR: actuator health check failed" >&2
  exit 1
fi

echo "Production readiness verification complete"
