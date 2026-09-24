# Proposal

## Why

The `lesson-capture` subagent is `AGENTS.md`'s growth engine, but it can only grow: it proposes additions, each merged
into or replacing a bullet, and never proposes a rule's _retirement_. So when a change adds enforcement that subsumes an
existing rule, the rule — still true, no longer needed — stays, and the only counterweight is the periodic
`agents-auditor` weeks later: `bound-agents-growth` named exactly that gap ("retirement happens only when someone
happens to look") and left retirement to the audit. The `Docs refresh on change` convention refreshes to the new
_state_, so it reaches a rule the change _falsifies_ but not one the change merely makes unnecessary.

## What Changes

- **The lesson-capture gains a retirement/replacement analysis.** Alongside its additions, it reports the in-scope rules
  this change makes obsolete or redundant, each naming the rule, why the change makes it so, and the retirement or
  replacement it proposes. It is a candidate for the main agent (which already applies the capture), not an action.
- **The verdict line names the retirement count** alongside the durable-proposal count, so a capture that retired two
  rules is distinguishable from one that retired none.
- **The definition, the `agent-skills` spec, the `AGENTS.md` bullet, and the README** describe the widened remit.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the lesson-capture's per-change requirement gains the retirement/replacement analysis
  and its count.

## Impact

- `.opencode/agent/lesson-capture.md` — the new analysis, its verdict line, and the frontmatter description.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — the lesson-capture requirement extended (a delta in this
  change).
- `AGENTS.md` — the lesson-capture bullet.
- `README.md` — the agent-table `lesson-capture` row and the `Lesson capture` bullet under "What the Agent Automates".
- No code, build, runtime, or deployment change.
