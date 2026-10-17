# Design

## Context

The `lesson-capture` subagent already decides _whether_ a lesson is durable (its promotion gate), _where an entry
belongs_ (the existing-bullet rule), and _what the unit retires_. What it does not do is diagnose the lesson's
**origin**: its gate routes a rule that fails the gate to a check, a spec, an ADR, or the change dir, which settles
whether to keep the rule in the file — not whether the fix's home is the mechanism that _produced_ the artifact a lesson
is about.

The repository already makes that choice by hand: `docs/ideas.md` parks two items in the `lesson-capture` definition
rather than in `AGENTS.md` — the excluded-incident confirmation and the rejected-proposal record — and
`establish-structured-writing-standard` fixed the drafting mechanisms (the `openspec/config.yaml` rules, the
`review-quick` class list, a spec requirement) rather than only restating the standard as prose. No recorded incident
has the capture mis-routing a mechanism-caused lesson into `AGENTS.md`; the change rests on the practice and the missing
step, and the Risk below records that.

## Goals / Non-Goals

**Goals:**

- Add one step before the capture proposes an entry: trace the lesson to its origin, and where the cause is a mechanism
  that produces an artifact, propose the correction there and report it as an authoring-system recommendation.
- Carry that recommendation in its own report group with its own verdict count, so the main agent routes it rather than
  applying it as an entry.

**Non-Goals:**

- No new subagent, auditor, or report class beyond the one group; the capture keeps its existing report shape otherwise.
- No change to the promotion gate's existing routing list. That clause's subject is a **rule that fails the gate**; the
  new obligation's subject is a **lesson whose cause is a mechanism**. The triggers differ, so the lists are kept
  separate rather than merged.
- No mechanical check: a capture's origin judgment is not a lint at reasonable cost.

## Decisions

**A definition + spec change, not an `AGENTS.md` addition.** The behavior belongs to the subagent, and the rule it
implements is agent-tooling meta, so it lives in `.opencode/agent/lesson-capture.md` with the normative text in the
`agent-skills` spec — the routing the authoring standard already states (the always-loaded file carries what a reader
needs to act; a spec carries the rest). Rejected: an `AGENTS.md` capture bullet (adds meta content to the always-loaded
file for a rule the subagent must read anyway).

**One `MODIFIED` requirement, adding one clause and one scenario.** The obligation refines "Per-change quality-gate and
analysis subagents are available", which already owns the capture's contract; a new requirement would split one subject
across two permanent entries, and folding it into the capture scenario alone would hide it from a reader scanning the
scenarios. The scenario makes the behavior independently identifiable; the clause keeps the capture's obligation list
complete.

**The verdict line gains the count because the shared report contract requires it.** "Report-producing subagents follow
a shared report contract" requires the first line to name the count of every class the report carries, so a report that
gains a group must name it — the same adjustment an auditor's verdict line makes when it gains an advisory class. The
net `AGENTS.md` delta is unaffected: an authoring-system recommendation changes no `AGENTS.md` line.

**Sweep the artifacts that enumerate the capture's output.** The definition's own frontmatter `description`,
`AGENTS.md`'s capture step 1, `README.md`'s agent table row, its "Lesson capture" bullet, and its Self-Learning Loop
prose, and `openspec/config.yaml`'s capture-task rule each describe what the capture produces, so each would be a stale
enumeration once the group exists; each is updated in this change.

**Apply the capture's merges; park its authoring-system recommendation.** This unit's own `lesson-capture` run returned
two additions merging into the review gate's step-2 bullet (a row's source states a reason by class; the table is
re-derived after the unit's last record append) and one authoring-system recommendation — a `proposal`-rule requirement
that a change derive its touched sites from its tasks. The merges are applied here (both extend existing clauses, net +3
`AGENTS.md` lines); the delta's scan clause carries the widened discipline, since the `AGENTS.md` sentence names
`agent-skills` as its owner; and the `openspec/config.yaml` `design` rule that restates the claims-table obligation is
synced. The recommendation is parked in `docs/ideas.md`, its fix belonging in the `proposal` rules rather than in
another `AGENTS.md` sentence, and it is not this change's subject.

## Risks / Trade-offs

- **A judgment, not a gate** → a false authoring-system recommendation costs the owner a review line, not a broken
  build; the capture still reports additions and retirements as before.
