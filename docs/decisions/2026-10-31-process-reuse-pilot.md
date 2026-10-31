# Decision record — a bounded transfer of the pause/resume slice

Date: 2026-10-31

Status: a bounded exploration. It binds no standing rule; it records what one transfer observed.

## The goal

The owner declared a project goal: make the agent process maximally prepared for easy, convenient reuse in other similar
projects. No real target project exists, so the owner proposed orienting on a project on the same technological stack.

## The question

Can one bounded process slice — pause and resume a unit — transfer into a similar project with localized adaptation,
preserving the behaviors it names?

## The fixture

A synthetic project, `ledger-service`, on the same **toolchain** stack: Java 21, Gradle Kotlin DSL, OpenSpec, OpenCode,
git. The application stack (Axon, Kafka, PostgreSQL, OpenSearch, React/Vite, Helm) is out of scope: the slice never
touches it, and a later slice (`ship-change`, CI) would need it.

The fixture is **predeclared and builder-blind**, not independently representative. Its specification was frozen before
the transfer and does not name the slice; a separate session built it from that specification alone. The main agent
selected the fixture and wrote the specification, so the fixture is not independent of the slice's author.

The specification also supplies exactly the inputs the checkpoint's fields need: a `tasks.md` carrying `Writer:` and a
`Pending decision`, and a working tree with one staged addition, one unstaged modification, and one untracked file. The
fixture is therefore project-shaped to exercise the slice's inputs, not a neutral skeleton; the ceiling below is read
with that construction in view.

- Specification: `/private/var/folders/rd/nw7pt03523dc_z_pzq1_rdjw0000gn/T/opencode/reuse-pilot/fixture-spec.md`,
  SHA-256 `e9b0aaf1aca700c1167c824a9eb207229e26411542efa642cf5ca5ea0d52c7a8`.
- Protocol (scenarios, manifest, budget, window), frozen before the transfer:
  `/private/var/folders/rd/nw7pt03523dc_z_pzq1_rdjw0000gn/T/opencode/reuse-pilot/pilot-protocol.md`, SHA-256
  `4b9236c8a3998397d8c4cc715a6c85927feb34b2d97bcba8a2450df3b353ba19`.
- Source revision: `9d7c6c97`.
- Fixture baseline commit: `20753b49757ba78bda406775b44713908c68a018`.

The builder's prompt named the specification path, forbade reading this repository's `.opencode/` and any file whose
name contains `pause` or `resume`, forbade running the build and adding a remote, and required the verification outputs.
The builder's observed inputs were the specification and the Gradle wrapper. Its deviations:

1. It set `git config status.showUntrackedFiles all` in the fixture, so plain `git status --porcelain` prints the
   untracked file rather than its directory.
2. It supplied commit authorship per command, since the fixture has no git identity.

It copied `gradlew`, `gradlew.bat`, and `gradle/wrapper/` from this repository, which the specification permits; that is
a permitted action, not a deviation.

The fixture's process-relevant contents are embedded below. The `AGENTS.md` embed is the pre-transfer version; the
transfer adds a **Pausing and resuming a unit** paragraph before "Workspace and ownership". The Java and Gradle files
are a nominal two-module skeleton (`ledger-api`, `ledger-core`, one record, one service, one test) whose exact text is
identified by the specification hash; they carry no process contract.

### `AGENTS.md`

```markdown
# ledger-service — agent instructions

Fixture marker: LEDGER-MARKER-7F3A9C.

## What this repository is

`ledger-service` records ledger entries. `ledger-api` holds the public types, `ledger-core` the service and its tests.

## How work is delivered

A unit starts on a branch named after it, forked from `main`. The owner approves the implementation before anything is
committed, pushed, or merged. An approval request stays open until the owner answers it, and a later message that does
not answer it does not close it.

## Workspace and ownership

The tree holding a unit belongs to one writer session. The writer is recorded in the unit's `tasks.md` as
`Writer: <session id>`. A different session that continues the unit records itself as the writer only after the owner
confirms the prior writer ended.

## Reviews

Review rounds are recorded in `docs/reviews.md`, one row per round: date, unit, round, findings, session. A round's
finding instances live in `docs/reviews/<unit>-round-<n>.md`.

## Commands

- `./gradlew :ledger-core:test` — run the core tests
- `./gradlew check` — the full gate

## Conventions

- Java 21.
- No comments in source code.
- Wrap at 120 columns.
```

