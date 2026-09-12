## Context

The `opsx-*` workflow keeps the main spec in sync with each change's delta at archive, and `openspec validate` checks
that specs and deltas are well-formed. Neither checks the corpus as a whole for structural consistency, and the
per-change review loop (`review-quick`, plus `/review-thorough` when invoked) checks a change's behavior against its
spec — not whether the specs are mutually consistent and well-structured. The `agents-auditor` subagent (added for
`AGENTS.md`) proved this class of gap is real: an un-gated document drifts (dead references, outgrown examples,
redundancy) even when every gate is green. The specs corpus is the other un-gated document — it grows with every
archived change (~22 files, ~157 requirements at the time of writing) and carries a live instance of the same drift (two
`#` titles that do not match their capability paths).

## Goals / Non-Goals

**Goals:**

- A repeatable, on-demand audit of `openspec/specs/` for structure and consistency.
- Findings verified against the repository and actionable — a spec/requirement location plus a concrete suggested
  rewrite.
- A judgment-aware duplicate check: report reused requirement headers, but distinguish "two genuinely different
  capabilities that share a name" from copy-paste residue, rather than auto-merging.
- A discoverable mechanism: the audit is surfaced in the README's Spec-Driven Development section, so a human reader
  meets it as part of how the specs are maintained rather than only in an agent table.

**Non-Goals:**

- **Behavior-vs-code drift** — verifying that each spec's scenarios hold against the implementation stays with the
  change workflow's review loop (`review-quick`, and `/review-thorough` when a deeper pass is wanted) and the
  archive-time sync; re-verifying every requirement against the codebase is unbounded and duplicates an existing gate.
- Scheduling or automation (a periodic GitHub workflow) — deferred and parked in `docs/ideas.md`, as for
  `agents-auditor`.
- Auditing the other docs (`AGENTS.md`, README, ADRs) — out of scope for this change; the `agents-auditor` owns
  `AGENTS.md`, and the auditor may surface cross-document inconsistencies it encounters; they are not its remit.
- Editing specs or deltas: the auditor reports; the main agent applies approved findings through the normal change
  workflow (a spec edit is a change, per the "sync the main spec only at archive" convention).

## Decisions

- **Pro model, temperature 0** (`opencode-go/deepseek-v4-pro`), matching `agents-auditor`, `review-thorough`, and
  `diagrammer`: a consistency audit is a careful, model-sensitive pass over a corpus, on demand.
- **Its own subagent, not a widening of `agents-auditor`.** Different artifact (the spec corpus vs. `AGENTS.md`) and a
  different property (structure vs. agent guidance); the `agents-auditor` remit is deliberately scoped to `AGENTS.md`.
- **Structure/consistency only, behavior excluded.** The design's sharpest boundary: this is what makes the audit
  bounded and non-duplicative. The agent definition states the exclusion explicitly.
- **Report, do not edit.** Mirrors `agents-auditor`/`experience-analyzer`: findings with suggested rewrites; the main
  agent verifies and applies the approved ones (via a change, since specs sync only at archive).
- **A command trigger.** Per the repo rule that a subagent is only invocable through a trigger.
- **Requirement placement.** A new requirement in `showcase/quality/agent-skills` (mirroring "AGENTS.md is audited for
  consistency and conciseness") rather than appending to the per-change-subagents requirement.

## Risks / Trade-offs

- **False positives on duplicate headers** — a header reused across genuinely different capabilities is legitimate (the
  command/query clients' `Business error translation`). The agent must read the bodies and report a reuse for judgment,
  not as a defect; the main agent and the user are the arbiters.
- **Scope creep into behavior** — the auditor could drift into checking scenarios against code, which the review loop
  owns. The definition excludes it explicitly.
- **Cost** — a pro-model pass over the corpus is not free, so the audit is on demand.
