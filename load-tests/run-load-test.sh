#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JMETER_BIN="${JMETER_BIN:-jmeter}"
USERS="${USERS:-5 10 20}"
DURATION="${DURATION:-60}"
THINK_TIME="${THINK_TIME:-1000}"

mkdir -p "$ROOT/results"
rm -rf "$ROOT/report"

for user_count in $USERS; do
    echo "=== Run: ${user_count} users, ramp-up ${user_count} s, duration ${DURATION} s, think time ${THINK_TIME} ms ==="
    "$JMETER_BIN" -n -t "$ROOT/load-test.jmx" -l "$ROOT/results/run-${user_count}-users.jtl" \
        -Jusers="$user_count" -Jrampup="$user_count" -Jduration="$DURATION" -JthinkTime="$THINK_TIME"
done

last_user="$(echo "$USERS" | awk '{print $NF}')"
"$JMETER_BIN" -g "$ROOT/results/run-${last_user}-users.jtl" -o "$ROOT/report"
echo "Done. Results: $ROOT/results; report: $ROOT/report"
