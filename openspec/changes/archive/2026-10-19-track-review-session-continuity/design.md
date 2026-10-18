# Design

## Context

The review gate's session-continuity rule already exists — `showcase/quality/agent-skills` carries it as a clause of the
quick-review scenario, and `AGENTS.md`'s review-gate decision bullet carries the rule's imperative and its recovery
cases, deferring only the rationale to the spec. Two facts make it easy to miss and impossible to verify:

- It is a decision bullet rather than a step, so nothing in the procedure makes the caller load the prior session's
  identifier immediately before a repeat invocation.
- `docs/review-rounds.tsv` records a round's result and not the session it ran in, so a fresh session and a valid
  continuation produce the same row.

`docs/ideas.md` (2026-10-18) parks exactly this work: add the reviewer's session id to the record and check its
continuity. This change adopts that idea rather than redesigning the loop.

One further fact shapes the design: the ledger already holds rows recorded before the fields existed, and those rows
carry no session field, so no identifier for them exists in the repository. A rule that required every existing row to
carry one would fail the build on the day it landed, and backfilling an identifier or a replacement reason for them
would assert what the repository cannot evidence.

## Goals / Non-Goals

**Goals**

- Make the preflight a step the caller runs immediately before each `review-quick` invocation.
- Make the record show the session each round ran in, and whether it was created, continued, or replaced.
- Fail the build on the record shapes the rule forbids.
- Keep the rule's authoritative statement in the capability spec, with `AGENTS.md` pointing at it.

**Non-Goals**

- Redesigning the review loop, or changing when a deep review runs.
- Backfilling session identifiers for rounds recorded before the fields existed.
- Treating a continued session as evidence that a fix is correct — the per-round instance hand-off remains the caller's
  obligation on every round.
- Sweeping the two enumerations `#628` left stale (the `check` note's omitted `testReviewRouting`, and the Prerequisites
  Python list's omitted `review-routing.py`); that is a separate parked idea, and this change did not cause it.

## Decisions

### The validation is a `commit-hygiene.py` mode, not a `testReviewRounds` assertion

The parked idea names `testReviewRounds`, but the capability spec already fixes where a ledger validation lives:
"Review-round ledger rows are unique" states that its verification "SHALL be a `commit-hygiene.py` mode run by a `check`
task, reading the tracked ledger rather than the staged set". A second ledger validation belongs beside that one, so the
ledger's validations have one home and one runner.

- **Chosen:** `commit-hygiene.py --session-continuity`, run by the new `verifyReviewSessionContinuity` task.
- **Rejected:** an assertion inside `test-review-rounds.py`. Its subject is the digest's rendering, and a test task
  proves the validator's behaviour over fixtures rather than gating the real ledger.

### The boundary is a comment line in the ledger, not a date

A date boundary cannot work here: the ledger holds rows recorded before the fields existed, and they carry no session
field, so any date that covers this change's own rows also covers rows no identifier exists for. An exemption keyed on a
date would therefore fail the build on landing.

- **Chosen:** a comment line in the ledger marking the first row to carry the fields. The checker requires the marker,
  validates every row below it, and requires every row above it to carry the pre-fields width, so the boundary is
  explicit, greppable, and stated by the record itself.
- **Rejected:** a date constant in the checker. It either exempts this change's own rows (making the check vacuous until
  a later day) or fails the build on the previous unit's rows.
- **Rejected:** a dynamic boundary at the first row carrying a session id. It moves silently when that row is edited,
  where a marker is a deliberate act.

### The three fields are appended, and the caller supplies them

Appending keeps every existing reader working: `review-rounds.py` and `review-routing.py` read the first six fields by
index, and the uniqueness checker reads three. The reviewer cannot know its own session identifier, so the caller —
which holds the identifier the invocation returned — appends the fields, and the report contract forbids inventing one.

- **Chosen:** fields 7–9 (identifier, transition, replacement reason), supplied by the caller from the invocation it
  performed.
- **Rejected:** a `session` field the reviewer fills in. The subagent is not given its own session identifier, so any
  value it wrote would be fabricated — the exact failure the record exists to prevent.

### The allowed reasons are the rule's three recovery cases, as tokens

