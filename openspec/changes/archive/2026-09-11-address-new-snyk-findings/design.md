## Context

See proposal.md — Why. `com.github.luben:zstd-jni:1.5.6-4` arrives transitively via
`org.apache.kafka:kafka-clients:3.9.2` (managed by the Spring Boot 3.5.16 BOM, consumed through `:platform`), on the
resolved classpaths of every Kafka-consuming module. `com.tdunning:t-digest:3.3` arrives via
`io.gatling:gatling-charts:3.15.1` in `load-tests` only. The platform BOM already constrains several transitives
(Jackson 3 via its BOM, `httpclient5`, `lz4-java`, ...), and the root `.snyk` policy already suppresses unfixable
findings with version-pinned, short-expiry ignores.

## Goals / Non-Goals

**Goals:**

- Clear the two new Snyk findings so `dependencySecurityCheck` reports a clean scan.
- Stay within the established mechanisms: platform-BOM constraints for fixable transitives, `.snyk` policy ignores for
  the unfixable one.

**Non-Goals:**

- Upgrading `kafka-clients` or the Spring Boot BOM (a larger, BOM-coordinated change).
- Changing the scan's scope (`--all-sub-projects`) or excluding `load-tests` from it.
- Removing the (unmaintained) `t-digest` from Gatling's chart generation.

## Decisions

**D1: Constrain `zstd-jni` at the `platform` BOM, not via a root substitution or a `kafka-clients` bump.** A plain
version constraint raises the transitive to the constrained version when it is higher than the requested one, which is
exactly this case (`1.5.7-16` > `1.5.6-4`). This reuses the mechanism already proven for `httpclient5` and Jackson 3,
unlike the root `dependencySubstitution` (needed only for the `lz4-java` group/artifact relocation). Upgrading
`kafka-clients` would be a BOM-coordinated change with broader blast radius. _Alternatives considered:_ root
`eachDependency` force (unnecessary for a same-coordinate version bump); bumping `kafka-clients` (rejected, larger
surface).

**D2: Pin `zstd-jni` to the latest patch-line release (`1.5.7-16`), not Snyk's minimal fix (`1.5.7-14`).** Both clear
the findings (`1.5.7-16` ≥ the fix); the repo's convention is to pin the latest patched version, and the catalog entry
then becomes a `dependencyUpdates`-reported coordinate. _Alternative considered:_ the minimal fix `1.5.7-14` (rejected —
immediately stale).

**D3: Suppress `t-digest` in `.snyk`; do not exclude it from the classpath or drop `load-tests` from the scan.**
`t-digest:3.3` has no patched release (`fixedIn: []`), so no constraint can clear it. Excluding it from `gatling-charts`
risks breaking Gatling's chart generation; dropping `load-tests` from `snyk test --all-sub-projects` changes the scan's
spec'd scope and hides the rest of that module. A version-pinned ignore (`* > com.tdunning:t-digest@3.3`) with a
short-term expiry matches the existing policy convention and lets the finding re-surface if a fix ships. _Alternatives
considered:_ classpath exclusion (rejected, may break chart generation); scan-scope narrowing (rejected, spec'd as all
sub-projects).

## Risks / Trade-offs

- [Bumping `zstd-jni` across the `1.5.6` → `1.5.7` patch line could change Kafka zstd-compression behavior] → the
  compression code comes from `kafka-clients`, which uses `zstd-jni`'s stable API; integration tests exercise Kafka
  serialization end-to-end.
- [Suppressing `t-digest` could hide a future fix] → the ignore is pinned to `t-digest@3.3` and carries a short-term
  expiry, so a different version (or the expiry passing) re-surfaces it.
- [`t-digest` is high severity] → it is a `load-tests`-only, non-deployed transitive used only for Gatling chart
  percentiles; the ignore records that rationale.
