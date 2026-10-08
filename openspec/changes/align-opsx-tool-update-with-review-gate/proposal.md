# Proposal

## Why

The `/opsx-tool-update` command's last step tells the agent to commit the result "as a standalone change",
unconditionally. `AGENTS.md`'s review gate orders the opposite — a clean `review-quick` pass, the per-unit
`lesson-capture`, and the user's manual review pass all precede any local commit — so running the command forces the
agent to stop mid-routine and surface the conflict instead of completing it, once per CLI release.

## What Changes

- `.opencode/commands/opsx-tool-update.md` — the terminal step is reworded so the commit is taken after the repository's
  review gate (the `review-quick` pass, the `lesson-capture`, and the user's manual review pass) rather than
  unconditionally, pointing at `AGENTS.md` instead of restating the gate.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — a project-authored command-definition edit, so `.openspec.yaml` sets `skip_specs: true`. No spec describes this
  command's behavior: the `agent-skills` and `code-quality` specs reference `opsx-tool-update` only as a file-scope
  example (the agents-auditor's in-scope set; the formatter's target set), and the commit discipline it clashes with
  lives in `AGENTS.md`, not a spec.

## Impact

- `.opencode/commands/opsx-tool-update.md` only; no source, build, test, or deployment surface changes.
- The command is a Spotless-gated markdown file, so `spotlessCheck` covers the edit; nothing else in `check` is
  affected.
