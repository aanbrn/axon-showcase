# Review rounds and their dispositions

The record of every quick-review round this unit has run, kept because a round's finding instances live only in its
report and the next round's hand-over must pass each one. The unit's `Resume checkpoint` is in `tasks.md`.

Round 1 ran against the base revision `0d8ef3f8`; the branch was later refreshed onto `ea1ee3b5` and the planning
artifacts were re-derived against it. Refresh 2 (before round 20) fast-forwarded onto `be23206`, where #708 had touched
`README.md`, `docs/ideas.md`, and this ledger; the three files were reconciled so both sides' additions survive.

| #   | Class                | Where             | Finding                                                                                                                                                 | Disposition                                                                                                                             |
| --- | -------------------- | ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | dropped-content      | trace rows D5, D6 | Marked `Retained`, but the added requirement carried neither clause.                                                                                    | Fixed: both clauses now sit in the analysis-contract scenario; the trace's destination column is accurate.                              |
| 2   | impact-omission      | delta             | `spec.md:916` states "the per-unit review and lesson-capture agents", which the change falsifies; no `MODIFIED` block existed.                          | Fixed: a `MODIFIED` block carries that requirement with the phrase retargeted, and the requirement's five scenarios verbatim.           |
| 3   | contradiction        | tasks 1.2, 2.2    | "Unchanged"/"byte-identical" was wrong for text the change retires (`AGENTS.md`'s "the capture record", `lesson-capture.md`'s marker rule).             | Fixed: tasks 1.13 and 2.2 now repoint that text and no longer claim it unchanged.                                                       |
| 4   | impact-omission      | live sites        | Four sites stating the per-unit model had no task: `AGENTS.md`'s experience-analyzer bullet, `lesson-capture.md:13/48–49/95`, `docs/ideas.md`'s header. | Fixed: tasks 1.15, 2.2, 2.5, 2.6 and 2.7 name each site.                                                                                |
| 5   | missing-scenario     | delta             | The retired merge scenario (3.1/3.2) had no replacement contract text.                                                                                  | Fixed: the merge duty is an `AND` in the recording scenario, and `AGENTS.md` points at it.                                              |
| 6   | claim-unreproducible | claims table      | Three rows recorded counts the commands did not return, one cited the experience-analyzer's input as the capture's, and one had no reproducing source.  | Fixed: every row re-derived against `ea1ee3b5`, with the actual counts and the observation named where the claim is an analogy.         |
| 7   | contradiction        | proposal Impact   | It listed "the completion criteria" while a task said no change was owed.                                                                               | Resolved by the merge: `61bcdee2` put the capture record into the completion criteria, so the item stands and task 2.3 covers the edit. |
| 8   | contradiction        | tasks 1.5 vs 5.3  | The record's analysis was called a later unit while the plan also ran this unit's inline capture.                                                       | Fixed: task 1.17 distinguishes the inline capture (5.3) from the record's later `Decision`.                                             |
| 9   | test-control         | task 4.4          | The gate's negative case was described, never exercised.                                                                                                | Fixed: task 4.4 requires both states exercised by an invocation or a scripted assertion, with both outcomes recorded.                   |
| 10  | structured-writing   | tasks 1.1, 2.1    | Each bullet packed a dozen independently actionable requirements.                                                                                       | Fixed: section 1 is one bullet per rule; task 2.1's required content is a nested bullet list, one element per line.                     |
| 11  | formatting           | claims table      | Three rows were fused into one line with literal escape separators, which Prettier does not repair.                                                     | Fixed: the table is 17 well-formed rows, and a grep for the literal separator returns nothing.                                          |
| 12  | impact-omission      | proposal Impact   | It omitted the two remaining agent definitions, the `docs/ideas.md` edits, and the archive-time Purpose refresh.                                        | Fixed: each is named.                                                                                                                   |
| 13  | contradiction        | tasks 1.1 vs 1.2  | Task 1.1's replaced range included the subsection task 1.2 moved.                                                                                       | Fixed: 1.1 replaces `:463–569`; 1.13 moves `:570–605`.                                                                                  |

