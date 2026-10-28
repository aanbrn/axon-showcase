# Tasks

Workspace: unit=gate-manual-review-readiness;
path=/Users/afanasyev/Development/axon-showcase-gate-manual-review-readiness; branch=gate-manual-review-readiness;
writer=ses_e804f4f1bffecIPYa7PieI2YY1

## 1. Implementation

- [x] 1.1 Add the readiness helpers to `scripts/commit-hygiene.py`: the non-ignored untracked paths, the resolution of a
      base revision and of a merge base, and the per-path deleted-line multiset read from `git diff --unified=0` with
      header state (a deleted content line may begin with `--`), taking a deleted file's path from its old-side header.
      Verify: each helper returns the set its docstring names on a scratch repository. Superseded by `4.1`: the
      deleted-line helpers were replaced by history containment.
- [x] 1.2 Add `check_review_ready`: report the three failures together, exit non-zero while any remains, never stage,
      never fetch, and on success report the revisions it compared. Verify: a repository failing two obligations names
      both in one run. Superseded by `4.1` and `4.2`: the criterion is history containment and the reads fail closed.
- [x] 1.3 Wire the mode: `--review-ready` in the mode group, `--base` defaulting to `origin/main`, and a clean failure
      for `--base` without the mode. Verify: `python3 scripts/commit-hygiene.py --base main` exits non-zero naming the
      missing mode rather than ignoring the revision.
- [x] 1.4 Extend the module docstring and `--help` text with the mode and what it reads. Verify: `--help` lists
      `--review-ready` and `--base`, and the docstring's mode list matches the parser's.
- [x] 1.5 Cover the mode in `scripts/test-commit-hygiene.py`: ready, unstaged new file (still untracked afterward),
      ignored untracked path, staged-then-edited path, base advanced under an untouched path, refreshed tree,
      unresolvable base, `--base` without the mode, two failures reported together, and the mode reached through `main`.
      Verify: `python3 scripts/test-commit-hygiene.py` passes, and removing one obligation from the checker turns its
      case red.
- [x] 1.6 Replace §5's two prose steps in `.opencode/skills/review-gate/SKILL.md` with the check-first procedure: fetch,
      run the mode, apply the remedy each failure class names, re-run after a refresh, and name the mode's output line
      in the request. Verify: §5 names the command, carries a branch per failure class, and still points at `AGENTS.md`
      for the refresh itself.
- [x] 1.7 Verify the delta spec against the corpus: `openspec validate --changes gate-manual-review-readiness` and
      `openspec validate --all`. Verify: both pass, and the delta sits at
      `specs/showcase/quality/commit-hygiene/spec.md`.
- [x] 1.8 Decide the docs refresh and record the outcome: whether `AGENTS.md`, `README.md`, or an ADR is stale after the
      change. Verify: the decision names each file and its reason; no file is edited on a hunch. Done: no edit owed —
      `AGENTS.md`'s branch-refresh rule keeps its wording, the README lists the pre-commit guard and the `check` tasks
      (this mode joins neither), and no ADR describes the boundary.
- [ ] 1.9 Record the `showcase/quality/commit-hygiene` `## Purpose` refresh as owed by the archive commit, since a delta
      cannot carry it: the check enumeration gains this mode and the boundary-asserted clause gains its member. Verify:
      the archive commit's `git diff` shows the Purpose edit, or the report states why none was owed.

## 2. Verification

- [x] 2.1 Run the checker's suite: `python3 scripts/test-commit-hygiene.py`. Verify: it passes, and the new cases appear
      in its output.
- [x] 2.2 Live negative-then-positive control in this unit's own tree, before any staging: run
      `python3 scripts/commit-hygiene.py --review-ready`. Verify: it exits non-zero naming this change's untracked
      planning artifacts; after `git add -- openspec/changes/gate-manual-review-readiness`, the same invocation exits
      zero and prints the three revisions. Superseded by `4.1`: the report prints the base and `HEAD`, not a merge base.
      This is the surface the mode reads, so it is the control that counts.
- [x] 2.3 Run the gates: `./gradlew testCommitHygiene spotlessCheck` and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. Verify: both green, and `spotlessApply` has run after
      the last edit to any Spotless-owned file.
- [x] 2.4 Measure the changed files at 120 characters with the recorded recipe over
      `git status --porcelain --untracked-files=all`. Verify: no line exceeds 120, and the recipe hits a known positive
      first.

## 3. Review gate

