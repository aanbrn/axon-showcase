## Context

The main agent runs on the cheap `opencode-go/deepseek-v4-flash` model, which is weak at ASCII diagram geometry —
diagram work has repeatedly cost extra effort, review cycles, and money. The `vision` subagent already establishes the
pattern of routing capability-specific work to a model-pinned subagent (`flash-vision-exp`) so the main session stays
cheap. ASCII diagram rendering is the same shape of specialization.

## Goals / Non-Goals

**Goals:**

- Route ASCII diagram drawing/fixing to a pro-model subagent, so the flash main agent doesn't fight geometry.

**Non-Goals:**

- Changing the main agent's model, the review subagents, or the diagram content decisions (the semantic mapping still
  requires human confirmation).

## Decisions

**D1: New `diagrammer` subagent pinned to `opencode-go/deepseek-v4-pro`, invoked on demand.** Mirrors the `vision`
precedent (model-pinned subagent, not auto-scheduled). The prompt encodes the three diagram lessons just captured:
establish the semantic mapping before rendering (which span ends where), align by character width, and preserve
deliberate asymmetry. `deepseek-v4-pro` is already used by `review-thorough`, so no new model dependency.

**D2: `/diagram` command trigger, mirroring `/review-thorough`.** The subagent reports (renders + verifies alignment);
the main agent applies it only on the user's go-ahead.

## Risks / Trade-offs

- [The pro model still needs the semantic mapping confirmed up front] → the command's step 2 and the agent's prompt both
  require stating the mapping before rendering; the AGENTS.md "confirm the semantic mapping" convention covers the
  process side.
- [A model-pinned subagent adds an expensive pass when used] → on-demand only, like `review-thorough`/`vision`; not
  auto-scheduled.

## Migration Plan

1. Add `.opencode/agent/diagrammer.md` and `.opencode/commands/diagram.md`.
2. Add the `diagrammer` entry to the agent-skills spec delta.
3. Docs refresh: mention `diagrammer` in AGENTS.md's subagent conventions (separate docs PR).

## Open Questions

None.