### `openspec/changes/unit-a/tasks.md` (before the transfer)

```markdown
# Tasks — unit-a

Writer: ses_fixture_writer

## 1. Implementation

- [x] 1.1 Add entry validation to `LedgerService`.
- [ ] 1.2 Add the rejection path and its test.
- [ ] 1.3 Run `./gradlew :ledger-core:test`.

## Pending decision

- Whether a rejected entry is dropped or retained with a `rejected` flag. The owner has not answered.
```

### `docs/reviews.md`

```markdown
# Review rounds

| Date       | Unit   | Round | Findings | Session           |
| ---------- | ------ | ----- | -------- | ----------------- |
| 2026-10-30 | unit-z | 1     | 2        | ses_fixture_prior |
| 2026-10-30 | unit-a | 1     | 1        | ses_fixture_prior |
```

### `docs/reviews/unit-a-round-1.md`

```markdown
# unit-a — review round 1

- `ledger-core/src/main/java/com/example/ledger/core/LedgerService.java:16` — the rejection path returns the entry
  unchanged, so an invalid entry is recorded as valid.
```

### `.opencode/opencode.json` (after the setup rescue, below)

```json
{
  "$schema": "https://opencode.ai/config.json",
  "permission": {
    "bash": {
      "*": "allow",
      "git worktree add *": "deny",
      "git worktree remove *": "deny",
      "git worktree move *": "deny",
      "git worktree prune *": "deny",
      "git -C * worktree add *": "deny",
      "git -C * worktree remove *": "deny",
      "git -C * worktree move *": "deny",
      "git -C * worktree prune *": "deny"
    },
    "session_move": "deny"
  }
}
```

### Git state

Branch `unit-a` at the baseline commit, with one staged addition
(`ledger-core/src/main/java/com/example/ledger/core/EntryValidator.java`), one unstaged modification
(`LedgerService.java`), and one untracked file (`docs/notes/rejection-path.md`). No remote.

## The dependency manifest

Derived from the slice before the transfer. `existing equivalent` means the fixture states the contract in its own
words; `discovery glue` means the slice must be pointed at a fixture path or heading; `missing contract` means the
fixture has no equivalent, so the behavior is not exercised.

The first pass under-enumerated the review-gate references; review round 1 re-derived the set from the slice's own terms
(`review gate`, `packs`, `Workspace:`), and the table below is the corrected manifest.

