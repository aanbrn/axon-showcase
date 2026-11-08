# Tasks

The unit's record is `docs/units/adopt-plugin-retried-fetches/unit.md`.

## 1. Release and the notes the bump stales

- [x] 1.1 Raise `helm-plugin` to `3.3.1` in `gradle/libs.versions.toml` (`helm-releases-plugin` shares the version ref)
      and verify `./gradlew buildLogicUpdates` no longer reports `helm-plugin` or `helm-releases-plugin`.
- [x] 1.2 Retarget the deprecation note in `.opencode/skills/gradle-build/SKILL.md` from plugin 3.3.0 to 3.3.1, since
      3.3.1 still calls `Project.getProperties()` and lists no `#145` fix, and verify no live doc still names 3.3.0 as
      the version emitting it (`grep -rn "gradle-helm-plugin 3\.3\.0" .opencode/skills/ docs/`).

## 2. Install path

- [x] 2.1 In `build.gradle.kts`, enable `helmAdd*Repository`, `helmAddRepositories` and `helmUpdateRepositories`; set
      `attempts = 3` and `retryDelay = 2 s` on the add tasks, and `attempts`, `retryDelay`,
      `repositoryCacheTtl = Duration.ZERO`, `repositoryNames` and the declared names in `extraArgs` on the update;
      delete the `updateHelmRepositories` registration and its bridge.
- [x] 2.2 Delete `build-logic/src/main/kotlin/UpdateHelmRepositoriesTask.kt` and verify a repository-wide grep for
      `UpdateHelmRepositoriesTask` and `updateHelmRepositories` finds no reference outside archived changes.
- [x] 2.3 Verify the install path's graph and scope: `./gradlew helmInstallToLocal --dry-run` lists the plugin's
      add/update tasks, and `./gradlew helmUpdateRepositories --info` executes `helm repo update <the declared names>`
      (read the plugin's `Executing:` line with a targeted grep and do not persist the log, which carries the process
      environment).

## 3. Chart packaging

- [x] 3.1 In `helm/chart/build.gradle.kts`, enable `helmAddBitnamiRepository`, `helmUpdateRepositories` and
      `helmUpdateMainChartDependencies`; set `attempts = 3` and `retryDelay = 2 s` on the add, update and dependency
      tasks; give the update `repositoryCacheTtl = Duration.ZERO`; scope the update to `bitnami` through
      `repositoryNames` and `extraArgs` (the plugin already sets `skipRefresh` on the dependency task); delete the
      `fetchMainChartDependencies` registration and its bridge.
- [x] 3.2 Delete `build-logic/src/main/kotlin/FetchChartDependenciesTask.kt` and verify a repository-wide grep for
      `FetchChartDependenciesTask` and `fetchMainChartDependencies` finds no reference outside archived changes.
- [x] 3.3 Run `./gradlew :helm:chart:helmPackageMainChart --info` and verify only the Bitnami fetches run, the packaged
      chart carries `charts/common`, and the executed `repo update` carries the `bitnami` scope (the `Executing:` line
      is read with a targeted grep and the log is not persisted).

## 4. Records

- [x] 4.1 Rewrite the install-path bullet in `.opencode/skills/deployment-operations/SKILL.md` for the plugin's tasks,
      their retry and scope, and verify it names no task this change deletes.
- [x] 4.2 Rewrite the chart-validation paragraph in `.opencode/skills/deployment-operations/SKILL.md` for the plugin's
      tasks and the Bitnami scope, and verify it matches `helm/chart/build.gradle.kts`.
- [x] 4.3 Remove the 2026-10-27 `failIfExists`, 2026-10-17 workaround-adoption and 2026-10-16 cache-skip entries from
      `docs/ideas.md`, rewrite the 2026-11-09 entry to park a per-download timeout on every fetch path, and verify
      `--ideas-sections` passes.

## 5. Upstream comment

- [x] 5.1 Draft the comment on `#238` naming the two API papercuts and their `extraArgs` workarounds, run it through the
      review gate, post it, and verify the comment URL resolves and is referenced from the pack. Posted at
      `https://github.com/build-extensions-oss/gradle-helm-plugin/issues/238#issuecomment-6728129491` (HTTP 200).

## 6. Integration verification

- [x] 6.1 Clear the configuration cache, run `./gradlew helmUpdateRepositories` twice, and verify the first run stores
      the entry and the second reuses it, with the task executing on the first run.
- [x] 6.2 Clear the configuration cache, run `./gradlew :helm:chart:helmPackageMainChart` twice, and verify the first
      run stores the entry and the second reuses it.
- [x] 6.3 Run the Docker-free root gate (`./gradlew check -PskipITs -Pcoverage.gate.enabled=false`) and verify it
      passes.
- [x] 6.4 Run `openspec validate --all` and `./gradlew spotlessCheck`, and verify both pass after the last edit.

## 7. Review gate

- [x] 7.1 Re-run the pre-write derivation pass over the revised artifacts and update the claims table in `design.md`;
      re-derive it after the last edit to the unit's own record. Verify: every row's source reproduces.
- [x] 7.2 Re-run the quick-review loop to a clean round over the revised artifacts, appending the ledger rows and
      running the ledger checks. Verify: the closing round reports no observations and `--expect-clean` passes.
- [x] 7.3 Derive the deep-review routing with `scripts/review-routing.py`, decide at or above the floor, and run the
      pass if it is `REQUIRED`. Verify: the outcome and its signals are recorded in `docs/units/<unit>/deep-review.md`.
- [x] 7.4 Write `docs/units/adopt-plugin-retried-fetches/process-defects.md`, recording any defect the unit observed or
      stating that it observed none. Verify: the file exists before the manual-pass request.
- [x] 7.5 Request the owner's manual review pass and stop. Verify: the request names the review surface; nothing is
      committed, pushed, or archived before the owner's approval.

## Workflow follow-up

- Archive the change after the review gate and the owner's manual pass, then merge.
