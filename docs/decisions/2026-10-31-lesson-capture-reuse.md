# Decision record — a bounded transfer of the lesson-capture slice

Date: 2026-10-31

Status: a bounded exploration. It binds no standing rule; it records what the second transfer observed.

## The goal

The owner's reuse goal, and the first pilot's reopening condition: a second slice is transferred, to compare its
adaptation burden and whether the same couplings recur.

## The question

Can the lesson-capture analysis transfer into a similar project with localized adaptation, preserving the behaviors it
names — and do the couplings the pause/resume slice exposed recur?

## The fixture

A frozen copy of the first pilot's `ledger-service`, `ledger-service-lc`, plus the specification's additions:

- `docs/process-defects/unit-b.md` — two records, one decided (`no-change`) and one undecided;
- `docs/reviews.md` and `docs/reviews/unit-b-round-1.md` — the review evidence the records cite;
- three `AGENTS.md` conventions — one to preserve, one to extend, one whose mechanism is absent;
- `docs/ideas.md` — one parked idea whose condition is met and one whose condition is unmet;
- `docs/contracts/lesson-capture-analysis.md` — the analysis contracts (the knowledge-authoring standard, the shared
  report contract, the recurrence dispositions, the audit assumptions), with provenance.

The specification was frozen before the transfer (`lesson-capture-spec.md`, SHA-256
`c71da548a56dda4f5dbdcd9ab81046e9886849f6d2ee8251fedc535fa7a98bc0`) and the protocol before the trial
(`lesson-capture-protocol.md`, SHA-256 `8e6ced5badac2d9c41f080c1830295319a5302084bd1169fdfdbecb6efef65ed`). The fixture
is **builder-blind, not slice-blind**: the additions were chosen to exercise this slice.

The builder's delta was the four new files and the `AGENTS.md` additions; it did not touch the first slice's uncommitted
state and modified nothing in the original fixture.

## The dependency manifest

| Dependency                                                              | Fixture equivalent                       | Category            |
| ----------------------------------------------------------------------- | ---------------------------------------- | ------------------- |
| `docs/process-defects/<unit>.md`                                        | the added records                        | existing equivalent |
| `docs/review-rounds.tsv`                                                | `docs/reviews.md`                        | discovery glue      |
| The knowledge-authoring standard                                        | the support document                     | discovery glue      |
| The shared report contract                                              | the support document                     | discovery glue      |
| The recurrence dispositions                                             | the support document and `docs/ideas.md` | discovery glue      |
| `AGENTS.md` destinations                                                | `AGENTS.md` bullets                      | existing equivalent |
| Subject packs, `check-and-test-authoring`, `ship-change`, `review-gate` | none                                     | missing contract    |
| `docs/guidance-evidence/`                                               | none                                     | missing contract    |
| The `audit-agents` reference                                            | none                                     | missing contract    |
| The `captured:` marker                                                  | keep                                     | existing equivalent |

## The transfer

The agent definition transferred with **six distinct identifier/path mappings**, **three structural adaptations across
seven sites**, one new confinement block, one declared support document, and one root pointer.

| Adaptation                                                                                                                | Sites                                                                                  | Classification   |
| ------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------- | ---------------- |
| `docs/review-rounds.tsv` → `docs/reviews.md`                                                                              | 1 (the given list)                                                                     | identifier/path  |
| The merged-units clause → the round reports under `docs/reviews/`                                                         | 1 (the given list)                                                                     | identifier/path  |
| The knowledge-authoring standard's `agent-skills` spec → the support document                                             | 1                                                                                      | identifier/path  |
| `/audit-agents` → "a periodic audit"                                                                                      | 1                                                                                      | identifier/path  |
| The generalization unit's change record → the caller's change record                                                      | 1                                                                                      | identifier/path  |
| "either skill, or a subject pack" → "the support document"                                                                | 1                                                                                      | identifier/path  |
| The destination list (subject packs, `check-and-test-authoring`, `ship-change`, `review-gate`, `### Evidence and search`) | 5 (frontmatter, intro, given-list bullet, destination paragraph, retirement paragraph) | structural       |
| The `### Evidence and search` pair                                                                                        | 1                                                                                      | structural       |
| The generalization Decision points → the caller                                                                           | 1                                                                                      | structural       |
| The confinement (`permission: edit/execute/bash deny`, the `tools` map)                                                   | 1                                                                                      | fixture-specific |
| The support document                                                                                                      | 1                                                                                      | declared support |
| The root pointer                                                                                                          | 1                                                                                      | declared support |

