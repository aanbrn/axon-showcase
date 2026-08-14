## Why

The nine `showcase/*` specs live flat under one namespace, mixing pipeline stages (gateway, write side, read side) with
infrastructure libraries (extensions), deployment (helm-chart), and quality (load-tests). For a reference app whose
identity *is* CQRS/Event Sourcing, the spec tree should teach the architecture: a reader should see the command flow and
the read side at a glance instead of scanning nine alphabetically flat files.

## What Changes

- Reorganize `openspec/specs/showcase/` into six groups by architectural role, moving the existing `spec.md` files into
  new directories:
    - `gateway/` → `api-gateway/spec.md`
    - `write-side/` → `command-service/spec.md`
    - `read-side/` → `projection-service/spec.md`, `query-service/spec.md`
    - `platform/` → `identifier-extension/spec.md`, `mapstruct-extension/spec.md`, `resilience4j-extension/spec.md`
    - `deployment/` → `helm-chart/spec.md`
    - `quality/` → `load-tests/spec.md`
- No requirement, scenario, or behavior text changes inside any spec — pure relocation.

## Capabilities

### New Capabilities

None — this change moves existing specs; it introduces no new capability behavior.

### Modified Capabilities

None — no spec-level behavior changes; reorganization only. (`skip_specs: true` is set accordingly.)

## Impact

- **Code touched**: none outside `openspec/specs/`.
- **Specs**: all nine capability paths change (e.g., `showcase/api-gateway` → `showcase/gateway/api-gateway`). Future
  proposal "Modified Capabilities" sections and delta specs must reference the new paths.
- **Systems**: none — no build, test, or deployment impact; spec validation (`openspec validate --specs`) still passes
  after the moves.
- **Archived changes**: historical snapshots under `openspec/changes/archive/` keep their old paths unchanged.
