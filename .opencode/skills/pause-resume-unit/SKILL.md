---
name: pause-resume-unit
description:
  The pause-and-resume procedure for a unit of work — the `Resume checkpoint` recorded before work stops, the unit
  record's state, and the verification, writer handover, unit resolution, and restored decision a session owes before it
  continues an unfinished unit. Load it before a pause and before continuing one, whether or not a pause was announced.
---

# Pausing and resuming a unit

The procedure the lifecycle the bindings declare triggers. This skill owns the unit record's pause fields, the pause's
steps, and the resume's steps; the lifecycle and the review gate the bindings declare own the approval stop and the
gate's entry point, and this skill adds no permission of its own.

Before using a binding — the lifecycle, the review gate, the review ledger, the workspace record, the change artifacts,
or the prior finding instances — read `docs/contracts/process-bindings.md`; it declares this repository's values.

**Trigger** — the owner announces a pause or invokes `/pause-unit`; the agent judges it cannot reliably continue the
unit in this session; or a session continues an unfinished unit, whether a pause was announced or the unit was simply
left waiting, and whether it invokes `/resume-unit` or not.

## The unit record

A unit has one record at `docs/units/<unit>/unit.md`, and it is the only authority for the unit's workspace, state, and
pause (ADR-0029).

- Its `Workspace:` line binds the tree, branch, and writer.
- Its `State:` line holds `active`, `paused`, or `closed`.
- Its `Resume checkpoint` section holds the pause's fields.
- A change's `tasks.md` references the record; it carries no copy of those fields.

A record without a state, or a record still inside a change's `tasks.md`, is **legacy**: it reads as unknown, never as a
pause, and it blocks the automatic choice until the unit mints its record.

## 1. Pausing

**Procedure**

1. Finish the operation in hand at a boundary — a command, an edit, a review round — and start nothing new.
2. Write the unit's `Resume checkpoint` in the unit record, and set its `State:` line to `paused`; a record the unit has
   not minted yet is written first, at the boundary, never in the middle of an edit.
3. Fill the field set below.
4. Read the record back and fix what it does not say.
5. End the turn with the pause reported: name the saved `Resume checkpoint`, the unit, and the tree, state the next
   step, and state that continuing this main session by its identifier keeps the unit's recorded agent sessions while a
   new session records each one's `parent-session-changed` replacement; name the `/resume-unit <unit>` entry beside that
   choice.

**The field set**

| Field                       | What it states                                                                                                                                                                                                                                                               |
| --------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Time and revision           | When the pause was recorded, and the revision observed (`git rev-parse HEAD`)                                                                                                                                                                                                |
| Workspace                   | A pointer to the record's `Workspace:` line — never a copy of its writer                                                                                                                                                                                                     |
| Stage                       | The interrupted operation and where the unit is in it — a proposal, a review round, the implementation, the record, the generalization, the manual pass, or delivery. This names the operation, never the lifecycle stage                                                    |
| Next step                   | One step, specific enough that a session can start it without re-deriving the plan                                                                                                                                                                                           |
| Change set                  | What `git status` showed — the staged, unstaged, and untracked sets, with nothing cleaned up                                                                                                                                                                                 |
| Blockers and open decisions | What is stuck, and what was decided locally without reaching the artifacts                                                                                                                                                                                                   |
| Pending decision            | What the unit waits for the owner to decide, if anything                                                                                                                                                                                                                     |
| Findings no record holds    | The prior review round's finding instances (`file:line` and its claim), at `docs/units/<unit>/findings/round-<N>.md`; a round predating that store is read from the checkpoint or the hand-over, and a unit holding neither is unrecovered context rather than a clean round |
| Operations in flight        | Each operation still running or stopped unseen, with its identifier, where its result would land, and that its outcome is unknown                                                                                                                                            |
| Agent sessions              | The agent-session register rows the next step continues, by agent, scope, and opinion — a selection, since the register stays authoritative for the identifier                                                                                                               |

**Decision points**

- A pause is not a delivery step: nothing is committed, stashed, pushed, archived, or merged for it.
- Pausing is the alternative to continuing through confirmation requests: when the agent cannot reliably continue, it
  records the checkpoint rather than ending each step with a request to confirm the next one. A decision the owner
  actually owes — an approval stop, a pending choice, a permission prompt — is not such a request.
- A session limitation the agent could not measure is its own assessment: the report does not call it an exhausted
  context window or provider quota unless the harness, the provider, or the account reported one. Upstream,
  `anomalyco/opencode#59531` asks for a remaining-budget signal to cite; it closed `not_planned` on 2026-10-27, so no
  harness fix is coming from it and its maintainer points at a plugin instead. This reference retires when a session can
  cite a reported figure, whether from the harness or from a plugin this repository ships.
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

