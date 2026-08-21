## Context

See proposal.md - Why. The project's Helm CLI is 4.x (pinned `helm = "4.2.3"` in
`gradle/libs.versions.toml`, downloaded by `helm-conventions.gradle.kts`; installed locally as
`v4.2.4`), but `README.md` and `AGENTS.md` still list "Helm 3.x" as a prerequisite. The "3.x" figure
is actually the Gradle helm plugin version (`helm-plugin = "3.1.2"`), a separate build-time concern.

## Goals / Non-Goals

**Goals:**
- Correct the Helm CLI prerequisite from "Helm 3.x" to "Helm 4.x" in the two docs.
- Bump the pinned Helm CLI version from `4.2.3` to `4.2.4` to align with the installed CLI.

**Non-Goals:**
- Not changing the Gradle helm plugin version (`helm-plugin = "3.1.2"` stays).
- Not changing any Helm chart or deployment behavior.

## Decisions

- **Bump `helm` to `4.2.4` in `gradle/libs.versions.toml`.**
  - Matches the locally installed CLI (`v4.2.4`), so the build's downloaded client and local
    `helm` agree.
  - Alternative: leave at `4.2.3`. Rejected — the proposal's Why explicitly calls out the lag, and
    the version bump is the requested scope.

- **Update the two doc references to "Helm 4.x".**
  - The prerequisite describes the CLI, so it must name the CLI major version (4.x), not the plugin
    version.
  - Alternative: phrase it as "Helm 4.x (the Gradle helm plugin is 3.1.2)". Rejected — adds noise;
    the plugin version is already captured in `gradle/libs.versions.toml`.

## Risks / Trade-offs

- [Bumping the downloaded CLI from 4.2.3 to 4.2.4 could surface a behavior difference in Helm
  tooling] → Mitigation: minor patch bump within the same major version; `helmInstallToLocal` and
  chart lint behave identically.

## Migration Plan

1. Bump `helm = "4.2.3"` → `"4.2.4"` in `gradle/libs.versions.toml`.
2. Update `README.md` and `AGENTS.md` "Helm 3.x" → "Helm 4.x".
3. Refresh the build model and run `./gradlew :helm:chart:helmLintMainChart` to confirm the bumped
   CLI lints the chart cleanly.

## Open Questions

- None.
