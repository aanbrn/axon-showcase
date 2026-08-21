# Proposal: Add local dependency security scan task

## Why

The dependency security check for the build (`snyk test --all-sub-projects`) is currently run manually from the shell,
with no task in the build to formalize it. This makes it easy to skip or run with inconsistent flags, and there is no
one-command entry point a developer can use to verify the dependency posture before merging.

## What Changes

- `build-logic`: add a `dependency-security-conventions.gradle.kts` convention plugin applied to the root project that
  declares a `dependencySecurityCheck` task running `snyk test --all-sub-projects`.
- The task SHALL be a standalone `Exec`-style task and MUST NOT be wired into `check` (running a network-dependent
  scan on every build would slow and destabilize the normal build).
- The scan reuses the existing Snyk CLI and honors the `load-tests/.snyk` policy file already in the repo.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-security`: adds a requirement that the build provides a local `dependencySecurityCheck`
  task that scans all sub-projects for known vulnerable dependencies and reports the result to the developer.

## Impact

- **Code**: `build-logic/src/main/kotlin/dependency-security-conventions.gradle.kts` (new), `build.gradle.kts` (apply the
  plugin to the root project).
- **Build**: a new `./gradlew dependencySecurityCheck` task; `check` and other existing tasks are unaffected.
- **Tooling**: requires the Snyk CLI on `PATH`; the task fails with a clear message when Snyk is not installed.
- **Tests**: no unit/integration test changes; verification runs the new task and confirms the scan passes with the
  current dependency state.