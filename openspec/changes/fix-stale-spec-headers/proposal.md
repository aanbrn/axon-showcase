# Proposal: Fix stale spec headers left by the role-group restructure

## Why

The 2026-08-14 role-group restructure `git mv`'d most spec files into `showcase/<role>/<capability>/` but never updated
their internal `#` titles. `openspec validate` never checks the title against the capability path, so the drift passed
CI silently (a lesson now captured in AGENTS.md after the `rest-api` rename fixed one such leftover). Eight specs carry
headers at the old `showcase/<capability>` path, omitting the role segment:

- `showcase/deployment/helm-chart/spec.md` — header says `# showcase/helm-chart`
- `showcase/extensions/identifier-extension/spec.md` — `# showcase/identifier-extension`
- `showcase/extensions/mapstruct-extension/spec.md` — `# showcase/mapstruct-extension`
- `showcase/extensions/resilience4j-extension/spec.md` — `# showcase/resilience4j-extension`
- `showcase/quality/load-tests/spec.md` — `# showcase/load-tests`
- `showcase/read-side/projection-service/spec.md` — `# showcase/projection-service`
- `showcase/read-side/query-service/spec.md` — `# showcase/query-service`
- `showcase/write-side/command-service/spec.md` — `# showcase/command-service`

## What Changes

- Update each of the eight specs' first line to match its capability path, e.g.
  `# showcase/deployment/helm-chart Specification`. The title then reflects the path the reader is looking at.
- No requirement, scenario, or behavior text changes — pure title fixes, declared via `skip_specs: true`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- The eight renamed capabilities keep their paths; only their internal `#` titles are corrected to match. This is a
  documentation fix, not a behavior change.

## Impact

- **Specs**: eight `spec.md` files get their `#` header corrected to `# showcase/<role>/<capability> Specification`.
- **Docs**: no `docs/ideas.md` or `AGENTS.md`/`README.md` change.
- **Behavior**: none — title-only edits.