1. Read this skill, the lifecycle the bindings declare, and the unit record when one exists.
2. Establish the unit.
   - A unit the request names is resolved by `python3 scripts/paused-units.py --unit <unit>`: exactly one registered
     tree assigns it, from the record whose `path` and `branch` name that tree.
   - Otherwise run `python3 scripts/paused-units.py` and read its document.
   - IF it is incomplete, or it carries any `unknown` entry, THEN end the turn: report what is unresolved and continue
     nothing, because the set of paused units is not established.
   - IF it carries one candidate, THEN continue it. Several are presented with their trees and end the turn for the
     owner's choice. None is reported and the turn ends.
   - The document's `selectable` flag says the same thing for this bare form: a caller continues nothing where it is
     false.
   - A named resolution reads `assignments`, not `candidates`: in that mode `selectable` means exactly one tree assigns
     the unit, and that assignment is what the resume continues or migrates.
3. Confirm the tree the resolution named: read the record's `Workspace:` line, then the live state — absolute path,
   branch, revision, and the staged, unstaged, and untracked set. A session in another tree moves there (`session_move`)
   only after the resolution, and re-verifies the path and branch afterwards.
4. Compare that live state with the record's `Resume checkpoint` when it exists; without one, state what no record holds
   as unrecovered and continue from the artifacts that do.
5. Hand the writer role over when this session is not the writer the record names: take the owner's confirmation —
   carried by the continuation request when it says the prior writer ended, asked for otherwise — then record this
   session as the writer in the record's `Workspace:` line.
6. Migrate a legacy unit, if the record is still inside its `tasks.md`, in that unit's own tree: read the legacy record,
   verify the tree, establish the handover, write `docs/units/<unit>/unit.md`, carry its checkpoint across unchanged,
   remove the fields from `tasks.md` leaving the reference, and verify both sides with no second live record. A resume
   that stops before the handover migrates nothing.
7. Reclassify the resumed work's decisions and state the `Packs:` line, per `AGENTS.md`'s subject-pack table.
8. Read the records the stage needs: the review rounds in the review ledger the bindings declare, the agent-session
   register's rows the checkpoint names, the change dir's artifacts, and the finding instances the checkpoint preserves
   from the prior round's report.
9. For each operation the record lists as in flight — or one the live state shows as interrupted — establish its outcome
   before relying on it or re-running it.
10. Continue the recorded sessions only when the stage's next step warrants the invocation, and only from the main
    session that recorded each row: equal, continue it by the recorded identifier and verify the identifier the
    invocation returns; different, start the replacement from the recorded hand-over and record its
    `parent-session-changed` row when the outcome is known, without attempting the refused continuation. An unresolved
    operation's outcome is established before any invocation, and a consultation still needs the owner's authorization.
11. Restore what the unit was waiting on: a decision the owner has not answered stays open, and an answer the request
    carries is applied rather than asked for again.
12. Set the record's `State:` line to `active` only at the point the work continues, after the verification and the
    handover; the checkpoint is frozen there as the pause's snapshot, and a later pause replaces it.
13. Continue at the recorded next step, or end the turn with the question the unit is waiting on.

**Decision points**

- The state verification is read-only and comes first; the writer handover is the first write the resume makes, and no
  work write precedes it.
- Membership comes from the record's `path` and `branch`; the tree's claim is consulted only to contradict them, never
  to establish the unit's name. A unit without a change directory resolves the same way, its claim being `none`.
- A working tree matching the record is not evidence that the prior writer ended: ADR-0024 records that nothing binds a
  write to a session.
- A handover does not depend on a checkpoint existing: a unit left waiting with neither a pause nor a checkpoint still
  hands over.
- The name establishes the unit to continue; it establishes neither the writer handover nor an answer to a pending
  decision.
- A pause alone starts no replacement session; a resumed unit in a new main session records the forced
  `parent-session-changed` replacement, as the agent-session requirement states, and a session's own recovery case
  governs any other replacement.
- A resume does not lift an approval its continuation request does not answer.
- A bare resume request is neither a writer confirmation nor an answer to a pending decision.
- A closed record with no tree assigning it is not reopened.

**Failure handling**

- IF the live state disagrees with the record, THEN stop and report the disagreement; do not repair the tree.
- IF the record names no writer, or a handover is required and its confirmation is not given, THEN stop for the owner's
  decision and write no work in the tree; no build or container must be stopped for that decision.
- IF the live state shows an interrupted operation the record does not list, THEN treat its outcome as unknown and
  establish it before relying on it.
- IF no checkpoint exists, THEN name the context no record holds rather than reconstructing it, and do not read the
  absence as evidence that the interruption went wrong.
- IF an operation's outcome cannot be established, THEN treat it as unknown: neither re-run it blindly nor report it as
  done.

## Completion criteria

A pause is complete when the record carries the checkpoint and `paused`, it is read back and reported with the unit and
the tree, and no delivery state changed.

A resume is complete when the unit is established; the state has been verified; the writer handover is established,
already held because this session is the recorded writer, or asked of the owner; a legacy record is migrated or the
resume stopped before the handover; the packs are reloaded; and the work has continued with the record `active`, or the
unit's question is back with the owner.
