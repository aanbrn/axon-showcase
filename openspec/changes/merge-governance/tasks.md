# Tasks

## 1. CI workflow

- [x] 1.1 Add `.github/workflows/ci.yml` with a single `build` job on `ubuntu-latest` (Temurin JDK 21), triggering on
      `pull_request` and `push` to `main`, and verify the workflow file parses (`yaml` lint / Actions preview shows no
      syntax errors)
- [x] 1.2 Wire the job so a pull-request run executes `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and
      `openspec validate`, and a `main` push executes `./gradlew check` and `openspec validate`, and verify the
      event-dependent command is selected correctly (e.g. via `github.event_name` conditional in the run step)
- [x] 1.3 Configure the Gradle invocation with the wrapper (`./gradlew`) and a JDK 21 setup step, and verify the job
      resolves Java 21 and the wrapper without a local JDK

## 2. Verify the check runs green

- [ ] 2.1 Open a pull request carrying `ci.yml` and verify the `build` check appears and passes on the PR head
      (PR path: `check -PskipITs` + `openspec validate`)
- [ ] 2.2 Merge the PR with a squash merge and verify the `build` check runs and passes on `main`
      (main path: full `check` with integration tests and coverage)
- [ ] 2.3 Confirm `openspec validate` passes for the `merge-governance` change itself and that no existing spec is
      broken by the workflow (the CI spec gate is green on both paths)

## 3. Verify baseline rulesets match the spec

- [ ] 3.1 Drift-check the four existing `main-*` rulesets against the `merge-governance` spec baseline via the GitHub
      API (`gh api repos/aanbrn/axon-showcase/rulesets`), and verify each matches its documented requirement:
      `non_fast_forward` on `main-block-force-pushes` (owner bypass), `required_linear_history` on
      `main-require-linear-history` (no bypass), `pull_request` with squash-only and one approval on
      `main-require-pr-on-merge` (owner bypass), and `deletion` on `main-restrict-deletions` (no bypass)
- [ ] 3.2 If any drift is found in 3.1, reconcile the live ruleset to the spec via the API (in-place update, no
      delete/recreate) and re-run 3.1 to confirm clean

## 4. Enforce the CI check via a no-bypass ruleset

- [ ] 4.1 Create a new branch ruleset `main-required-checks` targeting `refs/heads/main` (enforcement: active) whose
      only rule is `required_status_checks` on check name `build`, with NO bypass actors, and verify via the API that
      the ruleset lists no `bypass_actors` and the required check is `build`
- [ ] 4.2 Verify merge protection blocks a merge to `main` when the `build` check has not passed (open a PR with a
      forced-failing check or observe the required-check gate on a fresh PR, and confirm the merge is blocked)
- [ ] 4.3 Verify the existing four `main-*` rulesets are unchanged (force-push, linear-history, PR+squash, deletion)
      and that the owner bypass on `main-require-pr-on-merge` does not exempt the CI gate (the check requirement lives
      only in `main-required-checks`)

## 5. Docs refresh

- [ ] 5.1 Update `AGENTS.md` and `README.md` to mention the CI workflow (PR fast gate, full gate on `main`,
      required-check ruleset) and verify the edited files respect the 120-character line limit
- [ ] 5.2 Update `docs/ideas.md` to mark the CI idea as captured by this change, and verify no stale CI note remains
      pending