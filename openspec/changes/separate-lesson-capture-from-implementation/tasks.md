# Tasks

Workspace: unit=separate-lesson-capture-from-implementation;
path=/Users/afanasyev/Development/axon-showcase-separate-lesson-capture-from-implementation;
branch=separate-lesson-capture-from-implementation; writer=ses_e84c9b955ffeY6V3z6ZN1VELva

## Resume checkpoint

- **Time and revision**: 2026-10-27, at `be232061` — `origin/main`, and equal to `HEAD` because nothing is committed.
- **Workspace**: unit=separate-lesson-capture-from-implementation;
  path=/Users/afanasyev/Development/axon-showcase-separate-lesson-capture-from-implementation;
  branch=separate-lesson-capture-from-implementation; writer=ses_e84c9b955ffeY6V3z6ZN1VELva.
- **Stage**: implementation, at the close of round 34. All five of the capture's items are applied, the
  `/generalize-process-defects` smoke-run is done (5.4), the deep review's `REQUIRED` pass is complete, both advisor
  consultations' corrections are applied (tasks 5.5 and 5.6), and rounds 20–34 plus the deep review's findings are
  fixed; round 34 reported nothing.
- **Next step**: the owner's manual review pass, re-asked in this turn; nothing is committed, pushed, archived, or
  merged before their approval.
