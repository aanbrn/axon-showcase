# Proposal

## Why

`AGENTS.md`'s growth control is the accreted-rule count the `/audit-agents` verdict reports — the capture rule leans on
it ("the control is the periodic `/audit-agents` pass — whose verdict already reports the accreted-rule count"). But the
five reports under `docs/audits/` count different things: the raw `captured:` marker count (`24`, `87+`), the
attributed-origin or distinct-rule count (`16`, `~71`), and the number of representative items the report lists (`4`).
The series `24, 16, 87+, 4, ~71` is therefore not a trend, and the file's growth bound is unfalsifiable at the moment it
matters.

## What Changes

- `.opencode/agent/agents-auditor.md`: give the verdict's `accreted` count one definition — the number of distinct
  accreted (meta) rule units carrying at least one `captured:` marker, each counted once regardless of how many markers
  it carries — and require a second figure for the rule units added since the newest prior report under `docs/audits/`
  (or `none recorded` when none exists); state both in the report-contract verdict line.
- `.opencode/commands/audit-agents.md`: step 1 names the metric, so the trigger's read-list matches the definition.
- `openspec/specs/showcase/quality/agent-skills/spec.md` (delta): the agent-tooling audit's accretion-class paragraph
  defines the one metric and the new-since-last-report figure.
- `docs/ideas.md`: remove the implemented "Give the agents-auditor's growth metric one definition" entry, and update the
  blocked "Trend the audit counts across reports" entry now that its prerequisite lands.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the agent-tooling audit's accretion-class count gains a single definition plus a
  new-since-last-report figure, so the count is trend-comparable across reports.

## Impact

- Files: `.opencode/agent/agents-auditor.md`, `.opencode/commands/audit-agents.md`,
  `openspec/specs/showcase/quality/agent-skills/spec.md`, `docs/ideas.md`.
- No code, build, or deployment impact: the auditors are prompt-defined subagents, not build inputs, and no gate reads
  the count. The `README.md` needs no change — the auditor still "reports accreted meta rules with their origins"; only
  the count's definition changes, which the README does not state. `AGENTS.md` needs no change either — one reference
  names "the accreted-rule count" generically and the agents-auditor bullet defers the audit's details to the
  `agent-skills` spec, neither stating a competing definition.
