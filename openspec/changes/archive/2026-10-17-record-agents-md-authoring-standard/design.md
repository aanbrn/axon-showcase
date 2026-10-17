# Design

## Context

See `proposal.md` — Why. `AGENTS.md` is the always-loaded operational guide, and the two subagents that author and
reconcile it — `lesson-capture` (`.opencode/agent/lesson-capture.md`) and `agents-auditor`
(`.opencode/agent/agents-auditor.md`) — each already carry part of the authoring discipline: the capture's promotion
gate decides _whether_ a lesson is durable, and the auditor's conciseness classes decide _whether_ an entry is redundant
or misplaced. What is missing is the standard _between_ those: which representation an entry takes, the shape a
procedure or gotcha must have, what evidence a claim needs, and how an edit is validated. The recent shapings of
`AGENTS.md` (#610 `restructure-agents-md-workflows`, #611 `refactor-gotchas-section`, #612 `make-gotchas-operational`)
established the operational style in the file but recorded no standard anywhere — each touched only `AGENTS.md` and
`docs/review-rounds.tsv`, with no change dir and no spec edit.

## Goals / Non-Goals

**Goals:**

- State the authoring standard once, in the spec that owns the agent tooling, and bind the two `AGENTS.md`-specific
  subagents and the capture workflow to it.
- Keep it actionable: the definitions carry the distilled form that changes what they do; the full normative text stays
  in the spec, which a reader opens only when working the capability.

**Non-Goals:**

- A new auditor or a new report class: standard conformance is judged with the auditor's existing severities (folded
  into the consistency axis), so the shared report contract's verdict line is unchanged.
- A mechanical `check`: an authoring standard is not a lint at reasonable cost, which is what keeps it a rule rather
  than a gate.
