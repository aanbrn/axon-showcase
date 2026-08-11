## Why

The `load-tests` module (Gatling simulation of the showcase REST API with configurable injection profiles and
assertions) exists but has never been captured in an OpenSpec spec. Without a spec, the load-testing contract — what
the simulation exercises, the profiles available, and the pass criteria — is undocumented and changes to it are not
reviewable against a stated contract.

## What Changes

- Author the first OpenSpec capability spec that documents the load tests' current behavior as requirement statements
  (MUST clauses), derived from the existing Gatling simulation and its configuration.
- No behavioral or code changes. This change only introduces the spec delta so it can be reviewed and later archived
  into `openspec/specs/`.

## Capabilities

### New Capabilities

- `showcase/load-tests`: Current behavior of the load-testing setup — the Gatling showcase simulation exercising the
  API gateway, the scenario flow (schedule, start, finish, remove, fetch), the configurable injection profiles (smoke,
  average, soak, stress, spike, breakpoint), and the pass assertions per profile.

### Modified Capabilities

- None.

## Impact

- New spec only: `openspec/changes/capture-load-tests-behavior/specs/showcase/load-tests/spec.md`.
- No production code, APIs, dependencies, or build configuration are affected.
