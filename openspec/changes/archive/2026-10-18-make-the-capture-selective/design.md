# Design

## Context

The capture's **invocation** is the one part of the capture loop that was never evidence-gated. Three prior changes
shaped it, and this change continues their direction rather than reversing the loop:

- `2026-09-18-sharpen-the-capture` cut the run from twice per unit to once — "Both cost more than they return", because
  the post-merge run mostly re-read what the implementation run already had.
- `2026-09-28-scope-capture-trigger-to-the-unit` widened the trigger from the change to the unit.
- `2026-09-29-make-the-capture-visible` recorded **three skips** of the capture — twice in one retrospective window
  (`scope-capture-trigger-to-the-unit`, `check-unique-cron-schedules`), once earlier — each surfacing only at the merge,
  and chose a _record_ over more prose: "a skipped run looks exactly like a run that found nothing."

So the main-agent-decides model this change adopts is the one that produced those three skips. The premise was
interrogated before designing — the three changes above, `docs/adr/` (no ADR covers the capture's invocation), and
`docs/ideas.md` (two parked capture ideas, neither about the invocation) — and the owner confirmed the direction
explicitly, with the skip's record and the merge-time verification as the mitigation for that failure mode.

## Goals / Non-Goals

**Goals:**

- The implementation capture is invoked unless the skip condition in `AGENTS.md`'s capture workflow holds in full, and
  skipped when it does.
- A skip is a **recorded, checkable decision** — the record `make-the-capture-visible` shipped — not an invisible
  judgment, and the merge-time read re-verifies it.
- The promotion gate, the report contract, the retirement and provenance rules, and the subagent's behavior are
  unchanged.

**Non-Goals:**

- Changing the merge-time detection, which stays mandatory: the main agent cannot read a merge's non-diff effects, and
  that is where the documented skips surfaced.
- Making the subagent decide whether it was needed. It cannot observe its own invocation; the decision is the calling
  agent's, around the run.
- Automating the decision as a check. It reads a judgment about a candidate lesson, and the class it must not miss — a
  process mistake with no diff trace — has no mechanical predicate.
- Retitling the requirement header or any scenario: a `MODIFIED` block cannot rename them.

## Decisions

### Decision: the skip is a closed conjunction over surfaces the unit already produced, and the default is to invoke

The trigger becomes evidence-gated, but the gate is a **skip condition** — a conjunction that must hold in full — rather
than a list of reasons to run. The default stays invoke, which is what keeps the model safe against the three recorded
skips: the conjunction fails open.

The condition itself, and the surface each clause reads, are stated once in `AGENTS.md`'s capture workflow; the spec
states only the outcome, per the "specs describe outcomes" rule. The design's part is only why the gate is safe: the
default is invoke and the conjunction must hold in full, so the class the three recorded skips were — a unit with no
signal at all — fails it and the capture runs.

- **Alternative — a list of reasons to run:** the same content inverted, but it defaults to skip, which is the failure
  mode the three recorded skips document.
- **Alternative — the subagent decides whether it was needed:** it cannot observe its own skip, so the decision must be
  the calling agent's (the reasoning `make-the-capture-visible` recorded for leaving this definition alone).

### Decision: the skip rides the capture record, and the merge-time read re-verifies it

The record surface already exists: the `tasks.md` task the `openspec/config.yaml` `tasks` rule injects, or the
pull-request checklist line for a unit with no change dir. This change widens what it carries — the applied net delta
_or_ the skip and its reason — and adds the merge-time re-verification to the existing merge-time read, which already
reports an unrun capture. A skip whose condition no longer holds at the merge is run there.

- **Alternative — no record for a skip:** that is the three-skip failure mode exactly: a skip indistinguishable from a
  run that found nothing.
- **Alternative — a CI gate:** a gate can assert a checkbox exists, not that the decision was sound, so it would pass a
  fabricated tick and give a false green.

### Decision: the definition changes only how its use is stated

The subagent's behavior, gate, and report contract are unchanged; only its frontmatter `description` and its opening
sentence state when it is used. It still receives the unit's diff, the review findings, the change dir, and the main
agent's note, and it is still the arbiter of what the unit taught — the change gates the _invocation_, not the analysis.

## Risks / Trade-offs

The residual risk is the class the skip condition cannot see: a unit for which the condition holds in full and whose
lesson nonetheless leaves no trace in any surface the condition reads. The mitigation is structural rather than complete
— the default is to invoke, the skip must be recorded with its reason, and the merge-time read re-verifies it — so the
failure mode is reduced from an invisible skip to a recorded, reviewable one.

## Claims table

| Claim                                                            | Source / deriving command                                                                                                           |
| ---------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| The capture runs once per unit, after the clean quick review     | `git show HEAD:AGENTS.md`, the capture workflow's Trigger — the change rewrites it, so the anchor is its pre-change state           |
| Two runs per unit "cost more than they return"                   | `openspec/changes/archive/2026-09-18-sharpen-the-capture/proposal.md`                                                               |
| The trigger widened from the change to the unit                  | `openspec/changes/archive/2026-09-28-scope-capture-trigger-to-the-unit/proposal.md`                                                 |
| The capture was skipped three times, each surfacing at the merge | `openspec/changes/archive/2026-09-29-make-the-capture-visible/proposal.md` (names both window instances and the earlier one)        |
| "A skipped run looks exactly like a run that found nothing"      | `.../2026-09-29-make-the-capture-visible/proposal.md`, quoted                                                                       |
| The "do not skip the subagent" rule and its rationale            | `git show HEAD:AGENTS.md`, the capture workflow's Failure handling — the change rewrites it                                         |
| A round's classes and its compliance-miss count are recorded     | the `agent-skills` spec's quick-review scenario's record-line clause                                                                |
| No ADR covers the capture's invocation                           | `grep -rln "lesson-capture" docs/adr/` → no files                                                                                   |
| Neither parked capture idea concerns the invocation              | `grep -n "lesson-capture" docs/ideas.md` → the rejected-proposals and incident-exclusion entries only                               |
| Every site stating the capture's cadence                         | `grep -rniE "lesson-capture\|capture per unit\|per-unit capture" AGENTS.md README.md .opencode/ .github/ openspec/`                 |
| A definition edit owes a smoke-run                               | `.../2026-09-28-scope-capture-trigger-to-the-unit/tasks.md` (6.3) and `.../2026-10-17-capture-authoring-system-gaps/tasks.md` (3.3) |

`scripts/claim-scan.py --base origin/main` also returns candidates that are not claims about evidence, dismissed by
kind: the change's own normative text and the sites it names; this unit's own appended `docs/review-rounds.tsv` rows;
and the pre-existing lines the formatter reflowed.