- Duplicating the capture's promotion-gate criteria or the auditor's finding classes: where the capture requirement
  already applies a rule (a claim's source, extending an existing bullet), the standard states the general rule for
  every author and points at that site, rather than restating its criteria.

## Decisions

**The authoritative home is the `agent-skills` spec, not `AGENTS.md`.** The standard's normative text is long, and the
rule it states tells an author to keep one authoritative definition and move extensive reference material to supporting
documentation. Putting all of it in the always-loaded file would contradict the standard itself and duplicate what the
spec owns; instead the spec holds the standard, the two definitions carry the distilled, actionable form, and
`AGENTS.md` carries a pointer to it. Rejected: an `AGENTS.md` section (adds meta content to the always-loaded file and
duplicates the normative text) and a standalone `docs/` reference document (no precedent for a top-level reference doc,
and the `agent-skills` capability already owns the agent tooling).

**One ADDED requirement, not a second and not a fold-in.** The standard governs authoring beyond `lesson-capture` — the
main agent edits `AGENTS.md` directly, and the auditor judges entries — so it is not a refinement of the lesson-capture
requirement ("Lessons are captured after implementation"), and folding it there would hide the part that applies to the
auditor. A second requirement for its enforcement would split one subject across two permanent entries.

**The audit requirement keeps its `at least` coverage clause and is not MODIFIED.** "Project-owned agent tooling is
audited for consistency and conciseness" states its coverage as a minimum, so the new requirement can add the
standard-conformance check without contradicting it; restating the check in both requirements would duplicate one
subject. The check is reported with the existing `structural` severity, so the "Report-producing subagents follow a
shared report contract" requirement and its verdict line are untouched.

**The two definitions split the standard by role: `lesson-capture` authors under it, `agents-auditor` checks against
it.** Each keeps only what changes what it does — the capture's representation/shape/evidence/validation choices, the
auditor's conformance finding — and points at the spec for the normative text, matching the pointer discipline the
capture already follows for a rationale a spec owns.

**The standard speaks the file's own slot vocabulary.** Its procedure slots are the labels `AGENTS.md` already uses
(**Trigger**, **Procedure**, **Decision points**, **Failure handling**, **Completion criteria**) plus the two it adds
(**Prerequisites**, **Verification**), so an existing workflow is not reported non-conforming for a renamed slot. The
two added slots are stated "where applicable", so a workflow with no prerequisites or no separate verification is not
retroactively non-conforming.

## Risks / Trade-offs

- **A long spec requirement** → it is normative, so it belongs in the spec rather than the always-loaded file; the
  definitions hold the distilled form, and `AGENTS.md` carries only a pointer.
- **A definition edit is served from the pre-change copy until a reload or a new session** → the smoke-run of each
  edited definition (its behavior, not the standard's facts) is a precondition for the archive, per `AGENTS.md`'s
  subagent-reload gotcha.
- **Standard conformance is judgment, not a gate** → the auditor reports it as a finding with a concrete rewrite for the
  owner to approve; a false positive costs a review line, not a broken build.
- **The audit reports the README's and the spec Purpose's copies** → the README enumerates the audit's consistency
  sub-checks, and this change sweeps it; the capability's `## Purpose` enumerates what the capability holds, and the
  added standard is recorded as an explicit archive-commit task.

## Migration Plan

None: a spec addition plus two definitions, a command line, a `README.md` enumeration, and a prose pointer, taking
effect on the next capture or audit run. The `agent-skills` `## Purpose` refresh (a delta cannot carry a Purpose) lands
in the archive commit.

## Claims table

| Claim                                                                     | Source                                                                                                                                                                                           |
| ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The authoring standard is not recorded anywhere today                     | grep for its distinctive tokens (the representation taxonomy, "decision rule", "reference knowledge") over `AGENTS.md`, `openspec/specs/`, `docs/adr/`, and `docs/ideas.md` returns nothing      |
| #610–#612 shaped `AGENTS.md` but recorded no standard                     | `git show --stat 7c58b562 360ee554 5ed717ff` — only `AGENTS.md` and `docs/review-rounds.tsv`, no change dir, no spec edit                                                                        |
| Drift in the file is caught by the review loop, not pre-emptively         | the refactor units' review rounds in `docs/review-rounds.tsv` (`refactor-gotchas-section` 7 rounds, `restructure-agents-md-workflows` 3)                                                         |
| The capture and the audit already own parts of the discipline             | `AGENTS.md`'s "Capture lessons" workflow and `.opencode/agent/{lesson-capture,agents-auditor}.md`, each pointing at the `agent-skills` spec                                                      |
| The audit requirement's coverage clause is a minimum                      | `openspec/specs/showcase/quality/agent-skills/spec.md` — "The audit SHALL cover, at least:" (the "Project-owned agent tooling…" requirement)                                                     |
| The procedure's slots are already the file's convention                   | `AGENTS.md`'s named workflows (`**Trigger**`, `**Procedure**`, `**Decision points**`, `**Failure handling**`, `**Completion criteria**`), which the standard's Prerequisites/Verification extend |
| The README enumerates the audit's consistency sub-checks                  | `git show HEAD:README.md` — "consistency problems (contradictions, stale claims, dead cross-references, drift)" — which the added check makes stale                                              |
| The `agent-skills` `## Purpose` enumerates the capability's contents      | `openspec/specs/showcase/quality/agent-skills/spec.md` lines 3-16 (skills, subagents, the scheduled run, the standing unit)                                                                      |
| `lesson-capture` and `agents-auditor` are the `AGENTS.md`-specific agents | their definitions' scope (`.opencode/agent/*.md`) and the `agent-skills` requirements that own them                                                                                              |
| A definition edit is not exercised in-session                             | `AGENTS.md`'s subagent-reload gotcha (a changed definition body is served from the pre-change copy until a reload or a new session)                                                              |

**Dismissals** (scan candidates resolved to neither a row nor a fact, grouped by kind): absolute quantifiers in the
standard's normative text (`every`, `never`, `only`) and in the design's prose are modal wordings, not evidence claims;
counts that are task labels, the 120-character limit, or this unit's own appended `docs/review-rounds.tsv` row are not
facts about the repository; cross-reference candidates (`#610`, `#612`, `AGENTS.md`, `docs/review-rounds.tsv`,
`.opencode/agent/*.md`) point at the rows above; backticked prose, tool invocations, and slot labels
(`procedure/gotcha`, `representation/shape/evidence/validation`, `Prerequisites/Verification`, `scripts/claim-scan.py`,
`origin/main`) are not artifacts the change rests on.
