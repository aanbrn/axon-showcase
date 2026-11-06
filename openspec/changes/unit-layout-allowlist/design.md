# Design

## Context

`reconcile-review-records` made the finding store hold review rounds only, and made `--finding-instances` fail on a
tracked file there. Its own record shows the gap: the same unit that prompted it also received `continuity-violation.md`
at the unit's root, where that check never looks (`docs/units/assert-paketo-pin-coverage/process-defects.md#3`). An
advisor consultation on this question named the shape: one canonical layout predicate, not a second special case, and
the failure must name the unit and the offending path.

The facts the design rests on:

| Fact                                          | Source                                                                            | Result                                                                                                                                                                |
| --------------------------------------------- | --------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Every tracked file today is one of five kinds | `git ls-tree -r --name-only d420246c -- docs/units`                               | 274 units; every path is `rounds.tsv`, `process-defects.md`, `unit.md`, `review-handover.md`, or `findings/round-<N>.md`                                              |
| Presence varies by record kind                | the same tree, counted per kind at `d420246c`                                     | a rule can police kinds but must not require presence                                                                                                                 |
| No guidance or spec enumerates the five kinds | `grep -rn "review-handover.md" AGENTS.md .opencode docs/contracts openspec/specs` | the paths appear one at a time; the only enumeration was this change's `docs/ideas.md` entry, and the process-defect records' `Expected` fields, which are historical |

## Goals

- A tracked path under `docs/units/<unit>/` is admitted only if the two levels name it — four first-level filenames and
  the direct `findings/round-<N>.md` — so an admitted name in the wrong position fails too.
- The kinds have one owner in the spec, and the finding store's requirement points at it.
- The check is one predicate in one mode, run by one `check` task, with a failure that names the unit, the path and that
  the layout does not admit it.
- A legitimate new kind cannot be introduced by placing a file; it needs the requirement and the check to change
  together.

## Non-Goals

- Requiring presence of any kind. The kinds arrived at different times — the claims table's presence row carries the
  tallies and their command — so presence is each kind's own requirement's subject.
- Widening the pre-commit guard. Its staged selection is built from changed round files, so the layout check runs where
  it already does — in `check` and therefore in the pull request's gate — and the guard keeps its round scope.
- Per-unit files the layout would have to admit for a one-off purpose: a metric dump, a scratch note, a copied upstream
  review. The record's home is the hand-over, `docs/ideas.md`, or the change directory, and admitting one on the spot is
  what this rule exists to prevent.
- Touching the historical process-defect records whose `Expected` fields enumerate the layout; they state what was
  expected at their time.

## Decisions

### One predicate over the whole unit directory, in its own mode

`layout_stray_paths` reads every tracked path under `docs/units/` — recursively, because a path at any position must be
judged — splits it into the unit and the path within it, and admits exactly the four first-level filenames plus the
direct path `findings/round-<N>.md`. Every other complete relative path is returned with its unit. Recursion and
admission are separate: inspection reaches the whole subtree, while the admitted set is two levels and nothing else.

The mode is `--unit-layout`, with its own `check` task `verifyUnitLayout`. Reusing `--finding-instances` was rejected:
the mode's name states the round store's subject, and a root-level file is not that mode's business — the failure would
read as a store violation when the store was never involved.

`--finding-instances` therefore gives up the stray-file clause the previous change gave it, and its two arms move to the
layout's tests. The store's own violations — a round with no file, a file naming an unrecorded round, a class
disagreement — stay where they are.

### The layout requirement owns the kinds; the store's requirement points at it

The finding store's rule is a specialisation of the layout, so the file set lives once. The store's requirement keeps
its rationale, its destination rule for a non-review pass, its round-file content rules, its boundary and its guard
sentence; its file-set sentences become a pointer to the layout requirement. Its sixteen scenarios stay — a MODIFIED
block cannot drop one, and each remains true: the ones that speak of a file the store does not hold are now backed by
the layout requirement, which admits only a round file there.

### Presence stays legal

The requirement admits kinds and says so; a unit carrying only `rounds.tsv` passes, and a unit carrying all five passes.
This is what makes the rule deployable over a corpus that grew its record kinds at different times, and it keeps
`showcase/quality/commit-hygiene`'s existing unit-record requirement the owner of `unit.md`'s presence.

### The failure names the unit, the path and that the layout does not admit it

A message that says only "an unexpected file" leaves the reader to find it, and one that names the path alone leaves the
reason implicit. The mode prints the repository-relative path — which carries the unit and the path — and states that
the layout does not admit it; the test asserts all three parts, since a message missing the condition would pass one
that asserts only the path. The requirement owns that obligation; this section explains why it has three parts rather
than restating it.

## Risks

- **A legitimate future kind lands and the check fails.** That is the design: the failure is the signal that the
  requirement and the check must change in the same change as the first file. The obligation is review's to establish,
  not a second gate's; the cost is one deliberate spec edit.
- **An untracked file passes.** The check reads the tracked set, so a scratch file is invisible to it until staged; the
  guard's and the gate's scopes are unchanged, and a staged file is caught by the next `check`.
- **The move could weaken the store's own check.** Mitigated by keeping every store violation and its tests in place,
  and by the layout's nested case exercising exactly what the store's stray clause caught.

## Claims table

| Claim                                            | Source                                                                 | Result                                                                                          |
| ------------------------------------------------ | ---------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| Every tracked unit path is one of the five kinds | `git ls-tree -r --name-only d420246c -- docs/units`                    | one kind per path at that revision; the working tree adds this unit's own records, all admitted |
| Presence varies                                  | `git ls-tree -r --name-only d420246c -- docs/units`, counted per kind  | `rounds.tsv` 274, `process-defects.md` 126, `unit.md` 19, `review-handover.md` 15               |
| The root-level incident exists                   | `docs/units/assert-paketo-pin-coverage/process-defects.md#3`           | `continuity-violation.md` invented at the unit root and deleted uncommitted                     |
| The store's check cannot see it                  | `git show d420246c:scripts/commit-hygiene.py`, its `store_stray_files` | it walks only paths whose second component is `findings`                                        |
| No guidance or spec enumerates the five kinds    | `grep -rn "review-handover.md"` over the guidance and specs            | one path named at a time; no set                                                                |
| A new mode needs four declarations               | `scripts/commit-hygiene.py` and its test                               | `MODE_FLAGS`, the parser's mode group, the dispatch, `MODE_TEST_COVERAGE`                       |

### Dismissals

`scripts/claim-scan.py`'s candidates are dismissed by kind, one reason each:

- `absolute` — quantifier words in prose and records; the ones inside a table row are that row's claim, resolved by the
  row rather than dismissed.
- `count` — numerals in the artifacts' prose and records whose value the command or record beside them states; none is a
  fact the table omits.
- `path` — the surfaces the change edits or cites, the base revision, and the scan's own command.
- `cross-ref` — identifier-shaped strings: record ids, the change name, requirement names, slash-joined paths, and
  compound names.
