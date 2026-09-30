#!/usr/bin/env bash
set -eu

# Asserts the doctor's declared probe set, its behaviour, and its agreement with the README and the spec — three
# assertions, each covering a failure the others cannot:
#
#   1. SHAPE      the declaration is well-formed and unambiguous, validated by the doctor's own rules (invoked, not
#                 restated, so the two cannot drift) — so a malformed row cannot hide a probe.
#   2. BEHAVIOUR  the doctor actually REPORTS every declared probe: it is run and its output matched. A content
#                 assertion cannot see a probe that never reaches the code; this one can.
#   3. AGREEMENT  the declared set against the README's Prerequisites rows and the spec's required set — the class
#                 assertion the check previously lacked.
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
    "Python 3") echo "python3" ;;
    "Kubernetes cluster") echo "kubectl" ;;
    "OpenCode" | "OpenCode (v2 recommended)") echo "opencode" ;;
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
# "Java, Docker with Compose", and the repo-state requirement makes the Docker daemon the required exception. Held here
# rather than read from the table, so the comparison is against the spec rather than the declaration against itself.
expected_required="compose docker docker-daemon java"
required_probes=$(for key in $probe_keys; do
    [ "$(probe_field "$key" 2)" = "required" ] && printf '%s\n' "$key"
done | sort | tr '\n' ' ' | sed 's/ $//')
if [ "$required_probes" != "$expected_required" ]; then
    fail "the declared required probes are '$required_probes', not '$expected_required' — a probe's class changed,
or the required set no longer matches the default build-and-test path the spec names. Each probe's declared class:
$(for key in $probe_keys; do printf '  %s (%s)\n' "$key" "$(probe_field "$key" 2)"; done)"
fi

echo "doctor declaration is well-formed, and every declared probe is reported"
echo "$(printf '%s\n' "$probe_keys" | grep -c .) probes; required: $required_probes"
