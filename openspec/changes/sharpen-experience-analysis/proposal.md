# Proposal

## Why

The `experience-analyzer`'s retrospective reports what shipped and what went wrong, and classifies each suggestion as
`system` or `process` — but it never traces a failure to its cause and the stage that could have caught it, never
evaluates whether the previous retrospective's recommendations worked, and states no expected outcome a later window can
verify. The digest already carries the evidence for all three (the window's review rounds, classes raised, and
compliance misses, and the previous retrospective's file), so the retrospective can root-cause and verify from the
derived facts rather than from recollection. Where an improvement belongs and whether it warrants an instruction at all
are already owned by the per-unit capture's promotion gate and the auditor's route candidates, so they are pointed at
rather than restated here.

## What Changes

- **`showcase/quality/agent-skills`** — the "Experience analysis is available to agents" requirement gains a
  root-cause/earliest-prevention obligation, an evaluation of the previous retrospective's recommendations, and a
  verifiable expected outcome per suggestion (a body clause and three scenarios).
- **`.opencode/agent/experience-analyzer.md`** — the same three obligations in the definition's own words.
- **`.opencode/commands/retrospective.md`** — step 4 names the three.
- **`AGENTS.md`** — the `experience-analyzer` bullet names them.
- **`README.md`** — the retrospective prose names them.
- **`AGENTS.md`** and **`openspec/config.yaml`** — this unit's own capture merges three clauses into the pre-write
  pass's step 1 and the "recurring unit type" bullet (a universal claim is re-grepped like a count; a term with no
  referent is deleted rather than defined; the README's agent table row and prose bullets join the sweep, grepped by the
  artifact's name **and** its trigger command), with the first two mirrored into the config's `proposal`/`design` rules.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the experience-analysis requirement gains the root-cause/earliest-prevention step,
  the previous-recommendation evaluation, and the per-suggestion expected outcome.

## Impact

- A retrospective traces a failure to the stage that could have caught it, checks whether the previous window's
  suggestions helped, and states how each new suggestion would be verified; nothing about the product changes.
- **Build / tests / services**: no code, build, dependency, or deployment change — one agent definition, its command,
  `AGENTS.md`, `README.md`, and the spec delta.
- **Verification**: `openspec validate --all` and `spotlessApply`/`spotlessCheck` for the artifacts; a definition's
  behavior is only exercised by a run, which needs the edited definition picked up (a reload or a new session).
