# Tasks

## 1. Implementation

- [x] 1.1 Set `revision = "release"` in `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` (next
      to `gradleReleaseChannel = "CURRENT"`). Verify `./gradlew dependencyUpdates` then writes
      `The following dependencies have later release versions:` in `build/dependencyUpdates/report.txt` (and no
      `have later milestone versions:` header). — Done: the report now reads `using the latest release version:` /
      `have later release versions:` / `Gradle CURRENT updates:`.
- [x] 1.2 Fix `.github/workflows/dependency-updates.yml`'s catalog extraction: start the `awk` at
      `^The following dependencies have later release versions:` (drop the non-existent `have newer versions` match and
      the `later milestone versions` stop), and stop it at `^Failed to ` and `^Gradle CURRENT updates:` — the plugin
      emits its `Failed to …` sections between the upgrades and the Gradle section, so a single stop would sweep them
      into the block. Verify by running the exact `awk` over `build/dependencyUpdates/report.txt` and reading the output
      — it must list the stable catalog rows and none of the `Failed to …` lines. — Done: the extraction yields 28
      catalog rows and no `Failed to …` line.
- [x] 1.3 `.opencode/commands/dependency-updates.md`: replace the "Ignore the 'dependencies have later milestone
      versions' section … (a 'dependencies with newer versions' section) matter" premise with the report's
      `have later release versions` section as the actionable set. Verify the command no longer names a `newer versions`
      section. — Done: the command names the release section as the actionable set.
- [x] 1.4 Remove the "Record the `dependency-updates` tracker's catalog-extraction gap" idea from `docs/ideas.md` (the
      change's own idea removal rides the branch). Verify `grep -rn "catalog-extraction gap" docs/ideas.md` returns
      nothing. — Done: the idea is removed.

## 2. Verification

- [x] 2.1 Prove the extraction on known inputs: run the workflow's `awk` over `build/dependencyUpdates/report.txt` with
      (a) the correct `have later release versions:` header — expect the stable rows (e.g. 28); (b) the old
      `have newer versions:` header — expect empty (it exercises the start pattern); and (c) a synthetic copy with a
      `Failed to compare versions …` block inserted before `Gradle CURRENT updates:` — expect the same stable rows and
      no `Failed to …` line (it exercises the stop boundary, which the real report does not contain locally). Read all
      three outputs. — Done: (a) 28 rows, (b) 0 rows, (c) 28 rows with 0 `Failed to …` lines; (a) and (c) are identical.
- [x] 2.2 Run `openspec validate --changes` (the delta validates and preserves every existing scenario),
      `./gradlew spotlessApply` followed by `./gradlew spotlessCheck`, and `./gradlew workflowLint`; all pass. — Done:
      all pass.
- [x] 2.3 Live check after merge (owner/user): dispatch `dependency-updates.yml` and confirm the "Dependency updates"
      issue now carries the stable catalog-updates section (previously always empty). If no dispatch is possible, park
      the follow-up in `docs/ideas.md` and name it in the change's report. — Done: dispatched run 36132798579 succeeded
      and issue #13 now carries the `have later release versions:` section (28 rows).
