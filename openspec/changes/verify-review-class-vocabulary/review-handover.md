# Review hand-over

Round 1, proposal stage. Written before the invocation is waited on, so a stall costs the loop a wait rather than this
record.

- **Unit**: `verify-review-class-vocabulary`.
- **Base revision**: `aeb3b6af4bc73d4cdacec21c02311d7dcf4dfcb2` (`git merge-base HEAD origin/main`).
- **Current state**: the branch `verify-review-class-vocabulary` at that base, with the unit's files staged and
  uncommitted; no commit exists yet.
- **Working tree**: `/Users/afanasyev/Development/axon-showcase-verify-review-class-vocabulary`.
- **Supplied artifacts**: `openspec/changes/verify-review-class-vocabulary/proposal.md`, `design.md` (with its claims
  table and dismissals), `tasks.md`, and `specs/showcase/quality/commit-hygiene/spec.md`; plus the two docs edits in the
  unit's diff — the idea's removal and the parked digest follow-up in `docs/ideas.md`, and the S2 disposition in
  `docs/retrospectives/2026-10-31.md`.
- **Stage**: proposal. The implementation does not exist yet, so there is no code diff to screen.
- **Scope**: the supplied planning artifacts against the repository's conventions — proposal schema, delta-spec
  conventions, artifact coherence, task coverage, and the docs sweep the change claims.
- **Prior findings**: none; this is round 1.
- **Completed checks**: `openspec validate --all` (27 passed, 0 failed; the new requirement carries the corpus's usual
  length warning), `./gradlew spotlessApply` then `./gradlew spotlessCheck` (clean), and
  `python3 scripts/claim-scan.py --base origin/main` with its candidates resolved or dismissed by kind in `design.md`.
- **Known limitations**: the gate's reachability is a task, not yet evidence; the `review-quick` reviewer cannot verify
  the implementation's behaviour from these artifacts.
- **Subject packs**: `gradle-build` (the change decides a build task and the `check` graph).
- **Risk focuses**:
  1. Does each delta scenario describe what the planned mode verifies — the two class columns, the acknowledgement's
     scope, the unreadable-source failure — with no clause left unimplemented by a task?
  2. Is the single-sourcing claim true: no second copy of the class list or the boundary date in the artifacts or the
     plan?
  3. Does the sweep reach every site the new check's name and the ledger's class rule appear in — the ledger header, the
     spec requirement, the build wiring, the retrospective row, the ideas file — or does a site sit outside it?
  4. Is the four-row historical remediation admissible beside the spec's existing gap acknowledgement, or does it need a
     different mechanism?
- **Report language**: English; the report is agent-facing.

## Round 2

Same unit, base, tree, stage, artifacts, packs (`gradle-build`), and scope as round 1; reviewer session continued, not
replaced. Prior findings and their fixes:

1. `test-control` — the unreadable-source scenario had no control. Fixed by task 3.12 (a missing ledger, a missing
   definition, and an unloadable digest module each fail, naming the source; a missing ledger must not read as zero
   rows).
2. `contradiction` — Impact said three acknowledgement lines while What Changes said four. Impact now states four and
   the three retrospective reports.
3. `claim-unreproducible` — the proposal's "unmapped: none line is wrong" does not reproduce. It now states that the
   digest counts each label as a class while `unmapped` stays empty.
4. `claim-unreproducible` — the design claimed `check` runs three ledger checks. It now states two (`--unique-rounds`,
   `--session-continuity`) and the pre-commit guard's staged copy outside `check`.
5. `missing-task` — only the 2026-10-31 report was marked. Task 5.2 now names all three reports, and the 2026-10-15 (row
   and paragraph) and 2026-10-23 (row) edits are in this diff.
6. `impact-omission` — the checker's module docstring enumerates its modes. Added task 1.5, and task 5.4 now includes
   `scripts/commit-hygiene.py`'s mode enumeration.
7. `claim-missing` — the capability-routing premise had no claims-table row. Added row 9, quoting the `agent-skills`
   sentence that assigns the ledger row's format and verification to `commit-hygiene`.
8. `formatting` — two inline-code spans carried hand-wrap whitespace runs. Both were reworded; a scan for a double space
   inside a span over the four artifacts returns none.

