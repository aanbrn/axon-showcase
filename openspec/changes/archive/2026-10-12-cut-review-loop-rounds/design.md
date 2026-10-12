# Design

## Context

See `proposal.md` — Why. Each of the four claim classes the auto-review bullet names is already covered by a durable
`AGENTS.md` rule — the external-artifact claim by its "written from memory rather than derived" root-cause shape, the
restated model by its "single-source it" clause, the restated count by the documented-numbers gotcha, and the mechanism
or task-check gap by "A check is evidence only once it has been shown to fail" and the premise-interrogation bullet. The
gap is timing: those rules fire when a review finding names them, not before the author writes the claim, so the
proposal stage repeatedly takes several rounds. The reviewer's repeated-class flag (`review-quick.md`) fires only when
the caller supplies the prior round's findings.

## Goals / Non-Goals

**Goals:**

- Cut the proposal-stage review rounds on small changes without weakening the gate.

**Non-Goals:**

- Changing the reviewer's contract: a separate change, since it owes the `agent-skills` delta.
- A hard first-round finding budget: it would suppress genuine findings.

## Decisions

**A pre-write derivation pass on the auto-review bullet, not a new bullet.** The four classes' rules already exist, so
the durable improvement is to state _when_ they apply — before the first round — rather than restate them; a new bullet
would duplicate rules the file already carries, and the pass's derivations live on the bullet rather than being repeated
here.

**The prior-list clause on the same bullet.** The repeated-class flag needs the prior round's findings, and the
stop-and-re-derive remedy already expects that signal; making the caller pass the list supplies the trigger.

**The per-artifact rules in `openspec/config.yaml`.** It is the one surface OpenSpec feeds the model per artifact, so a
derivation rule there is applied at write time. The four classes map across the artifacts: `proposal` gains the
external-artifact and restated-count rules, `design` gains the restated-model and mechanism rules, and `tasks` already
carries the task-check rule (a known-bad/known-good control), so no addition is needed there.

## Risks / Trade-offs

- The `config.yaml` rules are an un-gated copy (CI probes only the list's shape) → refresh them if the obligation moves,
  per the docs-refresh convention.
- A pre-write pass costs the author a little time → it replaces review rounds, which cost more.

## Migration Plan

None: guidance and config, taking effect on the next unit.