No target process policy changed. Two unplanned rescues: the confinement's `execute` deny did not remove the pinned
session-move tool, so the `tools` map was added; and the synthetic record's cited round-2 report and ledger row were
absent, so the fixture was repaired. No undeclared support file was added. The full transferred-agent diff and the
support document are in the fixture directory; the table records each adaptation's site and content. The
obligation-preservation claim is checked against that diff by the review rounds; the trial table is the compact excerpt
of the sessions, which are not retained.

## The trial

Ten `opencode run` invocations from the fixture's own directory, each a fresh session, the agent selected with
`--agent lesson-capture`.

| Id    | Invocation                      | Expected                                                           | Observed                                                                      | Verdict        |
| ----- | ------------------------------- | ------------------------------------------------------------------ | ----------------------------------------------------------------------------- | -------------- |
| P1    | positive control                | the agent states its verdict line and the two destinations         | it did                                                                        | pass           |
| P2    | negative control                | the default agent cannot state the report contract                 | it could not                                                                  | pass           |
| C1    | edit control                    | the agent has no write capability                                  | it has none                                                                   | pass           |
| C2    | session-move attempt 1          | the tool is denied                                                 | the tool moved the session to `/tmp`                                          | **fail**       |
| C3    | session-move attempt 2          | the tool is denied                                                 | the agent refused by judgment, not by a permission                            | inconclusive   |
| C4    | session-move attempt 3          | the tool is absent                                                 | the `tools` map removed it; no callable target                                | pass           |
| S1a   | novel/covered/partial           | a proposal, covered, and a split                                   | withheld (the cited report and ledger row were absent), covered, split-no-add | fixture defect |
| S1b   | the same, after the repair      | a proposal, covered, and a split                                   | covered by the pause/resume skill, covered, split-no-add                      | partial        |
| S2/S3 | automation/authoring/retirement | route to CI, an authoring recommendation, a retirement, a preserve | routed to CI, missing contract, covered (no retirement), covered              | partial        |
| S4/S5 | recurrence/evidence             | reopen, already parked, no shared cause, withhold, reject          | reopen, already parked, withheld, rejected, retirement candidate              | pass           |

The first S1 run exposed a fixture defect: the synthetic record cited a round-2 report and a ledger row the fixture did
not have, and the agent correctly withheld the candidate. The repair added them; the second run then found the
obligation covered by the pause/resume skill the first slice transferred.

## The result

The analysis preserved the behaviors the trial exercised:

- it withholds a candidate whose recurrence evidence is absent (S1a) and rejects one with no evidence at all (S5a);
- it rejects a premise the repository contradicts (S5b);
- it reopens a parked idea whose condition is met (S4a);
- it leaves an unmet one parked (S4b);
- it refuses to infer a shared cause from a class label (S4c);
- it routes an automatable remedy to CI (S2a);
- it records a missing contract instead of inventing a destination (S2b);
- it proposes retiring a rule whose script does not exist (S5b);
- it finds an obligation covered by the first slice's skill (S1b);
- it applies the split disposition (S1c; the new clause was unsupported, so no addition).

Six branches were **not exercised**:

- **The novel addition.** The candidate was covered by the first slice's pause/resume skill ("Findings no record
  holds"), and the fixture has no candidate that skill does not cover.
- **The authoring-mechanism recommendation.** The fixture has no process-defect template or schema, so the agent
  recorded a missing contract rather than inventing the mechanism.
- **The "keep the new clause" half of the split.** The fixture's clause was unsupported by the evidence.
- **The audit control and the net-delta record.** No scenario exercised them.
- **The direct-retirement branch.** S2/S3's candidate 3 was covered because the script's absence did not establish its
  removal; the retirement in S5b came from a premise-false claim, not the direct case.
- **The renaming counterfactual.** The observation window was consumed by the confinement rescue.

### The measurement table

