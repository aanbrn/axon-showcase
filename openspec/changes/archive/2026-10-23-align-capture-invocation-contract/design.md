# Design

## Context

See `proposal.md` — Why. The evidence for the contradiction, read from the tree at the planning base:

- `AGENTS.md`'s capture workflow: the capture "is owed unless the skip condition holds in full", and step 1 says the
  condition "is the only case that may go without a run".
- The `agent-skills` requirement (in `main`): "the main agent decides from the unit's own evidence whether it shows a
  candidate lesson, and invokes the `lesson-capture` subagent only when it does".
- `openspec/changes/archive/2026-10-18-make-the-capture-selective/design.md`: "the default is to invoke", and the
  alternative "a list of reasons to run" is rejected because it defaults to skip.

The constraint that shapes the fix: a `MODIFIED` block replaces the whole requirement, so it must carry all four
scenarios and cannot rename them; the requirement's description is subject to the 500-character strict guidance.

## Goals / Non-Goals

**Goals:**

- One answer to "invoke or skip?" across the spec, the README, and the definition's usage line.
- The skip conjunction stays single-sourced in `AGENTS.md`; every other site states the outcome and points at it.
- The spec's four scenarios and their obligations survive unchanged apart from the invocation clause.

**Non-Goals:**

- Consolidating the capture stage's restated clauses (the parked follow-on).
- Changing the skip conjunction, the promotion gate, thresholds, dispositions, destinations, or the capture count.
- Adding a machine check, a recovery for an unparseable report, or audit/trend tooling.
- Editing `AGENTS.md` or `openspec/config.yaml`, which already state the adopted rule.

## Decisions

### D1 — Fix the spec's outcome and point at the owner, without restating the conjunction

The first scenario's `THEN` states the default invocation, the exception (the conjunction holds in full), and the
record; the conjunction's clauses stay in `AGENTS.md`. The decision's rationale — a skip is a closed conjunction over
surfaces the unit already produced, not a judgment about whether the work felt instructive — stays in `AGENTS.md` and
the archived decision; the spec states the outcome only.

- **Rejected — restate the conjunction in the spec**: a second copy of a closed condition is the drift surface the
  single-sourcing rule names, and the archived decision deliberately keeps it at the owner.
- **Rejected — change `AGENTS.md` instead**: the root is correct; the defect is the copy that disagrees with it.

### D2 — Align the four sites, and only those

`README.md` (the agent table row and two prose sentences) and the definition's usage line state the same precondition;
each becomes the outcome. The definition's input contract ("given a unit whose implementation is done and whose quick
review is clean") is left alone — it describes the hand-over, not the invocation rule.

- **Rejected — leave the prose**: a human-facing and a delegation-facing copy that contradict the spec reintroduce the
  same ambiguity at the point a caller reads.
- **Rejected — rewrite the surrounding sections**: they state behaviour this change does not decide.

### D3 — No new check

The fix is prose alignment. A grep for the inverted phrasing is a verification step, not a gate; a durable check would
need a semantic comparison the repository does not have.

### D4 — The smoke-run proves the caller's decision boundary

The invoke-or-skip decision belongs to the caller, so the check exercises the caller, not the subagent it already
invoked. A unit that changed an authoring surface with no candidate lesson noticed must still invoke the capture — the
old predicate would have skipped it — and a unit where the whole skip conjunction holds must skip and record its reason.
Both fixtures may lack a noticed candidate lesson, which is exactly what separates the two policies. The check also
reads that a compliance-miss finding requires the capture and that the merge-time re-verification stays mandatory.

## Risks / Trade-offs

| Risk                                                 | Mitigation                                                                                                |
| ---------------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| The `MODIFIED` block drops a scenario or renames one | Carried by copying the block and editing only two spans; `openspec validate --strict` checks preservation |
| A prose site is missed                               | The verification greps every live copy of the inverted phrasing, with a known-match control               |
| The new description exceeds the strict length        | Measured (488 characters) after the final description; `--strict` re-checks it                            |
| The fix reads as a policy change                     | The archived decision is cited in the proposal and the design as the adopted direction                    |

## Claims table

| Claim                                                                            | Source                                                                                                                                                                                                                   |
| -------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The root requires the capture unless the skip conjunction holds in full          | `AGENTS.md`, the capture workflow's step 1 and its decision point                                                                                                                                                        |
| `openspec/config.yaml` already states the correct rule                           | its `rules.tasks` capture rule: "run the `lesson-capture` subagent when the skip condition does not hold"                                                                                                                |
| The spec required invocation "only when" a candidate lesson is noticed           | `openspec/specs/showcase/quality/agent-skills/spec.md`, the requirement's first scenario at the planning base                                                                                                            |
| The archived decision fixed the default-invoke model and rejected reasons-to-run | `openspec/changes/archive/2026-10-18-make-the-capture-selective/design.md`, its decision section                                                                                                                         |
| Four live sites stated the evidence-gated precondition before this change        | `git show HEAD:README.md \| grep -nE -e 'candidate lesson' -e 'shows a lesson' -e 'shows evidence' -e 'evidence-gated'` → `:383`, `:430`, `:492`; the same over `git show HEAD:.opencode/agent/lesson-capture.md` → `:6` |
| The requirement carries four scenarios                                           | a block-scoped extraction: print the lines between the requirement header and the next `### Requirement:` and count `#### Scenario:` in that slice (the file-wide count is 232)                                          |
