#!/usr/bin/env bash
set -eu

# Asserts the doctor's declared probe set, its behaviour, its agreement with the README, the spec and the workflow, and
# the rules its own floor grammar and comparison obey — five groups, each covering a failure the others cannot:
#
#   1. SHAPE      the declaration is well-formed and unambiguous, validated by the doctor's own rules (invoked, not
#                 restated, so the two cannot drift) — so a malformed row cannot hide a probe.
#   2. BEHAVIOUR  the doctor actually REPORTS every declared probe: it is run and its output matched. A content
#                 assertion cannot see a probe that never reaches the code; this one can.
#   3. AGREEMENT  the declared set against the README's Prerequisites rows, the spec's required set and floor, and the
#                 workflow's setup-python input — the class and drift assertions the check previously lacked.
#   4. CONTROLS   the floor grammar and the comparison, exercised on the doctor's own functions — so a widened accept
#                 pattern or a weakened comparison fails here rather than in the field.
#   5. BELOW-FLOOR
#                 a below-floor interpreter through a stub PATH, so a satisfied reading of an old interpreter fails even
#                 when the helper is correct but not wired into check_tool.
#
# Usage: ./scripts/test-doctor.sh
# Exits non-zero on the first disagreement, naming the probe or the row.

repo_root=$(cd "$(dirname "$0")/.." && pwd)
doctor="$repo_root/scripts/doctor.sh"
readme="$repo_root/README.md"

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

[ -f "$doctor" ] || fail "doctor script not found at $doctor"
[ -f "$readme" ] || fail "README not found at $readme"

# --- the declaration -----------------------------------------------------------------------------------------------
# Read the table the doctor declares, rather than inferring the probe set from its source text: that is what makes every
# probe known by construction, so none can be invisible here.
probe_table=$(sed -n "/^probe_table='/,/^'$/p" "$doctor" | sed '1d;$d')
[ -n "$probe_table" ] || fail "no probe_table found in $doctor — the declaration is missing or its delimiters changed"

probe_keys=$(printf '%s\n' "$probe_table" | awk -F'|' 'NF == 7 && $1 != "" { print $1 }')
[ -n "$probe_keys" ] || fail "the doctor's probe_table declares no probes"

# One column of a declared probe's row. $1 key, $2 column (1-based).
probe_field() {
    printf '%s\n' "$probe_table" | awk -F'|' -v key="$1" -v n="$2" 'NF == 7 && $1 == key { print $n }'
}

probe_declares() {
    printf '%s\n' "$probe_keys" | grep -qx "$1"
}

# --- 1. SHAPE -------------------------------------------------------------------------------------------------------
# Invoke the doctor's own validation rather than restating its rules: two hand-written copies would drift, and the
# doctor's is the one that guards a person running it directly. Extracted here so no probe runs and no output is needed.
shape_rules=$(sed -n '/^validate_probe_table()/,/^}/p' "$doctor")
[ -n "$shape_rules" ] || fail "could not extract validate_probe_table from $doctor — the declaration's guard is gone"
shape_check=$(printf '%s\nprobe_table=%s\nvalidate_probe_table\n' "$shape_rules" "$(printf "'%s'" "$probe_table")")
if ! shape_output=$(printf '%s\n' "$shape_check" | sh 2>&1); then
    fail "the doctor's declaration is malformed — $(printf '%s' "$shape_output" | head -1)"
fi

# --- 2. BEHAVIOUR ---------------------------------------------------------------------------------------------------
# Run the doctor and require its output to name every declared probe. The doctor's EXIT CODE is deliberately ignored: it
# is host-dependent (0 where every required tool is present, non-zero where one is not), so depending on it would make
# this check fail for reasons a pull request cannot remediate. The output is host-independent — a missing tool is a
# status, never an omission — so this assertion holds on any machine.
set +e
doctor_output=$(sh "$doctor" 2>&1)
set -e
for key in $probe_keys; do
    # Anchored to the report line's own shape — two leading spaces, the key, then padding — rather than to the key
    # anywhere in the output: a detail or remedy is a single-line string that can contain the key surrounded by spaces
    # (a `Note:  java  ...` line matched unanchored without java being reported). The anchor is strong but not
    # absolute — a multi-line detail could emit such a line — so keep every probe's detail single-line. The key is a
    # plain identifier (the shape validator enforces it), so `-E` here carries no metacharacter risk.
    printf '%s\n' "$doctor_output" | grep -qE "^  ${key} " ||
        fail "the doctor declares probe '$key' but does not report it — the probe never reaches the reporting code"
done

# --- 3. AGREEMENT ---------------------------------------------------------------------------------------------------
# README Prerequisites rows (the bolded first column).
readme_rows=$(sed -n '/^### Prerequisites/,/^### Get the Sources/p' "$readme" |
    grep -oE '^\| \*\*[^|]+\*\*' |
    sed -E 's/^\| \*\*//; s/\*\*$//')
