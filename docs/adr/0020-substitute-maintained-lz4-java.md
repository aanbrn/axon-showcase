# ADR-0020: Substitute the maintained `lz4-java` fork for the unmaintained `org.lz4` coordinate

Date: 2025-12-08

Status: Accepted

## Context

LZ4 compression enters the stack transitively: `org.apache.kafka:kafka-clients`, the event-propagation client, pulls
`lz4-java` for its compression codecs. When this decision was made that dependency named the unmaintained
`org.lz4:lz4-java`; a later `kafka-clients` names the fork directly, so the substitution now fires for no current
consumer — it is a protective rule for the next one that names the original coordinate. That original line's last real
release is `1.8.0`, and it is affected by CVE-2025-12183 (GHSA-vqf4-7m7x-wgfc, an out-of-bounds memory access). A
community-maintained continuation exists at `at.yawk.lz4:lz4-java` (`github.com/yawkat/lz4-java`, Apache-2.0), whose
README states it "is the community-maintained continuation of lz4/lz4-java, which is no longer maintained", "was
originally created to fix CVE-2025-12183", and "is a drop-in replacement: the Java package names (`net.jpountz.*`) and
API are unchanged, only the Maven coordinates are different".

The arrangement this repository uses was recorded nowhere: the build's
`.because("Force relocation of LZ4 implementation")` restates what the substitution does, not why it exists. The
2026-10-11 architecture audit surfaced it. Recorded retrospectively on 2026-10-12; the substitution shipped in
`e28440c8` (2025-12-08).

## Decision

Force-relocate every consumer of `org.lz4:lz4-java` to the maintained fork, `at.yawk.lz4:lz4-java`, through the root
build's resolution strategy — a `dependencySubstitution` plus an `eachDependency` rule, covering both a module request
and a transitive one. Both artifacts contain the same classes, so exactly one may end up on the classpath.

The fork's own guidance is this mechanism ("exclude it (or use Gradle's dependency substitution) and add this artifact
instead"), and the substitution is preferred over per-consumer `exclude` blocks because it is declared once for the
whole build: a module that pulls the old coordinate transitively is redirected without repeating an exclude.

_Alternatives considered and rejected:_ keeping `org.lz4:lz4-java` (the unmaintained, CVE-affected line); per-module
`exclude` blocks plus an explicit fork dependency (every new consumer must repeat them, and the vulnerable coordinate
returns wherever one is forgotten); and depending on the fork only at the point of use (the original coordinate still
resolves for every consumer that names it transitively).

## Consequences

- The vulnerable coordinate never resolves, so the dependency-security scan reports the fork and the root `.snyk` policy
  needs no `lz4` ignore.
- The redirect is repo-wide and lives in the root build, so it is invisible at a module's dependency declaration:
  reading a module's `build.gradle.kts` shows neither coordinate, and the fork is what lands on the classpath.
- A future transitive consumer of the original coordinate is covered without a code change, and a future fork release is
  a single catalog bump (`lz4-java`) that nothing else follows.
- The substitution is a supply-chain swap to a third party (`at.yawk.lz4`): it is deliberate and now recorded, and the
  unchanged package and API (`net.jpountz.*`) are what keep it a drop-in rather than a code change.
