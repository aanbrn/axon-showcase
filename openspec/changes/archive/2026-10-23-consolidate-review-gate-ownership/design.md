# Design

## Context

See `proposal.md` — Why. The owner's objective is a consolidated, internally consistent agent process, and this change
is its first unit: the review gate is the pilot that establishes the ownership model — one owner per class of
obligation, one operational procedure for the calling agent, load routes instead of independent retellings — which the
later stages (capture, shipping, audits) reuse rather than re-invent.

The constraints that shape the approach:

- `AGENTS.md` is loaded into every session, including each subagent's, so text there is paid for by sessions that never
  run the review gate. The extraction pattern already in the repository is a stage entry point at root plus the
  procedure in a project skill — `ship-change` is the precedent, and `AGENTS.md`'s "Workflow: Extract an `AGENTS.md`
  section" owns the conditions.
- The `agent-skills` capability owns subagent behaviour; `showcase/quality/commit-hygiene` owns the round ledger's row
  format and verification; `review-quick.md` and `review-thorough.md` own the reviewers' contracts. The change must not
  move an obligation into a second owner.
- `openspec validate` checks that a `MODIFIED` requirement preserves every scenario, and does not check a
  `REMOVED`/`ADDED` pair at all, so a split must carry the scenario set by hand.
- The spec instruction permits splitting an existing long requirement when the change is made for that purpose; this
  change's purpose is the ownership split, and OpenSpec's own measure of that requirement's text is 927 characters
  (`openspec show showcase/quality/agent-skills --type spec --json --no-scenarios`), over the 500-character guidance.

## Goals / Non-Goals

**Goals:**

- One owner per class of review-gate obligation, stated in the artifacts' own terms (behaviour in the spec, procedure in
  the skill, reviewer contract in the definition, record format in the ledger's spec, decision floor in the script).
- The root carries only what must be loaded before the gate runs: the trigger, the stage owners, the authorization
  stops, and the skill pointer.
- A retrieval path that fails loudly — a missing skill stops the gate instead of running it from the root text.

**Non-Goals:**

- Changing the gate's membership, order, or obligation, or weakening the manual approval.
- Changing capture, shipping, diagram, model, permission, or subject-pack behaviour.
- Adding a workflow engine, a rule registry, or a universal prose-conformance check.
- Editing generated (`opsx-*`, `openspec-*`) or vendored (`axon4to5-*`, `ast-grep`) files.
- The parked audit-process ideas in `docs/ideas.md` (findings carry-forward, count trends, rejected-capture records).

## Decisions

### D1 — The procedure moves to a new project skill, not another root section or a process pack

Chosen: `.opencode/skills/review-gate/SKILL.md`, modelled on `ship-change` — one stage, one skill, a root entry point
that names it.

- **Rejected — keep the procedure in `AGENTS.md` and delete only the spec copy:** the spec copy is the drift surface the
  change exists to remove, but the root copy is the larger always-loaded cost and the copy every subagent pays for.
- **Rejected — one `process` skill holding review, capture, shipping, and audits:** its trigger fires on most turns, so
  the always-loaded saving disappears and every session pays for a larger document.
- **Rejected — extend `ship-change`:** shipping runs after the owner's approval and the gate before it; one skill would
  carry one stage's steps while loaded for the other.

### D2 — The spec requirement is replaced by `REMOVED` + six `ADDED` blocks

Chosen: remove `Per-change quality-gate and analysis subagents are available` and add:

- the quick loop;
- the routed deep review;
- the capture;
- the diagrams;
- the procedure's ownership;
- the root entry point.

- **Rejected — `MODIFIED` in place:** every existing scenario must be preserved, so the block would still own three
  subjects, and relocating a scenario out of it needs a `REMOVED`.
- **Rejected — `ADDED` only:** `main` would then hold both the old requirement and its replacements — two owners, which
  is the defect.
