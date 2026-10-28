# Tasks

Workspace: unit=add-flag-process-defect-command;
path=/Users/afanasyev/Development/axon-showcase-add-flag-process-defect-command; branch=add-flag-process-defect-command;
writer=ses_e7f86f892ffe5UDzeevFdhToUq

## 1. The command definition

- [x] 1.1 Create `.opencode/commands/flag-process-defect.md`: the trigger, the episode resolution with its one-question
      ambiguity branch, the bounded comparison of the required action against what occurred under the rule in force at
      the episode, the three outcomes, the record's placement in the episode unit's file, an already-recorded episode
      extended rather than duplicated, the fields a confirmed record fills, the failure handling, and the repository
      lifecycle. Verify: the file parses as a command definition (frontmatter `description` present, no unquoted `: ` in
      its value), and its body names `AGENTS.md`'s recording workflow as the owner of the record's field set.
- [x] 1.2 Keep the record's field set out of the command: it names the fields whose owner-signal content differs, and
      points at `AGENTS.md`'s workflow for the set itself. Verify: the command carries no numbered restatement of the
      record's field meanings, and `grep -c "Knowledge boundary" .opencode/commands/flag-process-defect.md` returns the
      count of directives that fill that field, not a definition of it.
- [x] 1.3 Keep the generalization's procedure out of the command: it states that a confirmed record enters the
      generalization queue, and points at `/generalize-process-defects` for what happens there. Verify: the command's
      text contains no step that selects, analyzes, or decides records.

## 2. The repository's other sites

- [x] 2.1 Add the `/flag-process-defect` row to `README.md`'s slash-command table, after the
      `/generalize-process-defects` row. Verify: the row is inside the table block (header, delimiter row, and row
      contiguous), `grep -n "/flag-process-defect" README.md` shows it as a table row, and the table's column count is
      unchanged.
