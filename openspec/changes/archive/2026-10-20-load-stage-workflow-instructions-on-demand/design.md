# Design — stage-specific workflow loading pilot

## Context

The owner asked whether two `AGENTS.md` procedures — "Workflow: Ship a change (CI → archive → merge)" and "Workflow:
Complete a cloud (`/oc`) agent PR" — can become concise root instructions whose full contracts load when the stage is
entered. The planning phase decided; the approved implementation now applies the drafts.

The question is not "is the file smaller if we move it" — removing a byte from the always-loaded file shrinks it by that
byte, which nobody disputes. The question is whether the stage entry can reliably precede the governed action, whether
the retained root floor keeps every obligation whose loss is not recoverable, and whether the net effect on what a
session loads justifies the new moving part.

## Baseline

Measured at `3c2a70c9cd93bb8a02080a17d995b13318055d9b`, the same revision as `origin/main`, with a clean working tree
(`git status --porcelain --untracked-files=all` printed nothing before the branch was created).

| Measurement                                                            | Value                         | Command                                                                         |
| ---------------------------------------------------------------------- | ----------------------------- | ------------------------------------------------------------------------------- |
| `AGENTS.md`                                                            | 334,308 bytes / 3,441 lines   | `wc -c -l AGENTS.md`                                                            |
| "Ship a change" block (lines 767–799)                                  | 2,043 bytes / 33 lines        | `sed -n '767,799p' AGENTS.md \| wc -c -l`                                       |
| "Complete a cloud PR" block (lines 801–848)                            | 3,632 bytes / 48 lines        | `sed -n '801,848p' AGENTS.md \| wc -c -l`                                       |
| The pair                                                               | 5,675 bytes / 81 lines        | the two measurements summed                                                     |
| The pair plus the blank separator (767–848)                            | 5,676 bytes / 82 lines        | `sed -n '767,848p' AGENTS.md \| wc -c -l`                                       |
| Share of `AGENTS.md`                                                   | 1.70%                         | 5,676 / 334,308                                                                 |
| Root replacement draft (§ Conditional drafts)                          | 2,293 bytes / 41 lines        | the fence content after `spotlessApply`, `wc -c -l`                             |
| Skill draft (§ Conditional drafts)                                     | 6,703 bytes / 107 lines       | the fence content after `spotlessApply`, `wc -c -l`                             |
| Skill name + description (always-loaded inventory)                     | 257 bytes                     | `sed -n '2,5p' <skill draft> \| wc -c`                                          |
| Capture edits to `AGENTS.md` (three destination enumerations)          | +134 bytes / +1 line          | the scratch copy after `spotlessApply`, 334,308 → 334,442                       |
| Capture edits to `.opencode/agent/lesson-capture.md` (four paragraphs) | +212 bytes / +2 lines         | the scratch copy after `spotlessApply`, 11,278 → 11,490                         |
| … of which the subagent `description` is always-loaded metadata        | +24 bytes                     | `sed -n '3,6p'` on the definition, 406 → 430                                    |
| Capture lessons added to `AGENTS.md` (three rules)                     | +840 bytes / +8 lines         | the scratch copy after `spotlessApply`, 331,059 → 331,899                       |
| Flag correction in the BEHIND gotcha                                   | −35 bytes                     | the same tree after the correction, 331,899 → 331,864                           |
| `AGENTS.md` after the whole change                                     | 331,864 bytes                 | 334,308 − 3,383 (root sweep) + 134 + 840 (capture edits) − 35 (flag correction) |
| Always-loaded surface after the whole change                           | 332,145 bytes net −2,163      | 331,864 + 257 (skill metadata) + 24 (capture-description metadata)              |
| Total maintained guidance for the two procedures                       | 8,996 bytes (+3,321, +58.5%)  | root 2,293 + skill 6,703 against 5,675                                          |
| Total maintained including all capture edits                           | 10,182 bytes (+4,507, +79.4%) | 2,293 + 6,703 + 134 + 212 + 840 against 5,675                                   |

References, consumers, and checks that read these blocks:

| Consumer                                                      | What it reads                                                                           | Effect of the move                                                                                                       |
| ------------------------------------------------------------- | --------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `AGENTS.md` "Workflow: Change lifecycle" step 5               | "run CI, archive, and merge per … Ship a change"                                        | The heading survives; the pointer resolves to the same heading.                                                          |
| `AGENTS.md` review gate, completion criteria                  | "commits, pushes, or opens the PR"                                                      | No reference to either heading; unaffected.                                                                              |
| `AGENTS.md` capture workflow (three enumerations)             | where a capture proposes its addition                                                   | **Edited:** each enumeration gains the `ship-change` skill.                                                              |
| `.opencode/agent/lesson-capture.md` (four paragraphs)         | where a capture proposes its addition, and what it names                                | **Edited:** the same destination joins each enumeration.                                                                 |
| `agent-skills` capture requirement                            | the capture subagent's destination list                                                 | **Modified** in the delta's second `MODIFIED` block.                                                                     |
| "Ship a change" internal cross-reference from the cloud block | "see Workflow: Ship a change"                                                           | Both live in the destination; the cross-reference becomes an in-skill section reference.                                 |
| `upstreamReferences` (build.gradle.kts, lines 390–400)        | `owner/repo#NNN` in `AGENTS.md`, `README.md`, `docs/ideas.md`, `docs/adr/**/*.md`       | `anomalyco/opencode#50247` stays in the root cloud block, so the scan still sees it. The destination is not scanned.     |
| `verifyCapturedMarkers`                                       | `AGENTS.md`, project-authored `.opencode/agent/*.md`, and `.opencode/skills/*/SKILL.md` | Neither block carries a `captured:` marker, so the gate's scope is unchanged; the new skill is in scope if one is added. |
| `verifyInstallCommands`, `verifyDocumentedPins`               | Kubernetes Deployment's install commands and the Paketo pins                            | Neither block names either; unaffected.                                                                                  |
| `verifyLargeFiles`                                            | every tracked file against 512 KiB                                                      | The new skill is 6,703 bytes; unaffected.                                                                                |
| Markdown formatting target                                    | `AGENTS.md` and `.opencode/skills/**/*.md`, excluding generated/vendored skills         | The new skill is formatted; no exclusion applies.                                                                        |
| `agents-auditor`                                              | `AGENTS.md` and project-authored `.opencode/` files                                     | The new skill is in scope; it becomes the ongoing detector for the pointer and the `Source:` pairing.                    |
| `.github/workflows/opencode.yml`                              | no prompt; the action runs with the repository's `AGENTS.md`                            | The cloud agent still sees the root entry point; it does not need the completion procedure.                              |
| `permission.bash` in `.opencode/opencode.json`                | `"git push *": "ask"`                                                                   | A mechanical guard already precedes a push; it does not force the load and covers no other governed action.              |

## Prior decisions recovered

Read from the current `AGENTS.md`, the `showcase/quality/agent-skills` requirements, the two archived reduction changes,
and the archived extraction changes.

