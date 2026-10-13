# Design

## Context

See `proposal.md` — Why. The evidence is the review loop's own cost: the four review-cost changes (#560–#563) each
needed several rounds to converge. The pass's four classes are stated in `AGENTS.md`'s review-loop bullet and restated
in `openspec/config.yaml`'s existing `proposal`/`design` rules; this change's new rule points at the bullet. The pass's
value is deriving a claim in one of those classes at authoring time rather than finding it by a round.

The repository's working pattern is `openspec/changes/archive/2026-09-29-make-the-capture-visible`: the capture was
unambiguous prose, was skipped twice, and became observable when the unit was made to carry a ticked task for it. S1 is
the same move for a different judgment-sized obligation.

## Goals / Non-Goals

**Goals:**

- Make a skipped pre-write pass a **missing box** — a task in the unit's `tasks.md`, or a checklist line for a unit with
  no change dir — so it is visible rather than reconstructed from recollection.
- Keep this change's new copy pointing at the bullet (which names the classes) rather than adding another restatement of
  the list.

**Non-Goals:**

- A gate that fails a build on a skipped pass — the capture's precedent is a record, not a check, and the pass is a
  judgment the unit's own review already tests.
- Changing what the pass derives or its four classes: this makes the existing obligation visible, nothing more.
- The retrospective's S2 (the documented-pin verification), a separate change.

## Decisions

**A ticked task, not a gate.** The capture's own pattern decided this: a record a reviewer reads at the merge (or the
unit's review reads at round 1) is enough, and a gate would need a machine-checkable definition the pass — a judgment
across four classes — does not have.

**The review-loop bullet is the classes' canonical home.** The bullet already names them; the `openspec/config.yaml`
rule points at the class list's home rather than restating it, and this change's own mentions point at it too.
`ci.yml`'s probe guards the rule's shape, not its wording, so the rule is written to parse as one string (no unquoted
`: `) and to point rather than restate.

**The PR-checklist line is the changeless-unit arm.** A unit with no change dir has no `tasks.md`; the template's line
is where its record lives, and the `AGENTS.md` obligation carries the line into the body (the repository's `--body-file`
convention supplies the whole body, so the template alone does not apply it).

## Risks / Trade-offs

- The task can be ticked without the pass being run → the same residual the capture record carries; the tick is an
  assertion, and the unit's own review loop is the check that it was earned.
- A `config.yaml` rule is injected into generated artifacts → it must parse as a single string; the change includes the
  probe's two arms (a clean create and a known-bad unquoted `: `) as the verification.
- The obligation adds a line to every future change's `tasks.md` → that is the point, and it mirrors the capture step's
  existing injected task.

## Migration Plan

None: an injected rule and a checklist line; existing change dirs are not backfilled.
