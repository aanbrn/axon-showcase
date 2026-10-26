# Design

## Context

See `proposal.md` — Why. What shapes the approach:

- `docs/review-rounds.tsv` records one row per reviewed round with the fields its header declares: date, unit, round,
  classes raised, classes repeated, compliance misses, the reviewer's session, the transition, and the replacement
  reason. A clean round records `-` in the classes column.
- Two modes read it, and both are `check` tasks: `--unique-rounds` (`verifyUniqueReviewRounds`) verifies one row per
  `(unit, round)`, contiguity from `1`, and the `--expect <unit>:<round>` rounds the caller names;
  `--session-continuity` (`verifyReviewSessionContinuity`) verifies the session fields.
- Before this change neither read the classes column: at the base revision the file's only `fields[3]` read is
  `_cron_fields`' cron-schedule parser, and the review-ledger readers use `fields[1]` (unit) and `fields[2]` (round) or
  hand whole rows to the session check. An existing test pins that gap deliberately — `--expect unit-a:1` must exit `0`
  against a row whose classes are `wording` (`scripts/test-commit-hygiene.py`).
- `--expect` was added for the same class of blind spot one step earlier: contiguity could not see a missing final row
  (archived change `2026-10-22-check-review-round-contiguity`).
- At this unit's base (`0d8ef3f8`) the ledger held 126 units and 1190 rows, **8** of them with a numerically highest
  round carrying a class.
- Before this change `review-gate` §5's preconditions named a clean quick review and any required deep review only,
  while `AGENTS.md`'s capture workflow requires the unit's capture record before that ask.
- `scripts/commit-hygiene.py` defines `check_unique_review_rounds` twice; the later definition — the one taking
  `expected` — shadows the earlier, dead one that takes only `repo`. A separate parked idea owns removing the duplicate.

## Goals / Non-Goals

**Goals:**

- One callable assertion that a named unit's review loop converged, as recorded by its highest round.
- The review gate's manual-pass step invokes it, so a premature transition fails loudly instead of reading as closed.
- §5 names the ask's conditions at the step: the recorded pre-write pass, a closing round with no class, and the unit's
  capture record at the implementation stage, beside the quick review and deep review it already names.

**Non-Goals:**

- Enforcing cleanliness ledger-wide in `check` — a round in flight is unclean by design, and the ledger's history holds
  8 units whose last recorded round carries a finding.
- Retroactive rows or acknowledgements for those units.
- An escape hatch for an accepted unclean round.
- A new mode in `check-and-test-authoring`, or moving the capture procedure into `review-gate`.
- Removing the shadowed duplicate definition.

## Decisions

- **The assertion is unit-scoped and called by the gate (`--expect-clean <unit>`, repeatable), not added to
  `--unique-rounds`'s ledger-wide pass.**
  - Ledger-wide inside `--unique-rounds`: rejected. That mode runs in `check`, so it would retroactively fail `main` on
    8 merged units, and it conflates a round still in flight with an illegitimate transition to the manual pass.
  - A baseline file, like the coverage gate's: rejected. It splits one predicate across two mechanisms and needs
    re-baselining on every legitimate exception.
  - Wording only, with no check: rejected. The obligation already exists in the skill; what went unnoticed was that
    nothing evidenced it, and a review rather than any mechanism caught the gap.
- **It requires `--unique-rounds`.** The cleanliness assertion is a property of the same parse (numeric rounds, one row
  per pair); running it alone would duplicate the ledger reader and could disagree with it.
- **"Highest" is numeric, not positional.** Rows are appended in report order, which a merge can reorder; the existing
  check already parses the round as a number and fails on a non-numeric value.
- **A clean round is the literal `-` in the classes field.** That is the ledger's own contract for "no classes"; an
  empty or malformed field fails rather than passing, so a truncated row cannot read as converged.
- **No acknowledgment escape.** Acknowledging an unclean round would record the violation rather than the convergence,
  and the unit-scoped mechanism gives no history it needs to exempt.
- **§5 gains the conditions, and the capture procedure stays where it is owned.** The skill names the requirements and
  points at `AGENTS.md`'s capture workflow rather than restating its steps.

### Requirement amended

