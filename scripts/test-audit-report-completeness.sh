#!/usr/bin/env bash
set -eu

# Asserts the auditor-report completeness check's verdict on every state it has to distinguish, so a regression in it
# fails `check` rather than a scheduled audit run nobody reads:
#
#   1. COMPLETE      all three reports present, named and stamped — exit 0, no annotation.
#   2. MISSING       one absent — exit 1, naming that auditor and no other.
#   3. EMPTY         one present but empty — exit 1, naming it.
#   4. NO-STAMP      one present and named but without the input revision — exit 1, naming the revision it lacks.
#   5. UNNAMED       one present and stamped but whose first line does not name its auditor — exit 1, naming the file.
#   6. NO-DIRECTORY  the retained directory itself absent — exit 1, naming all three.
#
# Every case passes its directory explicitly, and the stamped cases pass the revision too, so both arguments are
# exercised; the revision is optional, as it is for a reader running the check by hand.
#
# Usage: ./scripts/test-audit-report-completeness.sh
# Exits non-zero on the first disagreement, naming the case and the observed output.

repo_root=$(cd "$(dirname "$0")/.." && pwd)
check="$repo_root/scripts/audit-report-completeness.sh"

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

[ -f "$check" ] || fail "the completeness check is missing at $check"
[ -x "$check" ] || fail "the completeness check is not executable at $check"

work=$(mktemp -d "${TMPDIR:-/tmp}/audit-report-completeness.XXXXXX")
trap 'rm -rf "$work"' EXIT INT TERM

auditors="agents-auditor specs-auditor architecture-auditor"
revision="0123456789abcdef0123456789abcdef01234567"

# Runs the check against the seeded directory, capturing its output and exit status into $output and $status.
run() {
    set +e
    output=$("$check" "$work/reports" "$@" 2>&1)
    status=$?
    set -e
}

seed() {
    rm -rf "$work/reports"
    mkdir -p "$work/reports"
    for auditor in $auditors; do
        printf '# %s — %s\n\nreport\n' "$auditor" "$revision" >"$work/reports/${auditor}.md"
    done
}

# --- 1. COMPLETE ---------------------------------------------------------------------------------------------------
seed
run "$revision"
[ "$status" -eq 0 ] || fail "complete: expected exit 0, got ${status}: ${output}"
[ -z "$output" ] || fail "complete: expected no annotation, got: ${output}"

# --- 2. MISSING ----------------------------------------------------------------------------------------------------
seed
rm "$work/reports/specs-auditor.md"
run "$revision"
[ "$status" -eq 1 ] || fail "missing: expected exit 1, got ${status}"
case "$output" in
    *specs-auditor*) : ;;
    *) fail "missing: the annotation does not name specs-auditor: ${output}" ;;
esac
case "$output" in
    *agents-auditor* | *architecture-auditor*)
        fail "missing: the annotation names an auditor that is present: ${output}"
        ;;
    *) : ;;
esac

# --- 3. EMPTY ------------------------------------------------------------------------------------------------------
seed
: >"$work/reports/architecture-auditor.md"
run "$revision"
[ "$status" -eq 1 ] || fail "empty: expected exit 1, got ${status}"
case "$output" in
    *architecture-auditor*) : ;;
    *) fail "empty: the annotation does not name architecture-auditor: ${output}" ;;
esac

# --- 4. NO-STAMP ---------------------------------------------------------------------------------------------------
seed
printf '# specs-auditor\n\nreport without a revision\n' >"$work/reports/specs-auditor.md"
run "$revision"
[ "$status" -eq 1 ] || fail "no-stamp: expected exit 1, got ${status}"
case "$output" in
    *"${revision}"*) : ;;
    *) fail "no-stamp: the annotation does not name the missing revision: ${output}" ;;
esac

# --- 5. UNNAMED ----------------------------------------------------------------------------------------------------
seed
printf '# an auditor report\n\n%s\n' "$revision" >"$work/reports/agents-auditor.md"
run "$revision"
[ "$status" -eq 1 ] || fail "unnamed: expected exit 1, got ${status}"
case "$output" in
    *agents-auditor.md*) : ;;
    *) fail "unnamed: the annotation does not name the offending file: ${output}" ;;
esac

# --- 6. NO DIRECTORY -----------------------------------------------------------------------------------------------
rm -rf "$work/reports"
set +e
output=$("$check" "$work/absent" "$revision" 2>&1)
status=$?
set -e
[ "$status" -eq 1 ] || fail "no directory: expected exit 1, got ${status}"
for auditor in $auditors; do
    case "$output" in
        *"$auditor"*) : ;;
        *) fail "no directory: the annotation does not name ${auditor}: ${output}" ;;
    esac
done

echo "audit-report-completeness: 6 cases pass"
