#!/usr/bin/env bash
# SPDX-License-Identifier: MIT
#
# Runs the load-test measurement against a Helm cluster: a calibration ramp that finds the load knee, then a baseline
# plateau at an operating point below it, sampling per-service resource usage throughout. Writes the knee to
# knee.properties, the assembled measurement to report.md under load-tests/build/load-tests/, the plateau's per-request
# response times to the baseline reference the profiles assert against, and a ready-to-annotate record under
# docs/load-tests/. Exits non-zero when the calibration, the baseline, the recording, or a requested profile run fails.
set -uo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
DEFAULT_BASE_URL=http://axon-showcase-api
BASE_URL=${BASE_URL:-$DEFAULT_BASE_URL}
RATIO=${RATIO:-0.75}
CALIBRATE_RATE=${CALIBRATE_RATE:-200}
CALIBRATE_MAX_RATE=${CALIBRATE_MAX_RATE:-1600}
CALIBRATE_DURATION=${CALIBRATE_DURATION:-PT5M}
BASELINE_DURATION=${BASELINE_DURATION:-PT10M}
SSE_CONNECTIONS=${SSE_CONNECTIONS:-10}
SAMPLE_INTERVAL=${SAMPLE_INTERVAL:-10}
PROFILE=${PROFILE:-}

OUT_DIR="$ROOT/load-tests/build/load-tests"
KNEE_FILE="$OUT_DIR/knee.properties"
SAMPLES="$OUT_DIR/resource-samples.txt"
mkdir -p "$OUT_DIR"

if [ "$BASE_URL" = "$DEFAULT_BASE_URL" ]; then
    ENVIRONMENT_SLUG=""
else
    ENVIRONMENT_SLUG="-$(printf '%s' "$BASE_URL" | sed -E 's#^[a-z]+://##; s#[/:].*$##; s/[^A-Za-z0-9]+/-/g')"
fi
BASELINE_FILE="baseline${ENVIRONMENT_SLUG}.properties"
BASELINE_REF="$ROOT/load-tests/src/gatling/resources/$BASELINE_FILE"
RECORD="$ROOT/docs/load-tests/$(date -u +%Y-%m-%d)${ENVIRONMENT_SLUG}.md"

run() {
    echo "==> $*"
    (cd "$ROOT" && "$@")
}

ceiling=$CALIBRATE_RATE
calibration_steps=0
while :; do
    calibration_steps=$((calibration_steps + 1))
    if ! run ./gradlew :load-tests:gatlingRun -Pprofile=calibrate -PbaseUrl="$BASE_URL" -Prate="$ceiling" \
        -Pratio="$RATIO" -Pduration="$CALIBRATE_DURATION" -PsseConnections="$SSE_CONNECTIONS"; then
        echo "calibration failed" >&2
        exit 1
    fi

    CAL_LOG=$(ls -dt "$ROOT"/load-tests/build/reports/gatling/showcasesimulation-*/simulation.log | head -1)
    if ! run ./gradlew :load-tests:kneeFinder -Plog="$CAL_LOG" -Pknee="$KNEE_FILE" -q; then
        echo "knee derivation failed" >&2
        exit 1
    fi

    MEASURED=$(sed -n 's/^measured=//p' "$KNEE_FILE")
    echo "==> calibration step $calibration_steps: ceiling=$ceiling units/s, measured=$MEASURED"
    if [ "$MEASURED" = "true" ] || [ "$ceiling" -ge "$CALIBRATE_MAX_RATE" ]; then
        break
    fi
    ceiling=$((ceiling * 2))
    if [ "$ceiling" -gt "$CALIBRATE_MAX_RATE" ]; then
        ceiling=$CALIBRATE_MAX_RATE
    fi
done

KNEE=$(sed -n 's/^knee=//p' "$KNEE_FILE")
OPERATING=$(sed -n 's/^operatingPoint=//p' "$KNEE_FILE")
if [ "$MEASURED" = "true" ]; then
    KNEE_LABEL="Knee: $KNEE workload units/s"
    OPERATING_LABEL="Operating point: $OPERATING workload units/s (below the knee)"
else
    KNEE_LABEL="Knee: not measured (no sustained departure up to the $ceiling-unit/s calibration ceiling)"
    OPERATING_LABEL="Operating point: $OPERATING workload units/s (60% of the ceiling; raise CALIBRATE_MAX_RATE for a"
    OPERATING_LABEL+=" measured knee)"
fi
echo "==> knee=$KNEE units/s (measured=$MEASURED), operating point=$OPERATING units/s"

: > "$SAMPLES"
(
    while true; do
        printf -- '--- %s\n' "$(date -u +%H:%M:%S)"
        kubectl top pods -A 2>/dev/null || true
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
BASE_LOG=$(ls -dt "$ROOT"/load-tests/build/reports/gatling/showcasesimulation-*/simulation.log | head -1)

HOST_SHAPE=$(kubectl get nodes \
    -o jsonpath='{.items[0].status.allocatable.cpu} cpu / {.items[0].status.allocatable.memory}' \
    2>/dev/null || echo unknown)
SUMMARY_PATTERN='^> (request count|min response time|max response time|mean response time'
SUMMARY_PATTERN+='|response time 95th|response time 99th|mean throughput)'
SUMMARY=$(grep -E "$SUMMARY_PATTERN" "$OUT_DIR/baseline-run.log" || true)

cat >"$OUT_DIR/report.md" <<EOF
# Load-test baseline: $(date -u +%Y-%m-%d)

- Target: $BASE_URL
- Target node allocatable: $HOST_SHAPE
- Calibration: ramp to $ceiling workload units/s over $CALIBRATE_DURATION ($calibration_steps step(s))
- $KNEE_LABEL
- $OPERATING_LABEL
- Read share: $RATIO, SSE connections: $SSE_CONNECTIONS
- Plateau duration: $BASELINE_DURATION

## Gatling summary (baseline plateau)

\`\`\`
$SUMMARY
\`\`\`

## Per-service resource usage (\`kubectl top pods -A\`)

\`\`\`
$(cat "$SAMPLES")
\`\`\`
EOF

echo "==> wrote $OUT_DIR/report.md"

if ! run ./gradlew :load-tests:baselineStats -Plog="$BASE_LOG" -PbaselineOut="$BASELINE_REF" \
    -Ptarget="$BASE_URL" -q; then
    echo "baseline recording failed" >&2
    exit 1
fi

mkdir -p "$(dirname "$RECORD")"
{
    echo "<!-- Generated by scripts/load-test-baseline.sh for $BASE_URL; annotate before committing. -->"
    echo
    cat "$OUT_DIR/report.md"
    echo
    echo "## Notes"
    echo
} >"$RECORD"
echo "==> wrote $RECORD (annotate, then ./gradlew spotlessApply before committing)"

STATUS=$BASELINE_STATUS
if [ -n "$PROFILE" ]; then
    echo "==> running profile $PROFILE at kneeRate=$KNEE units/s, baseline $BASELINE_FILE"
    if ! run ./gradlew :load-tests:gatlingRun -Pprofile="$PROFILE" -PbaseUrl="$BASE_URL" -PkneeRate="$KNEE" \
        -PbaselineFile="$BASELINE_FILE" -Pratio="$RATIO" -PsseConnections="$SSE_CONNECTIONS" \
        >"$OUT_DIR/profile-run.log" 2>&1; then
        STATUS=1
        echo "profile $PROFILE failed its assertions" >&2
    fi
fi

exit "$STATUS"
