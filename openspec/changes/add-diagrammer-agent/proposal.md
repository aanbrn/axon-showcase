## Why

ASCII-diagram work (README flow diagrams, alignment, bracket spans) repeatedly cost the cheap flash main agent
significant effort, money, and review cycles — the flash model is weak at ASCII geometry, and the `review-thorough`
(pro) model visibly draws diagrams better. The repo already routes vision work to a model-pinned subagent (`vision`);
diagram rendering deserves the same specialization so the main session stays on the cheap model.

## What Changes

- Add a `diagrammer` subagent (`.opencode/agent/diagrammer.md`) pinned to `opencode-go/deepseek-v4-pro` that draws and
  fixes ASCII diagrams: it establishes the semantic mapping (which span ends where) before rendering, aligns by
  character width (not byte length), preserves deliberate asymmetries, and verifies alignment before returning.
- Add a `/diagram` command (`.opencode/commands/diagram.md`) that triggers the subagent, confirms the mapping with the
  user first, and applies the result only on go-ahead.
- Capture the new subagent in the `showcase/quality/agent-skills` spec.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents" requirement gains the
  `diagrammer` subagent, which draws/fixes ASCII diagrams with the pro model and preserves intentional asymmetry.

## Impact

- `.opencode/agent/diagrammer.md` — new subagent definition.
- `.opencode/commands/diagram.md` — new command trigger.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — delta for the modified requirement.
- `AGENTS.md` and `README.md` — a `diagrammer` entry in the subagent conventions and the agent table (docs refresh,
  separate docs PR).
