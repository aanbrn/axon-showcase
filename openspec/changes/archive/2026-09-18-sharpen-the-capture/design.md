## Context

Two observations from the capture chain that ran this session drove this change. First, the implementation and
post-merge captures overlap: both are handed the same diff, review findings and change dir, and the post-merge one adds
only the merge's non-diff effects — the reason it exists at all (`AGENTS.md:190`, "a capture verifies the live state the
merge left"). Second, the rubric leaks: asked for durable, non-obvious lessons, the subagent found something in every
one of nine chained rounds, and the later rounds produced prose-craft rules and process observations.

## Goals / Non-Goals

**Goals**

- One capture run per unit, at the point its context is freshest.
- Keep the merge's live-state check without a second full run.
- A filter that rejects trivia at authorship rather than at a later audit.

**Non-Goals**

- Removing the merge-time check — the live-state verification is the reason the post-merge capture existed.
- A budget or cap on rules per capture; the decision test is the filter, not a count.
- Scheduling a consolidation cadence. An audit after every Nth capture is worth doing, but it is a process idea rather
  than a behavior change, and is being parked as its own docs PR (task 3.1).

## Decisions

**The implementation capture stays; the merge becomes detection.** The spec's scenario names the implementation trigger,
and that is where the diff, the review findings and the change dir are all at hand. At the merge, the main agent reads
the merge's non-diff effects, reports any candidate lesson with the bullet it would extend, and asks before running
another — the same shape as a capture merge's detect-and-ask, so one pattern covers both.

**The decision test is the primary filter.** "Name the decision this rule governs; if there is none, it is trivia" is
the general form of the question that killed a bad rule during this session's own review ("would following the existing
discipline already have prevented this?"). It is stated with the recurrence/severity question so a rule that governs a
decision nobody will meet is also rejected. The `disable-axoniq-console-message` deletion this repo just made is the
worked example: a fact governing no decision.

**The worth-check goes in the capture's review, not a new gate.** `review-quick` already reads the diff and reports
cheaply; asking it to challenge a capture's rules for durability puts an independent judgment in the path for almost no
cost. It is a refinement of the existing quick-review obligation, not a new pass.

## Risks / Trade-offs

- **A merge-time detection could miss a lesson the run would have found.** Mitigated by asking the owner, and by the
  implementation capture having already run with the same material; the genuinely merge-only content is the non-diff
  effect, which the detection reads directly.
- **The worth-check is a judgment, not a gate.** Two reviewers could class a rule differently; the criterion (a decision
  it governs, and whether a future change hits it) is stated so the judgment is reproducible.
