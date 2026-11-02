## Why

ADR-0026 recorded the bindings as declared, and `process-bindings-contract` adopted them in four slices. The remaining
process components still name `docs/review-rounds.tsv` literally, `review-gate` among them — the component a target
project copies first. A target must therefore edit the review gate by hand, which is the transfer cost the declaration
exists to remove. The consumer trace that names the adopters and non-adopters lives in the first change's archived
`design.md`, so the live set is recorded nowhere after that change closed.

## What Changes

- `review-gate`, `review-quick`, `experience-analyzer`, and the `review-thorough` command name the review-ledger role
  instead of the ledger's path, and point at `docs/contracts/process-bindings.md`.
- `docs/contracts/process-bindings.md` gains the consumer trace: the adopting components and the deliberate non-adopters
  with their reasons. The trace becomes the declaration's, not an archived change's.
- `openspec/specs/showcase/quality/agent-skills/spec.md`'s requirement gains the declaration-owned trace through a
  `MODIFIED` delta; no new requirement.
- `docs/adr/0026-process-bindings-are-declared.md`'s Consequences record the second adopting change.
- `docs/ideas.md` parks the lifecycle and review-gate heading roles' adoption, which this change leaves out.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the declared-bindings requirement's consumer trace moves to the declaration.

## Impact

- Docs: the declaration, the ADR, the parked idea, four process components, the spec.
- No runtime code, build, API, schema, image, chart, or deployment change.
