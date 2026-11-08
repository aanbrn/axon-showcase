# Design

## Context

See `proposal.md` — Why. Two records describe a unit's reviews. `rounds.tsv` carries the quick loop's rounds with their
session fields (nine fields) and is the population of three consumers: `--expect-clean` reads the highest round,
`review-routing.py` its repeated-class signal, and `review-rounds.py` the trend. `sessions.tsv` carries the deep
reviewer's and the unit-bound advisor's invocations (thirteen fields), is validated by `--agent-sessions`, and links
each registered row to a backticked hand-over slug. The quick round's hand-over section is `## Round <N>` and is checked
by `--handover <unit>:<round>`; the registered rows' sections are backticked slugs. The deep pass's finding statuses
live on the change's review task, and the quick findings in `findings/round-<N>.md`.

The first live mixed history is `gate-review-handover-section` (#864): six quick rounds, four advisor invocations (one
`created`, one `replaced`/`parent-session-changed` across the resume, two `continued`), and one deep pass. The parked
idea's revisit condition is met by that unit.

## Goals / Non-Goals

**Goals:**

- One unit-wide event sequence across the three stateful agents for a unit recorded after the boundary, with a numbered
  row per invocation.
- The quick reviewer's session continuity in the same record as the other agents' for a unit recorded after the
  boundary.
- The gates' typed projections, the finding store, and the deep budget unchanged.
- No rewriting of an existing unit's rows.

**Non-Goals:**

- One sparse table absorbing the quick loop's classes and compliance, or the deep statuses.
- Rewriting, backfilling, or renumbering existing rows.
- Unifying the two hand-over heading conventions.
- Changing the deep-review budget, the finding store, or the `--handover` check.
- Migrating a unit that already carries the ledger's session fields.

## Decisions

### D1 — The register is the event spine; the ledger keeps the quick loop's outcome detail

`sessions.tsv` becomes the unit's review-event register: every `review-thorough`, unit-bound `technical-advisor`, and
(for a post-retirement unit) `review-quick` invocation. `rounds.tsv` keeps the quick loop's outcome — date, unit, round,
classes, repeated, compliance — because the gates read that population and the deep statuses do not belong in it.

Alternatives rejected:

- **One sparse table with every column.** Most columns would be `-` for most agents; the gates would filter by agent,
  which is the split by another name.
- **A third events file.** It would be a third record during the transition, and the register already holds two of the
  three agents.
- **Duplicating the quick session's fields in both records.** Single-sourcing forbids it, and the two copies would
  drift.

### D2 — The ledger's session fields retire per unit, decided by its first round's date

A unit whose first round is dated on or after the retirement date (`2026-11-09`) carries the six-field rows for its
whole life; a unit whose first round predates it keeps the nine-field rows, apart from the pre-2026-10-18 rows that
predate the session fields and carry the six-field width above the marker. No unit mixes the retirement forms, so no
quick session chain spans the retirement boundary and no historical row is rewritten.

Alternatives rejected:

- **A per-row boundary.** A unit's rounds would span it, and its quick session chain — the ledger's
  `continued`/`replaced` rows — would have to join the register's, which the register's per-key first-row rule cannot
  accept.
- **Backfilling the register with existing rounds.** The register is append-only and the pre-commit guard rejects a
  rewritten row.

### D3 — A quick row's identity fields and its hand-over link

For a post-retirement unit, the register's quick row carries `review-quick` as the agent, the unit as the scope, `-` as
the opinion, its invocation as the pass, the round it attempts or reports, the session's
requested/returned/transition/reason, and `round-<N>` as the hand-over slug. The link resolves to the `## Round <N>`
heading the `--handover` check already matches. A round may carry several invocations — stalled attempts and the one
that reports — each its own row sharing the round and taking the next pass, so the pass stays per invocation and the
ledger keeps one row for the round; the stalled row is appended at the deadline and a replacement's row after its own
report. A nine-field unit has no stalled outcome in its ledger: its attempt evidence stays in the round's hand-over, and
the ledger carries the single completed round's row with that round's final session and the replacement's reason. The
two heading conventions stay.