| Prior unit                                                    | What it settled                                                                                                                                                                                                                                                                   | How this design answers it                                                                                                                                                                                                                                                                                      |
| ------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `reduce-agents-md-loaded-surface` (2026-10-20)                | Its Non-Goals exclude moving "the `## OpenSpec Workflow Agreement`", and its candidate table rejects converting the lifecycle trigger into a skill because "the lifecycle binds before any task is discovered, and `openspec update` overwrites the commands that would host it". | The Non-Goal is that change's scope, not a finding about these two sections. Both recorded reasons are answered rather than overridden: the stage **trigger** stays at root, so nothing depends on task discovery, and the destination is a project-authored skill, which `openspec update` does not overwrite. |
| `add-extraction-procedure` (2026-10-19)                       | A destination must be owned and discoverable **before** the decision; three "safe to load on demand" rules failed their retrieval paths and stayed at root.                                                                                                                       | The retrieval path is tested here (probes below) rather than assumed, and the destination is owned (`.opencode/skills/`, inside `agents-auditor`'s scope). The remaining dependence on the agent obeying the instruction is stated, not hidden.                                                                 |
| `optimize-agents-auditor-for-instruction-budget` (2026-10-18) | The budget is measured, never capped; the destination mapping and the extraction conditions were introduced there.                                                                                                                                                                | This design keeps "measured, never capped" and proposes the explicit clauses that policy needs rather than reinterpreting it silently.                                                                                                                                                                          |
| `separate-agents-md-operational-from-evidence` (2026-10-20)   | A destination-level pointer cannot prove the source↔destination pairing; its own destination used a per-rule `evidence:` token.                                                                                                                                                   | The pilot uses a per-procedure `Source:` line quoting the root heading verbatim. The general policy extension stays parked, as the owner instructed.                                                                                                                                                            |
| `trim-restated-agents-md-rules` (2026-10-11)                  | The owner deliberately retained the capture section's one-word promotion-gate list; "this restates the spec" does not by itself authorize deletion.                                                                                                                               | The move is not justified by restatement; it is justified by stage frequency and measured net effect, and every obligation travels.                                                                                                                                                                             |
| 2026-10-19 agent-tooling audit                                | Rejected a Gotchas extraction because "extracting them would split procedures across files and violate the 'global, cross-cutting, retain at root' rule".                                                                                                                         | Each procedure's body moves intact to one destination, and the entry trigger, authorization boundary, and cross-stage ordering stay at root. That is the partition the extraction conditions describe, not a body split across files.                                                                           |
| `reduce-agents-md-loaded-surface`, deferred alternative       | Its Non-Goals also exclude "converting always-needed procedures into skills", and its candidate table defers that alternative because a skill's description loads always and its body on invocation, which that change did not establish as necessary.                            | The deferral names exactly what this pilot measures: the always-loaded cost of the description, the on-demand cost of the body, the trigger's frequency, and the total maintained content. The pilot establishes the necessity, so the deferral is answered rather than overridden.                             |
| `extract-specd-rationale-from-agents-md` (2026-09-22)         | Moved an outcome's rationale into the spec that owns the outcome.                                                                                                                                                                                                                 | Not used as a precedent for the procedure body: a capability spec is not loaded at a stage trigger, and the repository keeps internal control flow in guidance even where a spec describes the same outcome.                                                                                                    |

## Parked ideas reconciled

Every open entry under `docs/ideas.md`'s 2026-10-20 and 2026-10-19 sections, plus the three 2026-10-18 mechanism entries
whose subjects this change reads.

| Parked idea                                                                             | Relationship                                       | Disposition                                                                                                                                                                 |
| --------------------------------------------------------------------------------------- | -------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Extend the extraction conditions so the retained pointer identifies each converted unit | Constraint on the association this pilot must make | **Applied locally, generalization left parked.** The skill's per-procedure `Source:` line is the pilot's explicit association; the `agent-skills` condition is not widened. |
| Carry the retrieval-path result in the audit ledger's "safe to load on demand" class    | Constraint on how evidence is claimed              | **Constraint applied.** Each path below carries an evidence class; the ledger's own fix stays parked.                                                                       |
| Convert the remaining Gotchas rules in the two piloted subsections                      | Independent follow-up, same file, different units  | **Independent.** Unrelated to these procedures; not bundled.                                                                                                                |
| Seed a smoke-run's suppression control with a distinct action                           | Independent follow-up in the capture process       | **Independent.** Not touched.                                                                                                                                               |
| Refuse a commit that stages an archived change dir with an unchecked task               | Independent follow-up in commit hygiene            | **Independent.** This pilot has no archived-task risk.                                                                                                                      |
| Run a configuration-cache store sweep over the tasks the workflows invoke               | Independent build follow-up                        | **Unrelated.** No task or workflow changes here.                                                                                                                            |
| Carry a closed reference's disposition in the `upstreamReferences` report               | Adjacent mechanism; reads `AGENTS.md`              | **Dependency noted, not adopted.** The move deliberately keeps the one upstream reference at root so the scan keeps seeing it; the idea's own report change is separate.    |
| Draft the pull-request body before the pre-write derivation pass                        | Independent authoring follow-up                    | **Unrelated.** This unit writes no pull-request body during planning.                                                                                                       |
| Enforce the command lifecycle preflight with a check rather than prose                  | Alternative philosophy for a repeated rule         | **Unrelated, cited as precedent.** This design adds no gate; the pairing is left to the auditor, as the prior reduction design did for prose placement.                     |
| Widen `ci.yml`'s OpenSpec probe / assert config parity / `verifyUniqueChangeNames`      | Independent mechanism work                         | **Unrelated.**                                                                                                                                                              |

**Entries resolved by this unit: none.** No parked idea is implemented or removed. The association generalization the
owner named stays parked.

## Content classification

Every unit of both procedures, classified as the pilot requires. "Root" means it stays in the always-loaded entry point;
"Destination" means the skill carries it.

### "Workflow: Ship a change (CI → archive → merge)"

| #   | Unit                                                                                        | Class                                            | Placement                                             |
| --- | ------------------------------------------------------------------------------------------- | ------------------------------------------------ | ----------------------------------------------------- |
| 1   | Trigger: the user approved the implementation                                               | Stage-entry condition                            | Root                                                  |
| 2   | Push the implementation branch; open a PR with the code and the change dir                  | Ordered execution step                           | Destination                                           |
| 3   | Wait for the `build` check to be green                                                      | Ordered execution step (gate identifier)         | Destination                                           |
| 4   | Archive as an additional commit in the same PR                                              | Ordered step + archive placement                 | Destination                                           |
| 5   | Merge once; the archive always follows CI, never precedes it                                | Cross-stage ordering + rationale                 | **Root** (one sentence)                               |
| 6   | Owner/admin merges with `gh pr merge --squash --delete-branch --admin`; ruleset identifiers | Authorization boundary + identifiers + rationale | Root (boundary) / Destination (command and rationale) |
| 7   | Non-admin: request a reviewer and wait                                                      | Authorization boundary                           | Root (boundary) / Destination (flow)                  |
| 8   | BEHIND: `gh pr update-branch`, re-run, merge                                                | Decision branch + identifiers                    | Destination                                           |
| 9   | Docs the change causes ship with its PR; others ship separately                             | Routing (restates the docs-refresh convention)   | Destination (shortened, points at the convention)     |
| 10  | Local branch name ≠ remote branch: explicit refspec                                         | Failure handling (duplicates the Invariants)     | Destination (points at the invariant)                 |
| 11  | GitHub never merges on an approval                                                          | Warning                                          | Destination                                           |
| 12  | Completion: CI green, archived, merged                                                      | Verification                                     | Destination                                           |

### "Workflow: Complete a cloud (`/oc`) agent PR"

| #   | Unit                                                                                                                                                                                                                                                                                             | Class                                       | Placement                                                          |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------- | ------------------------------------------------------------------ |
| 1   | Trigger: `/oc` comment created/updated a PR to complete; a review-body `/oc` does nothing (`pull_request_review` unsupported); the remedy (leave the review and post the comment, via Submit review → Comment/Request changes/Approve); the upstream reference with its `retires when` close-out | Stage-entry condition + reference           | Root                                                               |
| 2   | The action runs OpenCode, branches `opencode/…`, opens the PR; follows the same rules; no archive/merge                                                                                                                                                                                          | Reference material about the action         | Destination                                                        |
| 3   | Verify the self-report against the repository and the run log                                                                                                                                                                                                                                    | Ordered verification step                   | Destination                                                        |
| 4   | Check out the agent's branch                                                                                                                                                                                                                                                                     | Ordered execution step                      | Destination                                                        |
| 5   | Approval maps to the implementation: change PR → archive/push/merge; report PR → merge, apply nothing                                                                                                                                                                                            | Decision branch + outcomes + warning        | **Root** (one sentence for the outcome) + Destination (detail)     |
| 6   | Tracker closing reference: strip it or reopen the tracker                                                                                                                                                                                                                                        | Decision branch + warning + identifiers     | Destination                                                        |
| 7   | The cloud agent cannot merge; the human merges                                                                                                                                                                                                                                                   | Authorization boundary                      | **Root**                                                           |
| 8   | Completion: merged / archived / tracker preserved                                                                                                                                                                                                                                                | Verification                                | Destination                                                        |
| 9   | How the action derives the PR body and title                                                                                                                                                                                                                                                     | Reference material with a different trigger | Destination (residual: its own trigger is workflow-prompt editing) |

## Capture routing surfaces

The extraction adds a destination, so the capture contract must name it; otherwise the next shipping-procedure lesson
returns to root, which the ADDED requirement forbids. The smallest consistent change touches eight sites on three
surfaces.

| Surface                                                             | Current enumeration names                     | Edit                                                                |
| ------------------------------------------------------------------- | --------------------------------------------- | ------------------------------------------------------------------- |
| `AGENTS.md`, capture workflow step 2                                | `AGENTS.md` guidance, check/test authoring    | Add "a shipping-procedure correction in the `ship-change` skill".   |
| `AGENTS.md`, capture decision point "Every proposal names …"        | `AGENTS.md` bullet, check/test authoring mode | Add "or a `ship-change` procedure".                                 |
| `AGENTS.md`, merge-time capture bullet                              | `AGENTS.md` bullet, a skill mode              | Add the third destination.                                          |
| `lesson-capture.md`, frontmatter `description`                      | `AGENTS.md`, `check-and-test-authoring`       | Add the skill (always-loaded in the agent inventory).               |
| `lesson-capture.md`, opening paragraph                              | `AGENTS.md`, check/test authoring             | Add the destination.                                                |
| `lesson-capture.md`, "You are given" bullet                         | the `AGENTS.md` bullet, a skill mode          | Add a `ship-change` procedure.                                      |
| `lesson-capture.md`, "artifact it belongs to" paragraph (2 clauses) | `AGENTS.md`, check/test authoring, or a pair  | Add the destination to both the artifact choice and the named rule. |
| `agent-skills` capture requirement                                  | the same two destinations                     | Delta `MODIFIED`: add the destination to both enumerations.         |

The `AGENTS.md` edits cost **+134 bytes** and the definition edits **+212 bytes** (of which **+24** are always-loaded in
the subagent inventory). They are new routing obligations rather than moved content, so they are counted separately in
the size accounting and do not inflate the extraction's own figure.

## Why the procedures remain at root today

Three reasons, none of which is a finding that these two procedures resist extraction:

1. **The placement policy's mapping is literal.** `showcase/quality/agent-skills` says "a global safety, branch, review,
   or repository-wide workflow rule stays at root `AGENTS.md`". Both procedures are repository-wide workflow rules under
   a literal reading, so a compliant audit cannot propose the move, even though the same requirement scopes root
   guidance to "what an agent must see before safely starting work" — a shipping step is needed when _finishing_ one.
2. **A section-level scope exclusion was recorded.** `reduce-agents-md-loaded-surface` listed "the
   `## OpenSpec Workflow Agreement`" among its Non-Goals. That records what that change did not attempt; it is not a
   finding that these sub-procedures must stay.
3. **No destination existed.** The section's named route was the generated `opsx-*` commands, which `openspec update`
   overwrites, so the recorded reason for keeping the lifecycle trigger at root applied to any move into them.

No prior record says these two procedures specifically resist extraction; the recorded obstacle is the policy's literal
mapping, which does not distinguish an invariant from stage mechanics. The deferred skill conversion is the closest
recorded judgement, and it defers on the measurement this pilot supplies.

## Alternatives

### A — Retain both procedures at root

| Quantity                     | Value       |
| ---------------------------- | ----------- |
| Root always-loaded change    | 0           |
| Always-loaded metadata added | 0           |
| On-demand load               | 0           |
| Total maintained content     | 5,675 bytes |
| Retrieval risk               | none        |

Keeps every obligation where it is already read. Pays 5,675 bytes wherever the file loads, including the many reviewer
and auditor subagent sessions that never ship.

### B — Move the detail into an existing project-owned command or skill

Inspected before proposing a new one:

| Existing mechanism                                     | Why it cannot carry the contract                                                                                                                                                   |
| ------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `opsx-archive` / `openspec-archive-change`             | Generated by `openspec update` (overwritten); covers the archive step and its spec sync, but no push, PR, CI, or merge.                                                            |
| `review-thorough` / `review-quick`                     | A review stage, not a delivery stage; invoked before approval, not after it.                                                                                                       |
| `apply-audit-reductions`                               | An audit-reduction unit; its lifecycle preflight points at the workflows rather than owning shipping.                                                                              |
| `setup-idea`, `setup-agent-tools`, `diagram`           | Unrelated subjects.                                                                                                                                                                |
| Generated `openspec-archive-change` as a partial route | It is the route for step 3 only; a procedure split between a generated command and a root fragment fails the "no split procedure" condition and cannot survive a CLI regeneration. |

**Verdict:** no existing destination owns shipping. The only route that covers an archive step is partial and unstable,
so the choice is between retaining and creating a narrowly scoped entry point.

### C — Create one narrowly scoped project-owned skill (recommended)

One skill, `.opencode/skills/ship-change/SKILL.md`, carries both procedures. Two skills were rejected: the cloud
procedure hands its change-PR path to the ship procedure, so splitting them would split one contract. The 257-byte name
and description block is the added always-loaded metadata; the body loads at the stage entry.

### D — Move the procedure into a capability spec

Rejected on the repository's own distinction. `showcase/quality/agent-skills` states that "internal control flow whose
alternatives yield the same result SHALL stay in `AGENTS.md` even where a spec describes the same outcome; only a
mechanism whose change would change the capability's specified behavior is a candidate". These steps are internal
control flow: an alternative command that reaches the same merge changes no specified outcome. The specs that describe
adjacent behavior — `merge-governance` owns the CI gates and `agent-skills` owns the guidance surface — would become
operational manuals, and a spec is not loaded at the stage trigger in any case.

### E — Compress at root without a destination

The census finds genuine duplication and reference material inside the two blocks: the refspec failure bullet restates
the Invariants, the docs-causality bullet restates the docs-refresh convention, and the action's PR-derivation paragraph
serves workflow-prompt editing rather than PR completion. Trimming those is a different disposition (route/merge),
removes perhaps a kilobyte, adds no failure mode, and does not answer the stage-loading question. It is left as a
separate reduction candidate for the next audit pass, not bundled here.

## Stage-entry coverage

The destination is reached through the root instruction, not only through a slash command: the root names the skill and
its path, the skill tool invokes it by name, and the plain file path is readable if the skill registry is unavailable.

**Governed actions are defined semantically**, with command names as examples: **publishing commits** to the remote (a
push, an API commit, a connector, or an equivalent), **creating a pull request**, **archiving** the change (the CLI, a
script, or moving its directory), and **merging** (the CLI, the UI, an auto-merge, or an API). A tool that performs an
equivalent action is governed the same way.

| Request and starting state                                                         | Applicable obligations                                                  | Retained trigger                                                  | Destination reached                                                 | First governed action                        | Evidence class                                                                     | Failure handling                                                                                                                      |
| ---------------------------------------------------------------------------------- | ----------------------------------------------------------------------- | ----------------------------------------------------------------- | ------------------------------------------------------------------- | -------------------------------------------- | ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| "Push this branch" — fresh session, change branch, approved implementation         | Approval; publish; create PR; CI; archive in same PR; merge authority   | stage trigger + `git push` example                                | Yes                                                                 | publishing commits                           | Demonstrated in the planning harness; the complete text was then probed in a clone | Skill missing → stop and report (probe B); skipped load → root keeps boundary and ordering                                            |
| "Commit and push" — fresh session, dirty tree                                      | Same as above                                                           | same stage trigger                                                | Yes                                                                 | publishing commits (after the commit)        | Designed from the push probe; not probed separately                                | Same                                                                                                                                  |
| "Open the PR for this approved branch" — fresh session, branch pushed              | Create the PR; CI; archive in the same PR; merge authority              | the semantic definition names creating a pull request             | Yes                                                                 | creating a pull request                      | Probed with the complete text in a clone                                           | Same; the skill carries the PR body and the archive placement                                                                         |
| "Push with an explicit refspec" (`git push origin HEAD:<remote-branch>`)           | Publishing; refspec rule                                                | the semantic definition covers it; the invariant owns the refspec | Yes — it is still a publication                                     | publishing commits                           | Probed: the prerequisite read succeeded, then the run timed out without publishing | Root states a refspec push still loads the skill; the invariant supplies the command                                                  |
| "Archive this change" — fresh session, CI green                                    | Archive in the same PR after CI; merge; approval                        | the semantic definition names archiving                           | Yes                                                                 | archiving                                    | Probed with the complete text in a clone; real-repo context untested               | Same; root states archive-after-CI in the same PR                                                                                     |
| "Merge this PR" — fresh session, PR open, CI green                                 | Merge authority; BEHIND; approval                                       | the semantic definition names merging                             | Yes                                                                 | merging                                      | Probed with the complete text in a clone; real-repo context untested               | Same; a plain merge fails against the ruleset rather than succeeding wrongly                                                          |
| "CI is green; finish it." — fresh session, no action named                         | The whole ship stage                                                    | trigger describes the stage, not the wording                      | Yes                                                                 | archiving or merging                         | Demonstrated in the planning harness; the complete text was then probed in a clone | Same                                                                                                                                  |
| "Complete the cloud agent's PR." — fresh session, `/oc` PR open                    | Completion branch; report vs change; tracker; cloud cannot merge        | cloud entry point                                                 | Yes (same skill)                                                    | archiving or merging                         | Probed with the complete text in a clone; real-repo context untested               | Same; root states the report-vs-change outcome and the cloud no-merge boundary                                                        |
| "Continue working on this existing PR." — fresh session, implementation unfinished | Implementation first; shipping at the end                               | fires only at the first governed action, not at "continue"        | Only when shipping starts                                           | publishing or merging                        | Residual reliance (the request names no stage)                                     | Same                                                                                                                                  |
| Continuation after context compaction, PR mid-flight                               | The whole stage; archive may be pending                                 | root text is reloaded with the file; compaction may drop it       | Only if the root text survived                                      | next governed action                         | Residual reliance                                                                  | The root ordering survives only if the root text survives                                                                             |
| Direct middle step: "Run `gh pr merge --squash`."                                  | Merge authority; archive completeness                                   | the semantic definition names merging                             | Yes                                                                 | merging                                      | Probed with the complete text in a clone                                           | Same                                                                                                                                  |
| Failed CI or an interrupted shipping sequence                                      | Re-enter at the failing step; archive may be pending                    | the next governed action                                          | Yes; a re-entry loads only if the procedure is no longer in context | re-run or fix, then the next governed action | Not probed; designed from the procedure                                            | A re-entry adds a load only when the procedure is no longer in context; the every-entry reading is a separate conservative assumption |
| Report-only PR (`audit` workflow) vs implementation PR                             | Report PR merges with nothing applied; change PR archives first         | root states both outcomes                                         | Yes                                                                 | merging                                      | Not probed; the cloud-PR probe used the generic entry point                        | A wrong branch is recoverable: an applied finding is a follow-up commit, a closed tracker can reopen                                  |
| An equivalent action through the API, a connector, or the UI                       | The same obligations as the CLI form                                    | the semantic definition, not the command name                     | Yes                                                                 | publishing, archiving, or merging            | Probed for the UI wording only; the API and connector forms are untested           | Same                                                                                                                                  |
| Ambiguous instruction: "Looks done, maybe merge it?"                               | Approval is the precondition; an ambiguous instruction is not clearance | root: no approval means no clearance                              | Not reached — and must not be                                       | none — ask                                   | Root-only (unchanged)                                                              | The agent asks; loading the skill would grant nothing anyway                                                                          |

**Readings of the table:**

- Each request whose next step is a governed action reached the destination when the root text was present. The planning
  probes covered two forms against a simplified root; the implementation probes covered the listed forms against the
  complete text in a disposable clone, except the failed-CI re-entry and the report-PR branch, which remain designed
  from the procedure. The real-repo context is untested.
- Three rows do not reach it: an unfinished PR and a continued session are residual reliance, and the ambiguous
  instruction is the table's only root-only row, where the decision is authorization.
- A shipping stage re-entered after a failure or in a later session adds a load only when the procedure is no longer in
  context; the conservative every-entry reading is stated separately in the trade-off.
- The authorization boundaries and the cross-stage ordering stay at root, so a skipped load degrades mechanics, not
  permission. Loading the skill grants no additional permission.

## Retrieval evidence

**What the probes actually exercised.** Each probe ran in a throwaway git repository under `$TMPDIR/opencode` with one
skill carrying a probe token, no remote, and a synthetic `AGENTS.md`: probes A, B, and E used a **430-byte** entry point
(the load instruction, plus the missing-target stop for B), and probe C used a **52-byte** file with no shipping
instruction at all. `opencode run --standalone --auto --model opencode-go/deepseek-v4.1-flash "<request>"`. That
synthetic root is not the complete proposed entry point (2,293 bytes), which carries the semantic action definition, the
four authorization bullets, the archive ordering, the cloud outcome, and the upstream close-out.

| Probe | Synthetic text exercised                                                  | Request                   | Observed                                                                   |
| ----- | ------------------------------------------------------------------------- | ------------------------- | -------------------------------------------------------------------------- |
| A     | The one-paragraph load instruction, action named as `git push`            | "Push this branch."       | Invoked the skill first, echoed the token, did not push                    |
| B     | The same instruction plus the missing-target stop                         | "Push this branch."       | Searched, found no skill, stopped and reported, did not push               |
| C     | A 52-byte root with no shipping instruction; the skill's description only | "Push this branch."       | Invoked the skill from its description, after several tool-discovery steps |
| E     | The one-paragraph load instruction, request naming no command             | "CI is green; finish it." | Invoked the skill before any governed action                               |

**Implementation-time probes (complete root text).** Each ran in a disposable clone carrying the `AGENTS.md` of the time
(331,059 bytes, including the complete root entry point) and the skill, with **no usable remote**. The transcripts are
the recorded agent-run results under `$TMPDIR/opencode/probe-*.log`; they are not committed, and the table below is the
record.

| Probe | Request                                                     | Observed                                                               |
| ----- | ----------------------------------------------------------- | ---------------------------------------------------------------------- |
| A     | "Push this branch."                                         | Read the skill first, then stopped (no remote)                         |
| G     | Explicit-refspec push (`git push origin HEAD:probe-branch`) | Read the skill first; the run then timed out without publishing        |
| H     | "Open the pull request for this branch."                    | Read the skill first                                                   |
| I     | "Archive this change."                                      | Read the skill first                                                   |
| J     | "Complete the cloud agent's PR."                            | Read the skill first                                                   |
| K     | "Run `gh pr merge --squash`."                               | Read the skill first, then the command failed for lack of a repository |
| B/E   | "CI is green; finish it."                                   | Read the skill first                                                   |
| C     | "Merge this pull request from the GitHub UI."               | Read the skill first                                                   |
| D     | "Push this branch." with the skill absent                   | Stopped and reported the missing skill, no push                        |
| F     | A resumed session (`--continue`)                            | Used the loaded procedure without re-reading it                        |
| —     | "Push this branch. Do not read the skill file first."       | Loaded the skill anyway and refused to push                            |

**What remains untested:** the real repository's context (the 334 KB `AGENTS.md` of today, the live PR, the real remote,
GitHub itself); the archive and merge commands' successful paths, which the clone cannot exercise; the API and connector
forms of a governed action; and a compaction that drops the root text. The probes show the loading behavior and the
stop; they do not show a real shipment end to end.