## Reviewer's checks that held at round 1

`openspec validate --strict` valid and `--all` 27/0; all 39 trace rows present; the base-revision anchors reproduced; no
120-character finding outside formatter-owned table rows; `git status` showed only the change directory.

## Round 2

Round 2 ran against `ea1ee3b5` after the refresh and reported 12 findings; five classes repeated round 1, and the
`impact-omission` recurrence was traced to a hand-maintained site list rather than a derived one.

| #   | Class                  | Finding                                                                                                                   | Disposition                                                                                                                  |
| --- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| 1   | impact-omission        | `AGENTS.md:454` (the review-gate bullet) states the capture record; no task covered it.                                   | Fixed: task 1.16 names it, and the design's site derivation lists it.                                                        |
| 2   | impact-omission        | `AGENTS.md:1341` and `lesson-capture.md:106–107` still stated the per-unit model, unnamed.                                | Fixed: tasks 1.16 and 2.2 name them.                                                                                         |
| 3   | impact-omission        | Five spec references to "the capture requirement" would not resolve after a retitle.                                      | Fixed by design D9: the new header keeps "captured", so the references resolve; task 3.2 verifies each rather than counting. |
| 4   | claim-unreproducible   | `:168` should be `:186`; `:1023` should be `:1027`, and two prose sites read the analogy as a statement about this input. | Fixed: both anchors corrected, and the Context and proposal now say the property holds by analogy.                           |
| 5   | contradiction          | Task 2.3's verification grep would match the `lesson-capture` reference the task must keep.                               | Fixed: the grep is scoped to the retired phrases.                                                                            |
| 6   | coherence              | Tasks and proposal described the delta as `REMOVED` + `ADDED` only.                                                       | Fixed: both now name the `MODIFIED` block.                                                                                   |
| 7   | coherence              | Tasks 3.1, 3.2 and 3.4 instructed edits the delta already carried.                                                        | Fixed: they are confirmations now, and the unresolved-reference check moved to the archive step.                             |
| 8   | missing-task           | Eight section-1 tasks carried no verification of their own.                                                               | Fixed: every task states its verification.                                                                                   |
| 9   | incomplete-enumeration | Task 4.7's own artifact list omitted the planning copies that name the field set.                                         | Fixed: the task reads every created artifact and scopes the durable ones, permitting the planning copy.                      |
| 10  | structured-writing     | Task 1.9 packed three rules.                                                                                              | Fixed: split into 1.9, 1.10 and 1.11.                                                                                        |
| 11  | dropped-content        | The added requirement's description lost the SHALL that binds the generalizing unit to the whole contract.                | Fixed: restored, and the description stays under the length warning.                                                         |
| 12  | wording                | The `MODIFIED` block's appositive named an activity where the bearer list names subagents.                                | Fixed: it names the `lesson-capture` agent.                                                                                  |

Root-cause note: `impact-omission` repeated because the plan enumerated sites from prose. `design.md`'s "Site
derivation" now owns that set as a command and a classification, and task 4.2 reconciles the command's output instead of
a list.

## Round 3

