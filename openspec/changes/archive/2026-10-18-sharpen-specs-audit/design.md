# Design

## Context

The `specs-auditor` (`.opencode/agent/specs-auditor.md`) audits `openspec/specs/` for structure and consistency. Its
behavior is owned by `showcase/quality/agent-skills`'s "The spec corpus is audited for structure and consistency"
requirement, which carries four scenarios. It was added by `add-specs-auditor-agent`; its report shape was set by
`concise-agent-reports`.

Two judgments its method under-specifies are the ones it makes most, and both produce false positives:

- **A dead reference.** The current axis is one sentence ("classes, files, config keys, or commands the specs name that
  no longer exist or have been renamed"). The change makes the judgment evidence-based and names the false-positive
  classes the audit must not report — the definition carries the full set.
- **A duplicate.** The current axis flags a reuse "for judgment" with two examples; it does not distinguish a
  substantially identical obligation from a materially ambiguous overlap, nor a related-but-distinct requirement from an
  intentional repetition, and it does not require the verify-before-consolidating steps.

Its boundaries are also implicit: the corpus's capability decomposition belongs to the `architecture-auditor`, an issue
an existing gate enforces is not the auditor's to re-report, and the internal quality of a proposed change or change
delta is the review loop's — none of the three is stated.

## Goals / Non-Goals

**Goals** — sharpen the spec-corpus audit's method within its existing scope: make a dead-reference judgment
evidence-based and a duplication judgment semantic, and make the boundaries and the correction guidance explicit.

**Non-Goals** — no scope expansion (no behavior verification, no implementation review, no change-delta audit, no
capability-decomposition audit), no new report category or severity level, and no rename of the agent.

## Decisions

**The definition carries the operational form; the spec the outcome.** The definition names the false-positive classes,
the overlap taxonomy, the staged method, and the correction guidance in its own words; the spec states the obligations
and their scenarios.

**The report contract keeps the shared contract's first line, not the external spec's narrower one.** The task spec pins
the first line to "exactly `<n> findings`". The shared report contract (`showcase/quality/agent-skills`, "A report opens
with its verdict") requires the first line to name the count of every class the report carries, so the change keeps the
total plus the per-group counts and does not adopt the narrower pin — a definition edit may not narrow a contract its
own capability states.

**The three boundary exclusions are stated, not implied.** The corpus's capability decomposition is the
`architecture-auditor`'s (its requirement audits the spec corpus's capability decomposition); an issue an existing gate
enforces — `openspec validate`'s well-formedness, the review loop's behavior check — is not the auditor's to re-report;
and the internal quality of a proposed change or change delta is the review loop's. All three are recorded so a future
widening cannot silently absorb them.

**The staged method mirrors the architecture-auditor's.** Inventory → local structure → cross-spec consistency →
reference verification → report validation, reusing earlier evidence. It bounds the pass without reducing verification
quality, and it keeps the reference stage from becoming an implementation audit.

**A `## Purpose` refresh is not owed.** The Purpose names the on-demand auditors "of the `openspec/specs/` corpus"; the
change sharpens the method within that scope and adds no capability to it, so the Purpose's own text is not falsified.

## Risks / Trade-offs

- **A judgment, not a gate** → a misclassified duplication or an over-strict dead reference costs a report line, not a
  broken build; the audit stays read-only and every item is the owner's decision.
- **The dead-reference exclusions could under-report** → a reference that is genuinely stale but resembles a
  false-positive class is missed; the search-and-context steps bound that, and a real staleness in a generated or
  conceptual reference is rare.
- **A definition edit is served from the pre-change copy until a reload or a new session** → the smoke-run of the edited
  definition is a precondition for the archive, per `AGENTS.md`'s subagent-reload gotcha.

## Migration Plan

None: the change takes effect on the next audit run — the surfaces are the proposal's Impact. No `## Purpose` refresh is
owed, so the implementing PR's diff carries no main-spec edit beyond the delta and the archive commit carries only the
delta→main sync.

## Claims table

| Claim                                                                                                   | Source                                                                                                                                                 |
| ------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The dead-reference axis is one sentence naming no false-positive class                                  | `git show HEAD:.opencode/agent/specs-auditor.md` (the "Dead cross-references" axis)                                                                    |
| The duplication axis flags a reuse "for judgment" with two examples and no taxonomy                     | `git show HEAD:.opencode/agent/specs-auditor.md` (the "Cross-spec duplication" axis)                                                                   |
| The corpus's capability decomposition belongs to the `architecture-auditor`                             | the main spec's architecture requirement (it audits the "spec corpus's capability decomposition")                                                      |
| The shared report contract requires the first line to name every class the report carries               | the main spec's report-contract requirement ("A report opens with its verdict")                                                                        |
| The requirement carries four scenarios                                                                  | the main spec's spec-corpus requirement                                                                                                                |
| The auditor is described in `AGENTS.md`, `README.md`, and its command                                   | `git show HEAD:AGENTS.md` (the `specs-auditor` bullet), `git show HEAD:README.md` (the audit prose), `git show HEAD:.opencode/commands/audit-specs.md` |
| The auditor was added by `add-specs-auditor-agent`; its report shape was set by `concise-agent-reports` | `openspec/changes/archive/2026-09-12-add-specs-auditor-agent/`, `openspec/changes/archive/2026-09-15-concise-agent-reports/`                           |
| An issue an existing gate enforces is not the auditor's to re-report                                    | the main spec's requirement (the `openspec validate` well-formedness and the review loop's behavior check are existing gates)                          |
| The internal quality of a proposed change or change delta is the review loop's                          | the main spec's requirement (the review loop's remit)                                                                                                  |
| The external task spec pins the first line to "exactly `<n> findings`"                                  | the external task spec this change implements — not a repository artifact, recorded as the first Decision's premise                                    |

**Dismissals** (scan candidates resolved to neither a row nor a fact, grouped by kind, one reason per kind): **absolute
quantifiers and counts** are modal or structural wordings, not facts about the repository; **the `spec.md` delta's
copied requirement bodies and retained scenarios** are the main spec's text, so their quantifiers and cross-references
are pre-existing, not claims this unit states; **paths, commands, and cross-references** are name, path, or command
references rather than claims about the repository; **the `docs/review-rounds.tsv` candidates** are this unit's own
appended record rows, a round number and a class label in a one-line record, not a claim.
