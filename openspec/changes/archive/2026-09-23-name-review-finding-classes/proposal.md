# Proposal

## Why

The review gate's convergence check lives on the caller's side only. `AGENTS.md` records that a loop repeatedly finding
the same _class_ of observation has not converged and should be re-derived rather than patched — but the `review-quick`
definition never asks the reviewer to name a finding's class or to compare it with the prior round, so a caller who
loses track of the thread cannot tell that a class is repeating. Over one unit, a capture took five review rounds (2 → 1
→ 2 → 1 → clean) and a change's proposal and implementation two each; in every case the same class returned, and the
caller only recognized it on a later round — after wasted passes. The persistence of the caller's own prior-round
history in the prompt is what enabled the one round that did flag a repeat ("the exact shape the sweep sentence was just
fixed for"), which makes the signal a property of the prompt rather than of the review.

## What Changes

- **`.opencode/agent/review-quick.md`** — every finding is classified (e.g. `contradiction`, `dropped-content`,
  `formatting`), and a finding whose class a prior round already raised keeps that substantive class and is additionally
  reported as a repeat of it, naming the class — the repeat is a flag on the finding, not a class of its own, so the
  class stays comparable across rounds and the convergence signal comes from the review's own output rather than from
  the caller's memory. The verdict line names the classes the report carries and which of them repeats a prior round's,
  when the caller's request names the prior rounds.
- **`openspec/specs/showcase/quality/agent-skills/spec.md`** — the quick-review scenario gains the classification and
  the repeated-class flag; the shared report contract's verdict line is extended to name repeated classes, as it already
  names each class's count.
- **`AGENTS.md`** — the non-convergence sentence in the auto-review paragraph points at the reviewer's class flag, so
  the caller re-derives on the reviewer's signal rather than on its own recollection. The `review-quick` mention is
  updated; no new bullet.

Degradation is explicit: with no prior-round list supplied, the reviewer still classifies each finding but says the
repetition is unknown rather than guessing — the flag is advisory to the caller, and the caller remains the arbiter of
convergence.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the quick review classifies each finding and flags a class a prior round already
  raised; the shared report contract's verdict line names the repeated classes.

## Impact

- `.opencode/agent/review-quick.md` — the classification and repeated-class flag in the review instructions, and the
  report contract's verdict line.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — two `MODIFIED` requirements (the quick-review scenario and
  the shared report contract's verdict scenario); the `## Purpose` is refreshed in the archive commit only if its scope
  text needs it.
- `AGENTS.md` — the auto-review paragraph's non-convergence sentence, and the `review-quick` mention it carries.
- `README.md` — only if its description of the quick review states what the reviewer reports; verified in a task rather
  than assumed.
- No code, no runtime behavior, no dependency, no build or deployment change.