| Dependency the slice names                                       | Fixture equivalent                                                            | Category            |
| ---------------------------------------------------------------- | ----------------------------------------------------------------------------- | ------------------- |
| `AGENTS.md` "Workflow: Change lifecycle"                         | `AGENTS.md` "How work is delivered"                                           | discovery glue      |
| The review gate's approval stop                                  | `AGENTS.md` "How work is delivered"                                           | existing equivalent |
| The review gate's handover rule                                  | `docs/reviews/unit-a-round-1.md` (the prior round's report)                   | discovery glue      |
| The review gate's reviewer-session recovery cases                | none (the fixture has no reviewer sessions)                                   | missing contract    |
| `AGENTS.md` subject-pack table and the `Packs:` line             | none                                                                          | missing contract    |
| The stage list (the record, the generalization, the manual pass) | none (the fixture's stages: proposal, review round, implementation, shipping) | missing contract    |
| `Workspace:` record (path, branch, writer)                       | the `Writer:` record plus the live path and branch                            | discovery glue      |
| `docs/review-rounds.tsv`                                         | `docs/reviews.md`                                                             | discovery glue      |
| A prior round's finding instances                                | `docs/reviews/unit-a-round-1.md`                                              | discovery glue      |
| The unit's `tasks.md` as the checkpoint location                 | `openspec/changes/unit-a/tasks.md`                                            | existing equivalent |
| ADR-0024 citation and supporting analysis                        | none (the transferred text keeps the rationale, not the record behind it)     | missing contract    |
| `/pause-unit`, `/resume-unit` command names                      | the transferred commands                                                      | existing equivalent |

## The transfer

Three files were copied into the fixture's `.opencode/`, and one root pointer was added to its `AGENTS.md`:

- `.opencode/commands/pause-unit.md`;
- `.opencode/commands/resume-unit.md`;
- `.opencode/skills/pause-resume-unit/SKILL.md`;
- a **Pausing and resuming a unit** paragraph in `AGENTS.md`, before "Workspace and ownership".

The edits, every occurrence recorded:

| Mapping                                                              | Occurrences                                                            | Classification                |
| -------------------------------------------------------------------- | ---------------------------------------------------------------------- | ----------------------------- |
| `"Workflow: Change lifecycle"` → `"How work is delivered"`           | 4 (skill preamble, skill resume step 1, pause command, resume command) | identifier/path               |
| `"Workflow: The review gate"` → the fixture's approval stop          | 1 re-pointed (resume command); 1 deleted (pause command)               | identifier/path               |
| `docs/review-rounds.tsv` → `docs/reviews.md`                         | 1 (skill resume step 6)                                                | identifier/path               |
| `Workspace:` record → `Writer:` record                               | 3 (skill field row, resume step 2, resume step 4)                      | identifier/path               |
| The preamble's "review gate's entry point" clause                    | 1 (skill preamble)                                                     | structural — missing contract |
| The `Packs:` line and the subject-pack table reference               | 1 (skill resume step 5)                                                | structural — missing contract |
| The ADR-0024 citation for the handover                               | 1 (skill decision point)                                               | structural — missing contract |
| The review gate's handover-rule clause → the prior round's report    | 1 (skill field row)                                                    | identifier/path               |
| The review gate's approval-stop clause → the fixture's approval stop | 1 (the reference and the "manual pass" wording)                        | identifier/path               |
| The reviewer-session recovery bullet                                 | 1 (skill decision point)                                               | structural — missing contract |
| The "packs are reloaded" completion clause                           | 1 (skill completion criteria)                                          | structural — missing contract |
| The stage-list trim                                                  | 1 (skill field row)                                                    | structural — missing contract |
| The root pointer                                                     | 1 (added)                                                              | declared support edit         |

No target **process** policy changed. The sandbox permission config is pilot harness, not the target's process policy:
narrowing its `git worktree *` deny to the mutating subcommands is the one unplanned rescue, and it changed no unit
rule. No undeclared support file was added.

The transferred files' diff against the source revision is embedded below. The source files are in this repository at
`9d7c6c97`; the fixture's copies are those files plus this diff.

```diff
--- .opencode/commands/pause-unit.md
+++ .opencode/commands/pause-unit.md
@@
 **Repository lifecycle** — this command starts no unit: it creates no branch or tree, commits, stashes, pushes,
 archives, or merges nothing, stops no build or container, and leaves the tree assigned to the unit. `AGENTS.md`'s
-"Workflow: Change lifecycle" owns that lifecycle and its "Workflow: The review gate" the approval stop.
+"How work is delivered" owns that lifecycle and the approval stop.
--- .opencode/commands/resume-unit.md
+++ .opencode/commands/resume-unit.md
@@
-**Repository lifecycle** — this command starts no new unit. Establishing the workspace is `AGENTS.md`'s "Workflow:
-Change lifecycle" rule. It commits, stashes, pushes, archives, or merges nothing, and stops no build or container; the
-approval stop `AGENTS.md`'s "Workflow: The review gate" owns is unchanged.
+**Repository lifecycle** — this command starts no new unit. Establishing the workspace is `AGENTS.md`'s "How work is
+delivered" rule. It commits, stashes, pushes, archives, or merges nothing, and stops no build or container; the
+approval stop `AGENTS.md`'s "How work is delivered" owns is unchanged.
--- .opencode/skills/pause-resume-unit/SKILL.md
+++ .opencode/skills/pause-resume-unit/SKILL.md
@@
-The procedure `AGENTS.md`'s "Workflow: Change lifecycle" triggers. This skill owns the `Resume checkpoint`'s field set,
-the pause's steps, and the resume's steps; `AGENTS.md` owns the lifecycle, the approval stop, and the review gate's
-entry point, and this skill adds no permission of its own.
+The procedure `AGENTS.md`'s "How work is delivered" triggers. This skill owns the `Resume checkpoint`'s field set,
+the pause's steps, and the resume's steps; `AGENTS.md` owns the lifecycle and the approval stop, and this skill adds no
+permission of its own.
@@
-| Workspace                   | The unit's `Workspace:` record — its path, branch, and writer                                                                                                    |
-| Stage                       | Where the unit is: the proposal, a review round, the implementation, the record, the generalization, the manual pass, or shipping                                |
+| Writer                      | The unit's writer record (`Writer:` in `tasks.md`), with the live path and branch                                                                                |
+| Stage                       | Where the unit is: the proposal, a review round, the implementation, or shipping                                                                                 |
@@
-| Findings no record holds    | The prior review round's finding instances (`file:line` and its claim), which live only in that report; the review gate's handover rule owns why they are needed |
+| Findings no record holds    | The prior review round's finding instances (`file:line` and its claim), which live only in that report; the prior round's report owns why they are needed        |
@@
-- The review gate's approval stop stands: a pause while the manual pass is outstanding leaves that request outstanding.
+- The approval stop stands: a pause while the owner's answer is outstanding leaves that request outstanding.
@@
-1. Read this skill, `AGENTS.md`'s "Workflow: Change lifecycle", and the unit's `Resume checkpoint` when it exists.
-2. Establish which unit continues: read its `Workspace:` record for the tree, branch, and writer, then confirm the live
+1. Read this skill, `AGENTS.md`'s "How work is delivered", and the unit's `Resume checkpoint` when it exists.
+2. Establish which unit continues: read its `Writer:` record for the writer, then confirm the live
@@
-   session as the writer in the unit's `Workspace:` record.
-5. Reclassify the resumed work's decisions and state the `Packs:` line, per `AGENTS.md`'s subject-pack table.
-6. Read the records the stage needs: the review rounds in `docs/review-rounds.tsv`, the change dir's artifacts, and the
+   session as the writer in the unit's `Writer:` record.
+5. Reclassify the resumed work's decisions.
+6. Read the records the stage needs: the review rounds in `docs/reviews.md`, the change dir's artifacts, and the
@@
-- A handover is a discipline, not a mechanism: ADR-0024 records that nothing binds a write to a session, so a matching
-  tree is not evidence that the prior writer ended.
-- No replacement reviewer session is started because of a pause; the review gate's own recovery cases govern that.
+- A handover is a discipline, not a mechanism: nothing binds a write to a session, so a matching tree is not evidence
+  that the prior writer ended.
@@
-session is the recorded writer, or asked of the owner; the packs are reloaded; and the work has continued at the
+session is the recorded writer, or asked of the owner; and the work has continued at the
```

## The trial

Eight `opencode run` invocations from the fixture's own directory in the first pass, each a fresh session. The
composition is two preflight, five behavioral invocations — including three T3 attempts: a setup failure, a mismatch
control, and the confirmed continuation — and one sandbox invocation covering both controls. Two post-review re-runs
followed the correction of the transfer.

| Id            | Session                          | Observed                                                                                                                                                                                     | Verdict                 |
| ------------- | -------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------- |
| T0            | `ses_e7021b01fffeztB3DwhMlklSmp` | Reported `LEDGER-MARKER-7F3A9C` with no tool call.                                                                                                                                           | pass                    |
| T0 control    | `ses_e70218679ffefwHk8p7YLUqRO8` | Reported that no fixture marker is stated, with the marker removed.                                                                                                                          | pass                    |
| T1            | `ses_e7020a4baffedPx1rQfZoXc7je` | Loaded the skill, read the unit's records, wrote a `Resume checkpoint` with all nine fields, changed no delivery state.                                                                      | pass                    |
| T2            | `ses_e701ffe74ffeTcsr80az7RaQa1` | Bare continuation stopped for the writer confirmation and the pending decision; no work write.                                                                                               | pass                    |
| T3 attempt 1  | `ses_e701f7f78ffeEnqB7GhsM4tAjg` | A compound `git status … && git worktree list …` was denied by the over-broad deny; the session stopped.                                                                                     | setup failure           |
| T3 attempt 2  | `ses_e701df885ffeOqJgyik5jBDM4n` | Reported that `.opencode/opencode.json` was modified but not in the checkpoint's change set; stopped without repair.                                                                         | pass (mismatch control) |
| T3            | `ses_e701d4e8bffe26jawozZegVgeh` | Applied the owner's answer about the config change, recorded the writer handover as `ses_e701d4e8bffe26jawozZegVgeh`, restored the pending decision and asked it, changed no delivery state. | pass                    |
| S1 + S2       | `ses_e701c34b4ffevvM1CxvpM0gsbr` | `git worktree add ../escape` was denied; the session-move to `/tmp` succeeded.                                                                                                               | S1 pass, S2 **fail**    |
| Re-run pause  | `ses_e70125035ffep5em22A6E0XwKf` | After the correction, wrote the checkpoint and changed no delivery state.                                                                                                                    | pass                    |
| Re-run resume | `ses_e7011975cffeBxVwlukA1c04Dv` | Recorded the writer handover, restored the pending decision, changed no delivery state.                                                                                                      | pass                    |

Two deviations from the frozen protocol: the mismatch control used the change-set disagreement rather than the
protocol's recorded-revision trigger (the same branch: live state disagrees with the checkpoint), and S1 and S2 shared
one invocation. The post-review re-runs are outside the eight-invocation window. They started from the fixture's
baseline `tasks.md`, restored with `git checkout -- openspec/changes/unit-a/tasks.md`, with the corrected transfer in
place; the re-run pause re-recorded the checkpoint.

After the trial: one worktree (the fixture), no remote, no `../escape` directory, and the fixture's working tree
unchanged by the control.

## The result

The pause/resume slice transferred with **six concept-level identifier/path mapping groups** (twelve occurrences), **six
structural removals**, **one declared root pointer**, **zero process-policy changes**, **one unplanned rescue**, and
**zero undeclared support files**. The exercised behaviors survived:

- the pause writes the full checkpoint and changes no delivery state;
- a bare continuation stops for the writer confirmation and the pending decision;
- a confirmed continuation records the handover and restores the pending decision;
- a live-state disagreement stops without repair;
- the worktree-creation deny fires.

Six findings:

1. **The reviewer-session and subject-pack contracts are missing.** The fixture states an approval stop, which the
   transfer re-points to; the reviewer-session recovery cases and the `Packs:` line have no equivalent, and the handover
   rule re-points to the prior round's report. The transfer keeps the resume's reclassification but drops the `Packs:`
   clause, the preamble's review-gate entry-point clause, the reviewer-session bullet, the packs-reload completion
   clause, and the stages the fixture does not have. Those behaviors are unexercised, not preserved.
2. **The ADR-0024 citation is missing.** The transferred text keeps the rationale — "a handover is a discipline, not a
   mechanism: nothing binds a write to a session" — but drops the ADR citation and its supporting analysis. The behavior
   and the rationale survive; the record behind them does not.
3. **The `session_move` deny is not enforced.** `opencode debug config` normalizes the rule as
   `{"action":"session_move","resource":"*","effect":"deny"}`, yet the `opencode.session_move` tool moved the session to
   `/tmp`. The sandbox's isolation was incomplete on that axis; no files escaped and no worktree, remote, or escape
   directory was created. This is a finding about the permission mechanism, not the slice.
4. **The checkpoint's change set is a snapshot.** The setup rescue edited `.opencode/opencode.json` after the pause, so
   the next continuation stopped on the mismatch. That is the specified behavior, but it means a transfer's setup edits
   must be reconciled before the pause or carried as the owner's answer in the continuation request.
5. **The writer handover needs the session's own identifier.** The slice does not say how to obtain it; the session
   found it with `env | grep -i -E 'session|opencode'` and recorded it. A discovery step, not a policy change.
6. **The fixture records `Writer:`, not `Workspace:`.** The transfer maps the field to the `Writer:` record and the live
   path and branch; the pause session improvised the same mapping before the correction. A discovery-glue gap the first
   pass missed.

The transfer contains six concept-level mapping groups. Counting the approval-reference and "manual pass" wording
adaptations separately gives seven distinct adaptations, one above the provisional limit of six. Compliance with that
limit therefore depends on the grouping convention; this record does not establish unqualified compliance. The observed
behavioral results are unchanged. Budget accounting: process-policy changes 0 (limit 0); unplanned rescues 1 (limit 2);
undeclared support files 0 (limit 0). The first-pass observation window was 8 invocations (limit 8); two post-review
re-runs followed the correction.

## The ceiling

**Observed transfer of this slice into this synthetic fixture, within the recorded adaptation burden, preserving the
behaviors exercised.**

This is not evidence that the process is "maximally prepared" for reuse. One slice and one synthetic fixture do not
establish that, and the fixture's process content was built to supply the checkpoint's inputs — a `Writer:` record, a
`Pending decision`, and a mixed change set. The result is a favorable constructed case; it establishes no comparative
convenience bound across projects. The unexercised contracts and the failed sandbox control are part of the result, not
caveats to it.

## Reopening condition

The program-level proposals remain deferred; each waits for the signal below. The retrospective's S1 placement-rule
proposal (`docs/retrospectives/2026-10-31.md`) keeps its existing condition — a concrete uncovered authoring decision —
and this pilot is not that case.

Reopen this decision when any one of the following holds:

- **A real target project becomes available** — rerun the pilot on it; the synthetic fixture's evidence is not
  representative (external trigger: an owner-named project).
- **A second slice is transferred** — compare its adaptation burden and whether the same couplings recur; reconsider the
  core-versus-substance ADR when the boundary recurs (internal trigger: the next reuse unit).
- **A second target needs the same adaptations** — reconsider a bootstrap generator or broad extraction (external
  trigger: the second target's transfer).
- **Recurring coupling drift across transfers** — reconsider the auditor axis when later transfer evidence records that
  recurrence (internal trigger: the next reuse unit).
- **Multiple active consumers with an explicit lesson-routing and version-ownership model** — reconsider the template
  repository (external trigger: a second consumer).
- **A mechanism enforces the `session_move` deny** — rerun the sandbox control (external coordinate: the OpenCode
  permission surface this repository pins).
- **The fixture gains the subject-pack contract or the ADR citation** — rerun the resume scenarios that the missing
  contract and record left unexercised (internal trigger: a fixture revision).

The unit's process-defect record is `docs/process-defects/probe-pause-resume-reuse.md`.