The delta replaces the whole `Review-round ledger rows are unique` requirement. No existing clause is reworded; two
clauses are added and five scenarios are appended, after the existing scenarios and in their order.

| Clause                   | Was    | Now                                                                                                                                                                                                                                                                          | Why                                                                                                                     |
| ------------------------ | ------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| Clean-round verification | absent | "When the caller also names the units whose loops must have converged (`--expect-clean <unit>`, repeatable, and valid only with `--unique-rounds`), the same verification SHALL fail unless each named unit's numerically highest recorded round carries no finding class …" | The requirement certified the record's shape; nothing certified the condition the manual-pass step turns on             |
| Invocation and scope     | absent | "The assertion SHALL be invoked by the review gate's manual-pass step, and SHALL evaluate only the units the caller names, so the `check` task's invocation — which names none — never fails on a unit's history, where a round in flight is unclean by design."             | The gate, not `check`, needs the assertion; scoping it to the named units keeps the `check` invocation inert on history |

## Risks / Trade-offs

- **The check verifies the record, not that a reviewer ran, nor that the closing round covered the final edit** →
  Mitigation: it is an evidence assertion beside the existing steps; §2 still requires the run and the row, and the
  modes it adds do not replace `--expect` or the session check.
- **A caller can append a `-` row without running a round** → Mitigation: the ledger is a record of reported rounds, and
  the same trust already underwrites `--expect`; the check narrows the silent case, not the dishonest one.
- **A caller skips the invocation on the manual-pass step** → Mitigation: §2 names it at the manual-pass boundary and
  states explicitly that it is outside the "repeat both before the next reviewer invocation" rule, which governs only
  `--expect` and the session check.
- **The implementer edits the shadowed definition** → Mitigation: the task names the live one by signature — it takes
  `expected` — and leaves the dead duplicate to its own parked idea.

### Purpose refresh owed at archive

`showcase/quality/commit-hygiene`'s Purpose enumerates the ledger verifications and attributes them to the build checks;
the clean-round assertion widens that set and is invoked by the review gate rather than by `check`. A delta cannot carry
a Purpose, so the refresh is applied in the archive commit's edit of the main spec and ticks task 3.4 with task 5.5.

## Migration Plan

None: the mode is additive, no ledger data changes, and no Gradle task wiring changes. Rollback is a revert of the
change's commits.

## Claims table

| Claim                                                                            | Source or command                                                                                                                                                                                     | Reproduces |
| -------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------- |
| The ledger held 126 units, 1190 rows, 8 with an unclean highest round            | `git show 0d8ef3f8:docs/review-rounds.tsv` through the aggregate (units by `f[1]`, highest by numeric `f[2]`, clean iff `f[3] == '-'`)                                                                | yes        |
| Before this change neither mode read the classes column                          | `git show 0d8ef3f8:scripts/commit-hygiene.py` filtered for `fields[3]` → one hit in `_cron_fields`; `ledger_rounds` indexes `fields[1]` and `fields[2]`, and `session_rows` returns whole field lists | yes        |
| Both modes run in `check`                                                        | `build.gradle.kts:590,599,682,683`                                                                                                                                                                    | yes        |
| `--expect` responded to a missing final row                                      | archived `2026-10-22-check-review-round-contiguity/design.md`                                                                                                                                         | yes        |
| An existing test pins `--expect` passing on a row carrying `wording`             | `scripts/test-commit-hygiene.py`, `test_the_expected_round_is_reachable_through_main`                                                                                                                 | yes        |
| Before this change §5 named only a clean quick review and a required deep review | `git show 0d8ef3f8:.opencode/skills/review-gate/SKILL.md`, its manual-pass section                                                                                                                    | yes        |
| The capture workflow requires the record before the ask                          | `AGENTS.md`, `### Workflow: Capture lessons`                                                                                                                                                          | yes        |
| `check_unique_review_rounds` is defined twice, the later shadowing the earlier   | `grep -n "def check_unique_review_rounds" scripts/commit-hygiene.py` → two definitions: the live one takes `expected`, the shadowed one only `repo`                                                   | yes        |
| A unit can run six finding-bearing rounds before its first clean one             | `grep narrow-the-v1-config-probe-claim docs/review-rounds.tsv` → rounds 1–6 carry classes, round 7 is `-`                                                                                             | yes        |
