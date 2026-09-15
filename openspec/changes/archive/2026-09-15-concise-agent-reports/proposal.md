## Why

Every report-producing subagent closes with a "be concise" aside or a "lead with the highest-value fixes" line, and none
of it holds: capture and review reports run to hundreds of words each, carrying their own verification transcripts,
enumerations of everything the agent considered and rejected, and "you could also…" alternatives. The current
instruction is an adjective, not an output contract — it names a quality without bounding the output, so a correct-but-
verbose report satisfies it. A reader (and the main agent) must parse prose to find the verdict.

## What Changes

- The six report-producing subagents — `lesson-capture`, `review-quick`, `review-thorough`, `agents-auditor`,
  `specs-auditor`, `architecture-auditor` — gain a shared **output contract**: a verdict line first (a count for a
  capture/review, the section structure for the auditors), a per-item budget (a few lines: the item, its `file:line`,
  and one line of evidence), one line per candidate verified as already-covered or rejected, and no alternatives.
- The reasoning stays thorough: the contract bounds the **report**, not the **analysis**. Skipping verification to keep
  the report short is explicitly not the goal — the reports' value is that the agent ran the check.
- The `agent-skills` capability captures the contract as a requirement, so it is a durable behavior of the tooling
  rather than prose in six files that can drift apart.
- Each agent states the same contract in its own definition (a subagent cannot import one at runtime), and the trigger
  commands that today restate a report shape (`/audit-agents`, `/audit-specs`, `/audit-architecture`,
  `/review-thorough`) defer to it instead, so the shape lives in one place and cannot drift into variants.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the per-change subagents' report contract is specified — a verdict-first shape with a
  per-item budget and no alternatives, for every report-producing subagent.

## Impact

- `.opencode/agent/{lesson-capture,review-quick,review-thorough,agents-auditor,specs-auditor,architecture-auditor}.md` —
  each states the output contract in its own definition, since a subagent cannot import one.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — one requirement added for the contract.
- `AGENTS.md` — the relevant agent bullets note the contract where they describe the report.
- `.opencode/commands/{audit-agents,audit-specs,audit-architecture,review-thorough}.md` — each defers to the contract
  rather than restating a report shape.
- `README.md` — the agent table rows and the tooling prose stay accurate (no new agent, so only any report-shape prose).
- No behavior, code, or deployment change: this is agent tooling.
