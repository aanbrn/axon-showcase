---
name: review-gate
description:
  The calling agent's procedure for the review gate — load it before the gate's first stage, once a unit's proposal or
  implementation is finished and before the unit is committed, pushed, opened as a PR, archived, or merged. It carries
  the stages, the decision points, the failure handling, and the completion criteria; `AGENTS.md` carries the trigger,
  the stage entry point, and the authorization stops.
---

# The review gate

The authoritative procedure for the stage `AGENTS.md` triggers. It grants no permission of its own: the invariants and
the authorization stops in `AGENTS.md` govern, and the owner's approval is the precondition for every guarded step.

Source: `AGENTS.md` "Workflow: The review gate".

**Trigger** — you have finished a change's proposal (planning artifacts) or its implementation, or any non-OpenSpec unit
(a docs refresh, a standalone fix, a dependency bump), and you are about to commit, push, open a PR, archive, or merge.

## 1. Pre-write derivation pass and claims table

1. Run the pre-write derivation pass over the planning artifacts, deriving each of the five claim classes the loop keeps
   catching from its source: read every run-log, issue-state, or external-artifact claim from the artifact now; re-grep
   every count — and every universal or absolute claim (`scripts/claim-scan.py`'s `absolute` class is a floor, not the
   full set) over a corpus or an enumerated list — against its set, naming the instances it covers where the class has
   no greppable predicate, and splitting a claim whose quantifier spans more than one part — a list carrying an
   exception (the members that carry the attribute, and the excepted item with its own coverage), or a subject with two
   halves (a title **or** a body format, over the corpora the claim names) — rather than qualifying the exception or
   letting one part's command stand for the whole, since the quantifier asserts the attribute of each (a count of prior
   changes reproduces as different totals by criterion, so state the instances or the criterion, never the bare number);
   captured: extract-load-test-commands; single-source a claim, a rule set, or a record stated in more than one artifact
   (the rule is `AGENTS.md`'s, under "Invariants"); derive every mechanism or task-check claim from the tool or the
   graph, deleting a term or mechanism name whose grep finds no referent in the repository (a coinage lifted from an
   external prompt; a design carried three, each stripped by a review round); and, for a change whose facts are its own
   measurement, computation, or process record, derive each figure from the mechanism that produces it — the window or
   instant it spans and the aggregation it applies, or for a record figure the command that emits it (a review round's
   row from its report's record line, the capture record's net delta from `git diff --numstat AGENTS.md` after
   `spotlessApply`, per-hunk `git diff -U0` counts when other edits share the file) — re-deriving it after that
   mechanism's last edit, since a figure measured before the final tuning, or written from what the edits were meant to
   add, is the claim the next round reports unreproducible (`verify-unique-review-rounds` recorded a row clean before
   its round reported and stated a net `AGENTS.md` delta of `+2` where the diff measured `+5`; the
   measurement-attribution gotcha carries the series rule). captured: seed-claims-from-the-diff captured:
   record-agents-md-authoring-standard captured: sharpen-experience-analysis captured: sharpen-review-quick captured:
   standardize-pr-conventions captured: fix-advisor-worktree-read
2. Record the pass. Every unit carries it as a task — a task in a change's `tasks.md`, or a line in the pull-request
   checklist for a unit with no change dir — so a missing box is a skip, not a pass that found nothing. Its output is
   the change's **claims table** — each fact the change states about its evidence (the prior artifacts it rests on) with
   its source or the command that derives it, re-run so it reproduces the figure the row records, since a figure an
   earlier, broader scan produced beside a narrower command is the claim the next round reports unreproducible, its rows
   seeded from the recurring classes `docs/review-rounds.tsv` records — in `design.md`, or in `tasks.md` for a unit that
   writes no `design.md`, or on the pull-request checklist line for a unit with no change dir. A command sequence the
   change prescribes is itself one fact, reproduced whole in a scratch tree rather than per step: a step's local
   property can hold while the composition loses the safeguard a later step relies on (a drafted "unstage, then hard
   reset", where a plain `git stash` saves a staged new file and nothing for an untracked one). Seed it with
   `scripts/claim-scan.py` (via `/seed-claims`), whose candidates for the unit's diff are each resolved into a row or
   recorded as a dismissal beside the table — one reason per kind, never an enumerating clause, governing a row's source
   as well as a dismissal: a source that is a command's output states the reason by class, never a list of the output's
   lines, since a list of a kind's candidates or of an output's lines is incomplete by construction and the artifact
   stating it adds candidates to the scanned set — a lead list, never a gate, and it seeds the table rather than
   replacing the derivation (`showcase/quality/agent-skills` owns the behaviour). A dismissal's reason is itself a claim
   about the repository: verify it against the diff or the source it names before recording it, since a dismissal is
   recorded, not checked (`sharpen-architecture-audit` dismissed the README table rows as carrying only a formatter
   reflow, and the diff held no such change). A row whose claim is about the state before the change anchors its source
   to the unit's base revision (`git merge-base HEAD origin/main`), recorded as the SHA, never to `HEAD` or
   `origin/main`: `HEAD` moves at the unit's first commit and `origin/main` at every merge — the unit's own included,
   which changes what a corpus-wide grep returns there — so a ref-keyed row stops reproducing while still reading as
   anchored. Use `git show <sha>:<path>` for the change's own artifact and for any command it embeds (a script that
   opens the working-tree file reports the implemented result), and `git grep … <sha>` for a historical claim over a
   corpus ("no document mentioned it before this change"). captured: document-java-version-selector A site named for a
   later sweep is quoted by its text or token, never by a line number — a concurrent merge moves it, so the anchor is
   stale as committed (`AGENTS.md:NNNN` anchors were two lines out here). A task's `**Done**` note or a claims-table
   entry that must report a figure or a status states a relative result — the difference between two runs, which the
   corpus's growth leaves fixed — the command's outcome, a text-quoted site, or the record's own mechanism, never an
   absolute count its own writes move (its appended ledger rows, the candidates its own artifact adds to the scan) and
   never a status its own remaining steps move (a review round named "the final clean one"), and state the verification
   as of its own step and the resulting state separately, since a later step can destroy what it recorded. Re-derive the
   table after the unit's last append to its own record, which adds one. captured:
   widen-architecture-auditor-to-stale-ideas captured: normalize-the-review-round-digest captured:
   capture-authoring-system-gaps captured: sharpen-architecture-audit captured: extract-ci-narrative captured:
   convert-remaining-gotchas-rules captured: add-technical-advisor captured: projector-operability captured:
   stage-new-files-before-manual-review

## 2. Quick review loop

1. Run the reviewer-session preflight, then `review-quick`:
   - **Preflight** — before each invocation, the re-runs in step 2 included: create the reviewer's session on the unit's
     first round with a fresh invocation and retain the identifier it returns; on every later round pass that saved
     identifier as the invocation's session-continuation parameter, never a fresh invocation relabelled as a
     continuation; compare the identifier the invocation returns with the one you saved, and stop the round reporting a
     session-continuity failure if they differ; start a replacement only after the invocation you are replacing has
     produced one of the recovery cases the rule names, recording the concrete reason and both identifiers — the failed
     session and its replacement's — before you replace; and bound each invocation with a deadline your policy supplies,
     recording the invocation's hand-over **before** you wait on it. A reviewer invocation cannot be cancelled and
     reports nothing while it runs, so a stall is recovered from that record rather than from the stalled session. The
     rule, its recovery cases, the record's contract, the deadline's source, and the hand-over's fields are
     `showcase/quality/agent-skills`'s and `docs/review-rounds.tsv`'s, not restated here. Do not restart the quick
     reviewer to obtain an independent screen, including at the proposal-to-implementation boundary: independence is not
     a recovery case. IF no permitted recovery case is evidenced, THEN stop before starting a replacement and report the
     blocker — never select an allowed reason merely to satisfy the ledger checker; IF an unsupported restart has
     already occurred, THEN preserve the invocation evidence, record the continuity violation, and stop the gate for the
     owner's recovery decision.
   - **Run** — `review-quick` against the change's planning artifacts: for the proposal round, the
     proposal/design/tasks/spec-delta coherence and repo fit; for the implementation round, the tasks and delta spec.
     For a capture, supply the existing rules each proposal names (or its statement that none covers it), the round rows
     and each round's findings — a row's class label cannot show a recurrence's shared cause — and the change record,
     and the `lesson-capture` definition where its gate is checked. captured: override-opencode-plugin-transitives
   - Append each round's row with the session fields `docs/review-rounds.tsv` declares — the identifier, the transition,
     and a replacement's reason — supplying the identifier the invocation returned, never an invented one; an omission
     fails `verifyReviewSessionContinuity`. captured: track-review-session-continuity
   - **IF a review-round row is appended or edited, THEN immediately run both ledger checks:**
     `python3 scripts/commit-hygiene.py --unique-rounds --expect <unit>:<round>` and, as a separate invocation,
     `python3 scripts/commit-hygiene.py --session-continuity`. Repeat both before the next reviewer invocation and
     before the manual pass; stop on either failure. `<unit>:<round>` names the round whose report just arrived, so
     `--expect` detects a missing final row that contiguity alone cannot see. captured: idempotent-projection-writes
     (#670)
   - **Both of those attest the record's shape, never that the loop converged.** Before the manual pass, also run
     `python3 scripts/commit-hygiene.py --unique-rounds --expect-clean <unit>`, which fails while the unit's highest
     recorded round still carries a finding class. That invocation belongs to the boundary alone: a round in flight is
     unclean by design, so it is not part of the repeat rule above.
2. Fix everything it finds; re-run. Repeat until it reports no new observations.

No semantic self-review precedes the loop — a re-read shares the blind spot that produced the claim — so the author owes
only the pre-write pass above and the mechanical pre-flight (the formatter and the touched module's gates after the
final edit). captured: decide-against-pre-review

## 3. Deep-review routing

Route the deep review, once, from the completed implementation, the diff, the applicable artifacts, and the existing
review evidence: derive the floor with `python3 scripts/review-routing.py`, passing `--change <name>` when the unit has
a change dir and omitting it for a unit that has none; decide at or above it (`REQUIRED`, `RECOMMENDED`, or
`NOT_NEEDED`), and record the outcome and its signals on the change's review task in `tasks.md` (or on the pull-request
checklist line for a unit with no change dir).

- A unit with no change dir omits `--change <name>`; naming a directory that does not exist fails the derivation, which
  the rule below reads as `REQUIRED`, so the omission is not a lower floor. What the flag supplies is the tool's own
  contract — its docstring and its `--change` help — not a rule restated here.
- A `REQUIRED` outcome runs before completion; a `RECOMMENDED` one needs an explicit recorded resolution; a `NOT_NEEDED`
  one creates no deep-review session, prepares no hand-over, and spends no expensive-model invocation.
- A missing, malformed, or failed derivation is `REQUIRED` — never a silent skip.
- The outcomes and the procedure are specified in `showcase/quality/agent-skills` and `/review-thorough`.

## 4. Deep review

When the routing outcome selects it, run the pass through `/review-thorough`: its procedure, session, hand-over, pass
budget, and finding statuses are that command's and the reviewer definition's, and the routed pass's status is recorded
on the change's review task. The deep reviewer verifies the current state independently on every pass — session
continuity is not proof that a fix is correct.

## 5. Manual review pass

Only after the quick review reports no new observations, any required deep review has completed, the pre-write
derivation pass and its claims table are recorded, the unit's highest recorded round carries no finding class (§2's
`--expect-clean`), and — at the implementation stage, where a capture follows the implementation's quick review — the
unit's capture record exists (`AGENTS.md`'s `### Workflow: Capture lessons`), ask the owner for the manual review pass,
and end the turn there. None of those preconditions substitutes for the ask, and none is the approval: nothing is
committed, pushed, opened as a PR, archived, or merged before the owner approves (the stops are `AGENTS.md`'s).

Before the ask, stage the unit's own new files so the pass shows them:

- Run `git add -- <explicit paths>` for the unit's new files, its planning artifacts included.
- Re-run that add for any of those files edited after the previous add.
- Never use `git add -A`, `git add .`, or `git add -N` for this.
- A unit with no new files adds no step.

The ask names the review surface: the unit's workspace tree and its IDE Changes panel, where staged new files show as
Added and the remaining modifications as Modified — `git diff --cached` for the new files and `git diff` for the
modifications as the CLI equivalent. Staging is not committing: the approval stop is unchanged.

**Decision points**

- **Pass each prior finding's instance, not only its class:** IF you are re-running the review after a fix THEN pass
  each prior finding's **instance** (`file:line` and the claim) from the prior round's report, so the subagent can
  report an unfixed prior instance, or a new instance of a class a prior round already raised, rather than classifying
  each round in isolation. The instances live only in that report, not the class-only record line, so a loop whose
  **caller** is about to lose its context — a new session — writes them to a handover before it is lost and resumes by
  running the round, the handover written from the artifact (each `file:line` and its text), never from what the edit
  was meant to do: its claims are the successor's premise, and one that does not reproduce costs the round it was meant
  to save. captured: record-agents-md-authoring-standard
- **Continue the reviewer's session** — the quick loop's preflight is what makes that happen, and each round must not
  lean on the reviewer's memory in place of the hand-off, which is the caller's record. The caller's own re-check of the
  handover is a recorded deviation, not a passed round (a resumed round found defects the handover's self-review had
  passed). captured: retrospective-2026-10-15
- **A behavior added after the plan is drafted — by a finding or by the implementation — is a scope change, not a code
  fix:** applying it to the code and a test alone leaves the change's planning artifacts — proposal, design, tasks, and
  delta spec — describing the old behavior, and the next round reports the residue as spec-/task-drift. Sweep every one
  of them, not only the file the finding names, as part of applying the finding (and the docs, per the docs-refresh
  convention); the implementation's own additions take the same sweep, so run it once more after its last edit:
  `idempotent-projection-writes` implemented a `ShowcaseEvent`-without-aggregate-headers branch its design recorded and
  its delta spec did not, and capped its item backoff at `2^10` where the design still said only "doubling"; the sweep
  that followed the implementation is what put both into the planning artifacts. A **framing or coherence** correction
  takes the same sweep even though it adds no behavior: the artifacts the change states the model across — planning and
  shipped alike — state one model of the subject, so correcting the model in the artifacts the finding names leaves a
  sibling — often the shipped copy, not only `proposal.md` — stating the old framing, which the next round flags as
  coherence drift. Re-read every artifact that states the corrected claim, `proposal.md` and the shipped copies included
  — and the section _inside_ an artifact, such as a delta requirement's description beside its scenarios — before
  re-running the review; `openspec validate` checks a scenario's presence, never that it still agrees with the
  description. captured: record-baseline-operating-point captured: widen-architecture-auditor-to-revisit-triggers
  captured: add-apply-audit-reductions-command captured: record-agents-md-authoring-standard captured:
  idempotent-projection-writes
- IF the review loop keeps finding the **same class** of observation round after round, it is not converging — each fix
  is treating a symptom of a root cause that is still there. Stop, re-derive the root cause by the shapes under "Failure
  handling", and do not layer another special case.
- The `review-quick` subagent's contract and the pre-write pass record are specified in `showcase/quality/agent-skills`
  — act on its verdict line rather than on your own recollection of the thread.

**Failure handling — re-deriving a non-converging loop**

- **A claim about another artifact or about the change's own mechanism** that under-describes or contradicts it (a
  surface list, an artifact set, a list of touched files or test sites, a count, a unit, a decision's numbers, **what a
  command yields or which inputs it can receive**) was written from memory rather than derived from the surface it
  summarizes: re-derive it by grepping the corpus, the archive, the change's sibling artifacts, and the diff **— or, for
  a mechanism claim, by running the tool** — or, when the change's mechanism does not need the claim, **delete it** and
  shrink the change to its mechanism (the `derive-the-change-claims` proposal loop ran 14 rounds until that rewrite) —
  or abandon the unit. captured: single-source-the-scheduled-audit-mechanism captured: seed-claims-from-the-diff
