## Context

Implements the parked idea that the `agent-skills` Purpose omits the visual and diagram agents its requirement covers.
See the proposal.

## Goals / Non-Goals

- **Goal:** the Purpose enumerates every subagent the capability provides.
- **Non-Goal:** changing any requirement, scenario, or behavior text — the omission is in the Purpose prose only.

## Decisions

- **Edit the Purpose directly as a `skip_specs` change, not through a delta — applied in the archive commit.** A
  `## Purpose` is not a requirement, so no delta block can carry it, and `openspec archive` treats the main spec's
  Purpose as authoritative and leaves it alone. `AGENTS.md` records the Purpose as the second non-delta spec edit
  (alongside the `#` title) and says it **waits for the archive commit**, so it is not part of the implementation diff;
  the archived `address-new-snyk-findings` and `apply-specs-audit-findings` changes did the same.
  - _Alternative — fold it into the next change touching this capability:_ rejected as an indefinite deferral; the idea
    has been parked since the `concise-agent-reports` smoke-run and no such change is scheduled.
  - _Alternative — a delta with a MODIFIED requirement:_ rejected, it would change a requirement to fix a Purpose.

## Risks / Trade-offs

- A Purpose edited outside a delta is un-gated by `openspec validate` — accepted, and the same risk the `#` title
  exception carries; the change's review pass reads the diff, and the edit is a single enumeration addition.
