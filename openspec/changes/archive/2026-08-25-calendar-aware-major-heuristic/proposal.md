# Proposal: Calendar-aware major heuristic for dependency update suppression

## Why

The `majorOf()` heuristic in `dependency-versions-conventions.gradle.kts` treats the leading integer of a version as its
"major". That is correct for semver coordinates, but Spring-ecosystem components (Spring Cloud, Spring Data, Project
Reactor) use calendar versioning (`YYYY.MINOR.MICRO`, e.g. `2025.0.7`) where the *release train* is defined by the
first two segments — `2025.0` and `2025.1` are different trains that target different Spring Boot generations. The
current heuristic reads `2025.0.x -> 2025.1.x` as a same-major (minor) bump, so a calendar-train change would leak
through the major-suppression gate as an actionable "minor" update instead of being suppressed as the coordinated
migration it is.

This is latent today — the only catalog-owned calendar-versioned coordinate is `io.projectreactor:reactor-bom`
(`2025.0.7`), which is not major-disabled — but it will bite when the deferred Spring Boot 4 migration (ADR-0004) moves
Spring-family coordinates onto calendar trains.

## What Changes

- **Make the major heuristic calendar-aware** (`build-logic/.../dependency-versions-conventions.gradle.kts`): when the
  current version is calendar-schemed (leading segment is a 4-digit year), treat a change in the `YYYY.TRAIN` pair
  (first two segments) as a major bump; otherwise keep the existing leading-integer semver logic.
- **Document the behavior** in the `showcase/quality/dependency-management` spec (calendar-aware major comparison).

This is mechanism-hardening only: it makes the suppression gate classify calendar-train changes correctly. No
coordinates are added to or removed from the major-disabled list, and no policy changes. Project Reactor is explicitly
**not** added to major-disabled — Reactor does not target a specific Spring Boot generation the way Spring Cloud/Spring
Data trains do, so there is no deferral rationale for it; the heuristic fix simply ensures its train changes are not
mislabeled as minor.

No breaking changes to the build API; behavior of the report for non-calendar coordinates is unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-management`: the "Major updates can be suppressed per coordinate" requirement is
  extended so the major comparison is calendar-aware (train change = major) for calendar-versioned coordinates.

## Impact

- **Code**: `build-logic/src/main/kotlin/dependency-versions-conventions.gradle.kts` — the `majorOf()`/`blockedMajor`
  logic.
- **Specs**: `openspec/specs/showcase/quality/dependency-management/spec.md` — delta for the modified requirement.
- **Dependencies**: no dependency versions change; only how the `dependencyUpdates` report classifies majors.
- **Systems**: the `dependencyUpdates` Gradle task (report output). The Snyk `dependencySecurityCheck` and runtime are
  unaffected.
