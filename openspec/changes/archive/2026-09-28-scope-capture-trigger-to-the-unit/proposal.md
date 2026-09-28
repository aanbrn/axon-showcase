# Proposal

## Why

`AGENTS.md`'s capture rule was widened to "after a unit's implementation", because the trigger had excluded a non-change
unit (a data/docs refresh) while the same bullet already said "one capture per unit". But the widening touched that one
bullet: the same convention still reads change-scoped in the `lesson-capture` and `review-quick` definitions, the
`agent-skills` and `commit-hygiene` specs, and the README — so a per-unit rule is stated narrowly everywhere the
subagents, the spec, and the human actually read it.

## What Changes

- `.opencode/agent/lesson-capture.md` — its frontmatter `description`, trigger sentence, inputs list, retirement clause,
  marker convention, and promotion-gate source clause widened from the change to the unit.
- `.opencode/agent/review-quick.md` — the review subject widened to the unit, including the durability challenge's
  `for a unit whose diff adds AGENTS.md rules` clause, so a non-change unit that carries `AGENTS.md` rules still gets
  it.
- `showcase/quality/agent-skills` delta — two `MODIFIED` requirements: the per-unit quality-gate and analysis
  requirement (its quick-review scenario, capture scenario, retirement clause and scenario, marker clause, and gate
  source clause) and the shared report-contract requirement (its bearer list), both widened to the unit; the review
  scenario is included because `AGENTS.md`'s review gate is explicitly unit-scoped.
- `showcase/quality/commit-hygiene` delta — the captured-marker requirement's pointer to the marker form updated from
  `captured: <change>` to `captured: <unit>`.
- `README.md` — the self-learning prose and the `lesson-capture` agent-table row widened to the unit, and the
  auto-review bullet widened from "every change" to the unit.
- `.opencode/agent/experience-analyzer.md` — the `per-change` adjective in its opening layer description widened to
  `per-unit` (it names the layer this change widens).
- `AGENTS.md` — its experience-analyzer bullet's `per-change` adjective widened to the unit; its capture rule is
  otherwise already the unit-scoped authority the others are brought into line with.
- `docs/ideas.md` — the parked idea this change implements is removed.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the per-unit quality-gate and analysis requirement's review and capture scenarios,
  the retirement clause and scenario, the marker clause, and the gate's source clause — plus its description's
  `per-change` adjective — and the report-contract requirement's bearer list now read unit-scoped.
- `showcase/quality/commit-hygiene`: the captured-marker requirement's pointer to the marker form now reads
  `captured: <unit>`.

## Impact

- `.opencode/agent/lesson-capture.md` — the change-scoped wording widened to the unit.
- `.opencode/agent/review-quick.md` — the review subject and the durability clause widened to the unit.
- `.opencode/agent/experience-analyzer.md` — the `per-change` adjective in its opening layer description widened to
  `per-unit`.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — two requirements modified (a delta in this change), plus its
  `## Purpose` refreshed in the archive commit (a delta cannot carry one).
- `openspec/specs/showcase/quality/commit-hygiene/spec.md` — one requirement modified (a delta in this change).
- `README.md` — the self-learning intro line, the agent-table `lesson-capture` row, the `Code review` bullet, the
  `Lesson capture` bullet, and the `Every change closes the loop` bullet.
- `AGENTS.md` — the experience-analyzer bullet's `per-change` adjective widened; the capture rule is otherwise the
  unit-scoped authority and needs no edit.
- `docs/ideas.md` — the parked "widen the capture trigger's scope" entry removed in the same change.
- No code, build, runtime, or deployment change. `scripts/commit-hygiene.py` and the pre-commit guard treat `captured:`
  mechanically (placement only), so the marker-form change needs no script change.
