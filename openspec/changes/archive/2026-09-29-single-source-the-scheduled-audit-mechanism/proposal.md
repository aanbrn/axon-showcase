# Proposal

## Why

`showcase/quality/agent-skills` and `showcase/quality/merge-governance` each state the scheduled-audit mechanism — the
OpenCode action's scheduled path, its `prompt` input and OIDC `id-token: write`, the branch/pull-request artifact, and
the single batched run — so a change to the mechanism must be edited in two specs (the 2026-09-28 specs-auditor
duplicated-mechanism finding). And `merge-governance`'s Purpose enumerates only the report-producing scheduled
workflows, omitting the scheduled end-to-end, dependency-security, and deployment-smoke runs that the spec also holds.

## What Changes

- `openspec/specs/showcase/quality/agent-skills/spec.md` — delta: the scheduled-audit requirement keeps only what it
  uniquely owns (which audits are schedulable, the cadence's reconciliation purpose, the on-demand-only `readme-auditor`
  and `experience-analyzer`) and points at `merge-governance` for the mechanism.
- `openspec/specs/showcase/quality/merge-governance/spec.md` — the capability's `## Purpose` is refreshed (a delta
  cannot carry a Purpose, so it lands in the archive commit) to name every scheduled workflow, not only the
  report-producing ones.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the "The on-demand audits are also available on a schedule" requirement points at
  `merge-governance` for the scheduled-audit mechanism instead of restating it.

`merge-governance`'s Purpose refresh is listed in What Changes but is not a modified capability: its requirements are
unchanged, and a Purpose cannot ride a delta.

## Impact

- `openspec/changes/single-source-the-scheduled-audit-mechanism/specs/showcase/quality/agent-skills/spec.md` — the delta
  (synced into the main spec at archive).
- `openspec/specs/showcase/quality/merge-governance/spec.md` — the `## Purpose` refresh in the archive commit.
- No code, build, runtime, or deployment change.
