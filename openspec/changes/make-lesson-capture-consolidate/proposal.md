## Why

The `lesson-capture` subagent is `AGENTS.md`'s growth engine, and nothing bounds it: it proposes new bullets with no
obligation to check whether an existing bullet already covers the lesson, so the file reached 1534 lines in five weeks
and only the `agents-auditor` prunes, on demand. The first retrospective measured this and made consolidation its top
structural suggestion; the spec's lesson-capture scenario describes that lessons are captured, but not the shape of what
is proposed.

## What Changes

- The `agent-skills` lesson-capture scenario gains the consolidation obligation: every proposed addition names the
  existing bullet it extends or states that no bullet covers it, and a new rule merges into or replaces one rather than
  accreting.
- The capability's experience-analysis requirement widens a process suggestion's destination to include a subagent
  definition — a process fix can target the subagent contract itself, which is this change's own subject. The widening
  is then swept across every artifact that restates the destination, since four copies carried the narrow form.
- `.opencode/agent/lesson-capture.md`'s output contract states the obligation, so the subagent is instructed to apply
  it.
- `AGENTS.md` gains the matching clause on the capture bullet, plus the routing rule the retrospective's review exposed:
  a subagent/command definition edit that changes **spec'd** behavior is a change, not a docs edit — it owes that spec's
  delta — while a definition-only edit the spec does not describe (a model-pin bump) still ships as a change but with no
  delta, as `switch-flash-agent-to-v4-1` did with `skip_specs: true`. The architecture-auditor bullet's "agent-tooling
  clarification lands as a docs PR" is corrected, since it reads as sanctioning the route this change forbids.
- The retrospective's suggestion table records that suggestion 2 shipped as this change rather than "own change".

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the lesson-capture scenario requires that a proposed addition name the bullet it
  extends or say that none covers it; and a process suggestion's destination includes a subagent definition.

## Impact

- `openspec/specs/showcase/quality/agent-skills/spec.md` — two requirement blocks modified (the lesson-capture scenario
  and the experience-analysis classification), applied at archive.
- `.opencode/agent/lesson-capture.md` — the output contract gains the consolidation obligation.
- `.opencode/agent/experience-analyzer.md` and `.opencode/commands/retrospective.md` — their `process` → `AGENTS.md`
  phrasing widened to match the spec.
- `AGENTS.md` — the capture bullet gains the matching clause; the subagent/command sweep bullet gains the routing rule;
  the experience-analysis bullet and the architecture-auditor bullet's docs-PR sentence are corrected.
- `README.md` — its retrospective bullet's `process` → `AGENTS.md` phrasing widened, and its two `lesson-capture`
  descriptions gain the consolidation clause (or the no-edit decision is recorded).
- `docs/retrospectives/2026-09-16.md` — the suggestion table's disposition for suggestion 2 points at this change.
- No code or deployment change.
