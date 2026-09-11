## Why

The `opencode-go` provider catalog now lists `deepseek-v4.1-flash`, a newer sibling of the `deepseek-v4-flash` model the
repo's agents currently pin to (same 1M-token context / 384K output, same flash family, tool-calling and attachment
support). The main agent and the flash-pinned subagents should move to the current model so the repo's agent stack uses
the latest cheap text-only model.

## What Changes

- `.opencode/opencode.json`: default `model` and `small_model` switch from `opencode-go/deepseek-v4-flash` to
  `opencode-go/deepseek-v4.1-flash`.
- `.opencode/agent/review-quick.md`, `.opencode/agent/lesson-capture.md`, `.opencode/agent/experience-analyzer.md`: the
  flash-pinned subagents switch to `opencode-go/deepseek-v4.1-flash`.
- `.github/workflows/opencode.yml`: the `model` input on the opencode action switches to
  `opencode-go/deepseek-v4.1-flash`.
- `AGENTS.md`: the agent-description gotchas that name the cheap main-agent model (`deepseek-v4-flash`) are updated to
  `deepseek-v4.1-flash`.
- The `vision` subagent stays pinned to `opencode-go/deepseek-v4-flash-vision-exp`: no v4.1 vision variant exists in the
  catalog, and the vision model is a separate experimental line.
- The pro-model subagents (`review-thorough`, `diagrammer`) and the `showcase/quality/agent-skills` spec are untouched —
  the spec describes the agents conceptually ("cheap flash main agent", "pro model"), which remains true.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is a tooling/docs-only change: the agent-skills spec captures agent behavior and capabilities, not the
  literal model IDs they pin to, so no requirement changes.

## Impact

- `opencode.json`, the three flash-pinned subagent definitions, the `opencode.yml` workflow, and the two `AGENTS.md`
  agent gotchas.
- No effect on application code, build, tests, or deployment.