- [x] 3.1 Run the pre-write derivation pass and record its claims table in `design.md`; re-derive it after the last edit
      to the unit's own record. Verify: every row's source reproduces.
- [x] 3.2 Run the quick-review loop to a clean round, appending the ledger rows and running both ledger checks. Verify:
      the closing round reports no observations, and `--unique-rounds --expect-clean gate-manual-review-readiness`
      passes.
- [x] 3.3 Derive the deep-review routing with `scripts/review-routing.py`, decide at or above the floor, and run the
      pass if it is `REQUIRED`. Verify: the outcome and its signals are recorded on this task. Done:
      `scripts/review-routing.py --change gate-manual-review-readiness` → `REQUIRED`, signal `requirement-block-added`.
      Pass 1: `rename-detection-false-negative` (should-fix, `scripts/commit-hygiene.py`, `deleted_lines_relative_to`) —
      FIXED_AND_VERIFIED; `foreign-untracked-remedy-wording` (nitpick, `.opencode/skills/review-gate/SKILL.md` §5) —
      FIXED_AND_VERIFIED. Pass 2: an `unread` word in the same §5 step (nitpick) — fixed and verified by reading. Pass 3
      (a third full pass, spent by a recorded decision): the owner's second consultation replaced the base criterion and
      made the reads fail closed after both earlier passes, so neither covered the current code; the residual material
      risk is that wholesale replacement, and the expected value an independent check of the containment criterion and
      the fail-closed paths. Pass 3: nothing remains — the criterion, the exit-code split in every read, the tests'
      ability to fail, and the delta's twelve scenarios verified independently; one observation, not a finding, that the
      base had advanced in the unit's own tree, handled at the boundary.
- [x] 3.4 Write `docs/process-defects/gate-manual-review-readiness.md` as defects are observed, or state that none was.
      Verify: the file exists before the manual-pass request. Done:
      `docs/process-defects/gate-manual-review-readiness.md` records two defects — the base-anchoring miss and the
      non-hermetic fixture.
- [x] 3.5 Run the readiness check itself at the boundary, stage this unit's new files, and request the owner's manual
      review pass, naming the mode's output line. Verify: the request names the review surface and the revisions
      checked; nothing is committed, pushed, or archived before the owner's approval. Done: `--review-ready` passes at
      the boundary, after the refresh onto `origin/main`, with the unit's new files staged; the request names the ready
      line it printed rather than a revision recorded here, which a later refresh would make stale.

## 4. Rework after the independent second consultation

- [x] 4.1 Replace the deletion heuristic with history containment: the base must be an ancestor of `HEAD`
      (`merge-base --is-ancestor`), with `merge-base`'s exit 1 (no shared history) kept apart from a failure and the
      report naming the base's missing commits and the paths they changed. Verify: a base that advanced and a base that
      dropped a line both fail; a unit with its own commits on a current base passes.
- [x] 4.2 Make the reads fail closed: a failed `git status`, `merge-base`, `rev-list`, or `rev-parse` reports
      `Readiness is not established` with the command, rather than an empty result; `rev-parse`'s "does not resolve"
      stays an answer. Verify: a corrupted index fails as not established rather than as ready.
- [x] 4.3 Rebuild the mode's tests on the new criterion: ready, own commits on a current base, untracked, ignored
      untracked, staged-then-edited, base advanced, base dropped a line, refreshed tree, unresolvable base, no shared
      history, `--base` without the mode, failed git read, and the mode through `main`. Verify: the suite passes and
      each of the four obligations turns its own cases red under a mutation.
- [x] 4.4 Rewrite the delta's requirement and scenarios on the containment criterion, dropping the binary/mode-only and
      rename scenarios the deletion reading needed. Verify: `openspec validate --changes` passes.
- [x] 4.5 Restate the design's criterion, accepted false negatives, and the failed-read decision, with the
      counterexample recorded beside the alternatives rejected. Verify: the design no longer describes the deletion
      comparison.
- [x] 4.6 Update the review-gate skill's §5 remedies: the moved-base branch names the containment failure and the
      fast-forward, and a new branch handles "readiness is not established". Verify: §5's branches cover each failure
      the mode can report.
- [x] 4.7 Record the two defects this rework surfaced in `docs/process-defects/gate-manual-review-readiness.md` (records
      `#3` and `#4`). Verify: both records carry their evidence.
- [x] 4.8 Re-run the gates: `python3 scripts/test-commit-hygiene.py`,
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `spotlessCheck`, `openspec validate --all`, and the
      120-character recipe. Verify: all green.