| #   | Class                  | Finding                                                                                              | Disposition                                                                                        |
| --- | ---------------------- | ---------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------- |
| 1   | impact-omission        | `spec.md:1089` lists "the capture decision" among the review gate's components; no block covered it. | Fixed: a whole `MODIFIED` block for the reduction-unit requirement, substituting the record check. |
| 2   | incomplete-enumeration | The derivation's pattern under-matched its own plan (`AGENTS.md:1123` fell out).                     | Fixed: the pattern was rebuilt from the subjects and 1.16 names `:1123`.                           |
| 3   | docs-residue           | Two live `docs/ideas.md` entries were absorbed by the dated-record class.                            | Fixed: task 2.8 repoints them; the class excludes live entries.                                    |
| 4   | claim-unreproducible   | Claims row 7 still recorded `:168`.                                                                  | Fixed: `:186`.                                                                                     |
| 5   | claim-unreproducible   | The delta's `Reason` restated the session-only property as a statement about this input.             | Fixed: it states the analogy.                                                                      |
| 6   | coherence              | The `Reason` quoted the requirement's old title word.                                                | Fixed: it quotes the header as it stands.                                                          |
| 7   | incomplete-enumeration | D9 counted references, and task 3.2's pattern missed `capture's promotion`.                          | Fixed: D9 names the forms without a count; 3.2's pattern includes the possessive.                  |
| 8   | wording                | Task 3.5 was phrased as an edit already carried.                                                     | Fixed: a confirmation.                                                                             |

## Round 4

| #   | Class                  | Finding                                                                                                                                     | Disposition                                                                                                                                                            |
| --- | ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | incomplete-enumeration | The two-way rule closed in one direction only: the wide pattern produced false positives the catch-all row absorbed, so 4.2 could not fail. | Fixed: the design now closes the _scope_ (the durable surfaces) and classifies every hit by sense; the pattern is a checklist, and a hit no sense names is a plan gap. |
| 2   | missing-task           | The record stopped at round 2 and the checkpoint said rounds 1–2.                                                                           | Fixed: this file carries rounds 3 and 4, and the checkpoint is refreshed.                                                                                              |
| 3   | coherence              | Three places described the delta as three blocks; it has five.                                                                              | Fixed: one `REMOVED`, one `ADDED`, three `MODIFIED`, stated in each.                                                                                                   |
| 4   | wording                | Task 3.6 said one phrase each; the authoring-standard block substitutes twice.                                                              | Fixed: three occurrences across the two blocks, plus the formatter's rewrap.                                                                                           |

## Round 5

| #   | Class                  | Finding                                                                                                                                                                      | Disposition                                                                                                                                                                                        |
| --- | ---------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | incomplete-enumeration | The `\b`-anchored alternatives were inert in `git grep`, so the derivation could not see the file task 1.20 edits.                                                           | Fixed by removing the claim: completeness is now per file (design's "Site sweep"), each editing task sweeps its own file, and the pattern is proved against a known line before a zero is trusted. |
| 2   | incomplete-enumeration | Hits fit none of the eight senses (a merge-time read, historical citations and filenames in `AGENTS.md`, the gate's name inside requirements, live `docs/ideas.md` entries). | Fixed by the per-file rule: survivors are recorded by sense per file, and the sense list is stated as the only allowed classes.                                                                    |
| 3   | coherence              | The "live" row named two phrases that tasks 2.6 and 1.16 edit.                                                                                                               | Fixed: the sense list no longer assigns a phrase to a class globally; the editing task records the sense at the site.                                                                              |

Process incident: round 4's ledger row was appended late — written only when round 5's `--expect` check failed — and the
missing row is recorded here rather than reconstructed as timely.

## Round 6

| #   | Class                  | Finding                                                                                             | Disposition                                                                                                |
| --- | ---------------------- | --------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| 1   | coherence              | The per-file sweep was stated in the design but implemented by no editing task except 1.16 and 4.2. | Fixed: one owner — task 4.2 applies `design.md`'s "Site sweep"; every other site points at it.             |
| 2   | incomplete-enumeration | The four-sense list dropped the generic-participle sense.                                           | Fixed: the fifth sense ("a different sense of the word") is in the design and task 4.2 points at the list. |
| 3   | missing-task           | The checkpoint still said rounds 1–4.                                                               | Fixed: rounds 1–6, six rows, round 7 next.                                                                 |
| 4   | docs-residue           | `tasks.md` still called the section a "site derivation".                                            | Fixed: "site sweep".                                                                                       |

## Round 7

| #   | Class           | Finding                                                             | Disposition                                                                                  |
| --- | --------------- | ------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| 1   | single-sourcing | Three sites restated the sweep rule, one with a shorter sense list. | Fixed: the design owns the rule and its five senses; the preamble, 1.16 and 4.2 point at it. |
| 2   | missing-task    | The checkpoint and handover lagged a round.                         | Fixed: rounds 1–7, seven rows, round 8 next; round 6 added here.                             |
| 3   | docs-residue    | `tasks.md` still said "site derivation".                            | Fixed: "site sweep".                                                                         |

## Round 8

| #   | Class           | Finding                                                                                                               | Disposition                                                       |
| --- | --------------- | --------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------- |
| 1   | impact-omission | The proposal's Impact missed `pause-resume-unit/SKILL.md` (task 1.20) and the two live `docs/ideas.md` entries (2.8). | Fixed: both named.                                                |
| 2   | formatting      | The sense list's fourth item ended with a period and the fifth with a semicolon.                                      | Fixed: the fourth ends with a semicolon, the fifth with a period. |

## Round 9

| #   | Class                | Finding                                                                                                        | Disposition                                                     |
| --- | -------------------- | -------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------- |
| 1   | claim-unreproducible | Two sites quoted the `pause-resume-unit` Stage field as "the capture decision"; the field reads "the capture". | Fixed: the proposal and task 1.20 quote the field as it stands. |

## Round 10

| #   | Class           | Finding                                                                                                  | Disposition                                            |
| --- | --------------- | -------------------------------------------------------------------------------------------------------- | ------------------------------------------------------ |
| 1   | single-sourcing | Task 1.20's verification restated the sweep's known-line control, which the design assigns to 4.2 alone. | Fixed: the clause is gone; 1.20 defers to 4.2's sweep. |

## Round 11

Clean — no finding, no class. The planning stage closed here.

## Round 12

| #   | Class        | Finding                                                                          | Disposition                                                                   |
| --- | ------------ | -------------------------------------------------------------------------------- | ----------------------------------------------------------------------------- |
| 1   | docs-residue | `lesson-capture.md`'s marker rule still carried the merge-time clause.           | Fixed: narrowed to the record's unit, as `AGENTS.md` states it.               |
| 2   | docs-residue | `README.md`'s loop bullet still described a merge-time capture.                  | Fixed: it states the merge-append rule.                                       |
| 3   | missing-task | A parked `docs/ideas.md` entry quoted the retired checklist text.                | Fixed: it quotes the process-defect record.                                   |
| 4   | test-control | Four task verifications did not hold on the shipped result (1.2, 1.4, 2.4, 2.5). | Fixed: the checks match the shipped text, and 2.5's second clause is applied. |

## Round 13

| #   | Class        | Finding                                                                                                        | Disposition                                                                               |
| --- | ------------ | -------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| 1   | missing-task | The handover ended at round 10 and the checkpoint claimed ten rows.                                            | Fixed: Round 11 (clean) and Round 12 added; the count corrected.                          |
| 2   | missing-task | No task was ticked although the implementation was done.                                                       | Fixed: 39 ticked; 3.4, 4.3, 4.4 and 5.1–5.5 stay open for archive-time work and evidence. |
| 3   | wording      | The generalization completion criteria said "applied observations" where the requirement says "applied delta". | Fixed: "the applied net delta … is recorded".                                             |

## Round 14

| #   | Class        | Finding                                                            | Disposition                                                                           |
| --- | ------------ | ------------------------------------------------------------------ | ------------------------------------------------------------------------------------- |
| 1   | missing-task | The checkpoint was a round stale again (round 12 state, ten rows). | Fixed: rounds 1–14, fourteen rows, the open-task list, and round 15 as the next step. |

## Round 15

Clean — no finding, no class. The quick-review loop closed here; the required deep review is the next gate stage.

## Deep review (`review-thorough`, session `ses_e84eb0645ffeaODOmPF0AR6uqO`)

Not clean: 2 should-fix findings, no blocking defect.

| #   | Finding                                                                                                                                                                 | Disposition                                                                                                                                                                                                                                                                                                                                                  |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | The delta renamed the analysis stage in `review-quick.md` but left five spec requirements calling it "capture", so the spec and the definition disagreed after archive. | Fixed by keeping the corpus's vocabulary instead of renaming it: `review-quick.md`'s "A capture"/"capture evidence"/"capture contract" and `AGENTS.md`'s "capture loop" are restored, which is what design D9 already decided (capture names the analysis). The reduction block's "capture bullet" pointer stays valid, and no new `MODIFIED` block is owed. |
| 2   | Tasks 4.3 and 4.4 were open and miscategorized, so the command's selection surface and the gate's new precondition shipped unexercised.                                 | Fixed: 4.3 measured (3 records undecided before; 2 after one `Decision` appended in a scratch copy), and 4.4 exercised on both states — no record fails, a current record passes, and §5 carries the clause. Both ticked.                                                                                                                                    |

## Round 16 (post-deep-review fixes)

| Class        | Finding                                                                                                                                     | Disposition                                          |
| ------------ | ------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------- |
| coherence    | Two ticked tasks still asserted changes the D9 vocabulary decision reverted (`:1419`'s "capture loop", `experience-analyzer.md`'s pointer). | Fixed: both recorded as live-reading survivors.      |
| missing-task | The checkpoint still pointed at the deep review as pending.                                                                                 | Fixed: it records the deep review and its two fixes. |

## Round 17

| Class                | Finding                                                   | Disposition                                       |
| -------------------- | --------------------------------------------------------- | ------------------------------------------------- |
| coherence            | Task 1.16 still listed `:1419` among the repointed sites. | Fixed: it defers that record to task 4.2's sweep. |
| claim-unreproducible | The checkpoint's row count lagged one round.              | Fixed: seventeen.                                 |

## Round 18

| Class        | Finding                                              | Disposition                                      |
| ------------ | ---------------------------------------------------- | ------------------------------------------------ |
| missing-task | This file was two rounds behind (no Round 16 or 17). | Fixed: both added here, with this round's entry. |

## Round 19

Clean — no finding, no class.

## Round 20 hand-over (replacement reviewer)

Round 20 continues the loop after a writer handover, and its reviewer session had to be replaced. Recorded before the
replacement was invoked, so the context does not depend on an invocation that could not run.

- **Continuity record**: session `ses_e890d2d66ffeBhPAaMQrN7zuqg` (rounds 1–19) could not be continued — the harness
  refused the continuation with `Session ses_e890d2d66ffeBhPAaMQrN7zuqg is not a child of the current session`, the
  writer having moved to `ses_e84c9b955ffeY6V3z6ZN1VELva`. Round 20's ledger row records a `replaced` transition with
  reason `expired` and this round's session identifier; the prior identifier stays here. The prior session's findings
  below are its instance-level evidence, carried forward as context.
- **Unit and workspace**: `separate-lesson-capture-from-implementation`, tree
  `/Users/afanasyev/Development/axon-showcase-separate-lesson-capture-from-implementation`, branch of the same name.
- **Revisions**: base `be232061` (`origin/main`, refreshed before this round); `HEAD` is that commit, and the whole
  implementation is staged and uncommitted.
- **Supplied artifacts**: the change dir (`proposal.md`, `design.md`, `tasks.md`, `capture-report.md`,
  `review-handover.md`, and the delta at `specs/showcase/quality/agent-skills/spec.md`), the diff (`git diff HEAD`, 22
  files), and `docs/process-defects/separate-lesson-capture-from-implementation.md`.
- **Scope**: the implementation-stage screen over the tasks and the delta, plus the five areas that changed after round
  19 closed — the `check-and-test-authoring` catch-all mode, the capture report's applied dispositions and figures, task
  5.4's smoke-run record, the `be23206` reconciliation of `README.md`/`docs/ideas.md`/`docs/review-rounds.tsv`, and this
  record's Round 19 entry and writer update.
- **Prior findings**: rounds 1–18 in the tables above, round 19 clean, and the two deep-review findings fixed.
- **Completed checks**: `spotlessApply` run over the refreshed tree; both ledger checks pass at round 19
  (`--unique-rounds --expect …:19` and `--session-continuity`).
- **Known limitations**: the build gates (`spotlessCheck`, `openspec validate --all`, `check`) are re-run after this
  round's fixes, so no round has yet read their output on the final tree; the smoke-run ran in a scratch copy, not here.

## Round 20

| #   | Class                  | Where                       | Finding                                                                                                                                            | Disposition                                                                                                               |
| --- | ---------------------- | --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| 1   | missing-task           | checkpoint                  | It still recorded `ea1ee3b5`, "four of five applied", the smoke-run as next, and 12 unstaged.                                                      | Fixed: rewritten from the live state.                                                                                     |
| 2   | claim-unreproducible   | Context                     | The requirement range `spec.md:2145–2261` reproduces neither boundary; the claims row states `:2133`.                                              | Fixed: `:2133–2250`.                                                                                                      |
| 3   | claim-unreproducible   | claims row 8                | The recorded `-E` command escapes its pipes, so it exits 1 and states positions its own source cannot produce.                                     | Fixed: pipes unescaped; the command returns the four scenarios at `:2141`, `:2225`, `:2233`, `:2241`.                     |
| 4   | claim-unreproducible   | defect record, Evidence     | "round 3 named the omitted `AGENTS.md:454` and `:1419`" — `:454` is round 2, `:1419` is a live-reading survivor, and round 3 named `spec.md:1089`. | Fixed: each attributed to the round that named it, and `:1419` recorded as the survivor it is.                            |
| 5   | incomplete-enumeration | defect record, Evidence     | `incomplete-enumeration` is given as rounds 2–5, but round 6 carries it too.                                                                       | Fixed: rounds 2–6.                                                                                                        |
| 6   | docs-residue           | `experience-analyzer.md:34` | "reuse the AGENTS.md gotchas and per-change captures" still names the retired per-unit capture, and no task covered the site.                      | Fixed: repointed to the units' `docs/process-defects/` records, and task 2.5's site list now names it.                    |
| 7   | impact-omission        | proposal Impact             | The diff edits `.opencode/skills/check-and-test-authoring/SKILL.md`, which Impact did not name.                                                    | Fixed: named, with its provenance.                                                                                        |
| 8   | claim-unreproducible   | round-20 hand-over          | It said the ledger row names both session identifiers, but a row has one and carries `expired` as the replacement's reason.                        | Fixed: a `replaced` transition with reason `expired` plus this round's identifier, with the prior identifier stated here. |
| 9   | over-claim             | capture report, opening     | "Its full text … is reproduced below by reference" — the table carries one line per proposal, with no decision difference or evidence.             | Fixed: the opening states what the table holds, and that the subagent's report is not reproduced.                         |
| 10  | wording                | new authoring mode          | "this unit's site sweep" names no unit, where every sibling mode names its own in prose.                                                           | Fixed: names `separate-lesson-capture-from-implementation`.                                                               |

## Round 21

| #   | Class                  | Where                     | Finding                                                                                                                                     | Disposition                                                                                                      |
| --- | ---------------------- | ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | formatting             | claims row 8              | Round 20's fix unescaped the pattern's pipes, and a bare `\|` in a table cell splits the row, so the command no longer read as one command. | Fixed: one `-e` pattern per alternative, so the cell holds no pipe; the command returns the same four scenarios. |
| 2   | incomplete-enumeration | defect record, `Observed` | Its sibling still said "rounds 2–5 repeated the same class", contradicting the corrected `Evidence` line one bullet below.                  | Fixed: rounds 2–6.                                                                                               |

## Round 22

| #   | Class         | Where               | Finding                                                                                                                                                                                        | Disposition                                                                                                                                   |
| --- | ------------- | ------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | contradiction | `tasks.md:17`/`:24` | The checkpoint's next step named round 22 while its pending-decision line still said the manual pass follows round 21, and its revision line read the refresh as an event of the current turn. | Fixed: the whole checkpoint was re-derived from the round's state rather than patched field by field, as the finding's root-cause note asked. |

## Round 23

Clean — no finding, no class.

## Deep review, follow-up pass (routing `REQUIRED` after round 23)

- **Continuity record**: the change's deep-review session `ses_e84eb0645ffeaODOmPF0AR6uqO` (round 15) could not be
  reused — the harness refused the continuation with
  `Session ses_e84eb0645ffeaODOmPF0AR6uqO is not a child of the current session` after the writer handover, the same
  mechanical cause as the quick reviewer's replacement at round 20. A replacement pass was started, and its identifier
  is recorded with its findings below.
- **Routing**: `REQUIRED`, from `scripts/review-routing.py --change separate-lesson-capture-from-implementation`, on
  `infrastructure-or-deployment` (`.github/PULL_REQUEST_TEMPLATE.md`), `requirement-block-added`, and
  `requirement-block-removed` (the delta).

## Deep review, replacement pass (session `ses_e84a8ccd8ffeoxQ2zd0QXyeKdl`)

3 findings (1 should-fix, 2 nitpick), no blocking defect; every substantive check passed (delta valid and strict, the
obligation trace resolving clause by clause, the `MODIFIED` blocks' scenarios verbatim, the retired-vocabulary sweep
clean, the ledger checks green).

| #   | Class                | Finding                                                                                                                                                                                             | Disposition                                                                                                                |
| --- | -------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| 1   | docs-residue         | `lesson-capture.md`'s input list still required "a short note from the main agent … essential", the session-bound input the change replaces, after the sibling bullet was repointed to the records. | Fixed: the bullet is gone; the selected records are the durable note, so no separate session note is owed.                 |
| 2   | missing-task         | The `Resume checkpoint` trailed the closed round 23 and omitted the deep review.                                                                                                                    | Fixed: the stage names the close of round 23 and the deep pass, and the next step is round 24 and the manual-pass request. |
| 3   | claim-unreproducible | The claims table's preamble called `ea1ee3b5` "`git merge-base HEAD origin/main`", which the second refresh moved to `be232061`.                                                                    | Fixed: the preamble names it a fixed base revision, which is why its commands still reproduce.                             |

## Round 24

| #   | Class         | Where                    | Finding                                                                                                                                                                                                            | Disposition                                                                                                                                                 |
| --- | ------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | coherence     | `lesson-capture.md:24`   | The input list required the recording unit's change dir, which no caller supplies — the command, `AGENTS.md`'s step and the delta's caller clause name only the records, their evidence and the existing guidance. | Fixed: the bullet is gone, and the evidence bullet absorbed its sibling, since the delta makes the record self-contained for a reader outside that session. |
| 2   | contradiction | `tasks.md` task 5.3 note | It said the capture's proposals were "pending application" where the tree and the capture report show all five applied.                                                                                            | Fixed: the note states all five are applied.                                                                                                                |

## Round 25

| #   | Class                | Where                   | Finding                                                                                                                                              | Disposition                                                                                                                                                           |
| --- | -------------------- | ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | claim-unreproducible | checkpoint's change-set | The distribution said 10 staged modifications and 3 partial where `git status` showed 9 and 4, because `lesson-capture.md` was edited after staging. | Fixed: every path re-staged, so the field now states 22 paths, all staged — 9 additions and 13 modifications — and it reproduces from `git status --porcelain -uall`. |

## Round 26

| #   | Class        | Where                    | Finding                                                                                                                                                   | Disposition                                                                                                                           |
| --- | ------------ | ------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | missing-task | hand-over and checkpoint | Round 25's close was unrecorded: no `## Round 25` table, the checkpoint still named round 24's close, and its pending-decision and findings lines lagged. | Fixed: this table and the round-25 one added, and the whole checkpoint re-derived to round 26's close rather than one field advanced. |

