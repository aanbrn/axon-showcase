# Proposal

## Why

The `lesson-capture` subagent runs once per unit after the implementation quick review — a deliberate 2026-09-18 choice
that traded two runs per unit for one. But the one run is unconditional: a unit whose review loop converged cleanly and
touched nothing the agent reads still pays it, and its verdict is usually "nothing durable". The owner's call is to gate
the invocation on the unit's own evidence of a reusable lesson, while keeping the record and the merge-time verification
that the three documented skips (`make-the-capture-visible`) exist to catch.

## What Changes

- `AGENTS.md` — the capture workflow's trigger becomes evidence-gated; a new decision point states the skip condition as
  a closed conjunction over surfaces the unit already produced; the "do not skip the subagent" absolute becomes "do not
  skip outside that condition"; the completion criteria record the decision rather than only the run.
- `.opencode/agent/lesson-capture.md` — its frontmatter `description` and its opening sentence state when the subagent
  is used.
- `showcase/quality/agent-skills` delta — the capture scenario gains the evidence-gated invocation and the recorded
  skip, and its record clause carries the skip; the merge scenario verifies a recorded skip against the merge's own
  evidence.
- `openspec/config.yaml` — the injected `tasks` rule carries the decision, not only the run.
- `.github/PULL_REQUEST_TEMPLATE.md` — the checklist line records the decision for a unit with no change dir.
- `.opencode/commands/opsx-tool-update.md` — its review-gate line names the capture decision rather than the run.
- `README.md` — the `lesson-capture` agent-table row, the "Lesson capture" bullet, and the self-learning loop's per-unit
  sentence.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the per-unit quality-gate and analysis requirement's capture scenario gains the
  evidence-gated invocation and the recorded skip, and its merge-time record scenario verifies a recorded skip.

## Impact

- `openspec/changes/make-the-capture-selective/specs/showcase/quality/agent-skills/spec.md` — one `MODIFIED` requirement
  (its delta), synced into `openspec/specs/showcase/quality/agent-skills/spec.md` at archive.
- `AGENTS.md` — the capture workflow's Trigger, Procedure, Decision points, Failure handling, and Completion criteria;
  the doc-consistency sweep bullet (widened by the unit's capture); and the pre-write pass's single-sourcing clause,
  aligned to the review-gate bullet's set.
- `.opencode/agent/lesson-capture.md` — its frontmatter `description` and its opening sentence.
- `openspec/config.yaml` — the injected `tasks` rule for the per-unit capture decision.
- `.github/PULL_REQUEST_TEMPLATE.md` — one checklist line.
- `.opencode/commands/opsx-tool-update.md` — one bullet's review-gate clause.
- `README.md` — the agent-table row, the "Lesson capture" bullet, and the self-learning loop's per-unit sentence.
- No code, build, test, runtime, or deployment change; the subagent's own behavior, its promotion gate, and its report
  contract are unchanged.