- **No recorded mis-routing incident** → this is a rule the repo's practice supports but no failure evidences. If the
  owner judges it speculative, the alternative is to park it in `docs/ideas.md` until an incident surfaces; the change
  states the practice that motivates it in Context rather than over-claiming an incident.
- **A definition edit is served from the pre-change copy until a reload or a new session** → the smoke-run of the edited
  definition (task 3.3) is a precondition for the archive, per `AGENTS.md`'s subagent-reload gotcha.

## Migration Plan

None: the change takes effect on the next capture run — the surfaces are the proposal's Impact. No `## Purpose` refresh
is owed — the capability's Purpose names the capture agent but not its output kinds, and this change does not alter what
the capability holds.

## Claims table

| Claim                                                                       | Source                                                                                                                                                                                                                                                                    |
| --------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The capture's gate routes a failed-gate rule, with no origin-diagnosis step | `git show HEAD:.opencode/agent/lesson-capture.md` (the promotion-gate paragraph) and `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` (the "Lessons are captured…" clause)                                                                            |
| No artifact routes a mechanism-caused lesson to its mechanism               | `git grep authoring-system HEAD` (the pre-change tree) returns no match; the current tree matches only in the files this change edits                                                                                                                                     |
| No recorded incident of the capture mis-routing a mechanism-caused lesson   | `grep -rniE "mis-?rout\|wrong home" .` (excluding `.git`, `.gradle`, and this change dir) returns no hit about the capture's own routing                                                                                                                                  |
| The repo routes a fix to the definition/mechanism by hand                   | `docs/ideas.md` — the excluded-incident entry ("It routes to the `lesson-capture` definition rather than `AGENTS.md`") and the rejected-proposal entry ("Routes to the `lesson-capture` definition plus the retrospective's digest")                                      |
| A prior change fixed drafting mechanisms, not only prose                    | `openspec/changes/archive/2026-10-17-establish-structured-writing-standard/tasks.md` — the `config.yaml` rules, the `review-quick` class list, and a spec requirement                                                                                                     |
| The capture's verdict format is fixed by the definition and the spec        | `git show HEAD:.opencode/agent/lesson-capture.md` (the report-contract verdict bullet); the main spec's capture verdict clause                                                                                                                                            |
| The shared report contract requires naming every class                      | the `agent-skills` spec's "Report-producing subagents follow a shared report contract" requirement                                                                                                                                                                        |
| The describing artifacts enumerate the capture's output                     | `git show HEAD:.opencode/agent/lesson-capture.md` (frontmatter `description`), `git show HEAD:AGENTS.md` (capture step 1), `git show HEAD:README.md` (the table row, the "Lesson capture" bullet, and the Self-Learning Loop prose), `git show HEAD:openspec/config.yaml` |
| The capture's own run returned two merges and one recommendation            | `tasks.md`'s capture task (3.4) records the run's verdict; the merges are the review-gate step-2 clauses, the recommendation the parked `docs/ideas.md` entry                                                                                                             |
| `openspec/config.yaml`'s `design` rule restates the claims-table obligation | `git show HEAD:openspec/config.yaml` (the `design` rule's claims-table clause), which the widened `AGENTS.md` clauses make stale                                                                                                                                          |
| `AGENTS.md`'s claims-table sentence names `agent-skills` as its owner       | `git show HEAD:AGENTS.md` (the scan sentence's "`showcase/quality/agent-skills` owns the behaviour" parenthetical), so the widened discipline is mirrored in the delta                                                                                                    |
| A definition edit is not exercised until it is reloaded                     | `AGENTS.md`'s subagent-reload gotcha                                                                                                                                                                                                                                      |
| No `## Purpose` refresh is owed                                             | the `agent-skills` spec's `## Purpose` names the capture agent but not its output kinds                                                                                                                                                                                   |

**Dismissals** (scan candidates resolved to neither a row nor a fact, grouped by kind, one reason per kind): **absolute
quantifiers and counts** are modal or structural wordings, not facts about the repository; **the `spec.md` delta's
block** is the main spec's requirement copied verbatim, so its quantifiers and cross-references are the pre-existing
spec's text, not claims this unit states; **paths, commands, and cross-references** are name, path, or command
references rather than claims about the repository; **the `README.md` table rows other than `lesson-capture`** appear in
the diff only because widen that cell re-padded the whole table (a formatter-owned reflow), and their text is unchanged;
**the `docs/review-rounds.tsv` candidates** are this unit's own appended record rows — a round number and a class label
in a one-line record, not a claim — together with the change's prose references to them.
