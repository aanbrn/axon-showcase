## Why

The shared report contract's verdict scenario says an auditor's first line states "the section structure it uses", but
no auditor does: all three open with a count — `<n> findings`, or `<n> findings, <n> advisory` for an audit that carries
an advisory section. The wording over-specifies one shape where the contract's own unification is the count, so the spec
describes output the agents are not instructed to produce.

## What Changes

- The `agent-skills` report-contract requirement's verdict scenario is reworded to state what every report actually
  opens with — whether anything remains and how many items — noting that an audit with an advisory section names both
  its findings and advisory counts.
- `AGENTS.md`'s own report-contract mention is reworded to match (the multi-artifact-sweep bullet carried the same stale
  "section structure" framing), since a durable artifact restating what the spec fixes must not keep the old wording.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the report contract's verdict scenario states the count every report opens with,
  instead of "the section structure" no auditor uses.

## Impact

- `openspec/specs/showcase/quality/agent-skills/spec.md` — one scenario's `THEN` bullet reworded (applied at archive).
- `AGENTS.md` — the report-contract mention in the multi-artifact-sweep bullet reworded to the count it describes.
- No agent definition, command, or `README.md` edit: every definition already opens with a count, so the spec is the
  artifact that drifted.
- No behavior, code, or deployment change.
