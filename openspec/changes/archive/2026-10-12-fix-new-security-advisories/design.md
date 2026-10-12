# Design

## Context

See `proposal.md` — Why. `snappy-java` is reached at runtime via `axon-kafka` → `cloudevents-kafka` →
`kafka-clients@3.9.2` → `snappy-java@1.1.10.5`; the platform constrains the runtime transitives (zstd-jni, netty, …) but
not `snappy-java`. `guava` is constrained by the platform to `33.7.2-jre` and the compile classpath resolves it, but the
tool classpaths do not reach the platform: `checkstyle:13.11.0` pulls `guava:33.6.0-jre` (the version Snyk reports) and
`error_prone_core:2.50.0` pulls `33.5.0-jre`. Snyk fixes `snappy-java` at `1.1.10.10` and `guava` at `33.7.2-jre`.
`.snyk` suppresses three older `snappy-java` advisories with the rationale "no patched release (vulnerable through
1.1.10.8)".

## Goals / Non-Goals

**Goals:**

- The scheduled `Dependency Security` scan passes.
- The runtime constraint is verified.

**Non-Goals:**

- Suppressing the advisories: all have fixes.
- A Spring/Boot migration: unrelated.

## Decisions

**Constrain `snappy-java` in the platform BOM.** A fix exists (the minimum is `1.1.10.10`), so it is constrained to the
latest fixed release rather than suppressed; the platform is the repository's single BOM, so a per-module constraint
would fragment it. _Alternatives rejected:_ a per-module constraint; a suppression (a fix exists).

**Constrain the tool classpaths' `guava` to the platform's version, rather than suppress the finding.** The platform
already pins `guava` at `33.7.2-jre`; the tool classpaths simply do not import it. The Error Prone classpath
(`annotationProcessor`, which extends `errorprone` — the edit target, carrying the reported `33.5.0-jre`) reaches the
platform via `errorprone(platform(project(":platform")))`, and the `checkstyle` classpath (carrying the reported
`33.6.0-jre`) is corrected by a dependency constraint (`constraints { checkstyle(libs.guava) }`), because that
configuration does not accept a platform project. _Alternative rejected:_ a `.snyk` ignore (a fix exists).

**Remove the three dead `snappy-java` ignores.** Pinning `snappy-java` to `1.1.10.12` clears the three older advisories
too, so their ignores and the "no patched release" comment become dead and false.

## Risks / Trade-offs

- The `snappy-java` constraint is runtime-path → verified with the integration tests (Docker), not just a compile.
- Constraining the tool classpaths changes them → the quality gates (`./gradlew check`) confirm `checkstyle` and
  `errorprone` still run; the `checkstyle` configuration rejects a platform project (an attempt with
  `checkstyle(platform(...))` dropped the checkstyle tool and failed `checkstyleMain`), so its `guava` is constrained
  rather than imported.

## Migration Plan

None: a constraint and a suppression sweep, taking effect on the next build and scan.
