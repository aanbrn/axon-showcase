# Design

## Context

See `proposal.md` — Why. The bumps are version-catalog `[versions]` edits: the catalog is the single source and a
convention plugin reads each pin, so no build script changes.

## Goals / Non-Goals

**Goals:**

- Take the applicable release updates from the 2026-10-04 report.

**Non-Goals:**

- The excluded `log4j-core` row and any major version.

## Decisions

### Decision: exclude the spurious `log4j-core` row

The `log4j-core [2.17.1 -> 2.26.1]` row is not a real update — `log4j-core` already resolves to 2.26.1 everywhere
(`log4j = "2.26.1"`), and `2.17.1` is the floor of an external Log4Shell guard that `spotbugs-annotations` publishes
(ADR-0007). Bumping it would contradict the recorded decision, so it stays.

## Risks / Trade-offs

- **A runtime-path library bump can be binary-incompatible while compilation and the Docker-free check pass** → verify
  with the full `check` (integration tests), not the Docker-free variant.
- **`jgroups` and the OpenSearch REST clients are transport/runtime-path libraries** → the gateway/command-service ITs
  and the query-service/projection-service ITs exercise them over Testcontainers.
