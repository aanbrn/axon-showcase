## Context

Both analyses were run this session, each only because it was scoped explicitly. A normal `agents-auditor` run found a
single stale count; a run scoped to conciseness produced three merge candidates; a run scoped to non-durable entries
produced one deletion and fourteen trims, and confirmed about thirty entries durable. The scope was always there — the
axis names merging and trivia — so the gap is standing versus incidental.

## Decisions

**The analyses become standing, and keep their tests.** A merge candidate is reported with the merged text and must
preserve every anchor and piece of evidence the originals carried, and must not blend two distinct lessons — the rule
this repo's own consolidation passes learned. A removal candidate must clear the decision test ("name the decision this
rule governs; if there is none, it is trivia"), which is deliberately the same wording the capture's filter now uses, so
a rule is judged by one standard in both directions. Conservatism stays: a removal candidate names what would be lost
and whether git preserves it, and both classes are candidates for the owner, never actions.

**They are findings, not a new class.** The severity vocabulary already holds them (`redundant`, `structural`); each
carries a `merge` or `remove` label so the verdict line can count them. A separate class would imply a different kind of
object, and these are the same kind — conciseness findings.

**The verdict line names the counts.** The report contract's first line is what makes a class visible; without the
counts a run that found three merge candidates reads like one that found none, which is how they went unnoticed in the
first place.

## Risks / Trade-offs

- **A removal is destructive if applied carelessly.** Mitigated by the class being candidates, by the decision test, by
  naming the loss, and by the owner's approval — the same route the existing findings take.
- **A longer report.** The contract already bounds items per finding, and both analyses read the same file the audit
  already reads in full.
