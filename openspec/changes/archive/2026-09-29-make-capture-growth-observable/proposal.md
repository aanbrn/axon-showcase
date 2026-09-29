# Proposal

## Why

The capture rule says applying a capture should leave `AGENTS.md` no larger than it was, and that any net growth is a
justified decision stated with the proposal — but nothing states or records that delta: the capture's verdict gives
additions and retirements counts, not the net size, and the applying agent records no applied figure, so growth shows
only as the periodic audit's accreted-rule count, after the fact (`AGENTS.md` +523 net lines over nine days with 87 of
118 PRs touching it, while the clause was already in force).

## What Changes

- `.opencode/agent/lesson-capture.md` — the report contract's verdict gains the proposed net `AGENTS.md` delta
  (additions minus retirements, in lines), and the growth paragraph states the applying agent records the applied delta.
- `AGENTS.md` — the capture bullet's growth sentence states that the applying agent records the applied net delta, so
  the merge-time read sees it.
- `.github/PULL_REQUEST_TEMPLATE.md` — the capture checklist line gains the applied delta, so a unit with no change dir
  can record it (the line currently records only that the capture ran).
- `openspec/config.yaml` — the injected `tasks` rule states that the capture task records the applied delta.
- `openspec/changes/make-capture-growth-observable/specs/showcase/quality/agent-skills/spec.md` — the delta: the capture
  scenario gains the proposed-and-recorded-delta clause, synced into
  `openspec/specs/showcase/quality/agent-skills/spec.md` at archive.
- `docs/retrospectives/2026-09-28.md` — S4's disposition cell names this change (its suggestion paragraph is the
  window's record, left as written).

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents are available" requirement's
  capture scenario gains the net-delta reporting and recording clause.

## Impact

- `.opencode/agent/lesson-capture.md` — the report contract and the growth paragraph.
- `AGENTS.md` — the capture bullet's growth sentence.
- `.github/PULL_REQUEST_TEMPLATE.md` — the capture checklist line.
- `openspec/config.yaml` — the injected `tasks` rule.
- `openspec/changes/make-capture-growth-observable/specs/showcase/quality/agent-skills/spec.md` — the delta (synced into
  the main spec at archive).
- `docs/retrospectives/2026-09-28.md` — the S4 disposition cell.
- No code, build, runtime, or deployment change.
