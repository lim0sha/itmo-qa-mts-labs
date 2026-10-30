#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DURATION="${DURATION:-60}"
THINK_TIME="${THINK_TIME:-1000}"
USERS="${USERS:-5 10 20}"
cd "$ROOT"

for user_count in $USERS; do
    echo "=== Gatling run: ${user_count} users, ramp-up ${user_count} s, duration ${DURATION} s, think time ${THINK_TIME} ms ==="
    mvn -B gatling:test \
        "-Dusers=${user_count}" \
        "-Drampup=${user_count}" \
        "-Dduration=${DURATION}" \
        "-DthinkTime=${THINK_TIME}"
done

latest="$(ls -dt target/gatling/*/ | head -n 1)"
rm -rf report
cp -R "$latest" report
echo "Done. Reports: target/gatling; final: report"
