# Tasks

Workspace: unit=new-worktree-before-first-write; path=/Users/afanasyev/Development/axon-showcase;
branch=new-worktree-before-first-write; writer=ses_e6a51d9beffeto8qnIbWdlzo8j.

## 1. The wrapper

- [x] 1.1 Write `scripts/new-worktree.sh` (POSIX `sh`): resolve the primary from the script's own location, fetch
      `origin/main`, create the tree against the primary explicitly with `--no-track`, copy the primary's
      `.python-version` when the tree has none, run `./scripts/setup-idea.sh` in it, print both stage lines, and — only
      when the primary carries `.idea/` — exit non-zero unless the preparation succeeds and its Python stage reports
      `CONFIGURED`. Support `--continue <branch>`, a sanitized destination for a branch containing `/`, and a failed
      preparation that keeps and reports the tree. **Done**: exit 0 ready or nothing owed, 1 kept-but-not-ready, 2
      usage/git refusal; the session is never moved and a tree is never deleted.
- [x] 1.2 Write `scripts/test-new-worktree.sh` in the `test-setup-idea.sh` pattern: throwaway repositories, a stub
      `setup-idea.sh` inside each fixture, and one control per case — new tree, continued branch with `/`, a tree
      carrying `.python-version`, a primary without `.idea/`, `SKIPPED` with exit zero, and `CONFIGURED` with a setup
      failure that retains the tree. **Done**: the suite states one control per case — success paths, readiness
      refusals, destination refusal, git refusals and usage — all green under `sh`, under `dash`, and under
      `NEW_WORKTREE_SHELL=dash dash` (which drives the wrapper under `dash`); each control was proved by removing its
      mechanism, which failed the suite, with the restore returning it green.
- [x] 1.3 Add `.opencode/commands/new-worktree.md`: the trigger, the wrapper's invocation, and the hand-back (path,
      branch, stage lines, whether the tree is ready). **Done**: the command states the readiness rule, the kept-tree
      failure, the `session_move` step, and the never-delete rule.
- [x] 1.4 Register `testNewWorktree` in `build.gradle.kts` and wire it into `check`. **Done**:
      `./gradlew testNewWorktree` passes and `check` depends on it.

## 2. The guidance and the spec

- [x] 2.1 Rewrite the Workspace bullet's creation and preparation instructions: the deadline is the tree's first
      unit-content write, and creation goes through the wrapper. **Done**: the new-unit and continued-unit bullets
      invoke the wrapper, and the bold-lead bullet states the deadline, the readiness condition and the kept-tree
      failure.
- [x] 2.2 Update `.opencode/skills/setup-idea/SKILL.md`: the initialization trigger line follows the first-write
      deadline and points the creation case at the wrapper. **Done**: the section names `./scripts/new-worktree.sh`, its
      steps stay the wrapper's own steps and the not-ready handling.
- [x] 2.3 Author the `MODIFIED` delta for `showcase/quality/ide-config`: the created-tree requirement gains the
      first-write readiness boundary and the unsuccessful-preparation outcome, with the existing scenarios carried over.
      **Done**: the requirement's 7 scenarios are carried over verbatim and 3 are added;
      `openspec validate new-worktree-before-first-write --type change` → valid.

## 3. Verification

- [x] 3.1 Run the new tests through `sh` and `dash`, then `./gradlew testNewWorktree`. **Done**: all three green.
- [x] 3.2 Live run: create a tree in a throwaway clone with the wrapper and confirm the branch, the inherited
      `.python-version`, and the Python stage line. **Done**: `./scripts/new-worktree.sh live-probe` reported
      `Tree ready: …/axon-showcase-live-probe (live-probe)`, exit 0, `.python-version` `3.12.16`; the probe tree and its
      branch were removed afterwards.
- [x] 3.3 Run `spotlessApply`/`spotlessCheck`, `./gradlew :check -PskipITs -Pcoverage.gate.enabled=false`, and
      `openspec validate --all`; run the manual 120-character check over the whole changed set. **Done**: all green; the
      manual check's over-long lines are `AGENTS.md`'s pre-existing subject-pack table and the claims table's
      formatter-aligned rows, and the three over-long script lines it found were wrapped.

## 4. Review, records and the manual pass

- [x] 4.1 Record the pre-write derivation pass and its claims table in `design.md`, seeded by `scripts/claim-scan.py`.
      **Done**: `scripts/claim-scan.py --base 1e4e8759` seeded the table, whose candidates resolve into rows or the
      per-kind dismissals beside it.
- [x] 4.2 Run the `review-quick` loop, appending each round's row to `docs/review-rounds.tsv`, until a round reports
      nothing new. **Done**: one reviewer session (`ses_e69f4d8abffeukICPDmSsK1zmf`, `created` then `continued`); each
      row was appended when its report arrived, both ledger checks ran after each append, and `--expect-clean` is clean
      on the closing round.
- [x] 4.3 Derive the deep-review routing with `scripts/review-routing.py` and run the routed pass. **Routing**:
      `RECOMMENDED` — `[repeated-class]` on six of the quick loop's rows. **Owner's decision**: run, on the advisor's
      recommendation (a behavioural pass over the wrapper's lifecycle; no confirmed problems). Passes and statuses are
      recorded in 4.3.1.
- [x] 4.3.1 Record each deep-review pass and every finding's status. **Pass 1** (initial, session
      `ses_e69bad8b8ffeC09qBXNtaBzIw5`): - S1 `test-control` (`scripts/test-new-worktree.sh`): the remote-only
      `--continue` path the guidance claims (git's DWIM) was never exercised — **FIXED_AND_VERIFIED** (a `remote-only`
      control publishes the branch from a publisher clone, asserts the primary holds no local copy, and checks branch,
      upstream and commit; removing the fetch fails it). - N1 `claim-unreproducible` (`proposal.md` Why, `design.md`
      Context): the third record is an inheritance-ordering defect, not a preparation omission — **FIXED_AND_VERIFIED**
      (pass 2 re-read both passages against the records). **Pass 2** (independent verification, same session): S1 and N1
      verified; one nitpick — the new control was missing from the proposal's test description and the claims table's
      control list — **FIXED**; a record-only sweep, so no third pass was run and the two-pass budget stands.
- [x] 4.4 Write `docs/process-defects/new-worktree-before-first-write.md` — or state that no defect was observed.
      **Done**: the record carries the misplaced `captured:` marker the first gate run caught.
- [x] 4.5 Remove the `docs/ideas.md` 2026-11-01 created-tree entry, since this change implements it. **Done**: the
      section is gone and `verifyIdeasSections` passes.
- [x] 4.6 Ask the owner for the manual review pass; commit and push only after approval. **Done**: the owner approved on
      2026-11-01, after the advisor agreed the unit was ready and task 4.2 was closed.

## Workflow follow-up

- The deferred generalization pass (`generalize-process-defects-2026-11-01`) resumes after this unit merges, and the
  three created-tree records become `shipped` with this pull request's link.
