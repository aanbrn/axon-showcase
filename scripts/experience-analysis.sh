#!/bin/bash
set -euo pipefail

# Gathers the deterministic inputs for the experience-analyzer subagent: merged PRs, the git log, archived OpenSpec
# changes, AGENTS.md gotchas, and docs/ideas.md. The window applies to the events — merged PRs and the git log; the
# archived changes, AGENTS.md gotchas, and docs/ideas.md are current state and are gathered in full. Prints a structured
# digest to stdout for the main agent to feed to `.opencode/agent/experience-analyzer.md`.
# Run it through `/retrospective`: the digest is agent input, not a contributor report. It lives under `scripts/`
# because that is the repo's home for dev tooling whatever the caller — the human-documented `setup-idea.sh` and its
# script-only helper `ensure-idea-settings.py` sit alongside it — while `.opencode/` has no root-level script location
# (its entry points are agents, commands, skills, and plugins).
# Usage: ./scripts/experience-analysis.sh [since]   (since defaults to 7 days ago, e.g. 2026-09-02)

# Portable 7-days-ago: BSD date (macOS) uses -v-7d, GNU date (Linux) uses --date=-7day.
if [ $# -gt 0 ]; then
  SINCE="$1"
elif date -v-7d >/dev/null 2>&1; then
  SINCE="$(date -v-7d +%Y-%m-%d)"
else
  SINCE="$(date --date='7 days ago' +%Y-%m-%d)"
fi

echo "=== Window: since ${SINCE} ==="
echo
echo "=== Merged pull requests ==="
gh pr list --state merged --search "merged:>=${SINCE}" --limit 1000 --json number,title,mergedAt,mergedBy \
  --jq '.[] | "\(.number) | \(.mergedAt[0:10]) | \(.mergedBy.login) | \(.title)"'
echo
echo "=== Git log ==="
git log --since "${SINCE}" --oneline
echo
echo "=== Archived OpenSpec changes (all, not windowed) ==="
ls openspec/changes/archive/
echo
echo "=== AGENTS.md gotchas (current, not windowed) ==="
sed -n '/^## Gotchas/,/^## /p' AGENTS.md
echo
echo "=== docs/ideas.md (current, not windowed) ==="
cat docs/ideas.md