- **Change set**: 22 paths, all staged — 9 additions (the change directory's seven files, the command, the record) and
  13 modifications; no untracked file, and no stash.
- **Blockers and open decisions**: none external. The reviewer session was replaced at round 20 (`expired`, the harness
  refused a cross-writer-session continuation), recorded in `review-handover.md` and the round-20 ledger row.
- **Pending decision**: the owner's manual review pass — requested at the end of an earlier turn and still outstanding,
  since their advisor request did not answer it; the gate's approval stop stands until it is answered.
- **Findings no record holds**: none. Rounds 20–34's findings and the deep review's three are in `review-handover.md`,
  and the capture's five items are in `capture-report.md`.
- **Operations in flight**: none — no build, container, or background command is running.

## 1. Recording mechanism

- [x] 1.1 Replace `AGENTS.md`'s `### Workflow: Capture lessons` up to its `How a captured rule is recorded` subsection —
      `:463–569` at the base revision — with `### Workflow: Record process defects` whose Trigger is the moment a defect
      is observed. Verification: the heading exists, its trigger names the observation moment, and the section states no
      skip condition, no review-gate precondition, and no schedule.
- [x] 1.2 State the record's home and identity: one file per unit at `docs/process-defects/<unit>.md`, `<unit>` being
      the change name or a slug naming the work, with one `### <unit>#<n> — <title>` heading per defect, numbered within
      the file. Verification: `git grep -n 'docs/process-defects' AGENTS.md` returns the path at each site that names
      it, every one inside the recording or generalization section, and the heading form is stated once.
- [x] 1.3 State the field set the recorder fills — `Recorded`, `Stage`, `Expected`, `Observed`, `Evidence`,
      `Consequence`, `Knowledge boundary` — as its one authoritative copy. Verification: all seven names appear in the
      recording section, and no other live artifact repeats the list (task 4.7).
- [x] 1.4 State the recorder's prohibitions and explicit-none rule: no cause diagnosis, proposed improvement, or cost
      assessment is required; a unit that observed nothing says so, and that is not a claim that nothing went wrong; no
      secret and no full diagnostic dump enters a record. Verification: one sentence states all three prohibitions — no
      cause diagnosis, no proposed improvement, no cost estimate — and the explicit-none rule states what it does not
      claim.
- [x] 1.5 State that immediate damage — a lost file, a broken result, a wrongly closed tracker — is repaired in the unit
      rather than deferred to an analysis. Verification: the rule names all three examples and no deferral.
- [x] 1.6 State that the record ships with the unit's pull request rather than in its change directory, so it survives
      the archive. Verification: the rule names the pull request and the archive, and no change-directory location.
- [x] 1.7 Add `### Workflow: Generalize process defects` with a Trigger naming the owner's decision that the records are
      due, and a Procedure that selects the records without a `Decision` line, invokes `lesson-capture`, applies what
      the owner approves, and appends each `Decision`. Verification: the trigger names the owner, the procedure names
      the command, and no schedule or queue guarantee appears.
- [x] 1.8 State the `Decision` line's values — `no-change` with its reason, `proposed` with a link, `shipped` with a
      merged pull-request link — and that an open pull request is never `shipped`. Verification: all three values and
      the open-pull-request rule appear once.
- [x] 1.9 Point at the `agent-skills` requirement for the analysis contract instead of copying it. Verification: no
      promotion-gate criterion, evidence threshold, or disposition is restated in the section; the requirement is named.
- [x] 1.10 Retire the skip conjunction and the merge-time capture, and state that a merge appends to the merged unit's
      record any defect the merge alone reveals. Verification: `git grep -n 'skip condition' AGENTS.md` returns nothing
      outside the historical incident text, and the merge rule is present.
- [x] 1.11 Carry the recurrence disposition the replaced section states — instruction-following failure in a `captured:`
      rule, a reopened parked entry whose own `Revisit when:` condition the incident meets, and an already-parked
      problem — into the generalization section. Verification: all three branches appear with their strength, and each
      names what the capture does.
- [x] 1.12 State the branch and pull-request boundary: the originating record rides the product unit, and the
      improvement is its own unit on its own branch and tree from a freshly fetched `origin/main`, consuming records
      already merged. Verification: the rule names the branch source and rules out an unmerged branch.
- [x] 1.13 State the role division: the unit's session records; the process owner generalizes, the owner by default and
      otherwise the assignee a unit's tasking names; the owner chooses the moment and approves; the assignment grants no
      approval, self-review, or merge authority; no role registry exists. Verification: each of those five clauses is
      present, and the authority sentence names all three withheld powers.
- [x] 1.14 Move `AGENTS.md`'s `How a captured rule is recorded` subsection (`:570–605` at the base revision) under the
      generalization section, narrow its `captured:` marker rule to the record's unit, and repoint its "capture record"
      sentences to the generalization's record. Verification: `git grep -n 'capture record' AGENTS.md` returns nothing
      inside that subsection, and the marker rule names no pull request.
- [x] 1.15 Retire the review-loop ledger paragraph from the old section's completion criteria — the append duty and its
      checkpoints are `.opencode/skills/review-gate/SKILL.md` §2's and were restated here. Verification:
      `git grep -n 'review-rounds.tsv' AGENTS.md` returns no rule duplicating §2's steps, and §2 still states them.
- [x] 1.16 Repoint `AGENTS.md`'s retired sites — the "No in-flight branch" example (`:380`), the review-gate bullet's
      capture record (`:454`), the extraction decision point's "future captures" (`:758–759`), the docs-refresh bullet's
      `(the capture)` and its merge-time capture (`:1123`, `:1126`), and the experience-analyzer bullet's "per-unit
      `review-quick`/`lesson-capture` agents" (`:1341`). Verification: each listed site carries the new text, and task
      4.2's sweep records `:1419`'s "capture loop" as a live reading rather than a repointed site.
- [x] 1.17 Create `docs/process-defects/separate-lesson-capture-from-implementation.md` as the format's worked example,
      recording this unit's own process defects under 1.3's field set, with no `Decision` line: this unit's inline
      capture (5.3) is a separate act under the rule in force, and the record's analysis is a later, owner-triggered
      unit. Verification: the file carries one heading per defect and ships with this unit.
- [x] 1.18 Replace the per-unit `lesson-capture` task with the per-unit defect-record task in `openspec/config.yaml`'s
      `tasks` rules and in `.github/PULL_REQUEST_TEMPLATE.md`. Verification: both name the record and its `Decision`
      line, neither names a skip condition, and the YAML rule stays a list of strings (the config-parse gate).
- [x] 1.19 Remove the parked idea this change supersedes — `docs/ideas.md`'s "Require the unit's capture record before
      the manual-pass request" — only if it is still present. Verification:
      `git grep -n 'Require the unit.s capture record' docs/ideas.md` returns nothing, and the consolidation idea stays
      open.
- [x] 1.20 Repoint `.opencode/skills/pause-resume-unit/SKILL.md`'s Stage field, which lists "the capture" among the
      stages a unit passes, to name the unit's record. Verification: the field names the record rather than the capture
      stage, and the skill's other fields are unchanged; task 4.2's sweep of this file reports its survivors.
- [x] 1.21 Decide `AGENTS.md`'s "the clause drops at the merge — the merge-time read owns that drop" sentence against
      the retirement of the merge-time capture, and repoint it to the merge's record append. Verification: the sentence
      names the record append, and `git grep -n 'merge-time read' AGENTS.md` returns nothing.

## 2. Generalization mechanism

- [x] 2.1 Add `.opencode/commands/generalize-process-defects.md` with:
  - a frontmatter `description`, and a Trigger naming the owner's invocation, optionally with the units to generalize;
  - a Procedure that selects the records without a `Decision` line, shows that set to the owner, invokes
    `lesson-capture` with the selected records, the existing guidance a candidate would extend, and the review evidence
    they cite, then designs and implements the approved improvements and appends each `Decision`;
  - Decision points for an empty selection, a record whose evidence is missing, and a record already decided;
  - Failure handling, Verification, and Completion criteria;
  - the Repository lifecycle block its sibling commands carry, pointing at `AGENTS.md`'s workflows rather than restating
    them.

  Verification: the command names no schedule and grants no approval, and the build's definition-inventory probe
  resolves it.

- [x] 2.2 Update `.opencode/agent/lesson-capture.md`: the usage sentence, the opening "given a unit whose implementation
      is done and whose quick review is clean", the input list, the marker rule's merge-time clause, the applied-delta
      sentence that names "the capture record … so the merge-time read sees the figure", and the pointer to "the capture
      Decision points in `AGENTS.md`". Leave the gate, the dispositions, and the destinations untouched; leave the
      parked "either skill" defect alone. Verification: the gate text is unchanged apart from those clauses, and
      `git grep -n 'capture record\|merge-time' .opencode/agent/lesson-capture.md` returns nothing.
- [x] 2.3 Update `.opencode/skills/review-gate/SKILL.md`: replace §5's capture-record clause with the record-currency
      precondition, keep the gate's other preconditions and the "base has not moved" check, repoint §2's "For a capture,
      supply …" input line, repoint §1's net-delta figure, and update the completion criteria's capture-record phrase.
      Verification:
      `git grep -nE 'capture record|capture workflow|Capture lessons' .opencode/skills/review-gate/SKILL.md` returns
      nothing, while the `lesson-capture` definition reference it must keep (`:104–106`) stays.
- [x] 2.4 Update `.opencode/skills/ship-change/SKILL.md`'s in-flight docs bullet: the process-defect record rides the
      product pull request, while the improvement it causes is its own unit. Verification: the bullet states both
      halves, and `AGENTS.md`'s generalization Decision point carries the caveat that product fixes and their
      documentation are not excepted.
- [x] 2.5 Update `.opencode/agent/experience-analyzer.md`'s layer description above "the per-unit
      `review-quick`/`lesson-capture` agents", its promotion-gate pointer (whose "the capture" stays a live reading),
      and the lesson list's "per-change captures". Verification: the digest-based subject is unchanged, every site
      naming the retired per-unit capture is repointed to the units' defect records, and only those sites move.
- [x] 2.6 Update `.opencode/agent/review-quick.md`: the capture-review input (`:17–19`), the review contract's "A
      capture" entry (`:89–91`), and the missing-material clause (`:100`) name the unit's defect records and the
      generalization's proposals instead of "the capture evidence". Verification: the contract still rejects a proposal
      that restates existing guidance or governs no decision, and the input list matches what the new command supplies.
- [x] 2.7 Update `docs/ideas.md`'s header where it names "a merge-time capture" among the docs edits that ship as their
      own pull request. Verification: the header's routing rule still holds for the other edits it names.
- [x] 2.8 Update `docs/ideas.md`'s live entries that name the capture record as if the per-unit stage stayed: the parked
      entry whose `Revisit when:` names "its capture record" and the parked entry that quotes the pull-request
      checklist's "capture record". Verification: each live entry naming the retired artifact is repointed, each entry
      naming the live analysis is unchanged and recorded as such, and the dated entries are untouched.

## 3. Spec and docs

- [x] 3.1 Confirm the delta's five blocks — one `REMOVED`, one `ADDED`, three `MODIFIED` — and that the added
      requirement resolves the trace's retained rows: the two description clauses are in the analysis-contract scenario,
      the merge duty is in the recording scenario, and the generalizing SHALL is in the description. Verification:
      `openspec validate separate-lesson-capture-from-implementation --strict` passes, and every `Retained` trace row
      resolves to text in the added block.
- [x] 3.2 After the delta lands, check that the corpus's references to "the capture requirement" and "the main agent's
      capture workflow" still resolve against the retitled header (`captured` is retained for that reason), and record
      any that do not with its `MODIFIED` block. Verification: the command
      `git grep -nE "capture requirement|capture workflow|capture's promotion" openspec/specs/showcase/quality/agent-skills/spec.md`
      returns only references a reader resolves to the retitled requirement or a phrase the delta repointed, each read
      rather than counted.
- [x] 3.3 Refresh `README.md`'s sites — the Technologies prose, the agent table row, the "Lesson capture" bullet,
      "Applying suggestions", and the self-learning loop's "Every unit closes the loop" bullet — and add a
      `/generalize-process-defects` row to the slash-command table. Verification:
      `git grep -n -i 'lesson-capture\|lesson capture' README.md` returns only sites describing the generalization, and
      the agent-table row's column count is unchanged.
- [ ] 3.4 Refresh the `agent-skills` capability `## Purpose`, whose "applied by the capture when authoring" and "the
      per-unit review and lesson-capture agents" phrasings the change falsifies; a delta cannot carry a Purpose, so
      apply it in the archive commit. Verification: the archive commit carries the Purpose edit.
- [x] 3.5 Confirm the report-contract `MODIFIED` block changes only the retargeted appositive, with all five scenarios
      verbatim. Verification: a diff of the block's scenarios against
      `git show ea1ee3b5:openspec/specs/showcase/quality/agent-skills/spec.md` shows no other change.
- [x] 3.6 Confirm the two other `MODIFIED` blocks carry their requirements whole, changing one phrase kind each — the
      reduction unit's "the capture decision" (one occurrence) and the authoring standard's "the main agent's capture
      workflow" (two occurrences). Verification: `openspec validate --strict` preserves every scenario of both, and a
      diff against the base shows only those three substitutions plus the formatter's rewrap.

## 4. Verification

- [x] 4.1 Record the claims table in `design.md`, re-derived against the base revision after the last edit of each
      source. Verification: every row's command reproduces the result it records.
- [x] 4.2 Apply `design.md`'s "Site sweep" to every durable file the plan edits: prove the pattern against a known line
      of that file before trusting a zero, and record each survivor as one of the five senses the design lists.
      Verification: a survivor fitting none of the five fails the task, and each file's recorded list is reported with
      the file it came from.
- [x] 4.3 Verify the command's selection surface: the grep listing records without a `Decision` line returns this unit's
      record before a decision exists and excludes it in a scratch copy after one is appended. Verification: both runs
      shown, the positive case first, and the scratch copy under the agent's temporary directory.
- [x] 4.4 Exercise the review gate's new precondition on both inputs: a unit whose record is absent or stale since an
      observed defect, and one whose record is current or says "none observed". Verification: both states are exercised
      by an invocation or a scripted assertion, with the two outcomes recorded.
- [x] 4.5 Run `./gradlew spotlessApply` after the last edit, then `spotlessCheck`, `openspec validate --all`, and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. Verification: all green; a
      `Skipping … does not converge` line for a changed markdown file counts as a failure of that file.
- [x] 4.6 Check the changed set's non-markdown files for the 120-character limit with
      `perl -CSD -lne 'print if length > 120'` over `git status --porcelain --untracked-files=all`, and read the changed
      markdown's prose for what the formatter does not reflow. Verification: no over-length line outside a fenced block,
      and the check hits a known-positive fixture first.
- [x] 4.7 Verify the field set is stated once in the durable artifacts: read the delta, the new command, the README
      rows, and the record's own file for a restatement of the seven field names. Verification: only `AGENTS.md`'s
      recording workflow lists them; the planning artifacts (`design.md`, `tasks.md`) may name them as the contract they
      implement, and each other site points at `AGENTS.md`.

## 5. Review gate and delivery

- [x] 5.1 Run the `review-quick` loop to a clean round, appending each round's row and running
      `python3 scripts/commit-hygiene.py --unique-rounds --expect separate-lesson-capture-from-implementation:<round>`
      and `--session-continuity` after each append. **Done**: rounds 1–23; round 23 reported nothing, the reviewer
      session was replaced once at round 20 (`expired`, the harness refused a cross-writer-session continuation), and
      both ledger checks plus `--expect-clean separate-lesson-capture-from-implementation` pass at round 23.
- [x] 5.2 Derive the deep-review floor with
      `python3 scripts/review-routing.py --change separate-lesson-capture-from-implementation`, decide at or above it,
      and record the outcome and its signals on this task. **Done**: `REQUIRED`, on three signals —
      `infrastructure-or-deployment` (`.github/PULL_REQUEST_TEMPLATE.md`), `requirement-block-added` and
      `requirement-block-removed` (both on the delta).
- [x] 5.3 Run this unit's `lesson-capture` decision under the rule in force at implementation — the current `AGENTS.md`
      workflow, since this change's own rule takes effect only after it merges. **Done**: the capture ran (the diff
      touches authoring surfaces, so the skip conjunction fails); its 4 durable proposals, 1 authoring-system
      recommendation, and each one's disposition are recorded in `capture-report.md`, and all five items are applied.
