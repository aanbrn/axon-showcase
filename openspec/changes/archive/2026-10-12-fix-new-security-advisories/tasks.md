# Tasks

## 1. The spec

- [x] 1.1 Record the `dependency-security` `## Purpose` refresh as its own step: a delta cannot carry a Purpose, so add
      `org.xerial.snappy:snappy-java` and `guava` to the Purpose (which enumerates the constrained set) in the archive
      commit. Verify the Purpose names both after archive.

## 2. Constrain snappy-java

- [x] 2.1 Add `snappy-java` to `gradle/libs.versions.toml` and a constraint in `platform/build.gradle.kts`, pinning it
      to the latest fixed release (`1.1.10.12`). Verify the resolution
      (`./gradlew :showcase-command-service:dependencies --configuration runtimeClasspath` shows `1.1.10.12`).

## 3. Constrain the errorprone configuration

- [x] 3.1 In `build-logic`, constrain the tool classpaths' `guava` to the platform's version:
      `errorprone(platform(project(":platform")))` (the `annotationProcessor` classpath extends it and is the one the
      scan observes) and a `checkstyle` dependency constraint (`constraints { checkstyle(libs.guava) }` — the checkstyle
      configuration does not accept a platform). Verify with
      `./gradlew :showcase-command-service:dependencies --configuration checkstyle` and
      `--configuration annotationProcessor` (both show `33.7.2-jre`) and that the quality gates still run (task 5.1).

## 4. Sweep the dead ignores

- [x] 4.1 Remove the three `snappy-java@1.1.10.5` ignores from `.snyk` and the "no patched release" comment above them
      (the pin clears those advisories too).

## 5. Verification

- [x] 5.1 Run `./gradlew check` with the integration tests (Docker), covering the runtime-path constraint.
- [x] 5.2 Dispatch the credentialed scan against this branch
      (`gh workflow run dependency-security.yml --ref fix-new-security-advisories`) — ran (run 38195676967) and passed.
- [x] 5.3 Run a `review-quick` pass over the proposal and implementation, repeating until clean.

## 6. Capture

- [x] 6.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: a new Gotchas bullet (a build tool's vulnerable transitive does not reach
      the platform; a tool configuration that rejects a platform takes a constraint; derive the owning configuration
      from the graph) and a clause appended to the Build & Test `.snyk` note (a version-pinned ignore goes dead
      silently, so sweep it) — net +11 lines (12 insertions, 1 deletion). No retirements were proposed.
- [x] 6.2 Ask the owner for the manual review pass, after 5.3 is clean and 6.1 is done.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied and the owner approves.
- Verify the archived result.
