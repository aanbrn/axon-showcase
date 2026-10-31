## Why

The two reuse pilots lost or imported the same governance contracts. The pause/resume slice lost the reviewer-session
and subject-pack obligations; the lesson-capture slice imported the knowledge-authoring standard and the shared report
contract as a support document. Both contracts live in the `agent-skills` spec, which a target cannot load, so a target
must copy the source corpus or lose the rules. ADR-0025 §7 names the first consolidation: the two contracts get portable
owners.

## What Changes

- `docs/contracts/knowledge-authoring.md` (new) — the operational rules for authoring `AGENTS.md` entries, with
  provenance.
- `docs/contracts/shared-report.md` (new) — the shared report contract, with provenance.
- The report-producing definitions, the scheduled `audit` workflow, and `AGENTS.md` point at the contract files: the
  per-unit review agents, `lesson-capture`, the four auditors, and the technical advisor. The three audit commands keep
  the short name.
- `openspec/specs/showcase/quality/agent-skills/spec.md` gains `MODIFIED` deltas for the two owner requirements and the
  six requirements that reference the budget's owner: the descriptions retarget to the contracts; the scenarios stay.
- The `agent-skills` Purpose refresh rides the archive commit.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the eight requirements' ownership clauses (the two owners and the six referencing
  requirements).

## Impact

- Docs: the two contract files; `AGENTS.md`; the eight agent definitions; `.github/workflows/audit.yml`.
- Specs: a `MODIFIED` delta on `showcase/quality/agent-skills` only; the main spec changes at archive.
- No runtime code, build, API, schema, image, chart, or deployment change.