The recovery cases are already stated: the session expired, the reviewer's definition changed after it began, or it
stopped responding mid-round. The checker accepts those as `expired`, `definition-changed`, and `stopped-responding`, so
a replacement names which case it was rather than only that one occurred.

- **Chosen:** a closed set of three tokens, one per recovery case.
- **Rejected:** a free-text reason. It cannot be validated, so an unexplained restart would pass by writing any string.

### A reason is required exactly when the transition is a replacement

A reason on a `continued` or `created` row is a record defect of the same shape as a missing one, and policing it costs
one comparison, so the checker rejects both. The alternative — leaving a stray reason unpoliced — would be a
false-negative surface the design would then have to record.

### The identifier is compared trimmed rather than rejected for padding

A padded identifier (`" ses_a "`) and its trimmed form name the same session, so the checker trims before the emptiness
test and the identity comparison. Rejecting the padding instead would add a rejection branch for a value whose meaning
is not in doubt, and comparing the raw field would read the same session as a replacement — the defect the normalization
avoids.

- **Chosen:** trim the identifier before both tests.
- **Rejected:** rejecting a padded identifier as malformed. It buys a branch and a scenario for no added certainty.

### The preflight names the action, and the caller verifies the continuation

The ledger check validates what a row _says_, never how the session it names was obtained: a caller that started a fresh
invocation and wrote the previously saved identifier into the row satisfies it, which is the masquerade the record
exists to prevent. So the preflight names the action — a fresh invocation on the first round, the saved identifier
passed as the invocation's session-continuation parameter on every later round — and the caller compares the identifier
the invocation returns with the one it saved, stopping the round on a difference.

- **Chosen:** an execution-level guard beside the ledger check, with a mismatch reported as a session-continuity failure
  and a replacement recorded with both identifiers.
- **Rejected:** relying on the ledger check alone. It cannot distinguish a continuation from a fresh invocation whose
  row was written with the saved identifier, and this addition leaves the ledger's fields and validation unchanged.

### The README's command list under _Dependency Updates and Security_ is re-aligned to its new longest member

The new verification's task name is longer than any command in the README's command list, so the block's comment column
moves one character for all nineteen of its lines. That reflow is caused by this change — the block's alignment is to
its longest member — rather than being an unrelated tidy-up, and it is the only place the change touches a line it did
not otherwise need to.

## Risks / Trade-offs

- **The marker can be deleted to exempt rows.** Deleting it is a deliberate, visible edit to a tracked file, and the
  checker fails when the marker is absent, so the exemption cannot be taken by accident.
- **The marker can be moved down past a row, which would exempt it.** The checker enforces the boundary's position as
  well as its presence: a row above the marker must carry the pre-fields width, so moving the marker past a wider row
  fails rather than silently re-bounding the exemption.
- **The rule is now enforced twice — by the spec's wording and by the checker.** They can drift. The checker's reason
  tokens are the spec's three recovery cases spelled as tokens, and the spec's scenario names them, so a change to one
  without the other is visible at review.
- **A row written before the fields is not validated.** That is the deliberate exemption above, and it is bounded: every
  row from the marker down is validated, and a row above it must carry the width those rows actually have.

## Claims table