## Round 27

| #   | Class        | Where                   | Finding                                                                                                        | Disposition                                                                             |
| --- | ------------ | ----------------------- | -------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| 1   | missing-task | hand-over's round index | No `## Round 23` entry, though round 23 closed clean and this file records clean rounds as their own sections. | Fixed: `## Round 23` added after `## Round 22`, reading "Clean — no finding, no class". |

## Round 28

| #   | Class        | Where                   | Finding                                                                                                                                                       | Disposition                                                                                                                     |
| --- | ------------ | ----------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| 1   | missing-task | hand-over's round index | No `## Round 27` entry, while the checkpoint already claimed rounds 20–27's findings were recorded here — the checkpoint advanced a round ahead of the index. | Fixed: this table and the round-27 one added in the same edit that re-derives the checkpoint, which is `review-gate` §2's step. |

## Round 29

Clean — no finding, no class. The loop closed here.

## Round 30

| #   | Class                | Where                 | Finding                                                                                                                                                                                                                                                                  | Disposition                                                                                                                                                                                                                                         |
| --- | -------------------- | --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | claim-unreproducible | record #4             | It attributed the checkpoint-trailing occurrence to quick round 24, whose findings were the `lesson-capture` input list and task 5.3's note; the instance was the deep-review replacement pass's, it listed row 24 in a class it does not carry, and its count followed. | Fixed: the instance is attributed to the deep pass with its table (no ledger row), row 24 dropped, `claim-unreproducible` added for round 17, and the count stated as twelve quick rounds plus that pass.                                           |
| 2   | contradiction        | checkpoint            | Its stage named the close of round 30 while round 30 was the review being handed in, its next step and pending decision disagreed on whether the manual pass had been asked, and its findings line counted the round in flight.                                          | Fixed: the stage names round 29's close with round 30 open, the next step re-asks after this round, the pending decision states the request is outstanding because the advisor request did not answer it, and the findings line names rounds 20–29. |
| 3   | wording              | defect record, header | It said the defects were "observed while planning this change", though record #4 spans rounds 6–28.                                                                                                                                                                      | Fixed: "observed while working this change".                                                                                                                                                                                                        |

## Round 31

| #   | Class                  | Where     | Finding                                                                                                                                          | Disposition                                                                                                                  |
| --- | ---------------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------- |
| 1   | incomplete-enumeration | record #4 | Its enumeration ended at round 28 and its count at twelve, though round 30 raised the same subject and the record was edited after that finding. | Fixed: round 30 added to the observation and the row list, the count stated as thirteen, and the currency point as round 31. |

## Round 32

Clean — no finding, no class. The loop closed here, after the advisor consultation's correction.

## Round 33

| #   | Class        | Where              | Finding                                                                                                                                                                       | Disposition                                                                                                       |
| --- | ------------ | ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| 1   | over-claim   | README loop bullet | It still led with "Every unit closes the loop" and stated an unconditional outcome, where the improvement is a separate unit and a record may end as an explicit `no-change`. | Fixed: retitled "Every unit records what it learned", the outcome conditioned, and the `no-change` outcome named. |
| 2   | docs-residue | README loop bullet | "the analysis no longer sits between a change and your review" was a maintainer-facing contrast a README reader has no prior process for.                                     | Fixed: stated positively — "your review never waits on the analysis".                                             |

## Round 34

Clean — no finding, no class. The loop closed here, after the README correction.
