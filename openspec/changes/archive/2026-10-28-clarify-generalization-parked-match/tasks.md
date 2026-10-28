# Tasks

Workspace: unit=clarify-generalization-parked-match;
path=/Users/afanasyev/Development/axon-showcase-clarify-generalization-parked-match;
branch=clarify-generalization-parked-match; writer=ses_e7fc21367ffeOpRAKtbkZCJuU2

## 1. The command's match step

- [x] 1.1 Add the match step to `.opencode/commands/generalize-process-defects.md`, before the `lesson-capture`
      hand-off: it names `AGENTS.md`'s generalization **Decision points** bullet "Where the repository already records a
      recurrence decides its disposition" and states that the bullet owns the sites, their conditions, and their
      actions, without enumerating them. Verify: the step quotes the bullet's text verbatim and carries no site
      enumeration or condition (`grep -n 'Revisit when\|captured:' .opencode/commands/generalize-process-defects.md`
      returns nothing). **Done:** the step opens "Before the hand-off, decide each selected record's disposition against
      the recurrence sites", and the grep returns nothing.
- [x] 1.2 Split the presentation from the apply rule, so each is one action: a presentation step that carries each
      record's disposition, an apply step, and a Decision point withholding a proposal the disposition records as
      parked. Verify: `git diff -- .opencode/commands/generalize-process-defects.md` shows the procedure steps, the
      added Decision point, the Verification clause, and the renumbering, with no frontmatter change. **Done:** steps 5
      and 6 carry the presentation and the apply rule, the Decision point reads "IF a record's disposition already
      records the problem as parked, THEN a proposal for it is not applied", and the frontmatter is unchanged.
- [x] 1.3 Record in `design.md` why no spec delta, `README.md`, ADR, or `lesson-capture` edit is owed, each with the
      grep that establishes it; the `AGENTS.md` question is 1.5's and 1.6's. Verify: each of the six decisions names its
      evidence, and the Claims table carries a row per fact. **Done:** decisions 1–6 carry their evidence, and the
      Claims table carries a row per fact, including the `AGENTS.md` Procedure shape and the README row.
- [x] 1.4 Extend the command's Verification so a pass that skips the match fails a stated check. Verify: the clause
      requires every selected record's disposition, a record that produced no proposal included, and the withheld parked
      proposal. **Done:** the clause reads "every selected record's recurrence disposition is presented, including a
      record that produced no proposal, … no proposal the disposition records as parked is applied".
- [x] 1.5 Add the one-line pre-hand-off pointer to `AGENTS.md`'s generalization Procedure and renumber the steps after
      it. Verify: the pointer names the act and the **Decision points** below, copies no site or condition, and
      `AGENTS.md`'s diff is the pointer with the two renumbered lines. **Done:** step 2 reads "Decide each record's
      disposition against the **Decision points** below before the hand-off.", and `AGENTS.md`'s numstat is `4 3`.
- [x] 1.6 Record in `design.md` that the pointer takes the single-sourcing split's pointer form, and that trimming
      root's Procedure summary stays a Non-Goal. Verify: decision 5 names the pointer form and separates the two
      questions, and the Claims table carries a row for it. **Done:** decision 5 does both, and the row cites
      `AGENTS.md:293–299` at the base.

## 2. Records

- [x] 2.1 Write `docs/process-defects/clarify-generalization-parked-match.md`, recording the defects this unit observed
      as they occurred, or stating that it observed none. Verify: the file exists, ships outside the change dir, and
      carries no `Decision` line. **Done:** the file records each defect the unit observed, with the mandated field set,
      and carries no `Decision` line.

## 3. Verification

- [x] 3.1 Run the pre-write derivation pass and record its output as the Claims table in `design.md`, seeded with
      `python3 scripts/claim-scan.py` over the unit's diff; resolve each candidate into a row or a dismissal. Verify:
      the table's commands reproduce the figures they record, and every scan candidate is resolved. **Done:** the table
      carries a row per fact the change states about its evidence, and the scan's candidates are resolved as rows or
      dismissed by kind in the paragraph beside it.
- [x] 3.2 Format and gate the markdown: `./gradlew spotlessApply`, then `./gradlew spotlessCheck`. Verify: both succeed,
      and `spotlessCheck` reports no skipped file for this unit's paths. **Done:** both succeeded, and a `--rerun-tasks`
      run reported neither a skip nor a failure.
- [x] 3.3 Validate the change: `openspec validate --all`. Verify: it passes, and the change is listed as valid with
      `skip_specs: true`. **Done:** `openspec validate --all` reports 27 passed, 0 failed, and
      `openspec validate clarify-generalization-parked-match` reports the change valid with `skip_specs` set.