- **Consequence:** all 20 scenario names are carried by hand because validation does not compare the pair; a task
  asserts that every `HEAD` scenario name survives, and that the six names the delta adds belong to its two ownership
  requirements.

**Deviation, resolved by the owner.** `openspec instructions specs` permits a split "only when the user asks for it, in
a change made for that purpose", and prescribes a `MODIFIED` block that keeps the header and every scenario while
cutting its description, plus `ADDED` requirements for the behaviours removed from that description. That shape cannot
relocate a scenario, because validation requires a `MODIFIED` block to carry every existing scenario — so splitting this
requirement's scenarios takes the `REMOVED` + `ADDED` route `AGENTS.md` documents for a retitle, extended here to a
split, and the scenario-set check is what replaces the validation that route does not get.

- **Decision (owner, 2026-10-23):** allow `REMOVED` + `ADDED` for this requirement's split, provided every existing
  obligation is preserved and its traceability is verified.
- **Scope:** the exception covers this requirement's scenario split only; it changes no workflow rule, and it is not a
  general permission for other requirements.
- **Basis:** the advisor recommended it and the owner approved it; the record is the owner's decision, not the
  recommendation.

### D3 — The root keeps the authorization boundary and the authoring rule; the rest moves

Kept in `AGENTS.md`, one bullet per item:

- the `### Invariants` line that the review gate covers every unit;
- the authorization stops — a clean quick review is not approval, and nothing is committed, pushed, archived, or merged
  before the owner's pass;
- the trigger;
- the stage names with the owner each delegates to;
- the skill pointer and its missing-skill stop;
- the single-sourcing rule, which governs authoring before the gate and is referenced from `## Conventions`.

The single-sourcing rule moves verbatim from the gate's decision points into `### Invariants`, because it is
cross-cutting; the one `## Conventions` reference to it is retargeted to that heading.

Moved to the skill, one bullet per item:

- the stages' steps;
- the procedural decision points (prior-instance hand-off, session continuation, scope sweep on a behaviour-adding
  finding);
- the failure handling for a non-converging loop;
- the completion criteria.

- **Rejected — move the stops too:** an imperative a reader must act on before the dangerous action is stated at every
  site where that action appears, and the commit/push decision is made at root.
- **Rejected — move the single-sourcing rule:** it governs authoring, capture, and audits, not only the gate; moving it
  would hide it behind a stage trigger.

### D4 — Six scenarios differ from `HEAD` (five amended, one retargeted); the rest move verbatim

Six scenarios differ from `HEAD`. Five are rewritten from outcomes plus pointers: the quick loop's first, three
deep-review ones that restated the definition's dimensions, the command's hand-over list and its pass budget, and the
stalled-reviewer one, which receives the replacement cases `HEAD` carried in the first. The sixth is the capture
scenario, which gains the two `review-gate` additions the owner-approved narrow retarget makes — a destination and an
entry in its existing-rule enumeration. The other fourteen keep their clauses because they state behaviour rather than a
step, and the diagram scenarios move unchanged; consolidating the capture scenarios is a later unit's job.

- **Rejected — rewrite every body to outcome level:** it would amend capture behaviour this change does not own.
- **Rejected — move every body verbatim:** it would leave the duplication the change exists to remove.

The obligation trace below records where each obligation lands, per obligation rather than per group, so a loss is
visible where it happens rather than inferred from a summary.

### D5 — The retarget list is derived by grep, never hand-written

`git grep -l 'Workflow: The review gate' -- .opencode AGENTS.md` is the scope. At the planning base it returns the five
command definitions, and `git grep -n` over `AGENTS.md` returns three lines with three dispositions: the heading stays
(the entry point), the change-lifecycle step that points at the section stays (it points at the surviving entry point),
and the `## Conventions` reference to the single-sourcing rule is retargeted to `### Invariants`. A site the grep does
not find is out of scope; a site it finds later is in scope, so the task re-runs the grep after the edits.

### D6 — This pilot adds no machine check

