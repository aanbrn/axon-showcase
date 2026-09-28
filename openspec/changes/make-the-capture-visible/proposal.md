# Proposal

## Why

`AGENTS.md`'s capture rule is unambiguous — run the `lesson-capture` subagent once per unit — yet the subagent was
skipped twice in the last retrospective window (`scope-capture-trigger-to-the-unit`, `check-unique-cron-schedules`),
each skip surfacing only at the merge-time read, and an identical skip was recorded before that window. A judgment-sized
obligation stated only as prose leaves no trace when it is skipped: a skipped run looks exactly like a run that found
nothing. What is missing is not more prose but a record — a box the unit carries, so an unrun capture is visibly missing
at the merge.

## What Changes

- `openspec/config.yaml` — add a `tasks` rule so every change's `tasks.md` carries the per-unit `lesson-capture` step as
  a visible, tickable task.
- `.github/PULL_REQUEST_TEMPLATE.md` — add a checklist line recording the capture, carried into the body by the
  `AGENTS.md` obligation (the PR convention supplies the body from a file, bypassing the template), so a unit with no
  change dir (a docs refresh, a standalone fix, a dependency bump) carries the record too.
- `AGENTS.md` — the capture bullet states the record and that the merge-time read verifies it; the docs-refresh bullet's
  `openspec/config.yaml` copy sentence is widened to cover the per-artifact `rules` alongside the `context:` block,
  since the injected rule is a second copy of the capture rule.
- `.opencode/agent/lesson-capture.md` — assessed, no edit: the record is planned and verified by the main agent around
  the subagent's run, and the subagent cannot observe its own skip.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — delta: the per-unit capture requirement gains the record
  obligation and a scenario making a skipped capture observable at the merge.
- `README.md` — the self-learning bullet notes the visible record.
- `docs/retrospectives/2026-09-28.md` — S1's disposition names this change, as the analogous changes did for their
  retrospective's suggestion.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the per-unit quality-gate and analysis requirement's capture scenario gains the
  record obligation, and a new scenario makes an unrun capture a missing record at the merge.

## Impact

- `openspec/changes/make-the-capture-visible/specs/showcase/quality/agent-skills/spec.md` — one `MODIFIED` requirement
  (its delta), synced into `openspec/specs/showcase/quality/agent-skills/spec.md` at archive.
- `openspec/config.yaml` — one `tasks` rule added, written to parse as a single string (no unquoted `: `).
- `.github/PULL_REQUEST_TEMPLATE.md` — one checklist line.
- `AGENTS.md` — the capture bullet and the docs-refresh bullet's config-copy sentence.
- `README.md` — the self-learning bullet in the Agentic Process section.
- `docs/retrospectives/2026-09-28.md` — the S1 disposition cell.
- No code, build, test, runtime, or deployment change.
