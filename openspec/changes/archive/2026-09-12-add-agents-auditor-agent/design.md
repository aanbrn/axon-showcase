## Context

`AGENTS.md` grows monotonically: every change's `lesson-capture` pass appends conventions and gotchas, and entries are
rarely removed (a superseded rule is usually left beside its replacement). At roughly a thousand lines it is large
enough that the cheap flash main agent does not hold all of it in view at once, so cross-entry problems — a rule
contradicted elsewhere, an enumeration the repository has outgrown, a cross-reference to a renamed file or command — go
unnoticed. The existing review agents do not cover this: `review-quick` and `review-thorough` review a change against
its planning artifacts, `experience-analyzer` aggregates a time window of experience. The gap is a whole-artifact audit
of the guidance document itself.

## Goals / Non-Goals

**Goals:**

- A repeatable, on-demand thorough audit of `AGENTS.md` for consistency and conciseness.
- Findings verified against the repository (not re-derived from the prose) and actionable — a location plus a concrete
  suggested rewrite.
- No edits by the auditor: the main agent applies what the user approves, under the normal review gate.
- The audit is surfaced in the README's self-learning narrative as the memory's maintenance (accretion plus
  reconciliation), so a human reader meets it as part of the loop rather than only in an agent table.

**Non-Goals:**

- Scheduling or automation (a periodic GitHub workflow) — deferred and parked in `docs/ideas.md`.
- Auditing the other docs (README, ADRs, specs) — out of scope for this change; the auditor may surface cross-document
  inconsistencies it encounters, but they are not its remit.

## Decisions

- **Pro model, temperature 0** (`opencode-go/deepseek-v4-pro`), matching `review-thorough` and `diagrammer`: a
  consistency audit is a careful, model-sensitive pass over a long document, and it is an on-demand (not per-change)
  cost.
- **Two axes, verified.** Consistency and conciseness, with every claim treated as a hypothesis to check against the
  repository — the repo's own "doc claims must match their source and their strength" convention.
- **Report, do not edit.** Mirrors `experience-analyzer` and `review-thorough`: the subagent returns findings with
  suggested rewrites; the main agent verifies and applies approved changes (and runs the review gate on any resulting
  edit).
- **A command trigger.** Per the repo rule that a subagent is only invocable through a trigger, not its own
  documentation.
- **Requirement placement.** A new requirement in `showcase/quality/agent-skills` (mirroring the existing "Experience
  analysis is available to agents" requirement) rather than appending `agents-auditor` to "Per-change quality-gate and
  analysis subagents are available" — the audit is neither per-change nor a quality gate.

## Risks / Trade-offs

- **False positives** — flagging a deliberate asymmetry or an intentional repetition as a defect. The subagent must
  verify intent (grep for consumers, read the referenced files) before proposing a "fix", and the main agent and the
  user remain the arbiters; the agent definition states this explicitly.
- **Cost** — a pro-model pass over a long document is not free, so the audit stays on-demand (the periodic variant is a
  deferred idea, not part of this change).
- **Scope creep** — the auditor could drift into re-deriving or rewriting conventions rather than auditing the existing
  text; the definition constrains it to findings against the current content plus suggested edits.