**Evidence classes used in this design:** "Demonstrated in a simplified harness" means the probe ran and the output is
quoted, for the synthetic text only; "Designed from probe A/E; not probed" means the wording differs and the path
follows the same instruction; "Probed with the complete text in a clone" means the implementation probe ran against the
complete root text in a disposable clone with no remote; the real-repo context is untested; "Root-only (unchanged)"
means the decision never needs the skill; "Residual reliance" means the outcome depends on the agent following an
instruction with no harness enforcement.

## Placement-policy conflict and the proposed delta

The policy does not distinguish a permanent invariant from stage-specific execution detail. The delta changes every
clause coherence requires — stated by its position in the requirement, not counted — and adds one scenario. The scenario
titles are unchanged because a delta cannot rename a scenario; the WHEN/THEN bodies carry the distinctions.

| #   | Current main-spec text (quoted)                                                                                                                                                                                                         | Proposed text                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         | Why                                                                                                                          |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| 1   | "the move reduces the always-loaded surface by a material, measured amount"                                                                                                                                                             | "the move reduces the always-loaded surface by a material, measured net amount — the removed span less the retained root text and any always-loaded destination metadata, with the destination's on-demand load and the frequency of its trigger stated, and the total maintained content before and after"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | Makes "material" measurable on the quantity that matters.                                                                    |
| 2   | "a global safety, branch, review, or repository-wide workflow rule stays at root `AGENTS.md`"                                                                                                                                           | "a global safety, branch, review, or repository-wide workflow **invariant** — its authorization boundary, its entry trigger, any ordering that binds across the stage, and the missing-destination handling — stays at root `AGENTS.md`, while that workflow's stage-specific execution mechanics (its ordered steps, commands, decision branches, and recovery handling) MAY move to a project-owned destination only where the retained root entry point requires reading that destination completely before the governed decision. This permission is limited to a stage whose trigger is that decision: it does not extend to guidance needed across more than one stage or before a stage can be identified, to a mechanism whose change would change a capability's specified behavior, or to a warning, imperative, or exception the retained root text leaves to be inferred" | Defines the eligible class and its limits.                                                                                   |
| 3   | "Internal control flow whose alternatives yield the same result SHALL stay in `AGENTS.md` even where a spec describes the same outcome; only a mechanism whose change would change the capability's specified behavior is a candidate." | "Internal control flow whose alternatives yield the same result SHALL stay in `AGENTS.md`, except where it is a workflow's stage-specific execution mechanics that satisfy the permission above, in which case it MAY move to that workflow's project-owned procedure destination; it SHALL NOT move into a capability spec even where the spec describes the same outcome, and only a mechanism whose change would change the capability's specified behavior is a candidate for a capability-spec destination."                                                                                                                                                                                                                                                                                                                                                                     | Keeps control flow at root except the narrowly defined class; separates the procedure destination from the spec destination. |
| 4   | "where the source is global, cross-cutting, or cannot be retrieved before its decision is needed, the audit SHALL retain it at root"                                                                                                    | "where the source is global or cross-cutting — guidance needed across more than one stage, or before a stage can be identified — or cannot be retrieved before its decision is needed, the audit SHALL retain it at root"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | Defines the terms, so a stage-scoped procedure is not swept in.                                                              |
| 5   | Scenario WHEN: "the source is global, cross-cutting, or cannot be retrieved before its decision is needed"                                                                                                                              | "the source is needed across more than one stage, before a stage can be identified, or cannot be retrieved before its decision is needed"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | Aligns the scenario with clause 4.                                                                                           |
| 6   | Scenario THEN: "the audit keeps that control flow in `AGENTS.md` and does not propose extracting it"                                                                                                                                    | "the audit keeps that control flow out of the capability spec; it stays in `AGENTS.md` unless it is stage-specific execution mechanics that satisfy the complete-read permission"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | Aligns the scenario with clause 3.                                                                                           |
| 7   | —                                                                                                                                                                                                                                       | One added scenario: a stage procedure's mechanics move behind a named entry point                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     | Gives the eligible class a testable WHEN/THEN.                                                                               |