- **A passage the change was single-sourcing:** each round finds another residue of the duplicate the pointer should
  have removed because the edit trimmed the sub-clause that round named; replace the whole duplicated unit in one edit
  and derive what stays at the pointing site from that site's own subject, not from the finding. A description of a
  sibling artifact's contents — a source list, a delta description, a list of the change's own edits — is the same shape
  with a moving subject: it is stale the moment that artifact changes again, so delete the description and keep the
  pointer rather than updating it.
- **A class the loop reports round after round whose instances share a confirmed cause:** rewrite the affected section
  once from its intended content — correct verified errors, remove verified duplication, preserve every valid
  requirement, exception, warning, and identifier — and verify with the growth-bound bullet's trim sweep. Do not infer
  the shared cause from the class label: a class whose instances have different causes takes the cause-specific shape
  instead. captured: add-extraction-procedure
- **A rule's own boundary wording — or a claim's supporting reason:** a discriminator, or a reason a decision rests on,
  that reads plausibly but excludes (or no longer supports) the case it was written for must be re-derived from the
  mechanism that distinguishes the cases — not merely re-worded — and tested against that motivating case before its
  wording is swept across the artifact set. A review that falsifies a claim's reason falsifies the conclusion it
  carries: re-derive the conclusion rather than substituting a truer reason (`sharpen-architecture-audit` swapped the
  reason behind `No ## Purpose refresh is owed`, left the conclusion, and the conclusion did not reproduce). captured:
  relax-diagram-span-rule captured: sharpen-architecture-audit