Completed since round 1: `spotlessApply` and `spotlessCheck` clean; `openspec validate --all` 27 passed, 0 failed.

## Round 3

Same unit, base, tree, stage, artifacts, packs (`gradle-build`), and scope; reviewer session continued. Round 2's three
findings and their fixes:

1. `claim-missing` (repeat) — the dismissal list omitted `[cross-ref]`, a kind round 2's scan emits. Added a
   `[cross-ref]` dismissal (a merged change's or issue's identifier, or a hyphenated phrase read as a path shape, cited
   as a pointer), and the preamble now names the scan's scope as the unit's diff against the base rather than a staged
   diff.
2. `claim-unreproducible` (repeat) — decision 1's rejection reason claimed a ledger "committed without staging" ships
   the violation to CI, which does not reproduce. It now states the guard's real gap: it is activated per clone and
   bypassable with `--no-verify`.
3. `claim-unreproducible` (repeat) — the 2026-10-23 S2 row cited `docs/review-rounds.tsv:577` for the `marker-placement`
   row, and base line 577 is round 3 of that unit. The anchor now quotes `add-extraction-procedure` round 7.

Completed since round 2: `spotlessApply` then `spotlessCheck` clean; the round-2 ledger row appended and both ledger
checks pass.

## Round 4

Same unit, base, tree, stage, artifacts, packs (`gradle-build`), and scope; reviewer session continued. Round 3's
finding and its fix:

1. `claim-unreproducible` (repeat) — the `[cross-ref]` dismissal described its second shape as "a hyphenated phrase the
   scan reads as a path shape", while the candidate is `token/time` and the pattern matches a slash-joined
   `owner/repository` shape. The bullet now says exactly that.

Completed since round 3: the round-3 ledger row appended and both ledger checks pass.

## Round 5

Re-review after a base refresh, per the gate's rule that a hand-resolved conflict retires the closing round's coverage.
`origin/main` advanced to `2e48630f` (`adopt-heading-roles`, #812), which touched `docs/ideas.md` and
`docs/review-rounds.tsv` among other files. The tree was stashed, fast-forwarded, and popped; both conflicts were
resolved keeping both sides:

- `docs/ideas.md` — upstream removed the 2026-11-02 section's `adopt-heading-roles` entry; the section now carries this
  unit's parked digest follow-up only.
- `docs/review-rounds.tsv` — upstream's three `adopt-heading-roles` rows, then this unit's four rows.

The claims table's base revision is re-anchored to `2e48630f`; its figures reproduce there (the four offending rows at
the same base lines, the 20-class extraction, `PRE_CANONICAL_THROUGH`, and the two archive checks unchanged).

## Round 6

Re-review after a consultation's fixes, since the round-5 clean round no longer covers the tree. A `technical-advisor`
consultation raised three blockers; all three are applied:

1. Claims row 4 and Decision 4 claimed no round report is recoverable; two `docs/process-defects/` records describe an
   incident in the relevant round without naming a class. Row 4 now says no canonical class is derivable (widened
   source, per-record result), and Decision 4 states why a relabel would be this change's interpretation.
2. The acknowledgement control now covers the label dimension: another unknown label on the acknowledged row must fail,
   with the label half of the exemption key neutered; the delta's scenario covers all three occurrences.
3. Decision 5's digest rationale was false (the digest tallies the raised column only); it now justifies both columns
   from the ledger's header and states the digest's actual read.

The advisor closed with `Agreement: yes`, accepting three residual risks: prose-parser brittleness, membership not
judging classification, and the acknowledged labels staying unnormalized in the digest.

## Round 8 — implementation

Stage: implementation. The same tree, base `2e48630f`, change dir and docs edits, now with the implementation:

- `scripts/commit-hygiene.py` — new constants, `read_review_definition`, `review_rounds_boundary` (path-loads the digest
  script), `canonical_classes`, `acknowledged_class_triples`, `find_unknown_classes`, `check_class_vocabulary`, the
  `--class-vocabulary` mode and its `MODE_FLAGS` entry, and the module docstring's mode enumeration.
- `scripts/test-commit-hygiene.py` — `ClassVocabularyTests` (18 cases) and the `MODE_TEST_COVERAGE` entry.
- `build.gradle.kts` — `verifyReviewClassVocabulary` with four declared inputs, and the `check` dependency.
- `docs/review-rounds.tsv` — the header's acknowledgement-form paragraph and the four acknowledgement comments.

