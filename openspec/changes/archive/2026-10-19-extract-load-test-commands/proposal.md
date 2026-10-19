# Proposal

## Why

The 2026-10-19 agent-tooling audit's first extraction opportunity proposed moving the load-test command/derivation block
out of `AGENTS.md`'s `Build & Test` section into `showcase/quality/load-tests` and the baseline script's docstring,
behind a gate of its own: retain at root until a dedicated change verifies the spec carries every transferred
identifier. Running that gate changed the picture twice. The block is 28 lines and 3,006 bytes, not the ~136 lines ≈
~14KB the report recorded — that range spans the whole `Build & Test` tail. And the spec already carries most of what
the report listed as missing: twelve properties with their defaults and property-provided scenarios, and the SSE
connection count by scenario alone. What is genuinely root-only is the invocation, the `:load-tests:test` warning, and
the pointer; the Gradle `-P` spelling and the `sseConnections` identifier have no home, and the delta adds them. The
owner selected the opportunity knowing the measured size.

## What Changes

- `AGENTS.md`: the load-test block's semantic prose — the threshold derivation, the fallback values, and the
  baseline-replacement rules, which restate `showcase/quality/load-tests` — is replaced by the retained pointer, while
  the invocation, the `:load-tests:test` warning, and the property pointer stay.
- `openspec/specs/showcase/quality/load-tests` (delta): the requirement gains the Gradle `-P` spelling of the property
  set, and the `sseConnections` property's identifier and default.
- `scripts/load-test-baseline.sh`: the docstring gains the tolerance floors, the only run detail neither the spec nor
  the script states.
- `AGENTS.md`: the `baselineTrend` block shrinks to its invocation and a pointer.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/load-tests`: the delta adds the command surface the block carried — the `-P` spelling of the
  property set and the `sseConnections` identifier and default — to the requirement that already owns the property set
  and the baseline mechanics.

## Impact

- Files: `AGENTS.md`, `scripts/load-test-baseline.sh`, and `openspec/specs/showcase/quality/load-tests/spec.md` (at
  archive).
- No code, build, or deployment impact: guidance prose, a script docstring, and a spec delta.
- The second extraction opportunity (the CI narrative) is a separate unit and is not part of this one.