- **The change's own self-records (the claims table, the task `Done` notes, the dismissals):** a recurring
  `claim-unreproducible` means they were drafted from what the edits were meant to do, not from the source they name —
  so re-derive each **clause** from that source once the edits land, writing it from the named command's output and
  deleting a clause you cannot so source, and delete a claim whose described edit has no referent. When the repeated
  class is their completeness (a stated fact they omit, a source that does not reproduce), adding the omitted row
  re-triggers — the rows track the facts the artifacts state — so delete the unneeded fact rather than add the row. A
  dismissal's completeness finding is re-derived rather than repaired by deletion: re-derive it from the scan's whole
  candidate list grouped by kind, in one edit — appending the candidate a round named is the enumerating clause the
  claims-table step forbids, and the clause's own growth is the incompleteness the round reports. captured:
  dont-report-what-a-gate-covers captured: make-reviews-predictable-and-recoverable captured:
  apply-2026-10-19-audit-findings captured: sharpen-architecture-audit captured: track-review-session-continuity
- **A revert after a non-converging loop is a legitimate outcome** — record why in the change dir so the decision is not
  re-litigated. captured: propagate-ui-trace-context (#362) captured: realistic-load-test-profiles captured:
  check-load-test-drift captured: check-unique-cron-schedules (#436) captured: derive-retrospective-facts captured:
  derive-the-change-claims

**Completion criteria** — the quick review reports no new observations (the loop is clean, and the closing round is
recorded with no class), the pre-write pass and claims table are recorded as required, the deep-review routing decision
is recorded with its outcome, any `REQUIRED` deep review has completed, and the manual review pass has been requested. A
clean closing round, the recorded pre-write pass, any required deep review, and — at the implementation stage — the
capture record mean _ask the user now_; none of them is the owner's approval.
