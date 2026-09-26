#!/usr/bin/env bash
# SPDX-License-Identifier: MIT
#
# Runs the load-test measurement against the local Helm cluster: a calibration ramp that finds the load knee, then a
# baseline plateau at an operating point below it, sampling per-service resource usage throughout. Writes the knee to
# knee.properties and the assembled measurement to report.md under load-tests/build/load-tests/. Exits non-zero when the
# calibration or the baseline run fails.
set -uo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
BASE_URL=${BASE_URL:-http://axon-showcase-api}
NAMESPACE=${NAMESPACE:-axon-showcase}
RATIO=${RATIO:-0.75}
CALIBRATE_RATE=${CALIBRATE_RATE:-200}
CALIBRATE_DURATION=${CALIBRATE_DURATION:-PT5M}
BASELINE_DURATION=${BASELINE_DURATION:-PT10M}
SSE_CONNECTIONS=${SSE_CONNECTIONS:-10}
SAMPLE_INTERVAL=${SAMPLE_INTERVAL:-10}

OUT_DIR="$ROOT/load-tests/build/load-tests"
KNEE_FILE="$OUT_DIR/knee.properties"
SAMPLES="$OUT_DIR/resource-samples.txt"
mkdir -p "$OUT_DIR"

run() {
    echo "==> $*"
    (cd "$ROOT" && "$@")
}

if ! run ./gradlew :load-tests:gatlingRun -Pprofile=calibrate -PbaseUrl="$BASE_URL" -Prate="$CALIBRATE_RATE" \
    -Pratio="$RATIO" -Pduration="$CALIBRATE_DURATION" -PsseConnections="$SSE_CONNECTIONS"; then
    echo "calibration failed" >&2
    exit 1
fi

CAL_LOG=$(ls -dt "$ROOT"/load-tests/build/reports/gatling/showcasesimulation-*/simulation.log | head -1)
if ! run ./gradlew :load-tests:kneeFinder -Plog="$CAL_LOG" -Pknee="$KNEE_FILE" -q; then
    echo "knee derivation failed" >&2
    exit 1
fi

KNEE=$(sed -n 's/^knee=//p' "$KNEE_FILE")
OPERATING=$(sed -n 's/^operatingPoint=//p' "$KNEE_FILE")
MEASURED=$(sed -n 's/^measured=//p' "$KNEE_FILE")
if [ "$MEASURED" = "true" ]; then
    KNEE_LABEL="Knee: $KNEE workload units/s"
    OPERATING_LABEL="Operating point: $OPERATING workload units/s (below the knee)"
else
    KNEE_LABEL="Knee: not measured (no sustained departure at the $KNEE-unit/s calibration ceiling)"
    OPERATING_LABEL="Operating point: $OPERATING workload units/s (60% of the ceiling; raise CALIBRATE_RATE for a"
    OPERATING_LABEL+=" measured knee)"
fi
echo "==> knee=$KNEE units/s (measured=$MEASURED), operating point=$OPERATING units/s"

: > "$SAMPLES"
(
    while true; do
        printf -- '--- %s\n' "$(date -u +%H:%M:%S)"
        kubectl top pods -n "$NAMESPACE" 2>/dev/null || true
        sleep "$SAMPLE_INTERVAL"
    done
) >"$SAMPLES" 2>&1 &
SAMPLER=$!
trap 'kill "$SAMPLER" 2>/dev/null || true' EXIT

BASELINE_STATUS=0
if ! run ./gradlew :load-tests:gatlingRun -Pprofile=baseline -PbaseUrl="$BASE_URL" -Prate="$OPERATING" \
    -Pratio="$RATIO" -Pduration="$BASELINE_DURATION" -PsseConnections="$SSE_CONNECTIONS" \
    >"$OUT_DIR/baseline-run.log" 2>&1; then
    BASELINE_STATUS=1
    echo "baseline run failed its assertions" >&2
fi
kill "$SAMPLER" 2>/dev/null || true

HOST_SHAPE=$(kubectl get nodes \
    -o jsonpath='{.items[0].status.allocatable.cpu} cpu / {.items[0].status.allocatable.memory}' \
    2>/dev/null || echo unknown)
SUMMARY_PATTERN='^> (request count|min response time|max response time|mean response time'
SUMMARY_PATTERN+='|response time 95th|response time 99th|mean throughput)'
SUMMARY=$(grep -E "$SUMMARY_PATTERN" "$OUT_DIR/baseline-run.log" || true)

cat >"$OUT_DIR/report.md" <<EOF
# Load-test baseline

- Target: $BASE_URL (local Helm cluster)
- Target node allocatable: $HOST_SHAPE
- Calibration: ramp to $CALIBRATE_RATE workload units/s over $CALIBRATE_DURATION
- $KNEE_LABEL
- $OPERATING_LABEL
- Read share: $RATIO, SSE connections: $SSE_CONNECTIONS
- Plateau duration: $BASELINE_DURATION

## Gatling summary (baseline plateau)

\`\`\`
$SUMMARY
\`\`\`

## Per-service resource usage (\`kubectl top -n $NAMESPACE\`)

\`\`\`
$(cat "$SAMPLES")
\`\`\`
EOF

echo "==> wrote $OUT_DIR/report.md"
exit "$BASELINE_STATUS"