[ -n "$readme_rows" ] || fail "no README Prerequisites rows matched — the extraction pattern is stale"

# Maps each README row to the probe(s) it documents — the README's own authority, and the anchor the declaration is
# validated against (so a probe cannot erase its documentation by declaring itself undocumented). One row may document
# two probes. A row with no mapping is an error.
readme_to_probe() {
    case "$1" in
    "Java 21+") echo "java" ;;
    "Docker & Compose") echo "docker compose" ;;
    "actionlint") echo "actionlint" ;;
    '`pack` CLI') echo "pack" ;;
    "Helm 4.x") echo "helm" ;;
    "Snyk CLI") echo "snyk" ;;
    "Python "*) echo "python3" ;;
    "Kubernetes cluster") echo "kubectl" ;;
    "OpenCode" | "OpenCode (v2 recommended)") echo "opencode" ;;
    "ast-grep") echo "ast-grep" ;;
    *) echo "" ;;
    esac
}

# Direction one: every README row maps to a declared probe.
while IFS= read -r row; do
    [ -n "$row" ] || continue
    mapped=$(readme_to_probe "$row")
    [ -n "$mapped" ] || fail "README Prerequisites row '$row' has no mapping in scripts/test-doctor.sh"
    for p in $mapped; do
        probe_declares "$p" || fail "README row '$row' maps to '$p', which the doctor's declaration does not carry"
    done
done <<EOF
$readme_rows
EOF

# Direction two: each probe's declared `readme` must AGREE with the mapping — `yes` requires a row, `no` requires none —
# so a probe cannot drop out of the documented list by declaring itself undocumented while a row still names it, nor
# claim documentation a row no longer provides.
for key in $probe_keys; do
    declared=$(probe_field "$key" 4)
    covered=0
    while IFS= read -r row; do
        [ -n "$row" ] || continue
        for p in $(readme_to_probe "$row"); do
            [ "$p" = "$key" ] && covered=1
        done
    done <<EOF
$readme_rows
EOF
    if [ "$declared" = "yes" ] && [ "$covered" -ne 1 ]; then
        fail "probe '$key' is declared readme=yes but no README Prerequisites row documents it"
    fi
    if [ "$declared" = "no" ] && [ "$covered" -eq 1 ]; then
        fail "probe '$key' is declared readme=no but a README Prerequisites row documents it"
    fi
done

# Each declared `tool` probe must carry an install hint for every documented platform — a hint dropped for one platform
# cannot pass on the other's presence. A `state` step has no install command by nature and is exempt.
for key in $probe_keys; do
    [ "$(probe_field "$key" 3)" = "tool" ] || continue
    for pl in macos debian; do
        grep -qE "^[[:space:]]*${pl}:${key}\) echo \"[^\"]" "$doctor" ||
            fail "declared tool probe '$key' has no install hint for platform '$pl'"
    done
done

# The required set: the probes declared `required` must be exactly the prerequisites of the default build-and-test path.
# The spec states that path across two scenarios (showcase/quality/toolchain-check): the required-tool scenario names
# "Java, Docker with Compose … the Python 3.12+ interpreter the build's Python tooling runs under", and the repo-state
# requirement makes the Docker daemon the required exception. Held here rather than read from the table, so the
# comparison is against the spec rather than the declaration against itself.
expected_required="compose docker docker-daemon java python3"
required_probes=$(for key in $probe_keys; do
    [ "$(probe_field "$key" 2)" = "required" ] && printf '%s\n' "$key"
done | sort | tr '\n' ' ' | sed 's/ $//')
if [ "$required_probes" != "$expected_required" ]; then
    fail "the declared required probes are '$required_probes', not '$expected_required' — a probe's class changed,
or the required set no longer matches the default build-and-test path the spec names. Each probe's declared class:
$(for key in $probe_keys; do printf '  %s (%s)\n' "$key" "$(probe_field "$key" 2)"; done)"
fi

# The Python floor: the spec states it, the declaration implements it, and the README and the workflow must agree — the
# three surfaces a bump cannot miss. Held here against the spec, so the declaration is never compared against itself.
expected_python_floor="3.12"
declared_python_floor=$(probe_field python3 7)
[ "$declared_python_floor" = "$expected_python_floor" ] ||
    fail "the declared python3 floor is '$declared_python_floor', the spec's minimum is '$expected_python_floor'"

printf '%s\n' "$readme_rows" | grep -qxF "Python ${expected_python_floor}+" ||
    fail "the README's Python Prerequisites row must read 'Python ${expected_python_floor}+',
matching the declared floor"