- [x] 2.2 Add the owner-reported path to both `README.md` statements of the recording loop: the Self-Learning Loop
      bullet that already carries the merge clause ("A defect a merge alone reveals is appended to the merged unit's
      record"), and the "Recording, then generalizing" bullet under _What the Agent Automates_. Verify: both name
      `/flag-process-defect` and the verified-before-recorded condition; the Self-Learning Loop bullet alone carries the
      three outcomes and the bounded episode comparison; and the Self-Learning Loop bullet's merge clause and the other
      bullet's generalization half are unchanged.
- [x] 2.3 Extend `AGENTS.md`'s "Workflow: Record process defects" **Trigger** with the owner-reported path and a pointer
      to the command. Verify: the trigger names both paths; the section's numbered steps and its decision points are
      unchanged (`git diff origin/main -- AGENTS.md` shows a hunk inside `**Trigger**` alone, none between
      `**Procedure**` and the section's close); and the applied net delta is recorded in task 4.2.

## 3. Verification

- [x] 3.1 Prepare the isolated copy the seed run uses: a clone of this repository under `$TMPDIR/opencode` carrying a
      prepared documentation unit on its own branch — a branch named after a change with its `openspec/changes/<name>/`
      directory, or a slug plus the claim file for a unit with no change dir — with no remote and worktree creation and
      session-move denied in its config. Verify: `git worktree list` in the copy names exactly that one tree,
      `git remote` prints nothing, the prepared branch is checked out, and a second run of the same checks after the
      seed run reports the same. The run exercises that prepared unit's continuation; it does not exercise creating a
      new unit. **Done:** the copy is `$TMPDIR/opencode/flag-probe-copy` on branch `probe-docs-unit` with the claim
      `none`; `git worktree list` names that one tree and `git remote` prints nothing, checked before and after the
      seeds. `session-move` has no key in this configuration's shape, so only `git worktree` is denied in the copy's
      config; the run moved no session.
- [x] 3.2 Run four seeds through the definition, read at run time as the instruction, inside the copy from task 3.1: a
      true episode of a unit that has already merged (a stated rule the observed sequence violated), a false signal (the
      required action was performed), an ambiguous request that resolves no episode, and one attested by the owner's
      assertion alone, and — as a fifth step — the true episode again, to re-flag an episode the file already records.
      Verify: **a resolved episode** reports exactly one of the three outcomes; the true seed **writes** its record into
      the file of the unit the episode belongs to, keeps every record already in that file, names the owner's report as
      the signal, states the established action rather than the owner's interpretation, cites a verified fragment, and
      carries no `Decision` line; the false seed names its grounding and writes nothing; the assertion-only seed names
      the missing fragment, asserts neither the defect nor its absence, and leaves the copy's record file unchanged; the
      ambiguous seed asks one clarifying question and stops before any outcome, so it is not counted among the three;
      the copy's branch is unchanged, the record the true seed wrote is uncommitted, the copy's `main` is unchanged, and
      this tree's `git status --porcelain` is unchanged by the whole run; the re-flag of the true episode extends the
      record it finds, leaving the file's heading count unchanged. State which branch each seed exercised, and re-seed a
      seed the repository cannot exhibit. The command's registration is verified by the pull request's `build` probe —
      its definition-inventory step — not by this run. **Done:** in the copy, the true seed was confirmed and appended
      `probe-merged-unit#2` to that unit's file, with record #1 preserved, the append uncommitted, no `Decision` line,
      and the copy's branch tip and `main` unchanged; the false seed was refuted by the fixture log's second line and
      wrote nothing; the ambiguous seed asked one question and wrote nothing; the assertion-only seed was not
      establishable, named the missing fragment, and wrote nothing. This tree's `git status --porcelain` was unchanged
      across the run, and the copy's differed from its pre-run state only by the appended record. The fixture unit and
      its logs are synthetic, so this proves the write path, the placement, and the lifecycle assertions, not a
      historical reconstruction. The fifth step re-flagged the true episode, and the record it found was extended — the
      file's headings stayed at two — rather than duplicated.
- [x] 3.3 Run `openspec validate --all`. Verify: exit `0`, and the change's delta validates against the main spec's
      scenario set. **Done:** `openspec validate --all` → 27 passed, 0 failed; the one >500-character warning belongs to
      another capability's requirement, not to this delta, which validates.
- [x] 3.4 After the final edit, run `./gradlew spotlessApply` then `./gradlew spotlessCheck`. Verify: both exit `0` and
      the check prints no `Skipping '<path>' because it does not converge` line for any file this change touches.
      **Done:** `spotlessApply` then `spotlessCheck` → both exit 0, with no
      `Skipping '<path>' because it does not converge` line for any file this change touches.
- [x] 3.5 Run the 120-character recipe over the changed files the formatter does not own, deriving the set with
      `git status --porcelain --untracked-files=all`. Verify: `perl -CSD -lne 'print if length > 120'` prints nothing
      outside a table row or an intentional exception, with each exception named. **Done:** the recipe over the changed
      set derived from `git status --porcelain --untracked-files=all` prints nothing for the command file, the record,
      or the change's artifacts. The over-limit lines that remain are pre-existing (`README.md`'s badge line and its
      `verifyUniqueReviewRounds` example) and the ledger's rows, one line by contract. The outcome table first written
      in the command file exceeded the width, so it is a bullet list.
- [x] 3.6 Run the review gate's pre-write derivation pass and re-derive the claims table in `design.md` after the unit's
      last append to its own record. Verify: every row reproduces the figure or outcome it records, and each dismissal
      states its reason by kind. **Done:** every claims row reproduces at `eefe2543`, and the scan over the whole diff —
      command file, record, change artifacts, README, `AGENTS.md`, and the ledger — yields only the four kinds the
      dismissals cover, which were re-derived by kind once the record, the command file, and the ledger joined the diff.
- [x] 3.7 Derive the deep-review routing floor with `python3 scripts/review-routing.py` and decide at or above it.
      Verify: the outcome and the signals it relied on are recorded on this task. **Routing:**
      `python3 scripts/review-routing.py --change add-flag-process-defect-command` → `RECOMMENDED`, on the signal
      `8 rows repeat a prior class`. **Owner decision:** run, 2026-10-28. Before the pass, the lifecycle's prohibition
      was corrected to match the merged-unit bullet: the command writes only into the episode unit's tree, or into the
      documentation unit's tree when that unit has none. **Pass 1:** returned one finding — `tasks.md` task 3.6's `Done`
      note names `7ca39c1a` while the claims table anchors to `eefe2543` (the stale copy is the note; the claims
      themselves reproduce at `eefe2543`). Severity nitpick, not blocking the manual pass. Status `OPEN`. **Owner
      decision, 2026-10-28:** fix the identifier and verify it by a targeted re-review in the closing quick round rather
      than a full pass 2 — the change is a process-record identifier, the pass that found it already verified the claims
      at `eefe2543`, and the closing round reads the corrected note; the status becomes `FIXED_AND_VERIFIED` only when
      that round confirms it, naming the round. **Status:** `FIXED_AND_VERIFIED`, verified by closing quick round 23 —
      condition 1 (the note reads `eefe2543`, consistent with the rows preamble) and condition 3 (the finding is
      removed) hold, and the one intervening edit is the owner-approved lifecycle correction recorded above and in the
      unit's record #3. No pass 2 was run, by the owner's decision.
- [x] 3.8 Run the `review-quick` loop to a clean round, with the reviewer-session preflight on every invocation. Verify:
      the closing round reports `nothing remains`, both ledger checks pass (`--unique-rounds --expect-clean` and
      `--session-continuity`), and each round's row is in `docs/review-rounds.tsv`. **Done:** rounds 18–21 covered the
      implemented diff (closing round 21 reports `nothing remains`), both ledger checks pass, and each row is in
      `docs/review-rounds.tsv`.

- [x] 3.9 Read every artifact this change creates for the prohibited content tasks 1.2 and 1.3 name — the restated field
      set and the restated generalization procedure. Verify: each of the command file, both `README.md` bullets, and the
      `AGENTS.md` clause points at its owner instead of copying it. **Done:** the command file defines none of the
      record's fields and names `Knowledge boundary` only as a directive; it carries no step that selects, analyzes, or
      decides records, and points at `/generalize-process-defects`. Both README bullets and the `AGENTS.md` clause name
      the command and the verified-before-recorded condition without copying the procedure, and only the Self-Learning
      Loop bullet states the outcomes, as task 2.2 requires.

## 4. The unit's record and capture

- [x] 4.1 Write `docs/process-defects/add-flag-process-defect-command.md` as defects are observed, or state that none
      was observed. Verify: the file exists, each record carries the provenance, stage, expected and observed action,
      evidence, consequence, and knowledge boundary, and no record diagnoses a cause. **Done:** the file carries records
      #1–#3: #1 the formatter re-breaking a verified fix, #2 the probe's vacuously-true "uncommitted" assertion, #3 the
      lifecycle prohibition that forbade the write its own rule requires. Each carries provenance, stage, expected and
      observed action, evidence, consequence, and the knowledge boundary, and none diagnoses a cause.
- [x] 4.2 Record the applied `AGENTS.md` net delta on this task, measured with
      `git diff --numstat origin/main -- AGENTS.md` after the final edit. Verify: the figure reproduces from that
      command, and the task states the clause the growth buys. **Done:** `git diff --numstat origin/main -- AGENTS.md` →
      `3 1`, a net `+2`; the growth buys the owner-reported path and its pointer in the recording workflow's trigger,
      and nothing else.

## Workflow follow-up

- Ask the owner for the manual review pass before any commit, push, pull request, archive, or merge.
- Archive with `openspec archive add-flag-process-defect-command --yes`, stage both the change-dir move and the
  `openspec/specs` sync, and verify the archive.