- [x] 5.4 Run the end-to-end smoke-run of `/generalize-process-defects` before the review round, in a scratch copy under
      the agent's temporary directory: seed `docs/process-defects/` with two undecided records and one carrying a
      `Decision` line, run step 1's per-record selection, invoke `lesson-capture` on the selection, then append one
      `Decision` line per analyzed record. Verification: the selection listed both undecided seeds and excluded the
      decided one; the capture returned the contract's verdict-first report (`nothing durable (net 0)`); the same
      selection then excluded all three seeds; and the run added no worktree and no untracked file to this tree. The
      capture dispositioned both seeds `no-change` as already covered and falsified one seed's premise against the
      pinned `openspec` 1.15.0, which refuses a missing change dir and exits non-zero — the verification step working as
      designed rather than a gap.
- [x] 5.5 Record, in the unit's own defect record, the recurrence the advisor consultation surfaced: the record trailed
      its rounds across the unit. Verification: `docs/process-defects/separate-lesson-capture-from-implementation.md`
      carries the record with the rounds it cites, and the ledger rows for those rounds carry the class.
- [x] 5.6 Apply the second advisor consultation's README correction: align the introduction's forwarding sentence, the
      "Every unit closes the loop" bullet and the section's closing line with the shipped model — every unit records,
      and improvement is a separate owner-triggered unit — and drop the dangling pre-write sentence and the
      tracker-closing example from the human-facing narrative. Verification:
      `git grep -n 'lessons are written back' README.md` returns nothing, the three statements agree that improvement is
      separate and owner-triggered, and the parked idea's reopened note carries the evidence.