workflow="$repo_root/.github/workflows/ci.yml"
[ -f "$workflow" ] || fail "workflow not found at $workflow"
workflow_python=$(awk '
    /uses: actions\/setup-python@/ { in_setup = 1; next }
    in_setup && /^[[:space:]]*python-version:/ {
        line = $0
        sub(/^[[:space:]]*python-version:[[:space:]]*/, "", line)
        gsub(/["\047]/, "", line)
        print line
        exit
    }
' "$workflow")
[ -n "$workflow_python" ] || fail "no setup-python step with a python-version in $workflow"
[ "$workflow_python" = "$expected_python_floor" ] ||
    fail "the workflow provisions Python '$workflow_python', the declared floor is '$expected_python_floor'"

# --- 4. CONTROLS ----------------------------------------------------------------------------------------------------
# The floor grammar and the comparison, exercised on the doctor's own functions rather than restated: the validator must
# accept `major` and `major.minor` and reject everything else, and the comparison must order dotted versions numerically
# — so widening the accept pattern or weakening the comparison fails here.
version_rules=$(sed -n '/^version_at_least()/,/^}/p' "$doctor")
[ -n "$version_rules" ] || fail "could not extract version_at_least from $doctor — the floor comparison is gone"

# $1 detected version, $2 floor, $3 expected verdict (at-least|below)
expect_version() {
    verdict=$(printf '%s\nif version_at_least %s %s; then echo at-least; else echo below; fi\n' \
        "$version_rules" "$1" "$2" | sh 2>&1)
    [ "$verdict" = "$3" ] || fail "version_at_least '$1' '$2' answered '$verdict', expected '$3'"
}
expect_version 3.9.6 3.12 below
expect_version 3.12.0 3.12 at-least
expect_version 21.0.12+1 21 at-least
expect_version 3.13.0 3.12 at-least
expect_version 4.0 3.12 at-least

# $1 floor value, $2 expected verdict (valid|invalid)
expect_floor() {
    table="python3|required|tool|yes|python3|--version|$1"
    if printf '%s\nprobe_table=%s\nvalidate_probe_table\n' "$shape_rules" "'$table'" | sh >/dev/null 2>&1; then
        verdict=valid
    else
        verdict=invalid
    fi
    [ "$verdict" = "$2" ] || fail "the declaration's validator answered '$verdict' for floor '$1', expected '$2'"
}
expect_floor "" valid
expect_floor 21 valid
expect_floor 3.12 valid
expect_floor . invalid
expect_floor 3. invalid
expect_floor 3.x invalid
expect_floor 3.12. invalid
expect_floor 3.12.0 invalid

# --- 5. BELOW-FLOOR BEHAVIOUR ---------------------------------------------------------------------------------------
# A stub PATH drives the default run's verdict, so the below-floor path is exercised on a machine whose own Python is
# fine. The stubs satisfy every other required probe, so the exit status is attributable to Python alone.
fixture_dir=$(mktemp -d)
trap 'rm -rf "$fixture_dir"' EXIT
cat >"$fixture_dir/java" <<'STUB'
#!/bin/sh
echo "openjdk 21.0.12 2026-01-20"
STUB
cat >"$fixture_dir/docker" <<'STUB'
#!/bin/sh
case "$1" in
info) exit 0 ;;
compose) echo "Docker Compose version v2.38.2" ;;
*) echo "Docker version 28.0.4, build b8034c0" ;;
esac
STUB
cat >"$fixture_dir/python3" <<'STUB'
#!/bin/sh
echo "Python $FIXTURE_PY_VERSION"
STUB
chmod +x "$fixture_dir/java" "$fixture_dir/docker" "$fixture_dir/python3"

# $1 the fixture interpreter's version; sets fixture_output and fixture_status
run_fixture() {
    fixture_output=$(PATH="$fixture_dir:$PATH" FIXTURE_PY_VERSION="$1" sh "$doctor" 2>&1) && fixture_status=0 ||
        fixture_status=$?
}

run_fixture 3.11.2
[ "$fixture_status" -ne 0 ] ||
    fail "a below-floor python3 (3.11.2) left the default run's exit status at 0"
printf '%s\n' "$fixture_output" | grep -qE "^  python3 +too-old +3\.11\.2 \(needs >= 3\.12\)" ||
    fail "a below-floor python3 (3.11.2) was not reported as too old naming both versions"

run_fixture 3.12.0
[ "$fixture_status" -eq 0 ] || fail "python3 3.12.0 (at the floor) failed the default run"
printf '%s\n' "$fixture_output" | grep -qE "^  python3 +satisfied +3\.12\.0$" ||
    fail "python3 3.12.0 (at the floor) was not reported as satisfied"

run_fixture 3.15.1
[ "$fixture_status" -eq 0 ] || fail "python3 3.15.1 (above the floor) failed the default run"
printf '%s\n' "$fixture_output" | grep -qE "^  python3 +satisfied +3\.15\.1$" ||
    fail "python3 3.15.1 (above the floor) was not reported as satisfied"

echo "doctor declaration is well-formed, and every declared probe is reported"
echo "the floor grammar and comparison hold, and a below-floor interpreter fails the default run"
echo "$(printf '%s\n' "$probe_keys" | grep -c .) probes; required: $required_probes"
