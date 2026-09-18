## Why

The audit's merge analysis, as `widen-auditor-to-merge-and-removal` wrote it, tells the auditor to produce a merged text
that preserves every anchor and never blends two distinct lessons. A smoke-run with a seeded duplicate pair exposed the
case it does not cover: the auditor produced a perfectly good merged text and then noted that the text would itself
restate a bullet that already exists elsewhere in the file. So the pair's real disposition was deletion, not a merge,
and the analysis as written would have had the auditor stop one step short of saying so. The same shape recurs whenever
a candidate's content is already carried by a survivor.

## What Changes

- The merge analysis gains the disposition it was missing: where the merged text would only restate a rule the file
  already carries, the candidate is a **deletion of the duplicate**, not a merge — and the audit says which it is.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills`: the tooling-audit requirement's conciseness clause and its standing-findings scenario
  gain the deletion-of-a-duplicate disposition on a merge candidate.

## Impact

`.opencode/agent/agents-auditor.md` (the conciseness axis and its merge sentence) and `AGENTS.md` (the auditor bullet).
No code change.
