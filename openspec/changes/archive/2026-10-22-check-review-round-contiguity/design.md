# Design

## Context

The review gate requires the caller to append each round's row to `docs/review-rounds.tsv` as the report arrives,
because a deferred append "lands late or never". It landed late five times in two units
(`extract-deployment-operations-guidance` rounds 15, 18, 19; `consult-terminal-handover` rounds 3, 8), and a later
round's report has carried the gap as a `missing-task` finding (`consult-terminal-handover` rounds 4 and 9) — in round 4
as that round's only finding, in round 9 beside `claim-unreproducible` — which starts another fix-and-re-run cycle.

The parked idea (`git show 3b854aed:docs/ideas.md`, the 2026-10-21 entry this change deletes) asked for a contiguity
check. The advisor's review of that idea found it insufficient on its own: with the ledger at `1, 2` and round 3's
report received but not appended, `1..max` sees `max = 2` and passes — the gap only becomes visible once a later row
exists, by which time the next reviewer call has already run. Hence the expected-round assertion: the caller names the
round whose report just arrived, and the check fails when its row is absent. A reviewer wrapper that withholds findings
until the row is written would give the strict ordering guarantee; its feasibility pass is parked with its findings
rather than built now.

## Goals / Non-Goals

**Goals:**

- Make a deferred append fail in the same turn, before the fixes, at a cost of one command.
- Keep the check strict for every new row while staying honest about the one historical gap whose record is
  unrecoverable.

**Non-Goals:**

- The wrapper (parked in `docs/ideas.md` with its feasibility findings): it buys ordering only within its own route,
  needs buffering, idempotent writes, recovery and a v1/v2 session difference, and cannot prevent the Task-tool path.
- Changing `review-quick`, its `mode`, or its model pin; a new mode on the checker is enough.
- Repairing the historical gap by inventing a row: the archived change's records do not carry round 42's classes.

## Decisions

| Decision                                                                                                       | Alternatives considered                                      | Why                                                                                                                                                    |
| -------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Extend the existing `--unique-rounds` mode with `--expect`                                                     | A new mode; a separate script                                | One checkpoint checking duplicates, contiguity and the expected round is what the review gate runs; a second mode would be a second thing to remember. |
| Acknowledge the unrecoverable gap with a comment line naming the unit and round                                | Exempt the unit; a boundary comment; leave the check failing | Exempting a unit or a row range would blind the check to that unit's future gaps; naming the pair keeps every other row strict and records why.        |
| Place the checkpoint at three points (after the append, before the next reviewer call, before the manual pass) | Only after the append                                        | The first placement is the one a skipped append bypasses; the other two re-run the same command so a miss cannot survive into the next model call.     |
| Fail a malformed `--expect` with an error, not a silent skip                                                   | Ignore it                                                    | A mistyped expectation would otherwise read as a pass, the class the repository's checks exist to avoid.                                               |

## Risks / Trade-offs

| Risk                                                     | Mitigation                                                                                                                                                                                           |
| -------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The checkpoint is a caller act, so it can be skipped too | It is one command at a point the caller already passes, and the next placement catches the previous miss; the wrapper stays parked for a stricter guarantee.                                         |
| The acknowledgement comment becomes a loophole           | It names an exact `(unit, round)` pair; any other gap still fails, and the fixtures pin that.                                                                                                        |
| A future unit is recorded out of order                   | Not checked: rounds are read as a per-unit set, so row order is invisible to the check, and the ledger already carries one unit whose rows are out of order. Contiguity, not order, is the contract. |

## Verification

`python3 scripts/test-commit-hygiene.py` — whose fixtures are the check's committed controls;
`commit-hygiene.py --unique-rounds` on the real ledger (passes with the acknowledgement);
`--expect consult-terminal-handover:10` passes and `:11` fails; `./gradlew verifyUniqueReviewRounds` and
`verifyReviewSessionContinuity`; `openspec validate --all`; `spotlessCheck`. Negative control by construction: the gap
fixture fails before the acknowledgement is added and passes after, and `--expect` fails on a contiguous ledger.

## Claims table

| Claim (as stated here)                                        | Source / derivation                                                                                                                                                            |
| ------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The append was deferred five times, in those rounds           | `docs/review-rounds.tsv` for the two units, and the review rounds that reported the gaps (4 and 9 of `consult-terminal-handover`)                                              |
| The next reviewer reports the gap as a finding                | Round 4's and round 9's ledger rows carry `missing-task`                                                                                                                       |
| `--unique-rounds` detected only duplicates before this change | `git show 3b854aed:scripts/commit-hygiene.py`, `find_duplicate_review_rounds`                                                                                                  |
| The contiguity check alone misses a missing last row          | The advisor's counterexample, restated in "Context"; reproduced by the `--expect` fixture (`1,2` with `expect 3` fails while contiguity passes)                                |
| Round 42's record is unrecoverable                            | `openspec/changes/archive/2026-10-21-separate-agents-md-core-and-subject-packs/tasks.md` records rounds 25–27, 28–29, 30–38 and the ledger carries 41, 43, 44 — nothing for 42 |
| The wrapper's feasibility findings                            | The container probes of `opencode run --agent review-quick` on v1.19.3 and v2.1.2, recorded in the parked idea                                                                 |
| The contiguity idea was parked with the `1..max` scope        | `git show 3b854aed:docs/ideas.md`, the 2026-10-21 entry                                                                                                                        |
| One unit's ledger rows are out of order                       | `docs/review-rounds.tsv`, the `separate-agents-md-core-and-subject-packs` rows (round 26 precedes round 25)                                                                    |
| The two requirements the delta modifies                       | `git show 3b854aed:openspec/specs/showcase/quality/{commit-hygiene,agent-skills}/spec.md`                                                                                      |

**Dismissals.** `scripts/claim-scan.py --base origin/main` seeded the table; its other candidates resolve by class:
`count` — the numeric tokens are round numbers inside rows above (rounds 4 and 9, the archived change's
25–27/28–29/30–38), not measurements; `absolute` — scope words inside the rules this design states ("never", "only",
"every"); `cross-ref` — slugs such as `v1/v2` and `present/absent`, not tracked references; `path` — a command or path a
row's own source resolves.
