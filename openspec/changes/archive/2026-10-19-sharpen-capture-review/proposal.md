# Review capture proposals against their evidence

## Why

`review-quick`'s capture check asks whether a new rule governs a decision and whether it extends an existing rule rather
than restating one, but it gives no way to answer either from the supplied materials. `add-extraction-procedure`'s
capture proposed two candidates the promotion gate should have stopped — a claim-sourcing clause the source-derivation
rule already required, and a section rewrite inferred from a repeated review-class label — and the calling agent caught
both outside the review loop. A reviewer that cannot compare a proposal with the existing obligation, test the supplied
evidence against its cause and scope, or state what the comparison is missing cannot catch either class.

## What Changes

- `.opencode/agent/review-quick.md` — the capture check is replaced by one that compares the proposed obligation with
  the existing one and screens duplication, evidence and scope, recurrence versus cause, preservation, ownership, and
  growth from explicitly supplied materials.
- `.opencode/agent/review-quick.md` — the hand-over names the capture materials a review needs.
- `AGENTS.md` — the review gate's run step supplies those materials for a capture.
- `showcase/quality/agent-skills` — the quick-review scenario carries the same outcomes in a `MODIFIED` block.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the "Per-change quality-gate and analysis subagents are available" requirement's
  quick-review scenario gains the capture comparison, the evidence, preservation, ownership, and growth checks, and the
  supplied-materials limitation; no new requirement.

## Impact

- Files: `.opencode/agent/review-quick.md`, `AGENTS.md` (one clause in the review gate's run step), and the change dir,
  with one `MODIFIED` requirement in `showcase/quality/agent-skills`.
- No code, build, dependency, deployment, or runtime change: subagent instructions, one caller-side clause, and one
  delta requirement.