The mechanical half the gate already has stays as it is (`verifyReviewSessionContinuity`, `verifyUniqueReviewRounds`,
`verifyCapturedMarkers`, the ledger's `--unique-rounds --expect` checkpoint). Pairing two prose copies has no observable
violation short of semantic comparison, which the non-goal forbids; a gap the implementation actually demonstrates is
parked as its own unit with a defined input and a known-bad case.

## Risks / Trade-offs

| Risk                                                             | Mitigation                                                                                                                                                                                                                                                                                                                                                                                                                        |
| ---------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The caller skips the skill load, so the gate runs from a summary | The root keeps the trigger, the stage names, and the missing-skill stop; a smoke-run with a positive control checks the load                                                                                                                                                                                                                                                                                                      |
| A scenario rewrite loses an obligation                           | The obligation trace below names every obligation's destination; a row with no destination keeps the text in the scenario; a lost-term sweep runs over the removed text's distinctive tokens                                                                                                                                                                                                                                      |
| `REMOVED`/`ADDED` bypasses scenario validation                   | All 20 names carried by hand; a script asserts `HEAD` minus the delta is empty and lists the six names the delta adds                                                                                                                                                                                                                                                                                                             |
| The skill becomes a second copy of the scenario text             | The spec scenario points at the skill and the definition; the reviewer reports a restated step as `single-sourcing`                                                                                                                                                                                                                                                                                                               |
| The gate now costs an extra read per unit                        | Measured after implementation, over each route's full mandatory set: the caller reads `AGENTS.md` plus the skill — 2,739 → 2,804 lines (265,870 → 269,648 B, +65 lines / +3,778 B); a reviewer's or consultant's route, which does not load the skill, reads `AGENTS.md` — 2,739 → 2,595 lines (265,870 → 249,990 B, −144 lines / −15,880 B). The cost sits in the caller's route, the gain in the routes that never run the gate |
| A command keeps naming `AGENTS.md` after the move                | The grep-derived list is re-run as the last step of the retarget task                                                                                                                                                                                                                                                                                                                                                             |

## Migration Plan

One pull request; no deployed component. Revert is the commit revert — no data, no schema, no image. The skill must
exist before the root text is deleted, so the order is: create the skill, verify it loads, reduce the root, retarget the
references.

## Scenario mapping

Every scenario of the removed requirement, its destination, and its treatment. `Amended` means the body now states the
outcome and points at the owner of the steps; `verbatim` means the body is carried unchanged.

| Old scenario                                                                | New requirement                                                     | Treatment |
| --------------------------------------------------------------------------- | ------------------------------------------------------------------- | --------- |
| Quick review runs after proposal and implementation                         | The quick review loop runs on every unit                            | Amended   |
| A stalled reviewer is replaced from a recorded handover                     | The quick review loop runs on every unit                            | Amended   |
| A skipped pre-write pass is a missing record                                | The quick review loop runs on every unit                            | Verbatim  |
| Thorough review is available on demand                                      | The deep review is routed and bounded                               | Amended   |
| Deep review is routed by one three-outcome decision                         | The deep review is routed and bounded                               | Verbatim  |
| A missing or failed routing decision cannot skip mandatory review           | The deep review is routed and bounded                               | Verbatim  |
| A low-risk change bypasses the deep-review lifecycle                        | The deep review is routed and bounded                               | Verbatim  |
| A routed deep review runs in its own session with a task-relevant hand-over | The deep review is routed and bounded                               | Amended   |
| A failed deep-review hand-over is detected, not reported as a review        | The deep review is routed and bounded                               | Verbatim  |
| Deep review is bounded by a two-pass budget                                 | The deep review is routed and bounded                               | Amended   |
| A deep-review finding is tracked to a resolved status                       | The deep review is routed and bounded                               | Verbatim  |
| Deep review exhausts a class it raises                                      | The deep review is routed and bounded                               | Verbatim  |
| Lessons are captured after implementation                                   | Lessons are captured after implementation                           | Amended   |
| A change that obsoletes a rule yields a retirement candidate                | Lessons are captured after implementation                           | Verbatim  |
| A skipped capture is a missing record at the merge                          | Lessons are captured after implementation                           | Verbatim  |
| A lesson whose cause is an authoring mechanism is routed to it              | Lessons are captured after implementation                           | Verbatim  |
| Screenshots are reviewed visually                                           | Diagrams and screenshots are produced by their specialist subagents | Verbatim  |
| ASCII diagrams are drawn by the pro-model diagrammer                        | Diagrams and screenshots are produced by their specialist subagents | Verbatim  |
| Mermaid diagrams are drawn by the pro-model diagrammer                      | Diagrams and screenshots are produced by their specialist subagents | Verbatim  |
| A Mermaid render is visually reviewed before the diagram ships              | Diagrams and screenshots are produced by their specialist subagents | Verbatim  |

### Obligation trace for the five amended scenarios

Every independent obligation of the five rewritten scenarios and the place that carries it. `retained` means the amended
scenario still states it; `moved` names the artifact that owns it; `moved-in` marks content this change brings into the
scenario. Where no destination carries an obligation, the obligation stays in the scenario — a trace row with no
destination is a mandate to keep the text, not to drop it.

**Quick review runs after proposal and implementation**

| #   | Obligation                                                                                                                                                                | Destination                                                                                             |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| 1   | The trigger: proposal or implementation finished                                                                                                                          | retained                                                                                                |
| 2   | The reviewer reviews against the planning artifacts; the loop repeats to no-new-observations                                                                              | retained                                                                                                |
| 3   | The pass is a bounded screen over what it is given                                                                                                                        | retained, detail in `review-quick.md`                                                                   |
| 4   | The caller supplies the inputs as a list the report names                                                                                                                 | retained, detail in `review-quick.md`                                                                   |
| 5   | A capture's supplied list (existing rules, capture evidence, `lesson-capture`)                                                                                            | retained, detail in `review-quick.md`                                                                   |
| 6   | Risk focuses widen attention, not scope                                                                                                                                   | moved to `review-quick.md`                                                                              |
| 7   | The screen's fixed scope list                                                                                                                                             | moved to `review-quick.md`                                                                              |
| 8   | The pass is not required to search a class, reproduce rows, analyse mutation-style, or exhaust boundary conditions, error paths, callers, configuration, or documentation | moved to `review-quick.md`                                                                              |
| 9   | A risk the scope cannot settle escalates to the deep review                                                                                                               | retained                                                                                                |
| 10  | The stop condition scales with what is supplied                                                                                                                           | moved to `review-quick.md` (the scenario keeps only that the report names its scope and stop condition) |
| 11  | No scope expansion                                                                                                                                                        | moved to `review-quick.md`                                                                              |
| 12  | The report states its scope and stop condition                                                                                                                            | retained, detail in `review-quick.md`                                                                   |
| 13  | Governance stays in force (pre-write pass, tests, manual review)                                                                                                          | `review-quick.md`, `### Invariants`, root stops                                                         |
| 14  | Capture: challenge each rule's durability                                                                                                                                 | moved to `review-quick.md`                                                                              |
| 15  | Capture: apply the promotion gate and dispositions                                                                                                                        | moved to `review-quick.md` + `lesson-capture.md`                                                        |
| 16  | Capture: the finding's shape                                                                                                                                              | moved to `review-quick.md`                                                                              |
| 17  | Capture: report the specific limitation                                                                                                                                   | moved to `review-quick.md`                                                                              |
| 18  | The canonical finding-class list and its extension rule                                                                                                                   | moved to `review-quick.md`                                                                              |
| 19  | Every round emits the machine-readable record line                                                                                                                        | `review-quick.md` (emits), `commit-hygiene` (fields)                                                    |
| 20  | The caller appends the row with the session fields                                                                                                                        | `review-gate` skill (step), `commit-hygiene` (fields)                                                   |
| 21  | The ledger verification after the append and before the next invocation and the manual pass                                                                               | `review-gate` skill (step), `commit-hygiene` (verification)                                             |
| 22  | With no prior-round list, repetition is unknown                                                                                                                           | moved to `review-quick.md`                                                                              |
| 23  | A formatter-owned defect is routed to the formatter                                                                                                                       | retained, detail in `review-quick.md`                                                                   |
| 24  | A record artifact escalates instead of iterating                                                                                                                          | retained                                                                                                |
| 25  | Every unit records the pre-write pass                                                                                                                                     | `review-gate` skill                                                                                     |
| 26  | The claims table's content and location                                                                                                                                   | `review-gate` skill                                                                                     |
| 27  | The table is seeded by `scripts/claim-scan.py` and every candidate is accounted for                                                                                       | `review-gate` skill                                                                                     |
| 28  | The caller passes each prior finding's instance                                                                                                                           | `review-gate` skill                                                                                     |
| 29  | The reviewer-session preflight: create, retain, continue                                                                                                                  | `review-gate` skill                                                                                     |
| 30  | The returned identifier is compared with the saved one                                                                                                                    | `review-gate` skill                                                                                     |
| 31  | The three cases that justify a replacement                                                                                                                                | moved-in to the stalled-reviewer scenario; `review-gate` skill                                          |
| 32  | Reuse never substitutes for the instance hand-off                                                                                                                         | `review-gate` skill                                                                                     |
| 33  | The per-surface checks (proposal coherence, implementation semantics, conventions)                                                                                        | moved to `review-quick.md`                                                                              |
| 34  | Each finding carries its class's evidence                                                                                                                                 | moved to `review-quick.md`                                                                              |
| 35  | The report's order by practical importance                                                                                                                                | moved to `review-quick.md`                                                                              |
| 36  | Each finding names class, location, issue, evidence, correction                                                                                                           | moved to `review-quick.md`                                                                              |
| 37  | A partially fixed defect is a recurrence                                                                                                                                  | moved to `review-quick.md`                                                                              |
| 38  | A finding whose class a prior round raised is reported as a repeat, naming the class                                                                                      | moved to `review-quick.md`                                                                              |
| 39  | The caller re-derives the root cause on the repetition signal rather than patching the instance                                                                           | moved to `review-quick.md`                                                                              |

**A stalled reviewer is replaced from a recorded handover**

| #   | Obligation                                                             | Destination                                                |
| --- | ---------------------------------------------------------------------- | ---------------------------------------------------------- |
| 1   | The trigger: no response by the policy's deadline                      | retained                                                   |
| 2   | The failure is recorded as `stopped-responding` before the replacement | retained                                                   |
| 3   | The hand-over is the record written before waiting, with its fields    | retained                                                   |
| 4   | The deadline comes from the policy, never a universal figure           | retained                                                   |
| 5   | The replacement reviews the current state independently                | retained                                                   |
| 6   | The record holds only verifiable facts                                 | retained                                                   |
| 7   | The three replacement cases and the observed stall                     | moved-in from the quick scenario; also `review-gate` skill |

**Thorough review is available on demand**

| #   | Obligation                                             | Destination                   |
| --- | ------------------------------------------------------ | ----------------------------- |
| 1   | The trigger: a manual run or a routed outcome          | retained                      |
| 2   | The review is proportionate to the named risk          | retained                      |
| 3   | The inspection dimensions                              | moved to `review-thorough.md` |
| 4   | Each finding's quality and the no-preference rule      | retained                      |
| 5   | A manual run is explicit and never replaces `REQUIRED` | retained                      |

**A routed deep review runs in its own session with a task-relevant hand-over**

| #   | Obligation                                                                            | Destination                                      |
| --- | ------------------------------------------------------------------------------------- | ------------------------------------------------ |
| 1   | The trigger: the decision selects the review and the owner resolves to run it         | retained                                         |
| 2   | One dedicated session; its identifier and state preserved; only task-relevant context | retained, hand-over fields in `/review-thorough` |
| 3   | The three roles stay in separate sessions                                             | retained                                         |
| 4   | A follow-up reuses the session; a different change starts fresh                       | retained                                         |
| 5   | The deep reviewer inspects the current state independently                            | retained                                         |
| 6   | The pass is bounded to the change's artifacts and reports a limitation                | retained                                         |

**Deep review is bounded by a two-pass budget**

| #   | Obligation                                                                 | Destination                             |
| --- | -------------------------------------------------------------------------- | --------------------------------------- |
| 1   | The trigger: a routed deep review runs                                     | retained                                |
| 2   | The default budget and the recorded decision a further pass needs          | moved to `/review-thorough`             |
| 3   | A targeted re-review verifies a localized fix without concealing a finding | retained (no other artifact carries it) |
| 4   | The budget limit's consequences                                            | moved to `/review-thorough`             |

## Claims table

Each fact this change states about its evidence, with the command that derives it, re-run at the planning base
(`6f21b0d4`). A row about an artifact this change edits reads its pre-change state.

| Claim                                                                                                                                                                                                                                      | Source                                                                                                                                                                                                                |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The delta leaves 14 scenarios identical to `HEAD` and changes 6                                                                                                                                                                            | a body comparison of `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` (block 174–610) against the delta, whitespace normalized                                                                    |
| `AGENTS.md` documents the `REMOVED` + `ADDED` route for a retitle, which this split extends                                                                                                                                                | `AGENTS.md`, the "To retitle a requirement" decision point                                                                                                                                                            |
| `AGENTS.md`'s review-gate section is 202 lines                                                                                                                                                                                             | `git show HEAD:AGENTS.md \| sed -n '344,545p' \| wc -l`                                                                                                                                                               |
| The removed requirement is 437 lines with 20 scenarios                                                                                                                                                                                     | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md` sliced at its `### Requirement` boundaries (174–610)                                                                                             |
| Its first scenario is 129 lines (187–315) with 35 `AND` clauses                                                                                                                                                                            | the same slice, its first `#### Scenario` block                                                                                                                                                                       |
| The 20 scenarios split 3 quick / 9 deep / 4 capture / 4 diagram                                                                                                                                                                            | the names in the scenario mapping above                                                                                                                                                                               |
| Five command definitions name `AGENTS.md` as the owner of the steps                                                                                                                                                                        | `git grep -l 'Workflow: The review gate' -- .opencode` → `apply-audit-reductions`, `audit-agents`, `audit-architecture`, `audit-readme`, `audit-specs`                                                                |
| `AGENTS.md` has three matches for that heading: the heading, a change-lifecycle step pointing at it, and the `## Conventions` rule citation                                                                                                | `git grep -n 'Workflow: The review gate' -- AGENTS.md`                                                                                                                                                                |
| No member of the root `check` set pairs the two prose copies                                                                                                                                                                               | the `tasks.named("check")` block in `build.gradle.kts` (651–677): 22 `dependsOn` entries, of which 19 are the `verify*`/`test*` tasks and three are `workflowLint`, the chart package, and the included build's tests |
| The Markdown format target includes project-authored skills and excludes vendored/generated ones                                                                                                                                           | `build.gradle.kts` markdown `target`/`targetExclude` block                                                                                                                                                            |
| `verifyCapturedMarkers` scans project-authored `SKILL.md` files                                                                                                                                                                            | `build.gradle.kts` `verifyCapturedMarkers` inputs                                                                                                                                                                     |
| `upstreamReferences` scans project-authored `SKILL.md` files                                                                                                                                                                               | `build.gradle.kts` `upstreamReferences` `sourceFiles`                                                                                                                                                                 |
| OpenSpec measures the removed requirement's text at 927 characters, against the 500-character guidance `openspec instructions specs` states                                                                                                | `openspec show showcase/quality/agent-skills --type spec --json --no-scenarios`; `openspec instructions specs --change consolidate-review-gate-ownership`                                                             |
| The 2026-10-19 agent-tooling report's two findings are a model-pin identifier and a task enumeration                                                                                                                                       | `docs/audits/2026-10-19.md`, the agent-tooling section's two findings                                                                                                                                                 |
| `ship-change` is the extraction precedent: a stage entry point at root plus the procedure in a project skill                                                                                                                               | `.opencode/skills/ship-change/SKILL.md` and its `Source:` lines                                                                                                                                                       |
| The capability Purpose enumerates the project-authored guidance skills                                                                                                                                                                     | `openspec/specs/showcase/quality/agent-skills/spec.md` `## Purpose`                                                                                                                                                   |
| `AGENTS.md` is 2,739 lines / 265,870 bytes at the planning base                                                                                                                                                                            | `wc -l -c AGENTS.md`                                                                                                                                                                                                  |
| The review-gate section is 21,054 bytes and carries 41 `captured:` markers                                                                                                                                                                 | `git show HEAD:AGENTS.md \| sed -n '344,545p' \| wc -c` and the same slice piped to `grep -o 'captured:' \| wc -l`                                                                                                    |
| No parked idea covers this consolidation                                                                                                                                                                                                   | `git grep -in 'consolidat' docs/ideas.md` → the nearest entries are the audit-process and vendored-skill-single-sourcing ideas, neither of which proposes this unit                                                   |
| OpenSpec permits a split only when the change is made for that purpose, and prescribes a `MODIFIED` block that keeps every scenario; validation checks scenario preservation for `MODIFIED` but not across a `REMOVED`/`ADDED` pair        | `openspec instructions specs --change consolidate-review-gate-ownership`; `AGENTS.md`'s spec-delta failure handling                                                                                                   |
| OpenCode loads `AGENTS.md` into every session, each subagent's included                                                                                                                                                                    | `docs/audits/2026-10-19.md`, the instruction-budget ledger                                                                                                                                                            |
| The implementation's read set, per route over its full mandatory files: caller reads `AGENTS.md` + skill, 2,739 → 2,804 lines (265,870 → 269,648 B, +65 / +3,778); a reviewer/consultant reads `AGENTS.md`, 2,739 → 2,595 (−144 / −15,880) | `wc -l -c` over `AGENTS.md` and `.opencode/skills/review-gate/SKILL.md`; `git show HEAD:AGENTS.md \| wc -l -c` for the pre-change file                                                                                |

### Scan-candidate dispositions

`scripts/claim-scan.py` over the change's added lines reports four candidate kinds; each kind's disposition covers every
candidate of that kind.

- **`[count]`** — a counted figure. Each counted figure the artifacts state is a row above, except a figure that is a
  command's own argument or an example inside a quoted requirement, which the row's command reproduces with it.
- **`[path]`** — names of repository artifacts the change reads, edits, or creates. They are verified by existence, not
  by a row: each edit target is named in `tasks.md` and checked when the edit lands.
- **`[cross-ref]`** — prose naming a subagent or screenshot/diagram pair. They resolve to a requirement title and its
  definition, not to an external source.
- **`[absolute]`** — normative requirement text (`SHALL`, `every`, `only`) and scope qualifiers. The subset that asserts
  a repository fact is a row above; the rest is the requirement's own normative strength and has no source to reproduce.

## Open Questions

The first consultation's two questions are resolved: the destination is a new stage skill (D1), and the split route is
the owner's recorded decision (D2).

The second consultation's two questions are answered by this design's own evidence rather than left open:

- **Whether every removed obligation exists at its destination** — the obligation trace above answers it per obligation;
  the implementation review verifies the trace against the moved text, and a row with no destination keeps its text.
- **Whether the extraction's gain by role is real** — task 6.7 measures the gate's caller, a reviewer that does not
  orchestrate the gate, and a consultation separately, because the expected gain sits in the sessions that never run the
  gate while the cost sits in the caller's route.
