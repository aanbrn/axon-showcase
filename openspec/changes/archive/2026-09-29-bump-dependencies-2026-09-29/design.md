# Design

## Context

See `proposal.md` — Why. The JVM bumps are version-catalog `[versions]` edits (the catalog is the single source; a
convention plugin reads the pins). The npm bump needed a clean reinstall
(`rm -rf node_modules package-lock.json && npm install`) because `typescript-eslint`'s umbrella package pins its sibling
`@typescript-eslint/*` versions, so npm could not move them in place — the lockfile diff is therefore broader than the
direct bumps, and it re-adds three `extraneous` entries (ajv, fast-uri, json-schema-traverse) that `npm prune` removes.
The `spotless-plugin` 8.10.3 bump reflows 13 Java test sources (its bundled `palantir-java-format` changed); the reflow
is whitespace-only and `spotlessApply` applies it.

## Goals / Non-Goals

**Goals:**

- Take the applicable in-range/release updates from the 2026-09-28 reports, on both the JVM and npm surfaces.

**Non-Goals:**

- The excluded coordinates (see the proposal) and any major version.

## Decisions

### Decision: exclude the three recorded non-updates

`log4j-core`, `opensearch-java`, and `typescript` 7 each have a recorded reason not to bump (ADR-0007, the parked
`opensearch-java` hold, and the `typescript-eslint` cap). Taking them would contradict a decision, so they stay.

### Decision: a clean npm reinstall for the `@typescript-eslint` family

The umbrella `typescript-eslint` pins its sibling packages to the same version, so an in-place `npm install` fails
`ERESOLVE` when the siblings move ahead of the pinned umbrella; a clean reinstall resolves the whole family together.
The lockfile diff is described by what moved, not as "patches/minors".

## Risks / Trade-offs

- **A runtime-path library bump can be binary-incompatible while compilation and the Docker-free check pass** → verify
  with the full `check` (integration tests), not the Docker-free variant.
- **Lockfile churn from the clean reinstall** → the direct bumps are what is intended; the diff is reviewed and
  described by what moved.
