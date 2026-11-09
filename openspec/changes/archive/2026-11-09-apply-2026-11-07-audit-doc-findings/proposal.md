# Correct the config-guard tracking bullet

## Why

The 2026-11-07 repository audit's agent-tooling section (`docs/audits/2026-11-07.md`) reports the config-guard tracking
bullet as stale: it still lists the CI probe's `could not parse` arm under "Each guard retires on its own condition",
though the `openspec` pin reached `v1.16.0` and `ci.yml` now holds only the array-shape probe, so that arm is retired
and only the `#1894`-keyed array-shape arm remains live. The audit routes the correction as a small docs/consistency
unit, and `showcase/quality/agent-skills` requires findings of different routes or scopes to be separate units — hence
this unit, split from the spec-corpus fixes in `apply-2026-11-07-audit-findings`.

## What Changes

- `AGENTS.md` — the config-guard tracking bullet marks the `could not parse` arm retired and keeps only the
  `#1894`-keyed `must be an array of strings` arm live, with the applying unit's `captured:` marker.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — no capability spec states the bullet's rule, so the change declares `skip_specs`.

## Impact

- **Documents**: `AGENTS.md` only; no code, build, workflow, or spec change.
- **Behavior**: none — `ci.yml:108–123` already runs `openspec validate --all` before the array-shape grep and carries
  no `could not parse` arm.
- **Split**: the spec-corpus fixes are `apply-2026-11-07-audit-findings`, and the `handlebars` advisory is
  `remove-dead-handlebars-constraint`.
