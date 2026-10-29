# Process defects — trim-readme-practices-density

### trim-readme-practices-density#1 — An edit anchor was reused after the formatter rewrote the paragraph

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — take an edit anchor from a read that follows `spotlessApply`, since the formatter re-wraps prose and an
anchor saved before it is stale (`AGENTS.md`'s "Never chain an edit to a commit" rule states it).

**Observed** — the second `README.md` edit in the dependency-updates section failed with `Could not find oldString`. Its
anchor had been copied from the `git diff` output captured before `spotlessApply` re-wrapped that section; the formatter
had moved `own audit rather than Snyk` onto a different line, so the saved text no longer matched.

**Evidence** — the edit tool returned `{"error":{"type":"tool.execution","message":"Could not find oldString …"}}`; a
`sed -n '/^### Dependency Updates and Security/,/^### Load Testing/p' README.md` read afterwards showed the current
wrapping, and the retry built on that read applied.

**Consequence** — one failed edit and one re-read; no file state was changed by the failure.

**Knowledge boundary** — observed: the stale anchor, the tool's error, the re-read, and the applied retry. Not checked:
whether a later edit in the same session reused a pre-formatter anchor, which reading each edit's anchor source would
show.

### trim-readme-practices-density#2 — Gradle logs written to `/tmp` instead of `$TMPDIR/opencode`

**Recorded** — 2026-10-29, by the unit's writing session.

**Stage** — implementation.

**Expected** — agent scratch goes under `$TMPDIR/opencode`, which OpenCode v2 pre-approves (`AGENTS.md`'s "Agent scratch
files" convention), so no external grant is needed.

**Observed** — every verification run in this unit redirected its output to `/tmp/sa4*.log`, `/tmp/sc4*.log`,
`/tmp/vtr4*.log`, and `/tmp/check-pract.log`, outside the approved scratch directory; the unit's pull-request body draft
was written to `$TMPDIR/opencode/trim-readme-practices-pr-body.md`, the approved location.

**Evidence** — the shell invocations carry `> /tmp/sc43.log 2>&1` and the sibling paths; the body draft's path is the
`$TMPDIR/opencode` one.

**Consequence** — none to the repository or the run beyond the convention breach; the logs are throwaway.

**Knowledge boundary** — observed: the redirect targets in this session's commands. Not checked: whether those writes
required a permission grant in this environment, which the session's permission log would show.