| Source obligation                | Source owner                            | Target equivalent                       | Class            | Adaptation                  | Evidence                       | Exercised |
| -------------------------------- | --------------------------------------- | --------------------------------------- | ---------------- | --------------------------- | ------------------------------ | --------- |
| The review ledger                | `docs/review-rounds.tsv`                | `docs/reviews.md`                       | discovery glue   | path mapping                | the S1 report cited the ledger | yes       |
| The merged-unit diffs            | the given list                          | the round reports under `docs/reviews/` | discovery glue   | clause mapping              | the S1 report                  | yes       |
| The knowledge-authoring standard | the `agent-skills` spec                 | the support document                    | discovery glue   | mapping to the support text | S2/S3 candidate 2              | yes       |
| The audit control                | `/audit-agents`                         | "a periodic audit"                      | missing contract | reference mapping           | none                           | no        |
| The net-delta record             | the generalization unit's change record | the caller's change record              | discovery glue   | owner mapping               | none                           | no        |
| The edit target                  | "either skill, or a subject pack"       | the support document                    | discovery glue   | target mapping              | S2/S3                          | yes       |
| The destination list             | subject packs and skills                | `AGENTS.md` and the support document    | missing contract | structural, 5 sites         | S2/S3 candidate 2              | yes       |
| The evidence half                | `### Evidence and search`               | none                                    | missing contract | structural                  | none                           | no        |
| The generalization branch        | the `AGENTS.md` Decision points         | the caller's workflow                   | discovery glue   | structural                  | none                           | no        |
| The confinement                  | the trial harness                       | the `tools` map                         | fixture-specific | tool denial                 | C2 and C4                      | yes       |
| The support text                 | the analysis contracts                  | the support document                    | declared support | import                      | the authoring standard         | yes       |
| The root pointer                 | the slice                               | the `AGENTS.md` pointer                 | declared support | addition                    | P1                             | yes       |

The table's `Exercised` column is adaptation-level; the branch list above is trial-level.

### Coupling recurrence

| Coupling kind                   | First slice                                                                | Second slice                                                           | Recur?                                             |
| ------------------------------- | -------------------------------------------------------------------------- | ---------------------------------------------------------------------- | -------------------------------------------------- |
| Repo-specific paths or headings | the lifecycle/ledger headings, `docs/review-rounds.tsv`                    | `docs/reviews.md`, the destination headings                            | yes                                                |
| Absent governance contracts     | subject packs, the ADR citation                                            | subject packs/skills, the authoring standard                           | yes                                                |
| Absent supporting material      | the ADR citation and its analysis were dropped; the rationale was retained | the analysis contracts were imported as a support document             | yes — the dependency recurs, the treatment differs |
| Harness confinement             | the `session_move` rule was inert; the mechanism stayed unknown            | the `execute` deny did not remove the pinned tool; the `tools` map did | a harness property, not a slice coupling           |

The first three kinds recur: both slices need discovery mappings to repo-specific names, both lose or import governance
contracts the target lacks, and both depend on supporting material the target does not have — the first retained the
rationale but lost the ADR citation and its analysis, the second imported the contracts as a support document. The
fourth is a property of the trial harness, not the slice.

Budget accounting: process-policy changes 0 (limit 0); distinct identifier/path mappings 6 (limit 6); structural
adaptations 3 across 7 sites, none removing a behavioral obligation (limit 0); unplanned rescues 2 (limit 2); undeclared
support files 0 (limit 0). The observation window was 10 invocations (limit 10).

## The ceiling

**Observed transfer of the lesson-capture analysis into this synthetic fixture, within the recorded adaptation burden,
preserving the behaviors exercised.**

Recurrence across these two slices supports a cross-slice coupling pattern **within this constructed target**. The
renaming counterfactual was not run, so a filename-specific explanation is not weakened, and neither slice establishes
cross-project generality.

## Reopening condition

- **A real target project becomes available** — rerun the transfer on it (external trigger: an owner-named project).
- **A third slice is transferred** — compare its adaptation burden and the four coupling kinds (internal trigger: the
  next reuse unit).
- **The fixture gains a process-defect template** — rerun S2b to exercise the authoring-mechanism branch (internal
  trigger: a fixture revision).
- **The fixture gains a removed mechanism with its removal evidence** — rerun S3a to exercise the direct-retirement
  branch (internal trigger: a fixture revision).
- **The fixture gains a novel candidate the first slice's skill does not cover** — rerun S1a to exercise the novel
  branch (internal trigger: a fixture revision).
- **The renaming counterfactual is run** — repeat S1 with renamed paths and headings (internal trigger: the next trial).
- **A mechanism denies the pinned `opencode` tools at the agent level** — replace the `tools` map (external coordinate:
  the OpenCode agent-permission surface).

The unit's process-defect record is `docs/process-defects/lesson-capture-reuse.md`.