| #   | Claim                                                                                                                                                                                                                                      | Source                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The parked idea naming this work exists under `docs/ideas.md`'s `## 2026-10-18` section and names the record and the check as its mechanism.                                                                                               | `git show HEAD:docs/ideas.md` (this change removes it)                                                                                                                                                                                                                                                                                                                                                                                                                  |
| 2   | The session-continuity rule is one clause of the `agent-skills` scenario "Quick review runs after proposal and implementation".                                                                                                            | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                                                                                                                                                                                                                                                                                                                    |
| 3   | The ledger's data rows carry six fields before this change.                                                                                                                                                                                | `git show HEAD:docs/review-rounds.tsv` (this change edits it)                                                                                                                                                                                                                                                                                                                                                                                                           |
| 4   | A review-round ledger validation is a `commit-hygiene.py` mode run by a `check` task.                                                                                                                                                      | `showcase/quality/commit-hygiene`'s "Review-round ledger rows are unique" requirement                                                                                                                                                                                                                                                                                                                                                                                   |
| 5   | `review-quick.md`'s record line declares six fields.                                                                                                                                                                                       | `git show HEAD:.opencode/agent/review-quick.md` (this change edits it)                                                                                                                                                                                                                                                                                                                                                                                                  |
| 6   | The `check` members are enumerated at five live sites.                                                                                                                                                                                     | the live sites that enumerate the members — `AGENTS.md`'s `check` note, `README.md`'s prose and its command block, `build.gradle.kts`'s `check` block, and `docs/ideas.md`'s parked test-coverage list — derived by grepping `verifyUniqueReviewRounds` across the live tree and excluding `openspec/changes/archive/`, which yields two further hits that name the task without enumerating the set (its own registration, and `docs/ideas.md`'s illustrative mention) |
| 7   | The two enumerations `#628` left stale are parked, so sweeping them is not this change's.                                                                                                                                                  | `git show HEAD:docs/ideas.md`                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 8   | The ledger's rows above the marker carry no session field, so no identifier for them exists in the repository.                                                                                                                             | `git show HEAD:docs/review-rounds.tsv` — six fields per row, no session column                                                                                                                                                                                                                                                                                                                                                                                          |
| 9   | The digest's figures, the routing tool, and the uniqueness checker read the ledger's first six fields by index, so appending three leaves them unaffected (the digest's echoed rows carry the new fields, which is a presentation change). | `scripts/review-rounds.py` (`row[0]`–`row[5]`), `scripts/review-routing.py` (its `len(row) < 5` guard), and `scripts/commit-hygiene.py`'s `find_duplicate_review_rounds` (its `len(fields) < 3` guard and `fields[1]`/`fields[2]` reads)                                                                                                                                                                                                                                |
| 10  | The check's member count changes by one.                                                                                                                                                                                                   | `git show HEAD:build.gradle.kts`'s `check` block, re-derived after the edit                                                                                                                                                                                                                                                                                                                                                                                             |
| 11  | The README's command list under _Dependency Updates and Security_ aligns each comment to its longest member.                                                                                                                               | The block's own comment column, read after the edit: 19 `./gradlew` lines sharing one `#` column (the README holds other `./gradlew` blocks with their own columns)                                                                                                                                                                                                                                                                                                     |
| 12  | The checker names every rejected branch and passes the real ledger.                                                                                                                                                                        | `commit-hygiene.py --session-continuity` over the real ledger (exit 0) and over a scratch ledger carrying one row per branch (exit 1, one message per rejected branch)                                                                                                                                                                                                                                                                                                  |
| 13  | `AGENTS.md`'s review-gate decision bullet states the session rule's imperative and its recovery cases, deferring only the rationale to the capability spec.                                                                                | `git show HEAD:AGENTS.md` — the bullet's "Continue the reviewer's session across rounds …; start fresh only when that session has expired, the reviewer's definition changed …, or it stops responding mid-round", beside its "the session-reuse rule's rationale is in `showcase/quality/agent-skills`, not restated here"                                                                                                                                             |
| 14  | A probe of one continuation: passing the identifier a fresh invocation returned continued that conversation and returned the same identifier.                                                                                              | The probe's own output: a fresh call returned `ses_eaec68385ffegw5WcZHNKIwAb7` and continuing it with that identifier returned the same value — the kind of source a surface outside the repository needs, since the ledger's rows record what the caller wrote. One probe evidences one continuation, not the general behaviour.                                                                                                                                       |

The table is seeded from `scripts/claim-scan.py` over this change's authored diff (via `/seed-claims`), and the
candidates it raises but that are not claims are dismissed by kind rather than one by one:

- **`[count]` and `[absolute]` candidates in the change's own artifacts and in the code** — a task number, a step
  reference, a count in a comparison, an assertion's arity, and the ledger rows' own fields state what the artifact or
  the code does, not a fact about the repository a reader could check elsewhere. The task numbers the scan reads are the
  planning artifacts' own labels, and the ledger rows are the record the change appends.
- **`[cross-ref]` candidates on `./gradlew` and on a task name** — the README's command block and the Gradle
  registrations name the tasks the build defines; the registrations are the definition, and the block is its
  presentation.
- **`[absolute]` candidates in the review-gate step and the ledger's comment lines** — a rule's own wording
  (`every row below the boundary`, `only for a recovery case`) is the obligation, not a claim about a corpus.
- **`[path]` candidates** — a path the change names is a site it edits or points at, already carried by a task.
