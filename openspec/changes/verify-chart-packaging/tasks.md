# Tasks

## 1. The build wiring

- [x] 1.1 `build.gradle.kts` — add `dependsOn(":helm:chart:helmPackageMainChart")` to the standard `check` task, beside
      its existing members. Verify: `./gradlew check --dry-run` lists `:helm:chart:helmPackageMainChart` with its
      packaging dependencies, and `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes. **Done** — the
      dry-run lists the repository-setup, fetch, and lint tasks; the Docker-free check is `BUILD SUCCESSFUL in 21s`.

## 2. The spec

- [x] 2.1 The `ADDED` delta for `showcase/deployment/helm-chart` at
      `openspec/changes/verify-chart-packaging/specs/showcase/deployment/helm-chart/spec.md`: the standard `check`
      packages the chart and fails when the chart cannot bundle its declared `common` dependency, with three scenarios —
      the packaging running, the unbundlable dependency failing the check, and the cluster-free host. Verify:
      `openspec validate verify-chart-packaging --strict` passes, and the requirement's description is within the
      500-character guidance (record the count). **Done** — written in the proposal round; `--strict` valid, the
      description measures 334 characters, and the three scenarios are present.
- [x] 2.2 Record the `showcase/deployment/helm-chart` `## Purpose` refresh for the archive commit: the Purpose
      enumerates the build-side content it covers ("the Gradle Helm-release configuration the chart is deployed through
      (each release's namespace and the per-target kube context)"), so it gains this member. A delta cannot carry a
      Purpose, so the edit lands with the archive and the implementing PR's diff carries no main-spec change beyond the
      delta. **Recorded; owed at archive.**

## 3. The docs

- [x] 3.1 `AGENTS.md` — the `check` note names the chart packaging among its members, and the chart-validation bullet
      states that packaging now runs in `check`, so the dependency-fetch skip fails the pull request (in the strict lint
      packaging runs) rather than only the nightly deployment smoke. Derive the member set by grepping
      `verifyDashboardJson` across the live files: it returns `AGENTS.md`'s `check` note and `README.md`'s
      verification-command block, and the README needs no edit because this change adds no runnable task. Verify: the
      edited sentences read against `./gradlew check --dry-run`'s member list. **Done** — the note's first member and
      the chart-validation bullet's new lead sentence both match the dry-run's list.
- [x] 3.2 `docs/ideas.md` — remove the "Give the `build` gate a chart-packaging path" entry this change implements; the
      `## 2026-10-16` section keeps its other entry. Verify: `grep -n "chart-packaging path" docs/ideas.md` returns
      nothing. **Done** — the grep returns nothing and the section keeps its upstream-report entry.

## 4. Verification

- [x] 4.1 Prove the gate fails on a chart that cannot bundle its dependency: move
      `helm/chart/build/helm/charts/axon-showcase/charts/common-*.tgz` aside, then run
      `./gradlew :helm:chart:helmPackageMainChart` with `-x` excluding the fetch task
      (`:helm:chart:helmUpdateMainChartDependencies`) and each lint task (`:helm:chart:helmLintMainChart`,
      `:helm:chart:helmLintMainChartFull`, `:helm:chart:helmLintMainChartMinimal`), so the fetch cannot restore the file
      and no lint fails first. Confirm the run fails naming the missing dependency; restore the file and confirm the
      full task passes. Verify: both outcomes read from the run's own output, not assumed. **Done** — the run failed
      with "Error: found in Chart.yaml, but missing in charts/ directory: common" and `BUILD FAILED`; after restore,
      `BUILD SUCCESSFUL`.
- [x] 4.2 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, `openspec validate --all`,
      `./gradlew spotlessApply` followed by `spotlessCheck`, and the manual 120-character check over the changed set
      (the untracked change dir included: `git status --porcelain`, since the change dir is untracked until its first
      commit). Verify: each command's outcome is read from its own run. **Done** — the Docker-free check is
      `BUILD SUCCESSFUL`; `openspec validate --all` is 27 passed / 0 failed (the >500-character warnings are on
      pre-existing requirements of other specs and `helm-chart`'s own); `spotlessApply`/`spotlessCheck` are clean; the
      120-character sweep (with its positive control) flags only `docs/review-rounds.tsv`'s one-line rows and the
      design's table rows, both unwrappable, and `AGENTS.md`'s single over-120 line is pre-existing (1 at `HEAD`).
- [x] 4.3 Run `review-quick` over the proposal, design, tasks, and delta, then over the implementation, repeating with
      each prior round's instances passed and appending each round's record line to `docs/review-rounds.tsv`, until the
      review reports no new observation. Verify: the reviewer's verdict lines and the appended ledger rows. **Done** —
      the loop's rounds are appended to `docs/review-rounds.tsv`, the ledger naming each round's findings and the clean
      round reporting none.

## 5. Capture

- [x] 5.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Verify: the recorded delta reproduces with a `--numstat` diff of `AGENTS.md`
      against `origin/main`. **Done** — the capture ran and proposed one mode bullet (a check whose bad state the task
      it depends on already rejects can never fire) plus a single-sourcing pointer replacement, both applied; the
      applied delta is `git diff --numstat origin/main -- AGENTS.md` = 18 insertions / 11 deletions (net +7 for the
      change, of which the capture contributed +4).
- [x] 5.2 Ask the owner for the manual review pass, after 4.3 is clean and 5.1 is done. Verify: the request is in the
      session and the outcome is the owner's. **Asked** — the request is in this session; the outcome is the owner's.

## 6. The change's own records

- [x] 6.1 Record the pre-write derivation pass as a task (every unit owes it). Its output is the claims table in
      `design.md`, whose rows are derived from the `check` dry-run, the packaging probe and its `--dry-run`/`--info`
      runs, `AGENTS.md`'s chart-validation bullet and the `setOnlyIf { true }` workaround, the chart's `Chart.yaml` and
      the absence of a `.helmignore`, the root `check` task's registrations, `code-quality`'s control requirement, and
      the `helm-chart` Purpose. Verify: each row's source reproduces. **Done** — the table was written in the proposal
      round and the implementation left it untouched; its rows were re-reproduced across the review rounds.
- [x] 6.2 Seed the table with `python3 scripts/claim-scan.py` over the unit's diff and resolve every candidate into a
      row or a dismissal grouped by kind beside the table (design.md's "Dismissed scan candidates"), rather than letting
      the scan replace the derivation. Verify: every printed candidate maps to a row or a listed dismissal. **Done** —
      the scan's candidates each fall under a listed kind; the run carries no count, since the diff grows while the
      change is in flight.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit — the change-dir move, the main-spec sync, and the
  recorded `helm-chart` Purpose refresh — rides this PR.
