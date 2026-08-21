# Design: Scope dependency update reporting to catalog-owned versions with major blocking

## Context

See proposal.md — Why. The `dependencyUpdates` task (ben-manes `io.github.ben-manes.versions` 0.61.0) is configured in
`build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` with an `isNonStable` rejection rule. Two
noise sources dominate the report: BOM-inherited modules the project does not version (spring-tx, starter-*, etc.), and
major jumps for libraries pinned to an immovable major (Axon 4.x, Spring Boot 3.x).

An empirical experiment (scratch project against a fake Maven repo with versions `1.0.0`/`1.0.1`/`1.1.0`/`2.0.0`)
proved the plugin's `rejectVersionIf` participates in component selection: with a major-cap rule, the report falls back
to the highest non-rejected version (`[1.0.0 -> 1.1.0]`) instead of showing the rejected latest (`2.0.0`). This is the
mechanism the design relies on.

## Goals / Non-Goals

**Goals:**
- Report updates only for coordinates the project versions explicitly (exact `version.ref` in `libs.versions.toml`).
- Suppress major-jump updates only for an explicit, opt-in list of coordinates.
- Keep minor and patch updates visible for listed coordinates.

**Non-Goals:**
- A global "no major updates" default — majors stay reported for every catalog-owned coordinate not explicitly listed.
- Changing how `libs.versions.toml` declares versions.

## Decisions

**D1 — Implement both rules via `rejectVersionIf`, not `resolutionStrategy`/component selection.**
The existing convention file already uses `rejectVersionIf { isNonStable(...) }`. Adding predicates there is the
smallest change and keeps one mechanism for all version rejection. The scratch experiment confirmed `rejectVersionIf`
performs the same-major fallback, so no separate `resolutionStrategy` wiring is needed.

**D2 — The ownership filter rejects candidates whose coordinate has no exact version in the catalog.**
The convention plugin reads `gradle/libs.versions.toml`, collects the coordinates of `[libraries]` entries that carry an
exact `version.ref`, and rejects any candidate whose `group:module` is not in that set. This keeps BOM-inherited
modules out of the report entirely. The catalog is the single source of truth for "what we version."
- Alternatives rejected: an explicit owned-coordinate list file — duplicates the catalog and rots; using the generated
  `libs` accessors at configuration time — the plugin would need the toml parse anyway to know which entries have
  `version.ref` vs. a BOM-provided version.

**D3 — The major-block rule compares the candidate's major against the current version's major.**
`majorOf(candidate) > majorOf(currentVersion)` ⇒ reject, but only for coordinates in the config list. `majorOf` parses
the leading numeric segment of a version, tolerating qualifiers such as `-jre`, `.Final`, `.RELEASE`, and calendar-style
versions (`2025.0.13` → major `2025`). Non-numeric versions are treated as "no major change" (not rejected).

**D4 — The opt-in major-blocking list lives in `config/dependency-updates/major-disabled.properties`.**
A flat properties file, mirroring the existing `config/jacoco/coverage-baseline.properties` precedent. Each property
name is a coordinate (`group:module`) or group prefix; the value is ignored (empty). The convention plugin loads it
from `rootProject.layout.projectDirectory`. An empty or missing file yields an empty list.
- Alternatives rejected: (a) a Kotlin `setOf(...)` hardcoded in the plugin — editing a build-logic file for every change
  is heavier than editing config; (b) deriving the list from `libs.versions.toml` — that catalog is the ownership map,
  not a major-blocking policy, and conflating the two would surprise maintainers.

**D5 — Matching supports exact coordinates and group prefixes, dot-boundary aware.**
An entry `org.axonframework:axon-bom` matches only that module. An entry without a colon is a group prefix: it matches
the group exactly or as a dot-boundary prefix (e.g. `org.axonframework` matches `org.axonframework` and
`org.axonframework.extensions.kafka`; `org.springframework` matches `org.springframework`,
`org.springframework.boot`, `org.springframework.data`, and `org.springframework.security`). Dot-boundary awareness
prevents `org.axonframework` from matching a hypothetical `org.axonframeworkfoo` group. The config ships with the
initial entries `org.axonframework` and `org.springframework`, which cover the Axon train and the Spring Boot 4
migration blockers respectively.

## Risks / Trade-offs

- [Group-prefix matching could over-block a module that is fine to major-update] → Use exact `group:module` entries for
  such cases; the config file documents the distinction.
- [Version parsing edge cases (`-jre`, `.Final`, calendar majors)] → `majorOf` normalizes the leading numeric segment;
  any unparseable version is not rejected, so a parse bug cannot hide updates, only fail to block one.
- [A listed coordinate whose current major changes when the project finally migrates] → The rule is relative to
  `currentVersion`, so once the project adopts the new major, updates at that major are reported again automatically;
  the entry can then be removed.
- [A catalog coordinate without an exact `version.ref` (BOM-provided) is dropped even though the project names it in the
  catalog] → That is the intended semantics: naming a library in the catalog without pinning its version means the BOM
  decides, so the project cannot act on an update independently.