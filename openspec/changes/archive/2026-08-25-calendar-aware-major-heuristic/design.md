# Design: Calendar-aware major heuristic

## Context

See proposal.md — Why. The build's `dependencyUpdates` report suppresses major-version updates for coordinates listed in
`config/dependency-updates/major-disabled.properties`, via the `blockedMajor` guard in
`build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts`. That guard currently decides "is this a major
bump?" by comparing the leading integer of candidate vs current version (`majorOf()`). This is correct for semver
coordinates but wrong for Spring-ecosystem calendar versioning (`YYYY.MINOR.MICRO`), where the release train is the
first two segments.

The current state: `majorOf(version) = version.takeWhile { it.isDigit() }.toIntOrNull()`, used only in `blockedMajor`
(line 63). Only one catalog-owned coordinate is calendar-schemed today: `io.projectreactor:reactor-bom` (`2025.0.7`),
and `io.projectreactor` is not yet in the major-disabled list.

## Goals / Non-Goals

**Goals:**
- Treat a calendar-train change (`YYYY.TRAIN` pair change) as a major bump for suppression purposes, matching how Spring
  defines a release-train generation.
- Keep the existing semver leading-integer behavior for all non-calendar coordinates.

**Non-Goals:**
- Reclassifying *reporting* (minor vs major) for non-disabled coordinates — only the suppression gate's major
  classification changes.
- Adding any coordinate to, or removing any coordinate from, the major-disabled list. In particular, Project Reactor is
  **not** added: Reactor does not target a specific Spring Boot generation, so there is no deferral rationale for it.
  The fix only ensures its train changes are not mislabeled as minor.
- Adding a Spring (`org.springframework`) requirement or fixing the existing spec gap for `org.springframework`
  (out of scope; not required by this change).
- Making the scheme explicit/per-coordinate in config (B2b) — rejected in favor of inference (see Decisions).

## Decisions

### D1: Infer calendar scheme from a 4-digit leading year

Detect a calendar-versioned coordinate by whether the current version's leading integer is a 4-digit number (a year,
e.g. `2025`). If so, parse both versions as `YYYY.TRAIN` and treat a change in that pair as a major bump. Otherwise,
fall back to the existing leading-integer semver comparison.

**Rationale:** Spring's calendar scheme is always `YYYY.*`; a 4-digit leading segment is a reliable discriminator. No
catalog-owned semver coordinate has a 4-digit leading integer (verified: only `reactor-bom 2025.0.7` does), so the
inference is safe today and stays safe for the Spring-family coordinates that will become calendar-versioned.

**Alternatives considered:**
- *B2b, per-coordinate scheme declaration in config:* more explicit and robust to future exotic schemes, but adds config
  surface and a new file/section for no current benefit. Rejected — the 4-digit inference is unambiguous for the whole
  Spring ecosystem.
- *B1, treat any leading-integer change as major when calendar (year-only):* too coarse — `2025.0 -> 2025.1` (same year,
  new train) would be read as same-major, which is exactly the bug. Rejected.

### D2: "Major" for a calendar coordinate = change in the `YYYY.TRAIN` pair

Compare the first two dot-separated segments (`2025.0` vs `2025.1`). A change in either the year or the train ordinal is
a new train and therefore a major bump; only a change in the third segment (service release / micro) within the same
train is a non-major update.

**Rationale:** This matches Spring's own definition — "A Release Train generation is defined by the first two parts of
the version" — and the compatibility reality that `2025.0` targets Spring Boot 3.5 while `2025.1` targets Spring Boot
4.0/4.1. Treating them as the same major would leak a Boot-generation change past the suppression gate.

**Boundary behavior:** cross-year changes (`2025.x -> 2026.x`) also change the pair and are treated as major, which is
correct (a new year implies a new train).

### D3: No coordinates are added to the major-disabled list

This change is mechanism-hardening only: it corrects how the gate classifies calendar-train changes, without altering
which coordinates are suppressed. In particular, Project Reactor is intentionally **not** added to
`major-disabled.properties`.

**Rationale:** Reactor is a lower-level reactive library that does not depend on — or target — a specific Spring Boot
generation the way Spring Cloud/Spring Data release trains do. The "deferred Spring Boot 4 migration" justification that
backs the existing `spring`/`springdoc`/`spring-data-opensearch` entries therefore does not apply to Reactor. There is no
deferral policy reason to suppress its majors; the value of the fix is purely that a future Reactor train change is
reported as a major (a significant bump) rather than silently labeled a minor. Adding it to the disabled list without a
real policy rationale would be unjustified.

## Risks / Trade-offs

- **[Future calendar scheme not starting with a 4-digit year]** → Unlikely (Spring calver is always `YYYY.*`); if it
  happens, the per-coordinate declaration (B2b) remains available as a fallback.
- **[A semver coordinate with a 4-digit leading int appears]** → Would be misclassified as calendar; none exists today
  and the leading-4-digit pattern is rare outside calver. If it arises, the fallback to B2b resolves it.
- **[Calendar-train majors for reactor remain reported]** → Deliberate (D3): Reactor has no deferral rationale, so its
  majors staying visible is the correct behavior. The fix ensures they are labeled as majors, not minors.
- **[Spec currently lacks an `org.springframework` requirement]** → Pre-existing gap, not introduced here; noted as
  out-of-scope (Non-Goals) rather than silently fixed.

## Migration Plan

Single commit. No deployment or rollback concern: the change alters only how the `dependencyUpdates` report classifies
major updates. Rollback is reverting the build-logic edit. Verify by running `./gradlew dependencyUpdates` and
confirming calendar coordinates classify train changes as majors while semver behavior is unchanged.

## Open Questions

None. The design decisions fully determine the approach and task breakdown.
