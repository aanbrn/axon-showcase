# Proposal

## Why

The planning stage is read-only for the agent itself — the generated propose guidance says "keep inspection read-only"
and explore mode forbids implementing — but nothing binds a _delegation_ the proposing agent makes, so a planning-stage
delegate can edit implementation files out of stage and the change dir is its only boundary. That happened once
(session-accounted); the read-only boundary should bind the delegation, not only the caller.

## What Changes

- `AGENTS.md` — the OpenSpec Workflow Agreement gains a paragraph: a delegation made while a change is explored or
  planned (before implementation) is read-only against the project's source and configuration — the delegate may write
  only the change's own artifacts under `openspec/changes/<change>/`, its prompt names the paths it may touch, and the
  calling agent verifies it stayed inside them before using its output.
- No spec delta and no `openspec/config.yaml` rule: the boundary is agent process control flow (no capability covers
  it), and `AGENTS.md` is loaded on every invocation, so the always-loaded home carries it without a per-artifact copy.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — agent process control flow, not spec'd behavior (`.openspec.yaml` sets `skip_specs: true`)

## Impact

- `AGENTS.md` — one paragraph in the OpenSpec Workflow Agreement.
- `docs/retrospectives/2026-09-28.md` — S2's disposition cell names this change.
- No code, build, test, runtime, or deployment change.
