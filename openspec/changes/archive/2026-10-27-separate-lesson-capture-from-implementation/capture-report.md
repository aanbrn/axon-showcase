# Capture report (task 5.3, under the rule in force before the merge)

4 durable proposals (3 additions, 1 replacement, 0 retirements) and 1 authoring-system recommendation, all applied; each
landed artifact's measured whole-file delta is `AGENTS.md` 172+/203−, `review-gate` 16+/10−, `check-and-test-authoring`
13+/0−, and `openspec/config.yaml` 8+/6−, and each figure also carries the unit's own edits to that file, so none
isolates the proposals'. Produced by the `lesson-capture` subagent against this unit's diff, its 19 round rows, its
change dir, and the guidance named per proposal. The table below records each proposal's one-line claim, the artifact it
targeted, and where it landed; the subagent's own report is not reproduced here, so this file states the outcome rather
than the wording a later pass would apply from.

## Proposals and their disposition

| #   | Artefact                                                  | Proposal                                                                                                                                                  | Disposition                                                             |
| --- | --------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------- |
| 1   | `check-and-test-authoring` skill, new mode                | A disposition ending in a catch-all cannot fail; close the allowed classes and exercise an input that fits none.                                          | Applied: the skill's new `## A catch-all disposition cannot fail` mode. |
| 2   | `review-gate` §2                                          | Run the ledger checks when a round's report arrives, not when its row is appended — a check keyed on the append cannot see an append that never happened. | Applied: §2's ledger-checks bullet opens on the report's arrival.       |
| 3   | `review-gate` §2                                          | Bring the hand-over and the `Resume checkpoint` current in the same step as the round.                                                                    | Applied: §2's closing bullet brings the record current with the round.  |
| 4   | `AGENTS.md`, the retitle decision point                   | Grep the corpus for a term a change would rename before retitling, and keep the term where it still names the subject.                                    | Applied: the retitle decision point opens with the corpus grep.         |
| A1  | `openspec/config.yaml` `proposal` rule (authoring-system) | Derive a swept set from the durable surfaces, never from the change's own tasks — a set derived from the tasks cannot contain a missing task.             | Applied: the proposal rule now derives from the durable surfaces.       |

## Rejected or already covered

- Record #1 (the hand-written site list): the action is already determined by the single-sourcing invariant, the "search
  that under-matches" gotcha, and the config's "never a hand-written list" rule; its residual is proposal 1.
- Record #2 (the late ledger row): the immediate-check rule already governs it; only its trigger was wrong (proposal 2).
- Record #3 (a premise read from another unit's uncommitted tree): `review-gate` §1's base-revision anchor already
  requires the preventive read and repaired it; the incident is recorded rather than promoted, since its rule governs
  claims-table rows and the premise preceded the table.
- `claim-unreproducible` (5 rounds), `coherence` (6), `docs-residue`, `formatting`, `structured-writing`,
  `dropped-content`, `missing-scenario`, `test-control`: each has an owning rule; no shared cause beyond them was
  confirmed from the evidence.

## Retirements

None beyond what the unit's own diff replaced.
