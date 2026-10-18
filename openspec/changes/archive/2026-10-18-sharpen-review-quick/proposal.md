# Proposal

## Why

The quick review catches omissions and convention violations cheaply, but its checks are compressed: "any obvious
correctness problems visible at a glance" leaves out the boundary conditions, the changed semantics, and a test that
would still pass with the behavior broken. The evidence a class needs is implied rather than stated, so a suspicion can
read as a confirmed defect, and a finding's shape does not require its class or the correction it needs — a reviewer
that names the evidence and the correction is actionable in one pass, and one that says what to check finds the defect
the compressed line misses.

## What Changes

- **`showcase/quality/agent-skills`** — the review-quick scenario gains the checklists (proposal, implementation, and
  conventions), the per-class evidence requirements, the prioritization order, the finding shape, and the
  moved-or-rewritten recurrence clause (one `MODIFIED` block).
- **`.opencode/agent/review-quick.md`** — the same in the definition's own words.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the per-change quality-gate and analysis subagents requirement (one `MODIFIED`
  block).

## Impact

- A future quick review names what it checked, the evidence a finding rests on, and the correction it requires; nothing
  about the product changes.
- **Build / tests / services**: no code, build, dependency, or deployment change — one agent definition and the spec
  delta.
- **Verification**: `openspec validate --all` and `spotlessApply`/`spotlessCheck` for the artifacts; a definition's
  behavior is only exercised by a run, which needs the edited definition picked up (a reload or a new session).