- [x] 3.4 Route the deep review: `python3 scripts/review-routing.py`, decide at or above the floor, and record the
      outcome and its signals here. Verify: the recorded outcome names the floor and the decision. **Done:** the first
      derivation, on the command-only implementation, printed `ROUTING: NOT_NEEDED` with no matched signals. Re-derived
      after the implementation grew, it printed `ROUTING: RECOMMENDED`, signal
      `[repeated-class] 5 rows repeat a prior class`; the owner chose to run the deep review, whose pass and finding
      statuses are in §5.
- [x] 3.5 Run the review gate's quick-review loop to a clean round, appending each round's row to
      `docs/review-rounds.tsv`, and run the ledger checks before the manual pass. Verify: the closing round carries no
      finding class and every ledger check passes. **Done:** the ledger carries a row per round with its classes and
      repeats, its last row for this unit carries no class, and the three ledger checks pass.
- [x] 3.6 Ask the owner for the manual review pass and stop. Verify: the turn ends with the ask, and nothing is
      committed, pushed, archived, or merged. **Done:** the ask was made with the tree ready; the owner approved, and
      the ship sequence then ran per `ship-change`.

## 4. Review rounds

### Round 1 hand-over

Recorded before the invocation waited, so a stall costs the loop a wait rather than the round's work.

| Field                   | Value                                                                                                                                                                                    |
| ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Unit                    | `clarify-generalization-parked-match`                                                                                                                                                    |
| Base revision           | `c93607bde1555c7bcce17aa4445f62fb17d9f5ce`                                                                                                                                               |
| Current revision        | `c93607bd` plus the uncommitted working-tree diff                                                                                                                                        |
| Working tree and branch | `/Users/afanasyev/Development/axon-showcase-clarify-generalization-parked-match`, branch `clarify-generalization-parked-match`                                                           |
| Supplied artifacts      | `proposal.md`, `design.md`, `tasks.md`, `.openspec.yaml`, `docs/process-defects/clarify-generalization-parked-match.md`, the modified `.opencode/commands/generalize-process-defects.md` |
| Scope                   | the supplied artifacts and the command's diff, nothing else                                                                                                                              |
| Prior findings          | none — first round                                                                                                                                                                       |
| Completed checks        | `spotlessCheck` with no skipped file, `openspec validate --all` (27 passed), `claim-scan.py --base origin/main`                                                                          |
| Known limitations       | none                                                                                                                                                                                     |
| Subject packs           | none                                                                                                                                                                                     |
| Deadline                | the caller's policy: one round's wait; a round with no reply is recovered as `stopped-responding`                                                                                        |

### Rounds 2 and 3

Each round's hand-over went into its invocation rather than being written here before the wait — the miss
`docs/process-defects/clarify-generalization-parked-match.md` record #4 states. In outline, both rounds carried the same
unit, base revision, working tree, branch, supplied artifacts, and scope as round 1, plus the prior round's findings
with their instances and dispositions, the checks run before the round, and `none` subject packs.

- Round 2 — the prior findings were round 1's six, each with the disposition its fix gave it.
- Round 3 — the prior findings were round 2's five, each with the disposition its fix gave it.

The unit's base advanced three times mid-review — `c93607bd` → `a4c3cd9e` (`#722`) → `7db9c88d` (`#717`) → `d9f0f5a6`
(`#723`) — and the claims table is re-anchored to the current merge-base each time, with every row's command re-run
there. `#722` and `#723` both edited `docs/ideas.md`, moving the cited 2026-10-27 entry from line 58 to line 73; neither
touched another cited artifact.

## 5. Deep review

Routing: `RECOMMENDED` on the re-derivation (`[repeated-class] 5 rows repeat a prior class`); the owner chose to run it
on 2026-10-28. The pass record below is this section's, not the ledger's.

### Pass 1

- Invoked: the `review-thorough` subagent, session `ses_e7f84836bffeOXwhIemuaUXYeb` (created for this pass).
- Verdict: `1 finding` — one nitpick; no blocking and no should-fix findings.
- N1 — `.opencode/commands/generalize-process-defects.md` steps 3 and 5 and the new `Decision points` bullet say
  "disposition", while `Verification` says "recurrence disposition"; step 4's "the dispositions" is `lesson-capture`'s
  analysis-outcome kind, a different concept. Status: `ACCEPTED` on 2026-10-28 by the owner's explicit decision, on the
  independent consultation's recommendation. Reason: the record-level recurrence disposition is explicitly anchored to
  `AGENTS.md`, and `lesson-capture`'s inputs are separately enumerated; every selected record's recurrence disposition
  must be presented, including records with no proposal; the remaining inconsistency is terminological, and the owner
  accepts the residual reading ambiguity without changing the instructions.
- Pass 2: not triggered — the budget's verification pass follows fixed valid findings, and none was fixed.
- Passed: the omission is closed at both sites and `Verification` fails a skipping pass; the root pointer takes the
  split's pointer form with no drift from the command; `skip_specs: true` holds; the step sequence is coherent; all 12
  claims rows reproduce at the base; the proposal's schema fields match the diff.