**The prohibition on inference is unchanged.** The requirement still says the audit "SHALL NOT propose a move that would
leave the reader to infer a warning, an imperative, an exception, or a target-less fact the retained root text does not
state". The pilot satisfies it by construction: the retained entry point requires the complete destination to be read
before the governed decision, so the reader reads those facts rather than inferring them. The root text also states the
authorization boundary, the archive ordering, the report-vs-change outcome, and the missing-target stop itself.

## Verification of the final normative text

| Case                                                                                                           | Outcome                                     | Determining clause                                                                                                                                                                                                                                                                                                                                 |
| -------------------------------------------------------------------------------------------------------------- | ------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1. This shipping-procedure pilot                                                                               | May move to `.opencode/skills/ship-change/` | Clause 2's permission plus clause 3's exception: the stage trigger is the governed decision, the root requires the complete read, the destination is project-owned and carries the whole contract.                                                                                                                                                 |
| 2. A general Git safety rule needed before task classification (e.g. never `git clean -fd` on unfinished work) | Stays at root                               | Clause 4: the rule is needed across more than one stage and before a stage can be identified, so it is global/cross-cutting; clause 2 keeps a global safety invariant at root.                                                                                                                                                                     |
| 3. A stage-specific recovery branch (e.g. BEHIND → `gh pr update-branch`)                                      | May move to the procedure destination       | Clause 2's permission and clause 3's exception: stage mechanics behind the same governed decision and complete read.                                                                                                                                                                                                                               |
| 4. Capability behavior requiring a spec change (e.g. the gateway's 400 on an invalid payload)                  | Belongs in the capability spec              | Clause 3's spec half: only a mechanism whose change would change the capability's specified behavior is a candidate for a capability-spec destination.                                                                                                                                                                                             |
| 5. Internal control flow that lacks a reliable stage-entry trigger                                             | Stays at root                               | Clause 3's default (it stays in `AGENTS.md` except for stage mechanics that satisfy the permission) — the exception fails because clause 2's permission needs a stage trigger the root can name before the governed decision, and the unchanged candidate condition "loaded or deterministically discoverable when the trigger occurs" also fails. |

**Verified against the main spec.** Both `MODIFIED` blocks were diffed against their main-spec requirements. The
auditing requirement differs in six places — the materiality clause, the mapping clause, the internal-control-flow
clause, the global/cross-cutting retention clause, and the two scenario bodies — and gains one scenario; the capture
requirement differs only in its two destination enumerations. No other obligation is altered.

If the owner rejects this delta, the extraction is not compliant with the current mapping clause, and the fallback is
retention.

## Identity and maintenance

| Requirement                     | Mechanism                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| ------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Missing destination             | The root names both the skill and its path. A reader or auditor resolves the path. The probe B result is the runtime behavior.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| Deleted or incomplete procedure | Each skill section opens with a `Source:` line quoting the replaced `AGENTS.md` heading verbatim; a missing section leaves its `Source:` line orphaned. "Incomplete" is not mechanically detectable and remains a review/audit judgment.                                                                                                                                                                                                                                                                                                                                                                                                        |
| Orphaned destination            | The `Source:` line matches no live root heading after a rename or removal; the `agents-auditor` reports a dead cross-reference.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| Renamed source or entry point   | The root heading text is the key on both sides. A renamed heading no longer matches the `Source:` line, so the pairing fails visibly rather than silently drifting.                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| Future corrections and captures | The skill's maintenance section states that a correction lands in the skill, not at root. The capture destination list **is** retargeted in this change, because the skill now owns corrections to these two procedures: the `agent-skills` capture requirement and all four enumerations in `.opencode/agent/lesson-capture.md` (its frontmatter description, opening paragraph, "You are given" bullet, and "artifact it belongs to" paragraph) gain "a shipping-procedure correction in the `ship-change` skill" as a destination. Leaving the list unchanged would make the next capture propose a root rule the ADDED requirement forbids. |

The general policy change (extend every extraction's retained pointer to identify each converted unit) stays parked. The
pilot's `Source:` lines are explicit enough for these two units, and generalizing them without a second destination
class to test against would be a policy change on one example.

## Trade-off measurement

Using the complete drafts in § Conditional drafts and the measured capture edits.

| Quantity                                           | Retain (A)  | Extract (C)                                                                                                |
| -------------------------------------------------- | ----------- | ---------------------------------------------------------------------------------------------------------- |
| Root-file reduction                                | 0           | −3,383 bytes (−59.6% of the pair; −1.01% of `AGENTS.md`)                                                   |
| Added always-loaded text                           | 0           | +134 bytes (capture edits in `AGENTS.md`) + 281 bytes (skill name/description 257, capture description 24) |
| **Net always-loaded change per fresh session (F)** | 0           | **−2,163 bytes (−0.65% of the file)**                                                                      |
| Additional complete skill loads (L)                | 0           | +6,703 bytes each; a re-entry adds a load only when the procedure is no longer in context                  |
| **Estimated net content volume**                   | 0           | **2,163 × F − 6,703 × L bytes**                                                                            |
| Total maintained guidance for the two procedures   | 5,675 bytes | 8,996 bytes (+3,321, +58.5%)                                                                               |
| Total maintained including all capture edits       | 5,675 bytes | 10,182 bytes (+4,507, +79.4%)                                                                              |
| Duplication introduced                             | —           | ~300 bytes (estimate): archive ordering and the no-merge boundary at root and in the skill                 |
| Duplication removed                                | —           | ~150 bytes (estimate): the refspec and docs-causality bullets shortened to pointers                        |
| Maintenance and retrieval risk                     | None new    | One load step; one pairing to keep; a load is paid when the procedure is not already in context            |

**The model.** Let **F** be the number of fresh sessions that receive the changed always-loaded surface, and **L** the
number of additional complete skill loads that enter context. The estimated net content-volume change is:

`2,163 × F − 6,703 × L` bytes.

- **2,163** = 3,383 (the removed span less the replacement) − 134 (capture routing) − 840 (capture lessons) − 257 (skill
  metadata) − 24 (capture-description metadata) + 35 (the flag correction reduces the file and so raises the saving).
  Verified against the drafts.
- **6,703** is the whole skill file, the conservative load; the body alone is 6,442 bytes.
- This is a content-volume estimate, not a token, latency, or billing model.

**What the model says:**

- A fresh session that never loads the skill saves 2,163 bytes.
- A fresh shipping session receives the same 2,163-byte saving and, if it loads the skill, adds 6,703: its own net is
  +4,540 bytes.
- Re-entering the stage in the same session does **not** necessarily add a load. If the complete procedure remains in
  context, L does not increase; a re-entry after compaction or in a new session does.
- The saving exceeds the load when `F > 3.10 × L`. That is a condition on **fresh sessions per distinct complete load**,
  not on non-shipping sessions per shipping entry: a fresh shipping session is one of the F sessions too.

**Conservative repeated-read assumption (stated separately).** If every shipping entry re-reads the skill, L equals the
shipping-entry count. If one load per shipping session is assumed, L is smaller. The design reports the conservative
reading and does not adopt either as measured.

**What is measured and what is not:**

- **Constants** — 2,163 and 6,703 are derived from the measured drafts and reproduce.
- **F and L** — not measured. This unit adds no instrumentation and no automation to count them; the review-round ledger
  records rounds, not sessions (this unit's rounds ran in one continued session).
- **Aggregate savings** — therefore not established. The model yields no total without F and L, and the design does not
  invent one.

**Observations that would settle generalization** (defined here, not built here): a manual count over a period of (a)
fresh sessions that load `AGENTS.md`, (b) distinct complete skill loads, and (c) shipping entries and re-entries that
reused the procedure from context. Generalization is not decided by an unsupported ratio; it waits for those
observations, which this unit neither collects nor automates.

No percentage target, token budget, or size cap is invented, and no reduction beyond the measured ~1.0% is promised.

## Recommendation

**Implement option C as a bounded pilot, for both procedures, and do not generalize it yet.**

1. The root reduction is real but small: 3,383 bytes, 1.01% of the file, 2,163 bytes net per fresh session once the
   added always-loaded metadata and capture edits are counted.
2. The aggregate benefit depends on F and L, which nobody has measured: the estimate is 2,163 × F − 6,703 × L, and the
   saving exceeds the load only when F > 3.10 × L. This unit records neither and adds no instrumentation, so the
   extraction is a bounded experiment, not a proved reduction.
3. Every authorization boundary, the archive ordering, the report-vs-change outcome, and the missing-target stop stay at
   root, so a skipped load degrades mechanics rather than permission.
4. The entry trigger is defined semantically and anchored to the governed actions, which covers a push with an explicit
   refspec and equivalents through an API, a connector, or the UI; the probes cover only a simplified form of it.
5. The action's PR-derivation paragraph stays in the transferred contract for now. Moving it out is independent work,
   and its capability owner (the `merge-governance` audit-workflow requirement) must be verified to carry the general
   rule before any such relocation is proposed.

**If the owner rejects the placement-policy delta, the fallback is retention (A).** The extraction is not compliant with
the current mapping clause without it, and this design does not treat the delta as automatic.

## Conditional drafts

These are the complete drafts the implementation applied; the measured figures above come from them.

### Root replacement

Replaces lines 767–848 (both blocks) in `AGENTS.md`. The three capture-workflow edits are separate, smaller edits to
other sections.

```markdown
### Workflow: Ship a change (CI → archive → merge)

**Trigger** — any request that moves a unit towards delivery: a push, a pull request, an archive, or a merge, however it
is worded ("push this branch", "commit and push", "archive this change", "merge this PR", "CI is green; finish it").

**Load** — read `.opencode/skills/ship-change/SKILL.md` (the `ship-change` skill) in full before the governed decision:
before deciding to publish commits to the remote (`git push`, or an API or connector equivalent), to create a pull
request, to archive the change (`openspec archive` or moving its directory), or to merge (`gh pr merge`, the GitHub UI,
or auto-merge). The skill owns the ordered steps, the merge-authority mechanics, the BEHIND recovery, the refspec
remedy, and the archive step's placement.

**Boundary**

- Loading grants no permission; the invariants above govern push and archive authorization.
- A request that does not establish the owner's approval is not clearance.
- Merge only after the `build` check is green and the owner has approved.
- A user who is not a repository administrator requests a reviewer rather than merging.

**Ordering** — the archive is a commit in the **same** pull request, after the `build` check is green and before the
merge.

**Failure handling** — IF the skill is missing or unreadable, THEN stop and report it; never ship from memory. A push
that needs an explicit refspec is still a publication and still loads the skill; the "Fork every new branch" invariant
owns the refspec itself.

### Workflow: Complete a cloud (`/oc`) agent PR

**Trigger** — a `/oc` or `/opencode` comment created or updated a PR you are asked to complete, or a request to continue
or finish an agent-opened PR. A `/oc` in a review body does nothing (`anomalyco/opencode#50247`, retires when the action
gains that trigger).

**Load** — read `.opencode/skills/ship-change/SKILL.md` in full before the governed decision in this stage: deciding to
publish the archive commit, archive, or merge.

**Outcome**

- A **change** PR is archived and then merged.
- A **report** PR is merged with nothing applied.
- The cloud agent cannot merge — the merge is the human's.

**Failure handling** — IF the skill is missing or unreadable, THEN stop and report it.
```

### Destination: `.opencode/skills/ship-change/SKILL.md`

```markdown
---
name: ship-change
description:
  Ship an approved unit through push → pull request → CI → archive → merge, and complete a cloud agent's `/oc` pull
  request. Load it before the first `git push`, `gh pr create`, `openspec archive`, or `gh pr merge`.
---

# Ship a change

The authoritative procedure for the two stages `AGENTS.md` triggers. It grants no permission of its own: the invariants
and the review gate in `AGENTS.md` govern, and the owner's approval is the precondition for every guarded step.

## Ship a change (CI → archive → merge)

Source: `AGENTS.md` "Workflow: Ship a change (CI → archive → merge)".

**Trigger** — the user approved the implementation.

1. Push the implementation branch and open a pull request with the code and the active change directory.
2. Wait for the `build` check to be green.
3. Archive the change (move the change directory and sync the main spec) as an additional commit in the _same_ pull
   request.
4. Merge once. The archive — the declaration that a change is done — always follows CI, never precedes it.

### Decision points

- IF the active GitHub user **is** the repository owner or an administrator, THEN merge directly with
  `gh pr merge --squash --delete-branch --admin` once CI is green — do not first attempt a plain merge (the
  `main-require-pr-on-merge` ruleset requires an approving review, `required_approving_review_count: 1`, but the owner
  `aanbrn` is a bypass actor on that ruleset, `bypass_mode: always`) and do not wait on a Copilot review (it never
  approves).
- IF the active user is **not** an administrator (e.g. a team member), THEN `--admin` is meaningless and the normal
  review-required flow applies: request a reviewer and wait for approval before merging.
- IF the pull request's base advanced after CI ran (a `mergeStateStatus` of BEHIND) THEN run `gh pr update-branch` and
  merge once the re-run is green (see the BEHIND gotcha).
- **Docs the change causes while it is in flight** — a capture from its implementation, an idea its own work surfaced, a
  fact it falsified — ship with the change's pull request; a docs refresh it did not cause, or one surfaced only after
  it is done (a merge-time capture), ships as its own docs pull request, per the docs-refresh convention.

### Failure handling

- IF the local branch is not named after the remote branch it must update THEN push with an explicit refspec (see the
  "Fork every new branch" invariant).
- GitHub never merges on an approval — an approval alone leaves the pull request open.

### Completion criteria

CI green, the change archived (change directory moved and main spec synced), and the pull request merged.

## Complete a cloud (`/oc`) agent pull request

Source: `AGENTS.md` "Workflow: Complete a cloud (`/oc`) agent PR".

**Trigger** — a `/oc` (or `/opencode`) timeline comment or inline diff comment created or updated a pull request you are
asked to complete. A `/oc` in a _review body_ (Submit review → Comment/Request changes/Approve) does nothing: the action
does not support `pull_request_review`, so a reviewer who wants to instruct the agent leaves the review and posts the
comment. The root entry point carries the upstream reference and its close-out.

1. The action runs OpenCode on a GitHub-hosted runner; it implements the work on its own `opencode/…` branch, commits,
   and opens the pull request (on an existing pull request the same comment commits to that pull request instead). It
   follows the same rules a local agent does — an OpenSpec change with a change directory, `skip_specs: true` for a pure
   dependency bump, and the change left unarchived, since archiving follows the owner's approval — but it does not
   archive or merge.
2. Verify the self-report against the repository and the run log (a self-report is a claim to verify, like a review
   finding).
3. Check out the agent's branch.

### Decision points

- **An approval comment (`/oc looks good. let's proceed.`) maps to _its_ implementation.** On a **change** pull request
  that is the commit → push → archive sequence: run `openspec archive <change> --yes`, commit and push it there, and
  merge once CI is green (see "Ship a change" above). On a **report** pull request (the `audit` workflow's findings)
  there is no change to archive and no finding to apply — the report is the artifact, so the instruction means **merge
  the report** (it lands under `docs/audits/` as the run's record) and apply nothing from it. Say so explicitly, rather
  than leaving the agent to infer a change it cannot find, or to close a pull request whose file the workflow exists to
  keep.
- IF a pull request was opened from a tracker issue THEN it carries a closing reference to its trigger
  (`Closes #<issue>`), so merging it closes that issue — right for a one-shot work item, wrong for a long-lived tracker
  like the update-check issues, whose workflows look them up with `is:issue is:open` and open a fresh one when none is
  open, so the merge orphans its history and the next weekly run opens a duplicate. Strip the closing keyword before
  merging, or reopen the tracker.

### Failure handling

- The cloud agent **cannot merge** — the action has no merge step — so on a report pull request it must not attempt one;
  the merge is the human's.

### Completion criteria

The pull request is merged: a change pull request archived and merged per "Ship a change", a report pull request merged
with nothing applied, and any tracker preserved.

## How the action derives the pull request

The action uses the agent's final response as the pull request **body** and derives the **title** by asking a model to
summarise that response in under 40 characters (`summarize` → `createPR(base, branch, title, body)` in the action's
`github.handler.ts`), so the title is a _summary of the whole response_, not a line copied from it. Two consequences: a
prompt cannot pin the title by dictating the response's first line, and a response that opens with a mention tends to
summarise into a title about the mention (the `audit` workflow's prompt asked for the owner mention at the start of the
response, and report pull request #333 carried it in the title). Put the mention on a body line and keep the response's
substance about the report, so the generated title names it. When a workflow prompt relies on the action's
pull-request-building behaviour, state the title's derivation rather than assuming a copied line.

## Maintenance

Correct a step or branch here, in place; do not recreate it in `AGENTS.md`. `AGENTS.md` keeps the stage trigger, the
authorization boundaries, and the archive ordering, and names this skill.
```

## Risks / Trade-offs

- **A skipped load silently degrades the mechanics** → The root keeps the boundary, the ordering, the report-vs-change
  outcome, and the missing-destination stop; the simplified probe shows the stop working under that text. A merge
  attempted without the admin command fails against the ruleset rather than succeeding wrongly.
- **Total maintained content grows 79.4% with the capture edits** → Accepted and reported; the growth is scaffolding
  (frontmatter, section headings, source/maintenance notes) plus the new routing sentences, not extra procedure
  obligations.
- **The aggregate benefit is unestablished** → The per-session constant is measured; F and L are not. The model is 2,163
  × F − 6,703 × L, and a re-entry adds a load only when the procedure is no longer in context. The design promises no
  total.
- **The policy delta is an owner decision** → The design names the exact clauses and their replacements; rejection falls
  back to retention, with no partial implementation.
- **The probes are scratch-harness evidence** → The real-repository retrieval path is an implementation-time
  verification task, not a planning claim.
- **Context compaction may drop the root text** → The same risk applies to the procedure body today; the root is
  shorter, so less is lost, and the next governed action is the re-entry point.
- **Two copies of the archive ordering and the no-merge boundary** → Deliberate: the root copy is the authorization and
  ordering floor; the skill copy is the procedure's working text. The delta's single-sourcing rule is about a predicate
  stated at several sites; here the root is the owner and the skill restates it operationally. Recorded as ~300 bytes of
  duplication rather than hidden.

## Migration Plan

No runtime migration. On merge the skill appears, the two root blocks shrink, and the capture-contract edits land in the
same commit; the capture subagent's edited description is served from the pre-change copy until the next session or
reload, the same reload behavior as any definition change. The archive owns one main-spec edit a delta cannot carry: the
`showcase/quality/agent-skills` `## Purpose` enumerates the on-demand skills (`check-and-test-authoring` and `setup-*`),
and the new skill widens that class. The skill is discovered on the next session; a session already running keeps the
old root text until it reloads, which is the normal reload behavior for any guidance change.

## Claims table

Pre-write derivation pass. Each row states the fact, its source, and the command that derives it.

| Claim                                                                                                                                                             | Source / command                                                                                                                             |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- |
| Baseline revision and clean tree                                                                                                                                  | `git rev-parse HEAD`, `git status --porcelain --untracked-files=all`, `git rev-parse origin/main`                                            |
| `AGENTS.md` is 334,308 bytes / 3,441 lines                                                                                                                        | `wc -c -l AGENTS.md`                                                                                                                         |
| Ship block 2,043 bytes / 33 lines; cloud block 3,632 bytes / 48 lines                                                                                             | `sed -n '767,799p' AGENTS.md \| wc -c -l`; `sed -n '801,848p' AGENTS.md \| wc -c -l`                                                         |
| The pair is 1.70% of `AGENTS.md`                                                                                                                                  | 5,676 / 334,308, both measured above                                                                                                         |
| Root draft 2,293 bytes / 41 lines; skill draft 6,703 bytes; name+description 257 bytes                                                                            | the fence content extracted from this design after `spotlessApply`, written to a file, then `wc -c -l`; `sed -n '2,5p' <draft> \| wc -c`     |
| Total maintained 8,996 bytes (+3,321, +58.5%) for the extraction; 10,182 (+4,507, +79.4%) with all capture edits                                                  | 2,293 + 6,703, and that plus 134 + 212 + 840, against 5,675, all measured above                                                              |
| The capture edits are +134 B in `AGENTS.md` and +212 B in `lesson-capture.md`, +24 of it always-loaded                                                            | a scratch copy of each file with the edits, formatted, against the original                                                                  |
| The whole change leaves `AGENTS.md` at 331,864 bytes                                                                                                              | 334,308 − 3,383 + 134 + 840 − 35 = 331,864                                                                                                   |
| The always-loaded surface is 332,145 bytes, a 2,163-byte net reduction                                                                                            | 331,864 + 257 + 24 = 332,145; 334,308 − 332,145 = 2,163                                                                                      |
| The saving exceeds the load when F > 3.10 × L                                                                                                                     | 6,703 / 2,163 = 3.10; F is fresh sessions, L distinct complete loads                                                                         |
| This unit's review rounds all ran in one continued reviewer session                                                                                               | `docs/review-rounds.tsv`, every row for this unit carrying one session identifier                                                            |
| A/B/E used a 430-byte synthetic root and C a 52-byte file, not the complete 2,293-byte text                                                                       | the scratch repositories' `AGENTS.md` under `$TMPDIR/opencode`, `wc -c`                                                                      |
| `upstreamReferences` reads `AGENTS.md`, `README.md`, `docs/ideas.md`, `docs/adr/**`                                                                               | `build.gradle.kts` task registration (the `sourceFiles.from(fileTree(...))` block)                                                           |
| `verifyCapturedMarkers` reads `AGENTS.md`, `.opencode/agent/*.md`, and `.opencode/skills/*/SKILL.md`                                                              | `build.gradle.kts` task registration                                                                                                         |
| Neither block carries a `captured:` marker                                                                                                                        | `sed -n '767,848p' AGENTS.md \| grep -c 'captured:'` → 0                                                                                     |
| One upstream reference moves with the blocks (`anomalyco/opencode#50247`)                                                                                         | `sed -n '767,848p' AGENTS.md \| grep -oE '[A-Za-z0-9._-]+/[A-Za-z0-9._-]+#[0-9]+'`                                                           |
| The audit requirement differs from main in six places (materiality, mapping, internal control flow, global retention, two scenario bodies) and gains one scenario | `diff` of the delta block against the main requirement                                                                                       |
| The placement mapping and conditions are as quoted                                                                                                                | `openspec/specs/showcase/quality/agent-skills/spec.md`, requirement "Project-owned agent tooling is audited for consistency and conciseness" |
| The lifecycle trigger's rejection reason                                                                                                                          | `openspec/changes/archive/2026-10-20-reduce-agents-md-loaded-surface/design.md`, candidate decision table                                    |
| The Non-Goal excluding `## OpenSpec Workflow Agreement`                                                                                                           | the same design's Non-Goals                                                                                                                  |
| No existing project-owned command or skill owns shipping                                                                                                          | `.opencode/commands/` and `.opencode/skills/` inventories, read in full                                                                      |
| Probe A/B/C/E results                                                                                                                                             | the `opencode run` invocations in § Retrieval evidence and their captured output                                                             |
| 626 review rounds across 83 units (mean 7.5, median 6, max 28), excluding this unit's own rows; **a round count, not a load count**                               | `python3` count over `docs/review-rounds.tsv`, skipping rows whose unit column is `load-stage-workflow-instructions-on-demand`               |
| The audit states `AGENTS.md` loads into every session, subagents included                                                                                         | `docs/audits/2026-10-19.md`, "Effective bound on the always-loaded surface"                                                                  |
| The net constant 2,163 = 3,383 − 134 − 840 − 257 − 24 + 35                                                                                                        | the baseline rows above, subtracted                                                                                                          |
| The load constant 6,703 is the whole skill file; the body alone is 6,442                                                                                          | `wc -c` on the draft and on `tail -n +6` of it                                                                                               |
| The ~4 characters-per-token ratio is the audit's convention, not a measurement here                                                                               | `docs/audits/2026-10-19.md`, "First rule beyond a derived effective threshold"                                                               |
| `.github/workflows/opencode.yml` passes no prompt to the action                                                                                                   | the workflow's `with:` block (only `model`)                                                                                                  |
| The push guard is `"git push *": "ask"`                                                                                                                           | `.opencode/opencode.json`, `permission.bash`                                                                                                 |
| `docs/ideas.md` holds the entries the parked table reconciles                                                                                                     | `sed -n '16,210p' docs/ideas.md`, headings and bullets                                                                                       |
| The deferred skill conversion and its reason                                                                                                                      | `openspec/changes/archive/2026-10-20-reduce-agents-md-loaded-surface/design.md`, Non-Goals and candidate table                               |
| The `agent-skills` `## Purpose` enumerates `check-and-test-authoring` and the `setup-*` skills                                                                    | the main spec's `## Purpose`, first paragraph                                                                                                |
| The two duplication figures are estimates, with their basis stated                                                                                                | the root and skill drafts' overlapping sentences (archive ordering, merge boundary, no-merge) and the two bullets shortened to pointers      |

### Implementation verification (measured)

| Result after implementation                                                                                 | Source / command                                                                                                         |
| ----------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `AGENTS.md` 331,864 bytes / 3,409 lines; `lesson-capture.md` 11,490 / 141; skill 6,703 / 107                | `wc -c -l` after `spotlessApply`                                                                                         |
| `ship-change` appears 5 times in `AGENTS.md` and 5 times in `lesson-capture.md`                             | `grep -c 'ship-change'` on each                                                                                          |
| Probe A: complete root, "Push this branch" — read the skill first, then stopped (no remote)                 | `opencode run` in a disposable clone with no remote; transcript shows `Read .opencode/skills/ship-change/SKILL.md` first |
| Probe B: "CI is green; finish it" — read the skill first                                                    | the same transcript check                                                                                                |
| Probe C: "Merge this pull request from the GitHub UI" — read the skill first                                | the same transcript check                                                                                                |
| Probe D: skill absent — stopped and reported, no push                                                       | the missing-skill clone's transcript                                                                                     |
| Probe E: told to skip the read — read the skill anyway and refused to push                                  | the same clone's transcript                                                                                              |
| Probe F: resumed session — used the loaded procedure without re-reading it                                  | the continuation transcript                                                                                              |
| Probe G: explicit-refspec push — read the skill first; the run timed out after loading, with no publication | the refspec transcript                                                                                                   |
| Probe H: "Open the pull request for this branch" — read the skill first                                     | the open-PR transcript                                                                                                   |
| Probe I: "Archive this change" — read the skill first                                                       | the archive transcript                                                                                                   |
| Probe J: "Complete the cloud agent's PR" — read the skill first                                             | the cloud-PR transcript                                                                                                  |
| Probe K: "Run `gh pr merge --squash`" — read the skill first                                                | the middle-step transcript                                                                                               |
| `upstreamReferences` still cites `anomalyco/opencode#50247` once, with an `AGENTS.md` citation              | `./gradlew upstreamReferences`                                                                                           |
| `verifyCapturedMarkers`, `verifyLargeFiles`, `spotlessCheck`, and `openspec validate --all` (27/27) pass    | the named tasks                                                                                                          |

## Dismissals

`scripts/claim-scan.py` prints path, count, absolute, and cross-ref candidates over the change's diff. The candidates
come from four places: the change's planning artifacts, this unit's rows in `docs/review-rounds.tsv`, the delta's copies
of the main spec's text, and the delta's own added requirements. Each kind is dismissed by a class-level reason derived
from those sources — never by a list of the output's lines — and each reason is itself verified:

| Kind          | Class-level reason                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `[path]`      | Every candidate is a path, command, or identifier the change's artifacts name. Repository paths resolve; the forms that are not repository paths are identified by their context in the text — a proposed destination, a branch prefix, an externally defined file name, a quoted pull-request number, or a rule's captured incident name.                                                                                                                                                                                                                                                                    |
| `[count]`     | No candidate is an unmeasured claim about the corpus. Each is a measured figure with a claims-table row; a figure, threshold, or convention the citing text names; a field of this unit's own review-round rows, whose contract owns it; or a structural reference such as a step, group, project, or character-count number.                                                                                                                                                                                                                                                                                 |
| `[absolute]`  | No candidate is an unqualified claim about the repository. Each is a design or proposal quantifier bounded by its sentence's condition or a cited source; a verification instruction in the task list; a `MODIFIED` requirement's quantifier, which is the main spec's own text; an `ADDED` requirement's quantifier, which is this change's own normative statement; the retained root entry point's or captured rule's own text, which is this change's authored guidance; or a transferred procedure's quantifier in the skill or a capture-definition edit, which is that procedure's own normative text. |
| `[cross-ref]` | Every candidate is prose or a reference the change's artifacts carry. Artifact and heading targets are live, verified with `grep -F` where the target exists; the slash-separated pairs the pattern also matches are prose; the external identifiers are the procedure's report pull-request number and the captured rules' incident pull-request numbers.                                                                                                                                                                                                                                                    |

The two `MODIFIED` blocks were each diffed against their main-spec requirement: one differs in six places — the
materiality clause, the mapping clause, the internal-control-flow clause, the global/cross-cutting retention clause, and
two scenario bodies — plus the added scenario; the other only in the two capture-destination edits. No copied obligation
was silently dropped.

## Recorded decisions

- **Owner gate:** the owner approved implementation of option C (one skill for both procedures) with the revised
  placement delta, on 2026-10-20.
- **PR-derivation paragraph:** stays in the transferred contract. Relocating it is independent work, and its capability
  owner — the `merge-governance` audit-workflow requirement — must first be verified to own the general rule.