Alternatives rejected:

- **A backticked slug for quick sections.** It would rewrite every future round's heading and the `--handover` predicate
  the previous unit shipped, for a link the round heading already provides.
- **A separate result-reference column.** The quick row's round is the finding store's key, and the deep row's result is
  the review task its hand-over section describes.

### D4 — The event number is appended, unit-wide, and starts at the boundary

The `event` field is appended after `outcome`, so the first thirteen positions do not move. It is contiguous from `1`
per unit across the rows dated on or after the boundary (`2026-11-09`); a row dated before it carries the thirteen-field
width and no event. The per-key `pass` remains the agent's own invocation number — the quick reviewer's invocations, the
deep reviewer's passes, a consultation's invocations — and the quick row's separate round keeps the round numbering, so
the deep budget and the ledger's join do not change.

Alternatives rejected:

- **The event first, after the unit.** It would shift every existing column and every reader's index for a field the
  gates do not read.
- **Renumbering existing rows.** It would rewrite committed records.

### D5 — The gates stay typed projections, and each result keeps its owner

`--expect-clean` keeps reading the ledger's highest quick round, the routing floor its repeated-class signal, and the
digest its trend; the deep budget counts full passes on the unit's deep-review record; the deep findings' statuses are
recorded there too; the consultation's report stays in its hand-over section. The register adds the sequence and the
quick session's continuity, not a second verdict.

Alternatives rejected:

- **Letting `--expect-clean` read the register.** A deep pass or a consultation would then make the unit "unclean"
  although neither is a quick round, and the boundary's meaning would change.
- **Moving the deep statuses into the register.** The requirement gives them one authoritative record, and a second
  mutable inventory is what the previous change refused.

### D6 — The reason set's owner moves to the register's format

The quick ledger no longer records a session or a reason, so the recorded values — `expired`, `definition-changed`,
`stopped-responding`, `parent-session-changed`, and `caller-restarted` as a violation — are stated once in the
register's format requirement; the quick loop's policy keeps naming the cases that justify a replacement, and the
nine-field units' continuity requirement points at the register's set.

Alternatives rejected:

- **Leaving the set in the round format.** The requirement would own values its record no longer carries.
- **Two sets.** They would drift the moment a reason is added.

### D7 — The digest joins the register for a six-field row's session

`review-rounds.py` reads the row's outcome fields for every width and, for a six-field row, joins the register's quick
rows by `(unit, round)` for the session it echoes, selecting the round's resolved `report` row rather than a stalled
one; the echo never requires the session, so a record without a register still digests.

Alternatives rejected:

- **Dropping the session from the digest's echo.** It would lose information the register holds for the same round.
- **A second digest.** The trend is the ledger's; the echo is a per-row convenience.

### D8 — A post-retirement round and its report row correspond exactly

Each recorded round in a post-retirement unit's ledger has exactly one quick `report` row carrying that round; a
missing, duplicate, or unmatched report row fails. The check reads both records as committed or staged.

Alternatives rejected:

- **Leaving the correspondence to the digest.** The digest reads the ledger alone and is deliberately permissive; a
  round without session evidence would pass it.
- **Requiring the correspondence through the gates.** The gates' meanings stay the loop's outcome; a completeness rule
  is the record's, not a gate's.

### D9 — The deep review's result is the unit's record, not the change's task

`docs/units/<unit>/deep-review.md` holds the routing decision and its signals (including `NOT_NEEDED`), the owner's
run-or-skip resolution, each pass's reference to its register row and hand-over, its kind — a full pass, a targeted
verification, or a failed attempt — its reviewed revision, outcome, and limits, one canonical finding entry per stable
identifier or root cause with its current status and evidence, and the provenance of an `ACCEPTED` finding. The change's
review task, or a no-change-directory unit's pull-request checklist, carries the planned obligation and a pointer.

