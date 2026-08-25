# Tasks

## 1. Make the major heuristic calendar-aware

- [x] 1.1 Replace `majorOf()` in `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` with a
      calendar-aware "is this a major bump" check: when the current version's leading integer is a 4-digit year, compare
      the `YYYY.TRAIN` pair (first two segments) of candidate vs current and treat a pair change as a major; otherwise
      fall back to the existing leading-integer semver comparison. Verify `./gradlew :build-logic:compileKotlin` compiles.
- [x] 1.2 Confirm the calendar branch is reachable and correct for `reactor-bom` (`2025.0.7`): a `2025.1.x` candidate
      is treated as a major bump and a `2025.0.8` candidate as a non-major (same-train) update. Verify by running
      `./gradlew dependencyUpdates` and inspecting the reactor rows' classification.

## 2. Update the spec

- [x] 2.1 Sync the calendar-aware major-comparison requirement from the change delta
      (`openspec/changes/calendar-aware-major-heuristic/specs/.../spec.md`) into the main spec
      `openspec/specs/showcase/quality/dependency-management/spec.md`, folding it into the existing "Major updates can
      be suppressed per coordinate" requirement (the calendar rule is a refinement of that requirement's major
      classification).

## 3. Verify and polish

- [x] 3.1 Run `./gradlew dependencyUpdates` and confirm: calendar coordinates classify train changes as majors, same-train
      service releases as non-majors, and all semver coordinates behave exactly as before (no regression in existing
      reported updates).
- [x] 3.2 Run `openspec validate calendar-aware-major-heuristic` and confirm the change is valid, then run
      `./gradlew spotlessApply` on the touched Kotlin file.
- [x] 3.3 Refresh `AGENTS.md` and `README.md` if the dependency-updates behavior/conventions need documenting, per the
      docs-refresh-on-change convention.