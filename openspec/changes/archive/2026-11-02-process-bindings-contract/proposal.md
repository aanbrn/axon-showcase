## Why

ADR-0025 §4 deferred the path and heading abstraction. The two reuse pilots measured the bindings — the review ledger,
the lifecycle and review-gate headings, the `Workspace:` record, the change dir, and the prior finding instances — and
the slices still hardcode them, so a target must edit each definition. The first consolidation moved the governance
contracts into `docs/contracts/` and the auditor now covers them; the bindings are the next consolidation.

## What Changes

- `docs/contracts/process-bindings.md` (new) — the target-owned declaration: six canonical roles, this repository's
  values, the owner of each referenced contract, and the missing-binding behavior.
- The pause/resume slice (both commands and the skill) and the `lesson-capture` agent adopt the declared bindings; the
  hardcoded paths and headings become the declared roles.
- `docs/adr/0026-process-bindings-are-declared.md` (new) — the decision: declared bindings, not substitution.
- `openspec/specs/showcase/quality/agent-skills/spec.md` gains an `ADDED` requirement for the binding use and the
  incompatibility reporting, with valid, renamed, missing, and non-adopter scenarios.
- The consumer trace and the renaming counterfactual are recorded.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — a new requirement for the declared process bindings.

## Impact

- Docs: the declaration, the ADR, the pause/resume slice, the `lesson-capture` agent, the spec.
- No runtime code, build, API, schema, image, chart, or deployment change.