A unit whose deep pass predates the record's activation keeps its change-task record as recorded; a unit without a prior
pass records here even when its quick rounds began earlier; an earlier routing-only record is linked rather than copied,
and a missing historical record is unrecovered context.

Alternatives rejected:

- **Keeping the change's review task as the home.** A unit with no change directory has no `tasks.md`, and the routing
  decision's checklist fallback never covered the finding statuses — the gap that raised this decision.
- **A universal review front page.** It would duplicate the event sequence the register already provides; the unit's
  records stay typed by outcome.
- **A per-pass date boundary.** A unit's pass 1 and pass 2 would be owned by different records, splitting a finding's
  resolution chain.

## Risks / Trade-offs

- **A unit's first round lands just before the boundary.** → It keeps the nine-field form for its life; the next unit
  takes the new path. The decision is the unit's first row, not a later one.
- **A reader looks for one file.** → The register is the sequence; the ledger is the quick loop's outcome, joined by
  `(unit, round)`. The README and the review-gate skill say so.
- **The event boundary splits a unit's register.** → A unit with rows on both sides has no event on the earlier rows and
  starts at `1` at the boundary; the requirement states it and the check accepts the two widths by date.
- **The digest's join misses a register.** → The echo omits the session rather than failing; the digest's population is
  the ledger's.
- **The quick row's link and `--handover` drift apart.** → Both use the `Round <N>` predicate; a change to one is a
  change to the other.
- **A round loses its session evidence.** → The completeness rule fails a recorded round without exactly one `report`
  row, and an unmatched row; the controls cover a missing register, a missing report, a duplicate, and an unused row.
- **A deep pass's result has no home outside a change directory.** → The unit's `deep-review.md` holds it for every
  unit, and the change's task or pull-request checklist points at it.
- **An earlier unit's pass and a later one split across records.** → A unit with a pre-activation pass keeps its
  change-task record through closure; a unit without one records in its unit file.
- **The new path is exercised only by controls.** → A disposable post-boundary fixture runs the real modes before the
  manual pass; the unit's own rounds predate the boundary by design.

## Amended clauses

| Requirement                                   | Clause before                                                                                          | Clause after                                                                                                                                                                                                   | Why                                                                             |
| --------------------------------------------- | ------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| `commit-hygiene` round format                 | nine fields, ending with the session identifier, transition, and reason                                | six fields (date, unit, round, classes, repeated, compliance) for a unit whose first round is on or after the retirement date; the nine-field form stands for earlier units                                    | The session identity moves to the register; the ledger keeps the loop's outcome |
| `commit-hygiene` round continuity             | every row below the boundary records the reviewer's session and its transition                         | every unit's width is judged; a pre-retirement unit's session fields are judged and a post-retirement unit's quick continuity is the register's                                                                | Single-sourcing the quick session in one record                                 |
| `commit-hygiene` register format              | thirteen fields; the quick reviewer is not recorded; the reason set is the round format's              | fifteen fields with the event and the round; the quick reviewer is recorded for a post-retirement unit, its pass the invocation and its round the round; this requirement owns the reason set                  | The register becomes the event spine                                            |
| `commit-hygiene` register continuity          | per-key pass, identity, parentage, and a backticked slug                                               | the event's unit-wide contiguity; the quick rows' pass/round pair and their shared round, the attempts and the reporting row, the quick rows' `round-<N>` links, and the widths by date                        | The sequence joins the continuity rules                                         |
| `commit-hygiene` layout                       | `unit.md`, `rounds.tsv`, `review-handover.md`, `process-defects.md`, `sessions.tsv` at its first level | the same plus `deep-review.md`                                                                                                                                                                                 | The unit owns its deep-review result, so the layout admits the kind             |
| `agent-skills` deep review                    | the routed pass's status is recorded on the change's review task                                       | the routing decision and each pass's findings and statuses are recorded at `docs/units/<unit>/deep-review.md`, with or without a change directory                                                              | The unit owns its durable review result                                         |
| `agent-skills` register requirement           | the registered agents are `review-thorough` and `technical-advisor`                                    | the same, plus `review-quick` for a unit whose rounds are post-retirement                                                                                                                                      | The quick session joins the register                                            |
| `agent-skills` hand-over requirement          | its heading SHALL begin with the slug the agent-session row records                                    | the same for a registered row, and a quick row's `round-<round>` resolves to its `Round <N>` heading                                                                                                           | The quick section keeps the round heading                                       |
| `commit-hygiene` round completeness           | no rule                                                                                                | each post-retirement round has exactly one quick `report` row, and a missing, duplicate, or unmatched row fails                                                                                                | The record's two halves must correspond                                         |
| `agent-skills` quick loop                     | the caller's steps name the ledger row and its checkpoint                                              | for a post-retirement unit, the session row is the register's and the ledger row carries the outcome                                                                                                           | The record location follows the register                                        |
| `agent-skills` quick loop (stalled reviewer)  | the caller records the failure as `stopped-responding` in the round's record                           | for a post-retirement unit, the stalled attempts' rows at their deadlines then the reporting row's; for a pre-retirement unit, the attempt evidence in the hand-over and the single completed-round ledger row | The record location follows the register                                        |
| `agent-skills` quick loop (finding instances) | the round's `rounds.tsv` row keeps its fixed fields                                                    | it keeps the loop's outcome fields, and a nine-field unit's session fields too                                                                                                                                 | The instances sit beside the outcome record                                     |

