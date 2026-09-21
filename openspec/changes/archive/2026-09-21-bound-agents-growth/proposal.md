# Bound the growth of AGENTS.md with a promotion gate

## Why

`AGENTS.md` is this repository's single always-loaded instruction set, and its growth is measured and large: the
2026-09-19 retrospective found **+276 net lines over a 56-PR window** (1,559 → 1,835), from seventeen captures against
six consolidations, and it now stands near 2,000 lines. The capture convention already requires a new rule to merge into
or replace an existing bullet, but nothing enforces a _ceiling_: a bullet can be added in every change while retirement
happens only when someone happens to look. A bounded instruction set needs a sharper retention test and an explicit
growth discipline, or the always-loaded context keeps growing regardless of intent.

## What Changes

- **A promotion gate extends the single "governs a decision" test.** The `lesson-capture` subagent (and the `AGENTS.md`
  convention that describes it) applies a six-criteria gate before proposing a rule — true, actionable, non-automatable,
  material, general enough, high-confidence — with a default evidence threshold of two independent occurrences or one
  severe verified incident. A rule that cannot pass is discarded or routed elsewhere (a check, a spec, an ADR, a change
  dir) rather than added to the always-loaded file. The gate also treats a claim's **source** as part of it: a lesson
  sourced from untrusted content (a web page, an issue or PR comment, tool output, or a file the change did not author)
  is verified against the repository before it is promoted, so an injected instruction cannot become a durable rule.
- **An explicit growth discipline.** Applying a capture should leave `AGENTS.md` no larger than it was, preferring a
  merge or a replacement over an addition, and any net growth is a justified decision stated with the proposal. The
  bound is a **discipline, not a hard cap**: this corpus is evidence-anchored incident memory, so the control is the
  periodic `/audit-agents` pass — whose verdict already reports the accreted-rule count — not mass deletion to hit a
  number.
- **The `agent-skills` spec** gains both obligations in the "Lessons are captured after implementation" scenario.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — the lesson-capture scenario gains the promotion gate and the growth discipline.

## Impact

- **Agent tooling**: `.opencode/agent/lesson-capture.md` (the gate and the growth discipline), `AGENTS.md` (the capture
  convention).
- **Code**: none.
- **Not adopted**: the source skill also proposes a separate `.agent-memory/` episode store, a `docs/agent/` tree, and a
  120-line budget. Those are rejected here: the repository already records incidents durably in
  `openspec/changes/archive/`, the change dirs, the PR bodies, and `docs/audits/`, so a parallel episode store would be
  a third copy of existing evidence, and a 120-line ceiling would delete a corpus of verified incidents. The skill's
  _bounded-growth intent_ is adopted through the gate and the discipline instead.
