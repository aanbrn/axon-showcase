# Proposal

## Why

The owner retypes "if the branch is behind main, update it" on every unit. The procedure it names already exists —
`AGENTS.md`'s "Refreshing a branch" states the mechanism for each state, and `#704` added the stash recovery for a
refused fast-forward — so the missing piece is a name for the instruction, not another implementation of the rule.

## What Changes

- Add `.opencode/commands/sync-branch.md`: a prose command that compares the branch with `origin/main`, runs the refresh
  rule, resolves a conflict for both sides, and reports the outcome.
- Add the `/sync-branch` row to `README.md`'s slash-command table.
- Add the command's pointer to `AGENTS.md`'s "Refreshing a branch" bullet.
- Correct the tooling-placement bullet's derivation command, which reached only the executable-bit paths and so missed
  the non-executable tools the same bullet enumerates.
- Park three lessons in `docs/ideas.md` rather than shipping them here: two test-authoring modes whose evidence came
  from a script this change no longer delivers, and the `pause-resume-unit` `Stage` field whose wording invites the
  ledger's state.

## Capabilities

None. **`skip_specs: true`**: the command runs a rule the corpus already states — the mechanism per branch state in
`AGENTS.md`, the recovery route `#704` merged, and the command-lifecycle contract `showcase/quality/agent-skills`
already binds — so it adds no behavioral contract of its own.

## Impact

- New: `.opencode/commands/sync-branch.md`.
- `README.md` (one table row), `AGENTS.md` (one pointer sentence and the derivation-command correction), `docs/ideas.md`
  (three parked entries).
- No script, no test suite, no build wiring, and no spec delta: the refresh itself is the rule's procedure, executed by
  the agent that invokes the command.
