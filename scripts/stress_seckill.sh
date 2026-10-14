#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://127.0.0.1:8082}"
REQUESTS="${REQUESTS:-200}"
CONCURRENCY="${CONCURRENCY:-20}"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required to run this stress script" >&2
  exit 1
fi

payload() {
  cat <<EOF
{"userId":${RANDOM},"productId":1,"quantity":1,"requestId":"req-$(date +%s%N)"}
EOF
}

for i in $(seq 1 "$REQUESTS"); do
  body=$(payload)
  curl -sS -X POST "$BASE_URL/api/order/participate" \
    -H 'Content-Type: application/json' \
    --data "$body" >/tmp/seckill-stress-$i.json &

  if [ $((i % CONCURRENCY)) -eq 0 ]; then
    wait
  fi
done

wait

echo "stress test complete: $REQUESTS requests sent"
