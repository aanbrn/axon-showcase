## Why

The scheduled Snyk scan (`snyk.yml`, run 2026-09-21) failed with five new advisories across three transitive
coordinates. One is fixable by a patch-level bump; four have no patched release and need a scoped, time-boxed
suppression. The scan is observational, but a red scheduled run is a real signal that the dependency surface moved.

## What Changes

- Bump the `netty-bom` catalog pin from `4.2.17.Final` to `4.2.18.Final`, resolving `SNYK-JAVA-IONETTY-19778369` (HTTP
  Request Smuggling, Medium) across all five flagged paths — a real fix.
- Suppress the four advisories with no patched release, each pinned to its exact assessed vulnerable version and
  carrying a rolling expiry so the scheduled scan re-surfaces it:
  - `org.xerial.snappy:snappy-java@1.1.10.5` — three advisories (two High), reached on the **production** runtime
    classpath via Axon Kafka → `kafka-clients`.
  - `com.tdunning:t-digest@3.3` — one advisory (High), load-tests only.
- Extend `dependency-security`'s constraint requirement to name `io.netty` among the constrained transitives.

## Capabilities

### New Capabilities

None — this refines behavior an existing requirement already covers.

### Modified Capabilities

- `showcase/quality/dependency-security`: the constraint requirement gains `io.netty` as a constrained transitive
  resolving to at least `4.2.18.Final`, with a scenario asserting the resolution.
