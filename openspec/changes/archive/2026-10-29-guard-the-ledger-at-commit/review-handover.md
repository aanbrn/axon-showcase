# Review hand-over

## Round 1 — the proposal

- **Unit** — change `guard-the-ledger-at-commit`, branch of the same name, base `8f99989a`; no commits; the change
  directory is untracked; the branch name is the change name, so no claim file is needed.
- **Supplied artifacts** — `proposal.md`, `design.md` (four decisions, seven claims rows, risks, non-goals), `tasks.md`,
  `specs/showcase/quality/commit-hygiene/spec.md` (a `MODIFIED` block with the pre-commit requirement's description, all
  26 scenarios, the ledger clause and the append-only property, and one new scenario); the evidence the design cites:
  `scripts/git-hooks/pre-commit`, `scripts/commit-hygiene.py` (`check_staged`, the ledger checks), the spec's ledger
  requirement, `docs/ideas.md` 2026-10-24, `docs/review-rounds.tsv`.
- **Scope** — the proposal round: the guard's two additions, the working-tree decision, the controls, the delta; no
  implementation exists.
- **Prior findings** — none; the unit's first round.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  claims rows were re-run at the base.
- **Known limitations** — the staged-blob reading is deliberately the working tree, pinned by the stale-index check; the
  controls are planned, not written.
- **Deadline** — 15 minutes, the caller's policy.

## Open questions

none

## Round 2 — the premise and the surfaces reconciled

