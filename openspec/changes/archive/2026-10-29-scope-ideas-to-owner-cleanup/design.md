# Design

## Context

See `proposal.md` — Why. Three positions competed for the parked-idea lifecycle; this change records why the scratchpad
identity decides.

- `docs/ideas.md` describes itself as a scratchpad, not a backlog of planned work, and `/ideas` repeats that an entry is
  not a commitment.
- The two audit widenings (2026-10-16 for landed work, 2026-10-29 for the entry's own condition) each justified
  themselves by the file already being in the auditor's comparison span. That argument establishes reuse, not necessity:
  a shared input is not a shared purpose.
- The owner proposed a cleanup command earlier and it was turned down on that reuse argument (the owner's account; the
  consultation left no repository record). It was the right shape.

The identity does not make the file un-auditable; it decides **what kind of check** is appropriate. A scratchpad's
hygiene is pruning by its owner, not a weekly semantic verdict on relevance. An unimplemented note is not an overdue
obligation, and a met reopening condition means a decision has come due, not that a statement has gone stale — the
distinction the retired class preserved and that this change keeps for the ADR gate.

## Goals / Non-Goals

**Goals:**

- Return `docs/ideas.md` to its stated identity: notes pruned by their owner, with a cheap recurring act.
- Keep the audit's architectural content: the ADR `Revisit when:` gate, the rationale search, and the condition
  classification rules.
- Leave no reading of a parked entry that implies an obligation.

**Non-Goals:**

- A deterministic triage script. The cleanup is a reading act; a classifier would add machinery the objective does not
  need, and its signals (an entry naming an archived change) are hints, never verdicts.
- Mandatory decay: a 90-day expiry would address none of the current entries (the oldest section is 2026-09-01) and
  would replace model administration with owner administration.
- A new command: `/ideas` already manages the file, so the cleanup is a mode of it.
- Reverting the two widenings by revert: the landed-work sweep predates the condition sweep, so both are retired
  forward, in one change.

## Decisions

**The cleanup is a mode of `/ideas`, not a new command.** The file has one entry point; adding `/cleanup-ideas` beside
it splits the artifact's surface and leaves two places that must agree. The mode's procedure is the new content.

**One bounded batch, oldest first, with a cursor.** Age orders attention; it proves nothing about merit, so the batch is
a reading order, not a ranking. The owner disposes `keep` / `narrow` / `promote` / `drop`, and the batch advances a
cursor in the file's header whether an entry was kept or dropped — otherwise a kept oldest entry would be re-presented
forever, and the cleanup could never reach the later sections.

**Removals are presented, never silent.** A `narrow` or `drop` that rests on "this shipped" verifies that claim before
it is used — the described change, commit, workflow or mechanism exists, or a different mechanism achieves the entry's
objective; a drop that rests on "nobody wants this" needs no evidence beyond the owner's decision. A retained entry
keeps its original date and needs no re-justification.

**The audit keeps the file for the rationale class, not for a lifecycle.** A deliberate architectural choice may be
explained only by a parked deferral or band-aid, so the rationale sweep still reads the file for that. What goes is the
per-entry lifecycle check.

**Both sweeps retire together.** They share a subject and a failure mode; leaving the landed-work sweep in place would
keep the architecture auditor as the lifecycle owner of the same entries, one class smaller.

## Obligation trace

The removed requirement's clauses, each read against the replacement:

| Clause in the removed requirement                                                                                                                          | Destination                                                                    |
| ---------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------ |
| Report deliberate choices and absences whose rationale is unrecorded                                                                                       | retained                                                                       |
| Sweep the exclusion, suppressed-major, held-back, and npm-suppressed coordinate surfaces                                                                   | retained                                                                       |
| Sweep suppression annotations and retained deprecated APIs                                                                                                 | retained                                                                       |
| Sweep deferrals or band-aids recorded in an ADR or parked in `docs/ideas.md`                                                                               | retained — as evidence of a deliberate deferral whose rationale may be missing |
| Premise-met class: a decision deferred in an ADR whose `Revisit when:` condition appears met                                                               | retained                                                                       |
| Premise-met class: a parked entry whose work has landed                                                                                                    | **retired** — the scratchpad lifecycle leaves the audit                        |
| Premise-met class: a parked entry whose own recorded reopening condition appears met                                                                       | **retired** — same                                                             |
| Hold a recorded condition against the repository where it names a checkable fact; report none that names none                                              | retained, narrowed to the ADR condition                                        |
| Hold the parked entry against the work it describes rather than an artifact it merely cites                                                                | **retired**                                                                    |
| Search the repository for a recorded rationale before reporting; never report a recorded one                                                               | retained                                                                       |
| The deferred-record class is the inverse: reported only when its premise has expired                                                                       | retained, narrowed to the ADR deferral                                         |
| Each item names the choice or absence with its location and the question that would record the rationale; for an expired premise, the condition and signal | retained, narrowed                                                             |
| Advisory, not defects                                                                                                                                      | retained                                                                       |
| The rationale search covers ADRs, specs, `AGENTS.md`, build files, configuration, source, and conventions                                                  | retained                                                                       |
| A low-impact local detail is not an architectural decision                                                                                                 | retained                                                                       |
| A recorded condition keeps its qualifiers and compound structure; is classified four ways; reported only when satisfied                                    | retained, narrowed to the ADR condition                                        |
| A newer version alone does not satisfy a compatibility/adoption condition; an inability to verify is not an expiry claim                                   | retained                                                                       |
| An expired condition triggers a review, not a predetermined change                                                                                         | retained                                                                       |
| A parked entry's met condition is a recurrence that is due, never an instruction to remove the entry                                                       | **retired** — the tier itself retires                                          |
| A parked entry distinguishes landed, partly landed, and a different mechanism, and a cited artifact does not establish completion                          | **retired** — same                                                             |
| Every item carries the shared report contract's per-item budget                                                                                            | retained                                                                       |

Scenarios retained under their original names: "An unexplained deliberate choice is surfaced as a question", "A recorded
rationale is not reported", "Intent-clarification items are advisory", "A deferred decision whose condition appears met
is surfaced", "A deferral whose condition is not repository-decidable is not reported", "A deferral's condition is
classified before it is reported". Scenarios retired with the class: "A parked idea whose work has landed is surfaced",
"A parked idea that merely cites an artifact is not reported", "A parked idea whose work only partly landed is reported
as partial", "A parked idea whose own recorded condition appears met is surfaced as due", "A parked idea whose condition
is not met is not reported", "A parked idea's condition the repository cannot settle is not reported".

## Risks / Trade-offs

- **A parked entry whose condition is met goes unnoticed between cleanups** → the capture rule checks the condition when
  the owner generalizes an incident that meets it, and the owner reads the entry at the cleanup; the trade is
  deliberate, since a weekly semantic verdict on 99 notes was the cost this change removes.
- **The cleanup is owner-triggered, so it can be skipped** → the retrospective reads the file each window and can carry
  the batch; the owner decides the cadence.
- **A drop loses a still-useful note** → removals are presented before they are applied, and the durable lesson is
  routed first.
- **The audit's rationale sweep still lists the file** → it reads the file for a deferral's or band-aid's missing
  rationale, as one of the surfaces a deliberate choice implies; what it no longer does is decide whether an entry
  itself is still wanted or still true.

## Migration Plan

None: prose, a command procedure, and a spec requirement, taking effect at the next invocation.

## Claims table

| Claim                                                                         | Source                                                                                                                                                                  |
| ----------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The file describes itself as a scratchpad, not a backlog                      | the `docs/ideas.md` header and the `/ideas` decision points, quoted in the proposal                                                                                     |
| A cleanup command was proposed earlier and turned down on the reuse argument  | the owner's account: the consultation left no repository record, so this row is labelled rather than cited                                                              |
| The sweep covers every entry, including non-architectural notes               | the definition's sweep bullet ("for each parked entry"), and its Scope list naming `docs/ideas.md`; the file's entries include social-preview and support-surface notes |
| Both widenings justified themselves by the file being in the comparison span  | the 2026-10-16 and 2026-10-29 designs, each stating the reuse argument in its Context                                                                                   |
| A met condition is not a stale statement                                      | the retired class's own rule — a met condition is reported as a recurrence that is due, never as a removal instruction — restated here for the ADR gate                 |
| An owner-triggered cleanup needs no new command or script                     | `/ideas` already appends and prints the file; the cleanup is a procedure over the same artifact                                                                         |
| The retired scenarios cannot be dropped in a `MODIFIED` block                 | `AGENTS.md`'s delta rules (a `MODIFIED` block replaces the whole requirement and scenario omission fails validation), which is why the delta uses `REMOVED` + `ADDED`   |
| The scheduled audit keeps the weekly architectural checks                     | the `audit` workflow's prompt runs the same auditors; only the report's content changes                                                                                 |
| The capture rule checks a parked entry's condition at the next generalization | `AGENTS.md`'s generalization Decision points, which route an incident meeting a parked entry's condition to a reopened entry                                            |
