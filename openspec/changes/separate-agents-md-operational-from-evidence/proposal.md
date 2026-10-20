# Proposal

## Why

`AGENTS.md` is 338,964 bytes over 3,463 lines and loads in full into every session. Its `## Gotchas` (139,963 bytes) are
written as incident memory: a rule, the episode that produced it, and the action it requires. The previous unit retired
material whose subject a task narrows; it left every globally binding rule in place. This unit asks whether a rule's
operational text can stay at root while its episode moves to a record that keeps it recoverable — measured across all 30
rules of two Gotchas subsections, with seven converted rules drafted end to end and the rest dispositioned.

## What Changes

- **`AGENTS.md`** — two capture corrections ride this unit: the "point-in-time narrative belongs in a change's archived
  artifacts" sentence becomes "a dated record" (which the new record is), and the extraction workflow's gate decision is
  decided per item rather than per block.
- **`AGENTS.md`** — seven rules are rewritten to their operational text (D1–D5, R1, R2) and each carries an
  `evidence: <record-stem>` token naming its record. The token, not a subsection pointer, is what identifies a converted
  rule, so a deleted entry or a renamed rule is a finding rather than silence.
- **`docs/guidance-evidence/git-and-repository-workflow.md`** (new, 1,822 bytes, three sections) — one `Rule:`-keyed
  section per converted rule: the episode, its historical identifiers, and the provenance markers.
- **`docs/guidance-evidence/documentation-and-durable-claims.md`** (new, 5,747 bytes, four sections) — the same for the
  second subsection, including the upstream-reference episode and its closure-tracking statement; the per-reference
  inventory is planning evidence in `design.md`.
- **`.opencode/agent/agents-auditor.md`** — its scope line gains `docs/guidance-evidence/` and its checks gain the
  pairing test, so the delta's obligation is carried by the definition that runs the audit.
- **`openspec/specs/showcase/quality/agent-skills`** (delta) — one `MODIFIED` audit-scope requirement plus four `ADDED`
  requirements: the record is scoped and owned, a section states the episode and not a second rule, the root entry keeps
  every operational element and its token, and a historical identifier survives in the record. No preservation or
  removal clause changes.
- `openspec/changes/separate-agents-md-operational-from-evidence/` — this proposal, the design (baseline, census, all-30
  dispositions, the eight drafted rewrites, the two record drafts, the destination and pairing design, the upstream
  inventory, the policy reassessment, four alternatives, and the claims table), and the tasks.
- **Not in this unit:** R3's conversion (drafted, net −69 B), the other 23 rules, the duplicated-word fix beyond D4's
  own line, the reviewer-contract improvement, the audit-trending tool, and any stage-specific workflow redesign.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — it owns where agent guidance lives, what an `AGENTS.md` entry must carry, and which
  project-owned artifacts the audit covers. This change adds four requirements there and modifies one: a relocated
  evidence record is scoped, owned, `Rule:`-paired and audited, the root text keeps every operational element and its
  token, and a historical identifier survives in the record. The audit's own definition is edited in the same unit, so
  the obligation and the mechanism that carries it move together.

## Impact

- `AGENTS.md`: the **seven-rule conversion subtotal is −4,931 bytes (1.45%)**, measured, with the 307 bytes of
  `evidence:` tokens inside it. The **whole unit is −4,660 bytes (1.37%) provisional**, because the two capture
  corrections add 271 bytes and re-wrap two paragraphs; the final measurement runs after `spotlessApply`.
- Additional routinely loaded material: **0** beyond those tokens — no per-subsection pointer, no skill description, no
  `instructions` entry.
- Records: **+7,569 bytes** (the two drafted files). **Net total maintained content: +2,638 bytes for the conversion,
  +2,909 provisional for the unit**, because a record section carries its address, its `Rule:` key, and the context that
  makes the episode readable, so the records are larger than the root text they absorb.
- New owned surface: two files under `docs/guidance-evidence/`, and the `agents-auditor.md` scope line and checks that
  audit them.
- No code, build, dependency, API, or deployment impact. No gate reads the pilot spans; every draft keeps its
  `captured:` marker.
- Four of the five upstream references R2 moves stay in files `upstreamReferences` already scans; `micromatch/braces#70`
  leaves that set deliberately, because it is locked, its outcome is recorded, and it backs no live condition. No
  scanner change.
- `docs/review-rounds.tsv` gains this unit's review rows.