Completed: `python3 scripts/test-commit-hygiene.py` 271 tests OK; the two mutation probes (membership neutered → 4 of 18
fail; label key dropped → 1 fails; restored → 18 pass); `./gradlew verifyReviewClassVocabulary` BUILD SUCCESSFUL and its
presence in `check --dry-run`; the Docker-free `check` BUILD SUCCESSFUL (1m28s); `spotlessApply`/`spotlessCheck` clean;
`openspec validate --all` 27/0; both ledger checks and `--expect-clean` pass.

Prior findings: proposal rounds 1–7, all applied; round 7 clean.

## Round 9 — implementation re-review

Round 8's five findings and their fixes:

1. `test-control` — three branches were unexercised: the absent digest script, the canonical list's opening without its
   closing phrase, and a non-numeric round. Each now has a case, and the class carries 21 cases.
2. `missing-task` (verification ticks) — 8.1 through 8.5 are ticked with their results; 8.2's failure half was run
   through the Gradle task: a planted `gate-probe` row carrying `gamma-probe`, with an acknowledgement comment naming
   another label on that row, failed `:verifyReviewClassVocabulary` at `docs/review-rounds.tsv:2204`, and the restored
   ledger passes; "staged acknowledgement" is reworded to the acknowledgement comment.
3. `missing-task` (acknowledgement eligibility) — the mechanism trusts a comment that names a triple. That false
   negative is now recorded in the design's accepted-false-negatives bullet, and the delta states the comment is
   accepted as the record with a scenario for a reason the repository cannot evidence.
4. `redundant-code` — `find_unknown_classes`'s and `check_class_vocabulary`'s unused parameters are removed; both read
   the ledger and the definition from their owners' paths.
5. `formatting` — the five changed Python lines over 120 characters are wrapped;
   `perl -CSD -lne 'print if length > 120'` over both scripts returns nothing.

Re-run at 21 cases: membership predicate neutered → 4 fail; exemption key's label half dropped → 1 fails; restored → 21
pass. `spotlessCheck` and `openspec validate --all` (27/0) clean; the ledger checks pass.

## Round 10 — implementation re-review

Round 9's two findings and their fixes: claims row 11's restored figure now reads 21 (was 18); tasks 1.3 and 1.4 state
the current signatures (`find_unknown_classes(repo)`, `check_class_vocabulary(repo)`) and 1.3 names the unreadable-round
branch. `spotlessApply`/`spotlessCheck` clean; the round-9 ledger row appended and both ledger checks pass.

## Round 13 — after a consultation's boundary finding

A `technical-advisor` consultation on the implementation found one fail-open gap: `review_rounds_boundary` accepted any
nonempty string, so `PRE_CANONICAL_THROUGH = "banana"` made every dated row compare as pre-boundary and the check passed
clean while unknown classes sat in the ledger. Fixed by validating the declared value as `YYYY-MM-DD` and failing naming
the digest script; the delta's unreadable-source scenario now names a value that is not a date. A new case carries an
unknown class beside the malformed boundary, and removing the validation makes it fail (1 of 23).

Figures re-measured at 23 cases: membership predicate neutered → 4 fail; label half of the exemption key dropped → 1
fails; unit dimension dropped → 1 fails; restored → 23 pass. Suite 276 tests OK; Docker-free `check` BUILD SUCCESSFUL;
`spotlessCheck` clean; `openspec validate --all` 27/0.

## Round 15 — the boundary's calendar validation

The consultation's follow-up showed the shape check alone was not enough: `2026-99-99` matches `YYYY-MM-DD` and,
compared against ordinary row dates, still skips the whole ledger. The boundary and the row dates now share
`ledger_date`, which requires a written-out, calendar-valid `YYYY-MM-DD` (`date.fromisoformat` plus the round-trip
check), and the boundary failure names `scripts/review-rounds.py`. The cases cover `banana`, `2026-99-99`, and
`20261015` beside a row carrying an unknown class; a row's own date takes the same check. Neutering the boundary
validation fails its three sub-cases.

Suite 276 tests OK; Docker-free `check` BUILD SUCCESSFUL; `spotlessCheck` clean; `openspec validate --all` 27/0.
