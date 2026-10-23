# Proposal

## Why

The capture invocation contract disagrees with itself. `AGENTS.md`'s capture workflow — its owner — requires the capture
to be invoked unless a closed skip conjunction holds in full, and the archived `make-the-capture-selective` decision
fixed that default deliberately. The `agent-skills` scenario "Lessons are captured after implementation" instead says
the main agent invokes the `lesson-capture` subagent only when its evidence shows a candidate lesson, which is the
inverted model that decision rejected. Because the spec is the behavioural source of truth, an agent reading it can skip
a unit the root rule requires the capture for — for example a unit that changed `.opencode/` without noticing a lesson.

The same precondition is restated three times in `README.md` — the agent table row included — and in the
`lesson-capture` definition's usage line, so the contradiction is not confined to one artifact.

## What Changes

- **Modify** the `agent-skills` requirement "Lessons are captured after implementation": its description states the
  default-invoke outcome, and its first scenario's `THEN` replaces "only when it does" with "unless the skip condition
  defined by `AGENTS.md`'s capture workflow holds in full", keeping the record-of-skip obligation and every other
  clause.
- **Align** the three `README.md` sites (the agent table row and two prose sentences) and the `lesson-capture`
  definition's usage line with that outcome, without restating the skip conjunction — `AGENTS.md` stays its only owner.
- **Keep unchanged** the skip conjunction itself, the promotion gate, the evidence threshold, the dispositions, the
  destinations, the capture count, the merge-time re-verification, and every machine check.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the capture invocation contract (the requirement's description and its first
  scenario)

## Impact

- `openspec/specs/showcase/quality/agent-skills/spec.md` at archive, through the delta.
- `README.md`: the agent table row and two prose sentences in the agentic-process section.
- `.opencode/agent/lesson-capture.md`: the frontmatter's usage sentence.
- No code, API, dependency, build, test, or deployment change; `AGENTS.md` and `openspec/config.yaml` already state the
  correct rule and are not edited.
