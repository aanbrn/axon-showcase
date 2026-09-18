## Why

The capture convention runs `lesson-capture` twice per unit — once at the implementation, once after the merge — and its
only quality filter is the subagent's own rubric ("durable and non-obvious"). Both cost more than they return. The two
runs overlap heavily: the post-merge one mostly re-reads what the implementation one already had. And the rubric alone
does not hold the line — a nine-rule capture chain ended only when the owner capped it by hand, and its later rounds
produced prose-craft and process rules rather than lessons. Nothing at capture time asks whether a proposed rule governs
a decision; the only counterweight is a consolidation audit after the fact.

## What Changes

- **One capture per unit, at implementation.** The post-merge run becomes a **detection**: at the merge, read the
  merge's non-diff effects, report any candidate lesson with the bullet it would extend, and ask the owner before
  running another — the detect-and-ask shape already used for a capture's own merge.
- **A decision test.** A proposal must name the decision its rule governs; one that governs no decision is trivia, not a
  rule, and is not proposed. The recurrence/severity question ("would a future change plausibly hit this, and is the
  cost of not knowing it material?") accompanies it.
- **A worth-check at the capture's review.** `review-quick` already reads the diff; for a capture it also challenges
  each new rule's durability — does it govern a decision, does it restate or extend an existing rule.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills`: the capture scenario loses the post-merge run for a merge-time detection and gains
  the decision test; the quick-review scenario gains the capture worth-check.

## Impact

`AGENTS.md` (the capture bullet's trigger and rubric), `.opencode/agent/lesson-capture.md` (its trigger and the decision
test), `.opencode/agent/review-quick.md` (the worth-check). No code change.
