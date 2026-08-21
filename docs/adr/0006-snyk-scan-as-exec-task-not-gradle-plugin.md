# ADR-0006: Snyk dependency scan as an Exec task rather than the Snyk Gradle plugin

Date: 2026-08-21

Status: Accepted

## Context

The build must not ship known-vulnerable transitive dependencies, so the repository runs a Snyk dependency scan
(`snyk test --all-sub-projects`) from the shell. There is no formal Gradle entry point for it: the exact invocation
lives only in developer memory and an opencode command, which makes it easy to skip or run with inconsistent flags.
Exposing the scan in the build offers two options — invoke the Snyk CLI directly, or adopt the official Snyk Gradle
plugin.

## Decision

Expose the scan as a root-level `dependencySecurityCheck` `Exec` task, defined in the `dependency-security-conventions`
convention plugin applied to the root project. The task runs `snyk test --all-sub-projects` from the project root, fails
with a clear "Snyk CLI is required" message when the `snyk` binary is absent (instead of Gradle's raw "Cannot find
program" error), and is intentionally NOT part of `check` — the scan is network-dependent and slow, so running it on
every build would slow and destabilize the normal workflow.

The official `io.snyk.gradle.plugin.snykgradleplugin` was rejected: it registers per-module `snyk-test` tasks and
requires a `SNYK_TOKEN`, whereas the CLI command the repository already runs works without authentication for
open-source scans and is a single, well-understood root-level scan that honors the existing `load-tests/.snyk` policy.

## Consequences

- One Gradle command, `./gradlew dependencySecurityCheck`, replaces the ad-hoc shell invocation as the entry point for
  the local dependency security check.
- No Snyk token or account is needed for local scans; the existing Snyk CLI and `load-tests/.snyk` policy are reused.
- The scan stays out of `check`, so a slow or flaky network scan never blocks normal builds; a developer runs it
  explicitly before merging.
- CI enforcement and scheduled monitoring remain follow-on concerns. Because the task is a plain root-level task, wiring
  it into a future pipeline is straightforward.