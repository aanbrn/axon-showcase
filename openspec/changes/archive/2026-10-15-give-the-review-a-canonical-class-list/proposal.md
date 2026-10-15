# Proposal

## Why

The review's class labels are free-form: `docs/review-rounds.tsv` records **55 distinct labels** across its rows,
several of them naming one phenomenon (`claims-table`, `-source`, `-omission`, `-anchor`) and one used exactly once
while being recorded as _repeated_ — a label no prior round had raised. The repeat marking, the record's aggregation and
the retrospective digest all compare **labels**, so a drifting label silently defeats the loop's own convergence signal:
the class-exhaustion clause and the "re-derive the root cause" rule both fire on that signal, and a class split across
labels reads as several classes that never recur. The figures are in this change's claims table (`design.md`).

## What Changes

- **A canonical class list in the reviewer's contract** (`.opencode/agent/review-quick.md`): each class names _what must
  change_ — so the label is comparable across rounds and units — with the rule that a finding fitting none extends the
  list in the same change, so the vocabulary stays canonical rather than drifting again.
- **The record's header** (`docs/review-rounds.tsv`) names the vocabulary and the boundary: rows before this change
  predate it and keep their labels, since a record's measured data is what it records.
- **The mapping** from the record's 54 class labels to the canonical set is recorded in `design.md`, so the older rows
  stay readable and a later digest can normalize them.
- **`showcase/quality/agent-skills`** (delta) — the quick-review scenario's classification clause names the canonical
  list and the extension rule.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: the "Per-change quality-gate and analysis subagents are available" requirement's
  quick-review scenario, which already owns the classification and the repeat marking.

## Impact

- Files: `.opencode/agent/review-quick.md` (the list and the extension rule), `docs/review-rounds.tsv` (its header),
  `AGENTS.md` (its pointer to the reviewer's finding classes), and this change's delta.
- No behavior changes beyond the labels a review reports: the review's scope, severity, and the class-exhaustion
  machinery are untouched, and the record's older rows are deliberately not rewritten.
