# Proposal

## Why

A unit's process lessons are generalized inline: the `lesson-capture` analysis runs between the implementation's quick
review and the manual review pass, after a four-clause skip condition decides whether it runs at all. That puts a
judgment-heavy pass on the critical path of every product change, and it assumes the session that produced the defect is
the one that analyzes it — although the spec records of a neighbouring agent's input that it is "available only in the
session that lived it", a property this analysis needs by analogy.

The process must also work for a team. Recording a defect is work the implementing unit can do in the moment; analyzing
it and improving the agent process is a separate unit that a different person can own, on the owner's schedule. No ADR,
parked idea, or spec passage states why the analysis has to be inline.

## What Changes

- **Add** a per-unit process-defect record at `docs/process-defects/<unit>.md`: one file per unit, self-contained
  fields, written when the defect is observed, without cause analysis or a proposed improvement.
- **Add** the record-currency precondition to `review-gate` §5 — a current record, or an explicit statement that the
  unit observed no process defect. The manual review pass no longer waits for an analysis.
- **Add** an on-demand command, `/generalize-process-defects`, that hands selected merged records to the existing
  `lesson-capture` subagent and appends each record's decision and link; the calling unit designs and implements the
  approved improvements.
- **Keep unchanged** the analysis contract — promotion gate, evidence threshold, dispositions, owning destinations,
  growth bound, `captured:` provenance, and retirement candidates — now applied in the generalizing unit.
- **Retire** the four-clause skip condition, the merge-time capture, and the per-unit capture record: each selected the
  need for an inline analysis the result no longer performs.
- **Record** the functional division — who records, who generalizes, who chooses the moment, who approves — in
  `AGENTS.md`, with the owner as the default generalizer and any other assignment named in the unit's own tasking.
- **Bound** the generalizing unit's PR: the originating record ships with the product unit; the improvement is its own
  unit on its own branch from a fresh `origin/main`, consuming records that are already merged.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the capture requirement is retitled and split into recording, a product-unit gate, a
  separately-run generalization, the carried analysis contract, the carried retirement and authoring-mechanism
  scenarios, and the configuration-independent role outcome.

## Impact

- `openspec/specs/showcase/quality/agent-skills/spec.md` at archive, through the delta's `REMOVED`, `ADDED`, and
  `MODIFIED` blocks.
- `AGENTS.md`: the capture workflow becomes a recording workflow plus a generalization workflow, with the role division.
- `.opencode/skills/review-gate/SKILL.md`: the §5 precondition, the §2 capture input, the §1 figure's source, and the
  completion criteria.
- `.opencode/skills/ship-change/SKILL.md`: the docs-bullet's capture wording.
- `.opencode/agent/lesson-capture.md`: its trigger and input list now come from the generalizing unit.
- `.opencode/agent/experience-analyzer.md` and `.opencode/agent/review-quick.md`: the layer description and the review
  input that call `lesson-capture` a per-unit agent.
- `.opencode/skills/check-and-test-authoring/SKILL.md`: the new mode recording that a catch-all disposition cannot fail,
  from this unit's analysis.
- `.opencode/commands/generalize-process-defects.md`: new trigger command.
- `openspec/config.yaml` (the `tasks` rules) and `.github/PULL_REQUEST_TEMPLATE.md`: the per-unit task becomes the
  defect-record task.
- `README.md`: the Technologies prose, the agent table row, the "Lesson capture" bullet, "Applying suggestions", the
  self-learning loop, and the slash-command table.
- `docs/ideas.md`: its header where it names a merge-time capture, and the two live entries that name the capture record
  as if the per-unit stage stayed.
- `.opencode/skills/pause-resume-unit/SKILL.md`: its Stage field, which lists "the capture" among a unit's stages.
- `docs/process-defects/`: a new record corpus; this change ships its own record as the format's worked example.
- `openspec/specs/showcase/quality/agent-skills/spec.md`'s `## Purpose` at archive: its "per-unit … lesson-capture"
  phrasing.
- No code, API, dependency, build, test, deployment, or container change.