- **Prior findings and their instances (round 1), with fixes** —
  1. The incident premise claimed the guard would have caught `projector-operability`; that record is a wrong _allowed_
     reason, which the continuity check does not detect → the premise now cites the idea's account and states what the
     guard adds (commit-time detection of duplicates, gaps, continuity and row loss), not a claim about that unit.
  2. The working-tree decision was unsound for a type change → the checks now read the staged blob through
     `staged_file_text`, and the finders gain an optional text.
  3. The design said `check` runs only the first verification → corrected: it runs both, with a claims row.
  4. "No staged-blob reader" contradicted the existing `staged_file_text` → the design now says no reader is **added**,
     and reuses it.
  5. "Contiguity passes over a silently removed row" over-claimed → narrowed to a removed highest row and a unit's whole
     set, and the append-only rationale rests on those.
  6. The capability Purpose attributes the ledger checks to the build alone → task 4.4 refreshes it in the archive
     commit.
  7. The implemented idea was not removed → task 4.3 and the Impact name it.
  8. Two live enumerations of the guard's checks gain a member → README (task 4.1) and the `AGENTS.md` gotcha (task
     4.2).
  9. An unparseable ledger row would surface as a traceback → the guard reports it by name (task 1.3) with a control
     (2.6) and a scenario.
  10. The append-only premise had no claims row → row 10 derives that contiguity misses a highest-row removal, and row 6
      cites a historical removal.
  11. The delta's clause bundled five statements and its scenario three causes → four separate SHALLs and seven
      scenarios.
  12. Row 3 said "three definitions" for four matches → it names the four, including the shadowed one and the live one.
  13. Row 5's line range was pinned to the parent revision → every row is re-pinned to the base `8f99989a`, and the idea
      is cited by phrase.
  14. Row 6's `git diff --cached` printed nothing → it cites a historical removal (`7a7c8714`) that shows the `-` row
      shape.
  15. Row 4 said "no stated append-only rule" while the idea calls it a convention → narrowed to "no requirement".
  16. The task said "all 26 scenarios" → "all 25 scenarios", plus the seven added.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; every
  claims row was re-run at the base.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — the duplicate-repair rule is a design decision (the #606 repair would otherwise be blocked);
  the controls are planned.
- **Deadline** — 15 minutes, the caller's policy.

## Round 3 — the deletion hole and the two-model corpus

- **Prior findings and their instances (round 2), with fixes** —
  1. A staged deletion or rename-away of the ledger ran no check (`staged_paths` matches `ACMR`) → the gating is a
     deletion-inclusive set (staged paths, deletions, rename sources), an absent staged blob reads as empty, and the
     append-only SHALL names the deleted or renamed case; tasks 2.10 and the removed-row scenario cover it.
  2. The guard reads the staged blob while the ledger requirement says the verification reads the tracked file → a
     second `MODIFIED` block on that requirement scopes its input clause to the build and names the guard's staged read.
  3. No control exercised the staged-blob decision → task 2.9 stages a duplicated row, fixes the working-tree copy, and
     confirms the staged duplicate is still named.
  4. The proposal still claimed a wrong-base re-apply passes "both contiguity and review" → it now names the three
     shapes contiguity misses (a highest row, a unit's whole set, a deleted or renamed ledger).
  5. The type-change premise had no claims row → rows 11 (the status filter excludes `T`) and 12 (the guard already
     composes deletions and rename sources) derive it.
  6. The unparseable scenario named a session field that does not raise → it names the round column, the only
     `ValueError`.
  7. The clean-append scenario bundled the un-staged case and task 2.8 bundled the mutation → split: the clean append
     (2.7), the un-staged case (2.8), the staged-blob control (2.9), the deletion (2.10), the mutation (2.11), and the
     delta gains the eighth scenario.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; every
  claims row was re-run at the base.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — the controls are planned; the versioning of the ledger requirement's input clause is a
  clarification, not a behaviour change.
- **Deadline** — 15 minutes, the caller's policy.

## Round 4 — the ledger-scoped gating and the reading source

- **Prior findings and their instances (round 3), with fixes** —
  1. The deletion-inclusive set named helpers that are scoped to `openspec/changes/`, so it could never contain the
     ledger → D2 and task 1.1 name a ledger-scoped query
     (`git diff --cached --name-status --diff-filter=DR -- docs/review-rounds.tsv`) and state that the change-path
     helpers are not reused; claims row 12 records their scoping and row 13 the query.
  2. The ledger requirement's note pointed at a reading source the pre-commit requirement did not state → the pre-commit
     requirement now states "It SHALL judge the ledger's staged content, not the working tree's copy", and the ledger
     note is a one-clause scope ("That input clause scopes the build's verification").
  3. The proposal's What Changes did not name the second delta block → it now does.
  4. The proposal still said "round or session field" → "round column".
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — the controls are planned; how an absent staged blob is distinguished from a failed read is
  left to the implementation (`staged_change_presence` is the pattern to copy).
- **Deadline** — 15 minutes, the caller's policy.

## Round 5 — closing

- **Prior findings and their instances (round 4), with fixes** —
  1. The delta task said "the four ledger SHALLs" while the block carries five → "the five ledger SHALLs".
  2. Claims row 12 paraphrased the helpers' pathspec as `-- {0}` → it now states `-- openspec/changes/`
     (`CHANGES_PREFIX`).
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; rows
  1–13 re-run at the base; the DR query verified in a scratch repository.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — the controls are planned; the absent-blob handling is the implementation's.
- **Deadline** — 15 minutes, the caller's policy.

## Round 6 — the implementation

- **Unit** — change `guard-the-ledger-at-commit`, branch of the same name, base and `HEAD` `8f99989a`; no commits; the
  change directory, the unit's record and the ledger rows are uncommitted.
- **What the implementation does** — `commit-hygiene.py`: `ledger_source` (`None` reads the file, `""` is empty) is
  threaded through the ledger readers; `check_staged` runs the duplicate/contiguity and session-continuity checks when
  the ledger is in the staged paths or a `--diff-filter=DR` ledger query reports its deletion/rename, reading
  `staged_file_text` (an absent blob as empty), catching `ValueError` as a named failure; `check_ledger_preservation`
  compares the parent commit's `(unit, round)` keys with the staged text's, refusing a lost key and a malformed removed
  row. The hook header, `README.md` (seven→eight), the `AGENTS.md` gotcha and `docs/ideas.md` are updated; the unit's
  record is added.
- **Controls** — eleven in `LedgerGuardTests` (duplicate, gap, continuity, removed key, duplicate repair, unparseable,
  clean append, un-staged ledger, staged-blob discriminator, deleted ledger, renamed-away ledger); the suite is 203
  tests, OK.
- **Mutation evidence** — running the ledger block unconditionally (`if True`) breaks the suite (40 errors, 1 failure,
  including the un-staged control); replacing the staged-blob read with a worktree read makes the discriminator fail;
  both restored, hash-verified.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green;
  `./gradlew testCommitHygiene` green; the Docker-free `check`: `BUILD SUCCESSFUL`.
- **Known limitations** — the Purpose refresh is deferred to the archive commit (task 4.4); the continuity check
  validates recorded transitions, not that a replacement's reason occurred (the pre-existing boundary).
- **Deadline** — 15 minutes, the caller's policy.

## Round 7 — the gating control and the absent ledger

- **Prior findings and their instances (round 6), with fixes** —
  1. The un-staged control (2.8) could not fail: its ledger was committed, so an unconditional run read the clean staged
     blob → its fixture now holds a ledger absent from the index and `HEAD`, defective in the worktree, with another
     file staged. Under the gate the run reports nothing; with the gate replaced by `if True` the control fails with
     `GitFailure: git show :docs/review-rounds.tsv failed: … does not exist`. The earlier "40 errors" evidence is
     superseded: it came from unrelated fixture repos and did not name this control.
  2. A deleted or renamed-away ledger printed a spurious continuity reason ("carries no boundary comment") beside the
     removal → the ledger checks are skipped when the staged ledger is absent, and the deletion and rename controls
     assert that no "boundary comment" message appears.
  3. The fixture's 143-character `write_text` line → rebuilt from a rows list; the >120 scan over both scripts is clean.
  4. The unparseable message now names the ledger's path, matching the scenario's "names the file and the failure".
- **Completed checks** — `scripts/test-commit-hygiene.py` 203 tests OK; the gating mutation re-run and the detecting
  control named; the file restored (hash-verified); `openspec validate --all` 27/0; `spotlessApply`/`spotlessCheck`
  green.
- **Scope** — the implementation round.
- **Known limitations** — the Purpose refresh is deferred to the archive commit; the continuity check's recorded-reason
  boundary is pre-existing.
- **Deadline** — 15 minutes, the caller's policy.

## Round 8 — closing

- **Prior finding and its instance (round 7)** — the unparseable-row control asserted the failure text but not the
  ledger's path, so the scenario's "names the file" half was unchecked; dropping the path from the message left all
  eleven controls green. The control now asserts `docs/review-rounds.tsv`, and the path-drop mutation fails exactly that
  control; the file is restored and the suite passes.
- **Completed checks** — `scripts/test-commit-hygiene.py` 203 tests OK; the path-drop mutation run and the detecting
  control named; `openspec validate --all` 27/0; `spotlessApply`/`spotlessCheck` green.
- **Scope** — the implementation round.
- **Known limitations** — the Purpose refresh is deferred to the archive commit; the malformed-parent branch's
  unreachable state is noted.
- **Deadline** — 15 minutes, the caller's policy.

## Deep review — pass 1

- **Routing** — `RECOMMENDED` on `[repeated-class] 5 rows repeat a prior class`; the owner chose to run it.
- **Session** — `ses_e7b72a050ffeWE1vRLOnopKIOU`, created for this change; initial review, not a fix verification.
- **Packs** — `none` (Python tooling and git-hook logic; no JVM, test-tier, Gradle build, or deployment decision).
- **Findings** —
  - `S1` (should-fix, `OPEN`) — the module docstring's `--staged` enumeration omits the ledger check; the other three
    live enumerations were swept, this one was not.
  - `N1` (nitpick, `OPEN`) — a staged unparseable row also produces a misleading "row is removed" report, because the
    preservation comparison discards the staged side's malformed rows.
- **Checks passed** — the optional-text seam; the deletion/rename gating and its `DR` query; the key comparison
  (survivor passes, lost key and deleted/renamed ledger fail, duplicate repair passes); the five SHALLs against the
  code; the ledger note as a scope clause; all eleven controls discriminating; the crash paths; no false block of merge,
  rebase, or base-refresh shapes; the four surfaces and the unit's record; the delta's scenario preservation; the 120
  scan.
- **Pass budget** — pass 1 of 2; a verification pass follows the fixes.
- **Owner decision** — which findings to fix; no fix has been applied.

## Round 9 — the deep pass's fixes

- **Prior findings and the consultation's additions, with fixes** —
  1. `S1` — the module docstring's `--staged` enumeration omitted the ledger check → the enumeration is replaced with a
     purpose summary that names the ledger check and points at the pre-commit requirement, so the docstring states what
     the mode is for without a fourth copy of the list.
  2. `N1` — an unparseable staged row also produced a misleading "row is removed" report → the preservation comparison
     is skipped when the staged side has unparseable rows, and it prints that preservation was not assessed; the parse
     failure still blocks the commit, and repairing it exposes a real loss (a control stages a malformed row beside a
     removal, then repairs it and sees the removal).
  3. The predecessor probe conflated an absent ledger with a failed inspection → `predecessor_ledger_text` treats an
     unborn `HEAD` and a ledger absent at `HEAD` as no predecessor, and raises a named failure when `git ls-tree` fails;
     a control mocks the failure and expects `CheckFailure`.
  4. A first ledger with no predecessor is a control now: it passes.
- **Completed checks** — `scripts/test-commit-hygiene.py` 206 tests OK; `openspec validate --all` 27/0;
  `spotlessApply`/`spotlessCheck` green.
- **Scope** — the implementation round; the deep pass's findings are fixed, pending its verification pass.
- **Deadline** — 15 minutes, the caller's policy.

## Deep review — pass 2 (verification)

- **Session** — `ses_e7b72a050ffeWE1vRLOnopKIOU`, continued; the budget's second and final full pass.
- **Verdict** — nothing remains.
- **S1 — `FIXED_AND_VERIFIED`.** The docstring states a purpose summary and points at the pre-commit requirement; it no
  longer claims a complete list.
- **N1 — `FIXED_AND_VERIFIED`.** The preservation comparison is skipped for an unparseable staged side with a "not
  assessed" message; the parse failure blocks independently, and the repaired ledger exposes a real loss (controls prove
  both, and removing the early return fails them).
- **Predecessor probe — `FIXED_AND_VERIFIED`.** An unborn `HEAD` and an absent ledger read as no predecessor; an
  `ls-tree` failure raises. The exit-128 concern is resolved empirically: `git rev-parse --verify --quiet HEAD` exits 1
  for an unborn `HEAD` and 0 for a dangling one, whose `ls-tree` then fails and raises; a repository broken enough to
  fail `rev-parse` fails earlier in `check_staged`.
- **Checks passed** — 206 tests OK; the 120 scan clean; the surfaces, delta, design, tasks and record unchanged and
  accurate.
- **Deferred** — the archive-commit Purpose refresh (task 4.4).
