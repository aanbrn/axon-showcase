# Design — single-source the report contract's per-item budget

## Context

`showcase/quality/agent-skills` already owns the rule: the shared report contract's _Each item is budgeted_ scenario
requires every item — "a proposed addition, a finding, or an observation" — to name itself, its anchor, and one line of
evidence. This change states no new obligation; it makes the sites that describe a report's per-item format stop
restating that field set and reference the contract by name.

**Premise verification** (the seven definitions and the requirements read at `HEAD`). The shared contract names its
bearers — "the per-unit review and lesson-capture agents and the on-demand auditors of the agent tooling, the spec
corpus, the architecture, and the README" — and its item classes. Deriving `bearer × item-class` is what found the full
set; deriving it from the artifacts in hand is what the first five attempts got wrong.

| Definition                                          | Per-item budget at `HEAD`    | Report format elsewhere                                            |
| --------------------------------------------------- | ---------------------------- | ------------------------------------------------------------------ |
| `review-quick`, `lesson-capture`                    | named "one line of evidence" | —                                                                  |
| `architecture-auditor`                              | omitted it                   | Findings section: "each with a location and evidence"              |
| `review-thorough`                                   | omitted it                   | "every finding … cites repository evidence and a precise location" |
| `readme-auditor`, `specs-auditor`, `agents-auditor` | omitted it                   | none                                                               |

**Why the shape changed.** The first draft added the evidence line at each site that enumerates an item's fields. Six
quick-review rounds each found another such site — round 1 the four "…audit is produced" scenarios; round 2 the
design-intent scenarios and the README coverage scenario; round 3 the advisory and authoring-system-gap lists; round 4
the `lesson-capture` path, a bearer with no audit that a sweep of the auditors' budgets cannot see; round 5 three
definition method clauses; round 6 two more in the same definition. Each round falsified a different premise — the
method (a field-list grep is a lead, not a completeness gate), the boundary, the universe, and finally the _unit_: ~24
requirement sites and ~9 definition sites each restating one field set, so no sweep can prove the set complete. The
restatement is the drift surface, and the fix is the repository's own single-sourcing rule: state the field set once, in
the contract that owns it, and reference it by name everywhere else.

## Goals / Non-Goals

**Goals**

- One source for a report item's fields: the shared report contract's _Each item is budgeted_.
- Every definition and every requirement that describes a report's per-item format delegates to it by name, keeping only
  the fields its own class adds.
- No site's enumeration reads as complete while omitting a field the contract requires.

**Non-Goals**

- Changing the shared contract — it already owns the rule and its scenario is the field set's single source.
- Adding a scenario to any of the eight requirements: the pointer is the contract's obligation, and its own scenario
  covers it.
- Restating the contract's fields at any shipped site outside the Boundary (`design.md` itself states the field set to
  explain the change, and `tasks.md`'s tasks quote it; those are records of the change, not shipped guidance).
- Changing any auditor's scope, finding classes, verdict line, or the instruction-budget ledger.

## Decisions

- **Single-source the field set in the shared contract; every other site references it by name.** _Alternatives:_ add
  the evidence line at each per-item site (rejected — six review rounds showed the restatement is the drift surface and
  the set cannot be proven complete); add it only where the definition's report format states it nowhere (rejected —
  leaves the budget lines inconsistent with their own definitions); leave the contract as the only statement and change
  nothing (rejected — the operative per-item summaries read as complete without it).
- **Give the delta the complete set of eight requirements, each gaining one pointer sentence, with the per-item
  sentences and the scenario counts unchanged.** _Alternatives:_ restate the evidence line in each requirement
  (rejected, as above); add a scenario per requirement (rejected — the contract's own scenario is that obligation's
  coverage).
- **Revert the per-class evidence phrases the first draft added to the definitions**, so each definition's report
  contract states the delegation once and the class clauses carry only class-specific fields. _Alternatives:_ keep them
  and add a delegation clause (rejected — the per-class restatements are exactly the drift surface).
- **Keep the two `README.md` prose sites naming the evidence line.** _Alternatives:_ leave them untouched (rejected —
  they state a per-item format and read as complete); point them at the contract (rejected as unnecessary — they are
  prose for humans, and naming the evidence does not enumerate the field set).
