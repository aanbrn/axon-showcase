#!/bin/bash
set -euo pipefail

# Gathers the deterministic inputs for the experience-analyzer subagent: merged PRs, the git log, archived OpenSpec
# changes, AGENTS.md gotchas, docs/ideas.md, and a derived-facts block resolving the window's countable claims. The
# window applies to the events — merged PRs and the git log; the archived changes, AGENTS.md gotchas, and docs/ideas.md
# are current state and are gathered in full. Prints a structured digest to stdout for the main agent to feed to
# `.opencode/agent/experience-analyzer.md`.
# Run it through `/retrospective`: the digest is agent input, not a contributor report.
# Usage: ./scripts/experience-analysis.sh [since]   (since defaults to 7 days ago, e.g. 2026-09-02)
#
# The derived-facts block is the authoritative source for the retrospective's countable claims (the merged-PR boundary,
# the archived-change count, AGENTS.md's metrics, the window's captured: lines, and the window's review rounds): read
# them from there rather than re-deriving them by hand. Both the PR and the archive counts carry the same `since`
# boundary — the search and `git log --since` are inclusive of the `since` date — so the block names the `since`-date
# items and the previous retrospective's file, and the author resolves the split against it. The review-rounds block is
# printed by `scripts/review-rounds.py`: the window proper's figures beside the `since`-inclusive ones, and the classes
# normalized to the reviewer's canonical vocabulary.

# Portable 7-days-ago: BSD date (macOS) uses -v-7d, GNU date (Linux) uses --date=-7day.
if [ $# -gt 0 ]; then
  SINCE="$1"
elif date -v-7d >/dev/null 2>&1; then
  SINCE="$(date -v-7d +%Y-%m-%d)"
else
  SINCE="$(date --date='7 days ago' +%Y-%m-%d)"
fi

# A git window is wall-clock dependent when the date is bare: a date-only --since excludes commits before the current
# time of day on that date, so the count would vary run to run. Pin the start (and end) of the day instead.
SINCE_TS="${SINCE}T00:00:00"
UNTIL_TS="${SINCE}T23:59:59"

# AGENTS.md metrics at a git ref: total lines, top-level bullets (a line starting "- "), and lines mentioning
# "captured:" (the marker lines, plus the few prose mentions of the token).
agents_metrics() {
  git show "$1:AGENTS.md" 2>/dev/null | awk '
    { lines++ }
    /^- / { bullets++ }
    /captured:/ { captured++ }
    END { printf "%d lines, %d top-level bullets, %d captured: lines", lines, bullets, captured }'
}

# The captured: marker tokens at a git ref, tolerating the wrap between "captured:" and the token.
agents_tokens() {
  git show "$1:AGENTS.md" 2>/dev/null | perl -0777 -ne 'while (/captured:\s*([a-zA-Z0-9_-]+)/g) { print "$1\n" }' \
    | sort -u
}

# The archive dirs the commits in a window added.
archive_dirs() {
  git log --since="$1" --diff-filter=A --name-only --format= -- openspec/changes/archive/ \
    | { grep 'proposal.md$' || true; } | sed 's#/proposal.md$##' | sort -u
}

echo "=== Window: since ${SINCE} ==="
echo
echo "=== Derived facts (read these instead of deriving them by hand) ==="
PR_TOTAL="$(gh pr list --state merged --search "merged:>=${SINCE}" --limit 1000 --json number --jq 'length')"
echo "Merged-PR search (merged:>=${SINCE}) returned ${PR_TOTAL} — inclusive of the ${SINCE} date and open-ended, so"
echo "resolve the split (the previous window's tail) against the previous retrospective below."
echo "PRs merged ON ${SINCE}:"
gh pr list --state merged --search "merged:${SINCE}" --limit 1000 --json number,title \
  --jq '.[] | "  #\(.number) \(.title)"'
PREV_RETRO="$(ls docs/retrospectives/*.md 2>/dev/null \
  | awk -v s="${SINCE}" -F/ '{ d=$NF; sub(/\.md$/, "", d); if (d <= s) last=$0 } END { print last }')"
echo "Previous retrospective (the newest dated <= ${SINCE}): ${PREV_RETRO:-none on disk}"
ARCH_TOTAL="$(archive_dirs "${SINCE_TS}" | grep -c . || true)"
echo "Archived changes the window's commits added: ${ARCH_TOTAL}"
echo "Archive dirs added ON ${SINCE} (resolve as above):"
git log --since="${SINCE_TS}" --until="${UNTIL_TS}" --diff-filter=A --name-only --format= -- \
  openspec/changes/archive/ | { grep 'proposal.md$' || true; } | sed 's#/proposal.md$##; s#^#  #'
echo "AGENTS.md at HEAD: $(agents_metrics HEAD)"
echo "AGENTS.md just before each ${SINCE} PR — read the window's baseline off the first counted PR:"
gh pr list --state merged --search "merged:${SINCE}" --limit 1000 --json number,mergeCommit \
  --jq '.[] | "\(.number)\t\(.mergeCommit.oid)"' | while IFS=$'\t' read -r num sha; do
    if [ -z "${sha}" ] || [ "${sha}" = "null" ]; then continue; fi
    echo "  before #${num}: $(agents_metrics "${sha}^")"
  done
echo "captured: markers added since ${SINCE} (the ${SINCE} tail included — filter to the window's own PRs):"
RAW_BASE="$(git log --since="${SINCE_TS}" --format='%H' | tail -n 1 || true)"
if [ -n "${RAW_BASE}" ]; then
  comm -13 <(agents_tokens "${RAW_BASE}^") <(agents_tokens HEAD) | while IFS= read -r token; do
    if [ -z "${token}" ]; then continue; fi
    echo "  ${token} (introduced by $(git log -S"${token}" --format='%h %ad %s' --date=short -- AGENTS.md \
      | tail -n 1))"
  done
fi
python3 "$(dirname "$0")/review-rounds.py" --since "${SINCE}"
echo
echo "=== Merged pull requests ==="
gh pr list --state merged --search "merged:>=${SINCE}" --limit 1000 --json number,title,mergedAt,mergedBy \
  --jq '.[] | "\(.number) | \(.mergedAt[0:10]) | \(.mergedBy.login) | \(.title)"'
echo
echo "=== Git log ==="
git log --since "${SINCE_TS}" --oneline
echo
echo "=== Archived OpenSpec changes (all, not windowed) ==="
ls openspec/changes/archive/
echo
echo "=== AGENTS.md gotchas (current, not windowed) ==="
sed -n '/^## Gotchas/,/^## /p' AGENTS.md
echo
echo "=== docs/ideas.md (current, not windowed) ==="
cat docs/ideas.md
