# Design

## Context

The `experience-analyzer` aggregates a window of merged work into a retrospective and a classified suggestion list. Its
contract names two outputs — a retrospective (shipped PRs by theme, lessons, went-well/went-wrong) and suggestions
classified `system`/`process` — and the practice already exceeds it: the retrospectives from 2026-09-28 onward open with
a "The previous retrospective's recommendations" table and close with a "Recommended next direction". What is missing is
the middle: no contract clause traces a failure to its cause and the stage that could have caught it, evaluates whether
the previous window's recommendations worked, or states an expected outcome a later window can verify.

The digest already hands over the evidence: the derived-facts block carries the window's review rounds, the classes
raised, and the compliance-miss count, plus the previous retrospective's file. The change uses what the digest already
resolves rather than adding a new gather.

## Goals / Non-Goals

**Goals:**

- Trace each significant failure to a root cause and the earliest stage that could have prevented or detected it.
- Evaluate the previous retrospective's recommendations when their outcomes are observable.
- Give each suggestion a targeted pattern, an expected observable outcome, and its verification.

**Non-Goals:**

- No new subagent, no new gather, and no change to the capture or the auditor.
- No restatement of where an improvement belongs or whether it warrants an instruction at all: the per-unit capture's
  promotion gate and the auditor's route candidates own those, so the definition points at them.
- No priority scale and no fixed multi-section report format: the repo's retrospective is a stored docs file with a
  disposition practice, and a second vocabulary would be ceremony.
- No scheduled run: the retrospective stays on demand (its richest input is session-only).

## Decisions

**One `MODIFIED` requirement, one body clause and three scenarios.** The three obligations refine the requirement that
already owns the retrospective's content, so a new requirement would split one subject across two permanent entries.
Each obligation gets a scenario because each is independently observable; the body states them together so the
requirement reads as one contract.

**The three obligations draw on the digest, not a new gather.** The derived-facts block already carries the review-round
data and the previous retrospective's file, so the root-cause step and the previous-recommendation evaluation are read
from it — the same discipline the requirement already imposes for the countable claims.

**The definition carries the operational form; the spec the outcome.** The definition names the three in its own words
and points at the capture/auditor for where an improvement belongs and whether it warrants an instruction at all; the
spec states the obligations and their scenarios.

**The describing sites are swept.** `AGENTS.md`'s bullet, `README.md`'s prose bullets, its two table rows (the agent
table and the slash-command table), and the `/retrospective` command each enumerate what the analyzer produces, so each
is updated. No `## Purpose` refresh is owed: the capability's Purpose names the `experience-analyzer` but not what it
produces.

**Apply the capture's merges.** This unit's own `lesson-capture` run returned three additions merging into the pre-write
pass's step 1 and the "recurring unit type" bullet (proposed net +6 `AGENTS.md` lines, applied +5), with the first two
mirrored into `openspec/config.yaml`'s `proposal` and `design` rules; it returned no retirements and no authoring-system
recommendation.

## Risks / Trade-offs

- **A judgment, not a gate** → a root cause or a verification plan that later proves wrong costs a retrospective line,
  not a broken build; the obligations bound the retrospective's content, not its correctness.
- **More structure in the retrospective** → the three obligations add a paragraph per significant failure and a field
  per suggestion; the report stays proportionate because "significant" is the analyzer's own filter, as the existing
  contract already states.
- **A definition edit is served from the pre-change copy until a reload or a new session** → the smoke-run of the edited
  definition is a precondition for the archive, per `AGENTS.md`'s subagent-reload gotcha.

## Migration Plan

None: the change takes effect on the next `/retrospective` run — the surfaces are the proposal's Impact. No `## Purpose`
refresh is owed, so the implementing PR's diff carries no main-spec edit beyond the delta.

## Claims table

| Claim                                                                                   | Source                                                                                                                                                                          |
| --------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The retrospective's contract names no root-cause, previous-evaluation, or outcome       | `git show HEAD:.opencode/agent/experience-analyzer.md` (the two-section output) and the main spec's experience-analysis requirement                                             |
| The practice already evaluates the previous retrospective's recommendations             | `docs/retrospectives/2026-10-15.md` — the "The previous retrospective's recommendations" table with dispositions, which the contract does not require                           |
| The digest already carries the evidence for both obligations                            | the main spec's derived-facts clause (the review rounds, classes raised, compliance misses, and the previous retrospective's file)                                              |
| Where an improvement belongs and whether it warrants an instruction are owned elsewhere | the main spec's capture promotion-gate clause (the gate and its routing) and the auditor's route-candidate clause                                                               |
| The capture's own run returned three merges and no retirement                           | `tasks.md`'s capture task (3.4) records the run's verdict, and the applied net delta is `git diff -U0 AGENTS.md`'s two hunks                                                    |
| The analyzer is described in `AGENTS.md`, `README.md`, and its command                  | `git show HEAD:AGENTS.md` (the `experience-analyzer` bullet), `git show HEAD:README.md` (the prose and the two table rows), `git show HEAD:.opencode/commands/retrospective.md` |
| No `## Purpose` refresh is owed                                                         | the `agent-skills` spec's `## Purpose` names the `experience-analyzer` but not what it produces                                                                                 |

**Dismissals** (scan candidates resolved to neither a row nor a fact, grouped by kind, one reason per kind): **absolute
quantifiers and counts** are modal or structural wordings, not facts about the repository; **the `spec.md` delta's
block** is the main spec's requirement copied verbatim, so its quantifiers and cross-references are the pre-existing
spec's text, not claims this unit states; **paths, commands, and cross-references** are name, path, or command
references rather than claims about the repository; **the `docs/review-rounds.tsv` candidates** are this unit's own
appended record rows, a round number and a class label in a one-line record, not a claim.