- **Point the five commands' presentation steps and the `AGENTS.md` bullet at the contract rather than naming the
  evidence field.** _Alternatives:_ keep naming it (rejected — round 7 showed a contract-owned field named outside the
  contract is the same single-sourcing gap the strategy change removed); drop the clause (rejected — the presentation
  step is where a calling agent re-summarizes an item, so it must say to keep the contract's fields).
- **Turn the `AGENTS.md` `review-thorough` bullet's parenthetical into a pointer.** _Alternatives:_ extend its
  enumeration (rejected — a second copy of the field list that drifts).

**Boundary** — the sites that still name a contract-owned field, and why they stay. The definitions' method clauses name
_which_ location an item's anchor points at (a spec path, an ADR file, a `file:line`) — a class-specific referent, not
the field set. The two `README.md` prose sites name the evidence line for a human reader, per the Decisions. The
**requirements'** retained per-item sentences name the location, and the per-change requirement's review scenarios name
the evidence line ("each finding names its class, its location, the issue, one line of evidence, and the required
correction"; "cites repository evidence and a precise location") — kept per the Decisions, since the pointer sentence
delegates the field set and each sentence states that audit's own coverage. The five commands and the `AGENTS.md` bullet
delegate by name rather than naming the field. The shared contract's own scenario is the field set's single source.

## Risks / Trade-offs

- **A reader of a single definition must follow the reference to the contract.** Mitigation: each reference names the
  contract ("the shared report contract's per-item budget"); `readme-auditor.md` additionally names its capability path
  and _Each item is budgeted_ scenario, and every definition's class clauses keep their own fields, so a reader loses
  nothing the class itself owns.
- **The pointer sentences repeat the phrase "the shared report contract's per-item budget" eight times in the spec.**
  Mitigation: a reference by name is the sanctioned repetition; the field set itself is stated once.
- **The smoke-run now covers seven definitions, which is seven subagent invocations.** Mitigation: one run each, with
  the same two controls.

## Claims table

Every fact this change states about its evidence, with the source or the command that derives it. Rows about an artifact
the change edits anchor to that artifact's pre-change state.

| Claim                                                                                                                                                                            | Source / deriving command                                                                                                                                            |
| -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The shared contract requires each item to name itself, its anchor, and one line of evidence                                                                                      | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` — "Report-producing subagents follow a shared report contract", _Each item is budgeted_         |
| The contract's bearer list is "the per-unit review and lesson-capture agents and the on-demand auditors of the agent tooling, the spec corpus, the architecture, and the README" | same file, the same requirement                                                                                                                                      |
| `review-quick` and `lesson-capture` name the evidence line in their budget; the other five budgets omit it                                                                       | `git show HEAD:.opencode/agent/{review-quick,lesson-capture,…}.md` — each `Budget each …` line                                                                       |
| `architecture-auditor`'s Findings section and `review-thorough`'s "every finding … cites repository evidence" state it outside their budgets                                     | `git show HEAD:.opencode/agent/{architecture-auditor,review-thorough}.md`                                                                                            |
| The eight requirements are the complete set of per-item report formats for the seven bearers                                                                                     | reading each requirement's description **and** scenarios in `git show HEAD:…spec.md`, derived as `bearer × item-class`; the field-list pattern is a lead, not a gate |
| `review-thorough`'s requirement already requires a finding to cite repository evidence                                                                                           | same file — _Thorough review is available on demand_                                                                                                                 |
| Each delta block is the requirement plus one pointer sentence, with no scenario added                                                                                            | a block-diff against `git show HEAD:…spec.md` — 8 blocks, per-block scenario counts 18, 10, 29, 10, 9, 5, 11, 6, total 98                                            |
| The main spec holds 14 requirements; the delta carries 8 blocks                                                                                                                  | `grep -c "^### Requirement:"` over each file                                                                                                                         |
| No definition restates the contract's field set after the change                                                                                                                 | `grep -rn "line of evidence" .opencode/agent/*.md` returns no per-class restatement                                                                                  |
| `README.md`'s agent-table and slash-command rows name what each subagent audits; its two prose sites state a per-item format                                                     | `git show HEAD:README.md` — the two prose sites and the two row sets                                                                                                 |
| `openspec/config.yaml`'s rules carry no report-contract clause                                                                                                                   | `grep -n -i "report\|evidence\|contract" openspec/config.yaml` — its hits are the claims-table and task-evidence rules                                               |

**Scan dismissals** (`scripts/claim-scan.py --base origin/main`), each by class — the scan reads a new file's every line
as added, and its own artifacts are candidates, so the split is a property of the scan rather than a fixed figure:

- **The delta's copied requirement blocks** — pre-existing spec text, verified identical to the pre-change main spec
  apart from the pointer sentences by the block-diff above; a `MODIFIED` block's copies are unavoidable candidates.
- **The change's own task numbering** (`[count]`) — the scanner reads a leading task number as a count; not a claim.
- **Paths named inside a command the change runs or cites** (a task's `./gradlew spotlessApply`, `origin/main`,
  `scripts/claim-scan.py`, `python3 scripts/review-routing.py`, and the claims table's `git show HEAD:<path>` sources).
- **The change's own artifacts** — the planning artifacts, whose every line the scanner reads as added, and the
  tracked-file edits (a `docs/review-rounds.tsv` record row's round number, the `AGENTS.md` bullet's pointer, a
  definition's budget line).
- **The normative quantifiers** (`every`, `only`, `never`, `none`, `all`, `always`) — the contract's and the
  requirements' own wording, or the proposal's `None` sections; the statements this change makes with them are the rows
  above.
