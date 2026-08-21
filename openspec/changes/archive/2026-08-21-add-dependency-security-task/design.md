# Design: Add local dependency security scan task

## Context

See proposal.md — Why. The build has no formal entry point for the dependency security scan; today a developer must
remember the exact `snyk test --all-sub-projects` invocation. The repo centralizes cross-cutting build behavior in
`build-logic/src/main/kotlin/` convention plugins, and a `showcase/quality/dependency-security` capability spec now
documents the security posture.

## Goals / Non-Goals

**Goals:**
- A single `./gradlew dependencySecurityCheck` task that runs the exact scan the repo already uses and reports its
  outcome, failing on vulnerable paths.
- Reuse the Snyk CLI and honor the existing `load-tests/.snyk` policy.

**Non-Goals:**
- CI enforcement, scheduled monitoring, or Dependabot automation — these are follow-on concerns (CI is a separate
  change; the repo has no workflows yet).
- Replacing the scan tool or adding a new account/plugin dependency.
- Wrapping the scan into the `check` lifecycle.

## Decisions

**D1 — Implement as a convention plugin applied to the root project, not a task registered in `build.gradle.kts`.**
Add `build-logic/src/main/kotlin/dependency-security-conventions.gradle.kts` and apply it to the root project. This
matches how every other cross-cutting concern in the repo is structured (`code-check-conventions`,
`code-coverage-conventions`, etc.) and keeps `build.gradle.kts` free of task wiring.
- Alternatives rejected: registering the task inline in `build.gradle.kts` — simpler but breaks the repo's
  convention-plugin pattern and leaves the logic less discoverable/reusable (e.g. for CI later).

**D2 — Use an `Exec` task invoking the Snyk CLI, not the official Snyk Gradle plugin.**
Register `dependencySecurityCheck` as an `Exec` task running `snyk test --all-sub-projects` from the project root.
- Alternatives rejected: the `io.snyk.gradle.plugin.snykgradleplugin` adds per-module `snyk-test` tasks and requires a
  `SNYK_TOKEN`; the CLI command we already run works without auth for open-source scans and is a single, well-understood
  root-level scan that honors `load-tests/.snyk`.

**D3 — Guard the missing-CLI case with a clear failure message.**
Before executing, the task checks that the `snyk` binary is resolvable and fails with an explicit "Snyk CLI is required"
message when it is absent, instead of relying on the raw `Exec` "Cannot find program" error.

**D4 — Do not wire the task into `check`.**
The scan is network-dependent and slow (~40s for all sub-projects); running it on every build would slow and destabilize
the normal workflow. It stays an explicit, on-demand task.

## Risks / Trade-offs

- [Snyk CLI absent on a developer machine] → The task fails with a clear message (D3); the requirement documents that
  the CLI must be installed.
- [Network flakiness during a scan] → The task is on-demand and not part of `check`, so a transient network failure
  does not affect normal builds; the developer simply retries.
- [Scan drift from `.snyk` policy or Snyk DB changes] → Snyk reads the existing `load-tests/.snyk` policy automatically;
  policy changes are reviewed in the normal code-review flow.
- [Task result is only as current as its last run] → Acceptable for a local check; CI enforcement is the follow-on that
  makes the result merge-blocking.