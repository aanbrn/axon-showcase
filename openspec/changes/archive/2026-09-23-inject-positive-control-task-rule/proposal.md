# Proposal

## Why

The check-evidence rule — a check is evidence only once it has been shown to fail on a known-bad input and pass on a
known-good one — lives in `AGENTS.md`, which the agent reads on recall, so it does not fire at the moment a check is
added or a scratch command is used as evidence; the 2026-09-19 retrospective found the false-signal class recurring that
way. A rule in `openspec/config.yaml` is injected by the CLI at `openspec new change` / `openspec instructions` time, so
a change that adds or changes a check is prompted to plan the proof up front instead of relying on recall.

## What Changes

- `openspec/config.yaml`: add a `tasks` rule requiring a change that adds or changes a check to include a task proving
  the check fails on a known-bad input and passes on a known-good one, and a task to read the output of any scratch
  command used as evidence.
- `docs/ideas.md`: remove the parked idea (implemented by this change).
- `docs/retrospectives/2026-09-19.md`: update suggestion 5's disposition to name this change.
- No `AGENTS.md` edit: the check-evidence bullet stays the always-loaded statement of the rule, and the config rule is
  the planning-time injection of it — the two are not redundant copies but the recall and injection surfaces of one
  rule.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — the change adds a planning-time rule to the OpenSpec configuration; no capability spec covers the configuration's
rule content (verified by grepping `openspec/specs/`), so the change declares `skip_specs`.

## Impact

- **Tooling**: `openspec/config.yaml`'s `tasks` rule set (the rules the CLI injects when authoring a change's
  `tasks.md`); no application code, no build or test change.
- **Docs**: `docs/ideas.md`, `docs/retrospectives/2026-09-19.md`.
- **Build / tests / deployment**: none.
