---
name: pause-resume-unit
description:
  The pause-and-resume procedure for a unit of work — the `Resume checkpoint` recorded before work stops, and the
  verification, writer handover, and restored decision a session owes before it continues an unfinished unit. Load it
  before a pause and before continuing one, whether or not a pause was announced.
---

# Pausing and resuming a unit

The procedure `AGENTS.md`'s "Workflow: Change lifecycle" triggers. This skill owns the `Resume checkpoint`'s field set,
the pause's steps, and the resume's steps; `AGENTS.md` owns the lifecycle, the approval stop, and the review gate's
entry point, and this skill adds no permission of its own.

**Trigger** — the owner announces a pause or invokes `/pause-unit`; or a session continues an unfinished unit, whether a
pause was announced or the unit was simply left waiting, and whether it invokes `/resume-unit` or not.

## 1. Pausing

**Procedure**

1. Finish the operation in hand at a boundary — a command, an edit, a review round — and start nothing new.
2. Write the unit's `Resume checkpoint` in the unit's own record: its `tasks.md` when it has a change dir, otherwise its
   hand-off. Create it once and update it in place; never append a second section.
3. Fill the field set below.
4. Read the record back and fix what it does not say.
5. End the turn with the pause reported, naming the tree and the next step.

**The field set**

| Field                       | What it states                                                                                                                                                   |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Time and revision           | When the pause was recorded, and the revision observed (`git rev-parse HEAD`)                                                                                    |
| Workspace                   | The unit's `Workspace:` record — its path, branch, and writer                                                                                                    |
| Stage                       | Where the unit is: the proposal, a review round, the implementation, the record, the generalization, the manual pass, or shipping                                |
| Next step                   | One step, specific enough that a session can start it without re-deriving the plan                                                                               |
| Change set                  | What `git status` showed — the staged, unstaged, and untracked sets, with nothing cleaned up                                                                     |
| Blockers and open decisions | What is stuck, and what was decided locally without reaching the artifacts                                                                                       |
| Pending decision            | What the unit waits for the owner to decide, if anything                                                                                                         |
| Findings no record holds    | The prior review round's finding instances (`file:line` and its claim), which live only in that report; the review gate's handover rule owns why they are needed |
| Operations in flight        | Each operation still running or stopped unseen, with its identifier, where its result would land, and that its outcome is unknown                                |

**Decision points**

- A pause is not a delivery step: nothing is committed, stashed, pushed, archived, or merged for it.
- The staged set is recorded rather than cleaned, added to, or reset.
- The tree stays assigned to the unit, and no build, container, or daemon is stopped for the pause.
- The review gate's approval stop stands: a pause while the manual pass is outstanding leaves that request outstanding.
- IF the owner asks for a commit or a push in the same request, THEN that instruction is theirs and separate from the
  pause, which still changes no delivery state.
- The field set is this skill's; every other artifact points at it rather than enumerating it.

**Failure handling**

- IF the record cannot be written, THEN report it and keep the turn open: a pause nobody can read back is not a pause.
- IF an operation cannot be brought to a boundary, THEN record it as in flight with its unknown outcome rather than
  waiting on it indefinitely.

## 2. Resuming

**Procedure**

1. Read this skill, `AGENTS.md`'s "Workflow: Change lifecycle", and the unit's `Resume checkpoint` when it exists.
2. Establish which unit continues: read its `Workspace:` record for the tree, branch, and writer, then confirm the live
   state — absolute path, branch, revision, and the staged, unstaged, and untracked set.
3. Compare that live state with the checkpoint when there is one; without one, state what no record holds as unrecovered
   and continue from the artifacts that do.
4. Hand the writer role over when this session is not the writer the record names: take the owner's confirmation —
   carried by the continuation request when it says the prior writer ended, asked for otherwise — then record this
   session as the writer in the unit's `Workspace:` record.
5. Reclassify the resumed work's decisions and state the `Packs:` line, per `AGENTS.md`'s subject-pack table.
6. Read the records the stage needs: the review rounds in `docs/review-rounds.tsv`, the change dir's artifacts, and the
   finding instances the checkpoint preserves from the prior round's report.
7. For each operation the record lists as in flight — or one the live state shows as interrupted — establish its outcome
   before relying on it or re-running it.
8. Restore what the unit was waiting on: a decision the owner has not answered stays open, and an answer the request
   carries is applied rather than asked for again.
9. Continue at the recorded next step, or end the turn with the question the unit is waiting on.

**Decision points**

- The state verification is read-only and comes first; the writer handover is the first write the resume makes, and no
  work write precedes it.
- A handover does not depend on a checkpoint existing: a unit left waiting with neither a pause nor a checkpoint still
  hands over.
- A handover is a discipline, not a mechanism: ADR-0024 records that nothing binds a write to a session, so a matching
  tree is not evidence that the prior writer ended.
- No replacement reviewer session is started because of a pause; the review gate's own recovery cases govern that.
- A resume does not lift an approval its continuation request does not answer.
- A bare resume request is neither a writer confirmation nor an answer to a pending decision.

**Failure handling**

- IF the live state disagrees with the checkpoint, THEN stop and report the disagreement; do not repair the tree.
- IF the record names no writer, or a handover is required and its confirmation is not given, THEN stop for the owner's
  decision and write no work in the tree; no build or container must be stopped for that decision.
- IF the live state shows an interrupted operation the record does not list, THEN treat its outcome as unknown and
  establish it before relying on it.
- IF no checkpoint exists, THEN name the context no record holds rather than reconstructing it, and do not read the
  absence as evidence that the interruption went wrong.
- IF an operation's outcome cannot be established, THEN treat it as unknown: neither re-run it blindly nor report it as
  done.

## Completion criteria

A pause is complete when the `Resume checkpoint` is written, re-read, and reported, and no delivery state changed.

A resume is complete when the state has been verified; the writer handover is established, already held because this
session is the recorded writer, or asked of the owner; the packs are reloaded; and the work has continued at the
recorded step or the unit's question is back with the owner.
