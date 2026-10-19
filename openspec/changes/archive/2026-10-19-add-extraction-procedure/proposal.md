# Proposal

## Why

Both 2026-10-19 audit extraction opportunities have now shipped (`extract-load-test-commands`, `extract-ci-narrative`),
each as a dedicated change. The standing `/apply-audit-reductions` unit routes an extraction to a change but states no
procedure for the move itself.

The parked 2026-10-18 idea asked for that procedure once a second extraction was proposed; it now has been, twice. The
verification of the report's three remaining candidates also showed the check the procedure must state: all three fail
the retrieval path, and two are coupled to gates that read `AGENTS.md` (`verifyInstallCommands`,
`verifyDocumentedPins`), so deleting their block empties the check rather than moving it.

## What Changes

- `AGENTS.md`: a new `Workflow: Extract an AGENTS.md section` procedure states the move's ordered steps, pointing at the
  audit's conditions and the growth-bound bullet's trim verification rather than restating them.
- `.opencode/commands/apply-audit-reductions.md`: its extraction bullet points at that procedure.
- `docs/ideas.md`: the implemented 2026-10-18 entry is removed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. `showcase/quality/agent-skills` already states the reduction unit's route — an extraction
candidate is routed to the normal change workflow, which is unchanged — and no capability spec states the change
workflow's extraction steps, so no requirement's outcome changes.

## Impact

- Files: `AGENTS.md`, `.opencode/commands/apply-audit-reductions.md`, `docs/ideas.md`.
- No code, build, or deployment impact: guidance prose and a command definition's pointer.
