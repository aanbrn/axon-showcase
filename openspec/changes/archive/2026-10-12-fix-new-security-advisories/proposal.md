# Proposal

## Why

The credentialed `dependency-security.yml` dispatch (run 38184554382) surfaced three advisories (on two packages)
published since the 2026-10-10 scheduled run, so the scheduled `Dependency Security` run still fails on `main`:
`org.xerial.snappy:snappy-java@1.1.10.5` (a High and a Medium, fixable — Snyk fixes at `1.1.10.10`) and
`com.google.guava:guava@33.6.0-jre` (a High; fixed at `33.7.2-jre`, which the platform already constrains — the gap is
that the tool configurations do not reach the platform: `checkstyle` carries the reported `33.6.0-jre` and the Error
Prone tool classpath (`annotationProcessor`) carries `33.5.0-jre`). Between them they affect 15 of the scanned projects.

## What Changes

- `showcase/quality/dependency-security`: add `org.xerial.snappy:snappy-java` to the constrained-transitive set and name
  `guava` in it.
- `gradle/libs.versions.toml` + `platform/build.gradle.kts`: add a `snappy-java` coordinate and a platform constraint
  pinning it to the latest fixed release.
- `build-logic`: constrain the tool classpaths' `guava` to the platform's version —
  `errorprone(platform(project(":platform")))`, and a `checkstyle` dependency constraint (the checkstyle configuration
  does not accept a platform).
- `.snyk`: remove the three now-dead `snappy-java@1.1.10.5` ignores and their "no patched release" comment.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/dependency-security`: the constrained-transitive set gains `org.xerial.snappy:snappy-java` and names
  `guava`, and the scan scenario names both.

## Impact

- Files: `openspec/specs/showcase/quality/dependency-security/spec.md` (via the delta), `gradle/libs.versions.toml`,
  `platform/build.gradle.kts`, `build-logic`, `.snyk`.
- `snappy-java` is runtime-path, so the constraint is verified with the integration tests.
- The `guava` finding is on the tool classpaths; the fix keeps the tools' own dependency aligned with the platform.
