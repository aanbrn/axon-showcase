#!/usr/bin/env bash
set -eu

# Asserts the doctor's charter and the README Prerequisites table name the same tool set, so the two cannot drift: the
# README is the documented list, the doctor is the mechanical check of it. The charter is derived from scripts/doctor.sh
# itself (its check_tool/report call sites), not hand-copied, so a tool added there is seen here. The mapping to the
# README is deliberately not 1:1 — a README row may name a compound (`Docker & Compose`) or a state
# (`Kubernetes cluster`) rather than a binary — so each README row is mapped to the probe the doctor uses, and the
# doctor's probes with no README row are declared non-table and asserted genuinely absent from the README.
#
# Usage: ./scripts/test-doctor.sh
# Exits non-zero on the first disagreement, naming the tool.

repo_root=$(cd "$(dirname "$0")/.." && pwd)
doctor="$repo_root/scripts/doctor.sh"
readme="$repo_root/README.md"

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

[ -f "$doctor" ] || fail "doctor script not found at $doctor"
[ -f "$readme" ] || fail "README not found at $readme"

# The doctor's charter, derived from its call sites: every name it checks or reports, de-duplicated, order-stable.
doctor_tools=$(
    {
        grep -oE 'check_tool "[a-z0-9]+"' "$doctor" | sed -E 's/check_tool "//; s/"//'
        grep -oE 'report "[a-z0-9-]+"' "$doctor" | sed -E 's/report "//; s/"//'
    } | awk '!seen[$0]++'
)

[ -n "$doctor_tools" ] || fail "derived no charter from $doctor — the call-site pattern is stale"

# README Prerequisites table rows (the bolded first column).
readme_rows=$(sed -n '/^### Prerequisites/,/^### Get the Sources/p' "$readme" |
    grep -oE '^\| \*\*[^|]+\*\*' |
    sed -E 's/^\| \*\*//; s/\*\*$//')

[ -n "$readme_rows" ] || fail "no README Prerequisites rows matched — the extraction pattern is stale"

# Maps each README row to the doctor charter member(s) that cover it, space-separated. A row with no probe is an error.
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

in_charter() {
    printf '%s\n' "$doctor_tools" | grep -qx "$1"
}

# Direction one: every README row maps to a charter member.
while IFS= read -r row; do
    [ -n "$row" ] || continue
    probe=$(readme_to_probe "$row")
    [ -n "$probe" ] || fail "README Prerequisites row '$row' has no mapping in scripts/test-doctor.sh"
    for p in $probe; do
        in_charter "$p" || fail "README row '$row' maps to '$p', which is not in the doctor's charter"
    done
done <<EOF
$readme_rows
EOF

# Direction two: a charter member with no README row must be a declared non-table probe, and must genuinely be absent
# from the README table — adding a README row for it later forces updating this list rather than passing silently.
# `compose` is a docker subcommand (its README row is `Docker & Compose`); the rest are repo state or a README-absent
# tool (`gh` appears only in the MCP prose, not the table).
non_table_probes="compose gh git-hooks docker-daemon"
for tool in $doctor_tools; do
    covered=0
    while IFS= read -r row; do
        [ -n "$row" ] || continue
        for p in $(readme_to_probe "$row"); do
            [ "$p" = "$tool" ] && covered=1
        done
    done <<EOF
$readme_rows
EOF
    [ "$covered" -eq 1 ] && continue
    printf '%s\n' $non_table_probes | grep -qx "$tool" ||
        fail "doctor charter member '$tool' is neither mapped from a README row nor a declared non-table probe"
done

# Each charter member that is not a repo-state probe must carry an install hint for *every* documented platform, so a
# hint dropped for one platform cannot pass on the other's presence.
state_probes="git-hooks docker-daemon"
for tool in $doctor_tools; do
    if printf '%s\n' $state_probes | grep -qx "$tool"; then continue; fi
    for pl in macos debian; do
        # The hint table is read statically so the test needs no platform shim; the arm must echo a non-empty value,
        # not merely exist, so a blanked hint cannot pass on the label alone.
        grep -qE "^[[:space:]]*${pl}:${tool}\) echo \"[^\"]" "$doctor" ||
            fail "charter member '$tool' has no install hint for platform '$pl' (not a repo-state probe)"
    done
done

echo "doctor charter and README Prerequisites agree ($(printf '%s\n' "$doctor_tools" | grep -c .) probes)"
