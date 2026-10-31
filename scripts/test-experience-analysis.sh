#!/bin/sh
set -eu

# Controls the digest's process-defect index (`scripts/experience-analysis.sh`) by driving the real entry point over a
# temporary repository, with a stubbed `gh` so no network call runs.
#
# The fixtures discriminate what a hand-run against the real repository cannot: a record touched only before the window
# (not selected), one touched before and again inside it, one two window commits touch (selected once, not twice), one
# on the boundary day (selected, and named in the boundary list), one after it, one with no `###` heading (listed with
# zero, not dropped), one deleted inside the window (listed as absent), one whose path carries a space and a non-ASCII
# character (read `-z`-safely), and an empty selection (stated, never silent).
#
# Usage: ./scripts/test-experience-analysis.sh
# Exits non-zero on the first disagreement, naming the assertion.

repo_root=$(cd "$(dirname "$0")/.." && pwd)
digest="$repo_root/scripts/experience-analysis.sh"

fail() {
    echo "FAIL: $1" >&2
    exit 1
}

[ -f "$digest" ] || fail "digest script not found at $digest"

work=$(mktemp -d "${TMPDIR:-/tmp}/experience-analysis-test.XXXXXX")
trap 'rm -rf "$work"' EXIT
fixture="$work/repo"
stub_bin="$work/bin"
mkdir -p "$fixture/docs/process-defects" "$fixture/docs/retrospectives" \
    "$fixture/openspec/changes/archive" "$stub_bin"

# The stubbed gh answers with an empty PR list. It emulates the two filter shapes the digest uses over that list —
# `length` yields 0 and `.[] | …` yields nothing — and fails loudly on any other shape, so a changed call is reported
# rather than silently passing. No host `jq` is required: the real digest uses the `gh` CLI's built-in `--jq`.
cat >"$stub_bin/gh" <<'GH'
#!/bin/sh
filter=''
while [ "$#" -gt 0 ]; do
    if [ "$1" = "--jq" ]; then
        filter="$2"
        shift 2
        continue
    fi
    shift
done
case "$filter" in
    length) echo 0 ;;
    '.[]'*) : ;;
    *)
        echo "stub gh: unexpected --jq filter: ${filter}" >&2
        exit 1
        ;;
esac
GH
chmod +x "$stub_bin/gh"

cd "$fixture"
git init -q .
git config user.email "test@example.com"
git config user.name "test"

commit() {
    GIT_AUTHOR_DATE="$1T12:00:00" GIT_COMMITTER_DATE="$1T12:00:00" git commit -q -m "$2"
}

record() {
    printf '%s\n' "$2" >"$1"
}

printf '# fixture instructions\n\n## Gotchas\n\n- a fixture gotcha\n' >AGENTS.md
printf '# fixture ideas\n' >docs/ideas.md
printf '# Retrospective — fixture\n' >docs/retrospectives/2026-10-23.md
git add -A
commit 2026-10-20 "base"

record "docs/process-defects/before-only.md" "### before-only#1 — Touched only before the window"
record "docs/process-defects/older-updated.md" "### older-updated#1 — Created before the window, updated inside it"
record "docs/process-defects/gone.md" "### gone#1 — Created before the window, deleted inside it"
git add -A
commit 2026-10-23 "records before the window"

record "docs/process-defects/boundary.md" "### boundary#1 — Created on the boundary day"
git add -A
commit 2026-10-24 "boundary-day record"

printf '%s\n' "- **Decision** — no-change." >>"docs/process-defects/older-updated.md"
git add -A
commit 2026-10-25 "update the older record inside the window"

record "docs/process-defects/after.md" "### after#1 — First incident
### after#2 — Second incident"
record "docs/process-defects/no-heading.md" "A record whose prose carries no incident heading."
record "docs/process-defects/with space-ü.md" "### spaced#1 — A path with a space and a non-ASCII character"
printf '%s\n' "- **Disposition** — applied." >>"docs/process-defects/older-updated.md"
git add -A
git rm -q "docs/process-defects/gone.md"
commit 2026-10-26 "records after the boundary, a second touch inside the window, and one deletion"

indexed_rev=$(git rev-parse HEAD)

out=$(PATH="$stub_bin:$PATH" bash "$digest" 2026-10-24) \
    || fail "the digest exited non-zero on the fixture window"

contains() {
    printf '%s\n' "$out" | grep -Fq -- "$1" || fail "the index is missing: $1"
}

absent() {
    if printf '%s\n' "$out" | grep -Fq -- "$1"; then
        fail "the index should not carry: $1"
    fi
}

contains "indexed at ${indexed_rev}"
contains "6 record file(s), 5 incident heading(s)"
contains "docs/process-defects/older-updated.md (1 incident heading(s))"
older_line="docs/process-defects/older-updated.md (1 incident heading(s))"
older_lines=$(printf '%s\n' "$out" | grep -cF -- "${older_line}" || true)
[ "$older_lines" -eq 1 ] || fail "a record two window commits touched is listed ${older_lines} times, expected once"
contains "docs/process-defects/boundary.md (1 incident heading(s))"
contains "docs/process-defects/after.md (2 incident heading(s))"
contains "### after#2 — Second incident"
contains "docs/process-defects/no-heading.md (0 incident heading(s))"
contains "docs/process-defects/gone.md (absent at ${indexed_rev}"
contains "docs/process-defects/with space-ü.md (1 incident heading(s))"
absent "docs/process-defects/before-only.md"
absent "Touched only before the window"

boundary_section=$(printf '%s\n' "$out" | sed -n '/Process-defect records touched ON 2026-10-24/,/^Review rounds/p')
printf '%s\n' "$boundary_section" | grep -Fq -- "docs/process-defects/boundary.md" \
    || fail "the boundary list is missing the boundary-day record"
if printf '%s\n' "$boundary_section" | grep -Fq -- "docs/process-defects/after.md"; then
    fail "the boundary list carries a record no boundary-day commit touched"
fi

out=$(PATH="$stub_bin:$PATH" bash "$digest" 2026-11-01) \
    || fail "the digest exited non-zero on an empty window"
contains "0 record file(s), 0 incident heading(s)"
contains "(none selected: no docs/process-defects/ record changed since 2026-11-01)"

echo "OK: the process-defect index selects, counts, and states its boundary and empty cases"
