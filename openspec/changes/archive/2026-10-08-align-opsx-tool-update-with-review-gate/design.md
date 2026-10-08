# Design

## Context

See proposal.md — Why. `.opencode/commands/opsx-tool-update.md` is the project-authored command that regenerates the
OpenSpec instruction files and syncs the CLI pin after a release. Its final step ("Commit the regenerated instruction
files and the pin changes … as a standalone change") is unconditional, while `AGENTS.md`'s review gate ("Auto-review the
change before asking for a manual review" and "Never commit … on the strength of a clean `review-quick` alone") requires
the quick review, the capture, and the manual-review pass to precede a commit. `AGENTS.md` is loaded on every
invocation, so the command only needs to stop contradicting the gate, not re-teach it.

## Goals / Non-Goals

**Goals:**

- Make the command's terminal commit conditional on the review gate, so a routine run completes without stopping to ask
  whether the gate may be skipped.

**Non-Goals:**

- Restating the review gate inside the command.
- Changing the command's version check, `openspec update`, config-probe, or pin-sync steps.

## Decisions

**Defer the commit to the gate, and word the step to point at `AGENTS.md` rather than restating it.** The alternative —
copying the gate's steps (`review-quick`, `lesson-capture`, manual review) into the command — was rejected: `AGENTS.md`
already owns them and a second copy drifts.

**Keep the change to the command file only.** A docs-refresh sweep confirms none is owed: the README's table row
("Regenerates the OpenSpec command/skill files after an openspec CLI release"), `AGENTS.md`, `docs/adr/`,
`docs/ideas.md`, and `openspec/config.yaml`'s `context:` block all describe what the command regenerates, never its
commit step, so none needs an edit — the fix makes the command conform to `AGENTS.md` rather than altering it.

## Risks / Trade-offs

- [An agent could still commit without running the gate] → The step names the gate and points at `AGENTS.md`, whose
  rules are always loaded.