## Claims table

The base revision is `457dca951f19ba8a25f7e35d9118e29c243c7efe` (`git merge-base HEAD origin/main`).

| Claim                                                                      | Source                                                                                                               | Reproduces                                                                                                     |
| -------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------- |
| `gate-review-handover-section` recorded the first live mixed history       | `git show 457dca95:docs/units/gate-review-handover-section/sessions.tsv` and `rounds.tsv`                            | five register rows (one `replaced`/`parent-session-changed`) and six quick rounds                              |
| The register has thirteen fields and the ledger nine                       | `git show 457dca95:openspec/specs/showcase/quality/commit-hygiene/spec.md`                                           | the two format requirements state the widths                                                                   |
| The gates read the ledger                                                  | `git show 457dca95:scripts/review-routing.py`, `scripts/review-rounds.py`, and the spec's `--expect-clean` text      | the routing reads `rounds.tsv`; the digest reads rows by position; `--expect-clean` reads the classes column   |
| The `--handover` predicate matches `## Round <N>`                          | `git show 457dca95:scripts/commit-hygiene.py` and the spec's hand-over requirement                                   | the mode and the requirement state the heading form                                                            |
| The modes are mapped to tests                                              | `git show 457dca95:scripts/test-commit-hygiene.py`                                                                   | `MODE_TEST_COVERAGE` names every mode flag                                                                     |
| The no-change-directory gap is real                                        | `git show 457dca95:.opencode/skills/review-gate/SKILL.md` and `.opencode/commands/review-thorough.md`                | the routing decision has a pull-request fallback, the pass's statuses name only the change's review task       |
| The parked target and its constraints                                      | `git show 457dca95:docs/ideas.md`                                                                                    | the 2026-11-07 entry states the spine, typed gates, and the statuses' home                                     |
| The two-pass budget is unchanged; the statuses' home was the change's task | `git show 457dca95:openspec/specs/showcase/quality/agent-skills/spec.md` and `.opencode/commands/review-thorough.md` | the requirement names the change's review task as the statuses' record, and the command states two full passes |

Re-derived after the implementation's last edit: every row's source was re-run at the base revision, and each reproduces
as stated. The implementation adds no claim about a prior artifact beyond these rows; the behavior it ships is stated in
the decisions above and in the two delta specs.

## Verification

- `python3 scripts/test-commit-hygiene.py` and `scripts/test-claim-scan.py`.
- `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `openspec validate --all`, `spotlessApply`.
- The digest on the repository's records.
