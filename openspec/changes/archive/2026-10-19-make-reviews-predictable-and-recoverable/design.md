# Design

## Context

The review loop's quick pass is a `review-quick` subagent invocation the calling agent repeats until a round reports
nothing new, and its contract calls the pass a bounded screen whose stop condition scales with what it is given. Two
gaps sit beside that contract, and the second one's size depends on capabilities this design established rather than
assumed.

**What the interface actually supports.** Established from the `subagent` interface in use and the CLI's own help, not
from expectation:

- The invocation's parameters are the agent, a description, the prompt, a `sessionID` to continue, a `background` flag,
  and a model. **There is no timeout parameter and no cancellation parameter**, so the caller cannot bound an
  invocation's duration or ask it to stop.
- A background invocation is the only asynchronous form: it returns immediately and the caller is notified on
  completion. **No partial output is available while it runs**, so a stalled invocation reports nothing the caller can
  read.
- `opencode session` offers `list`, `delete`, `export`, and `import`. **There is no abort or stop**, so an in-flight
  session cannot be cancelled through the documented interface; a human can interrupt in the TUI, and either way the
  unreported work is gone.
- Continuation is supported: `sessionID` on the invocation, and `opencode --session <id>` from the CLI.
- `opencode api` is a generic OpenAPI caller whose operation set its own help does not enumerate (a bogus operation
  reports "Operation not found" without listing the valid set), so no abort operation can be relied on from the
  interface in use.

The consequence shapes the whole design: **a stall cannot be prevented by the caller and cannot be undone by it.** What
the caller controls is when it stops waiting and what it carries forward — so the only thing that makes a stall
recoverable is a record written before the wait, from the caller's own knowledge.

**What the loop already has.** `showcase/quality/agent-skills` owns the quick screen, the routing decision, the reviewer
sessions, and the records; `docs/review-rounds.tsv` declares the session fields; and `scripts/commit-hygiene.py` already
accepts `expired`, `definition-changed`, and `stopped-responding` as replacement reasons. So the replacement
_vocabulary_ exists — what is missing is the protocol that decides when to use it and what to transfer, which is why the
same clause has been read as an unexplained permission.

**The parked record this change sits under.** `docs/ideas.md` parks the review loop's rejected levers — a persistent
session as a substitute for the per-round hand-off, diff-scoped follow-ups, a fixed iteration cap on the quick loop, and
token, elapsed-time, or session-count telemetry — and directs any further quick-loop change to the baseline comparison
rather than a fresh proposal, reasoning that a scope-and-stop-condition bound stays measurable where a count would not.
This change re-derives none of the four levers: it keeps the session reuse and the per-round hand-off, adds no iteration
cap, and excludes the telemetry. Its bounded-screen half is the measurable kind the record names, so the comparison is
the instrument for its cost effect, and this change claims none — its driver is the stall. The record's direction stands
recorded rather than argued away, and whether that half should await the comparison is the owner's call.

**The delta's shape.** The requirement the loop lives in is a single requirement block of 352 lines, so a `MODIFIED`
block copies it whole — the shape the prior session changes used (the largest a 355-line delta) — and the new scenario
is added inside it. No requirement is added: every behaviour here refines one the requirement already holds, and a new
requirement would cost a permanent corpus entry for behaviour that belongs to this one.

## Goals / Non-Goals

**Goals:**

- Make a quick-review round's duration bounded by what it was given, so a round is predictable.
- Make a stalled invocation recoverable from the caller's own record, with no dependence on the stalled session.
- State when to wait, when to declare `stopped-responding`, and what the replacement receives.
- Keep the existing gate intact: the mandatory checks, the finding-evidence rules, the pre-write pass, the manual
  review, the routing decision, the pass budget, and the ledger's continuity rules.

**Non-Goals:**

- No timeout value: no measurement of a reviewer's duration is reproducible from the repository, so the design supplies
  a protocol and leaves the deadline to the invocation or the caller's policy.
- No new ledger field, and no duration, token, or session-count telemetry: the record keeps only facts a gate can
  re-derive.
- No new requirement, and no change to the routing decision, the pass budget, or the ledger's schema and checker.
- No attempt to cancel a running reviewer, since the interface offers none.

## Decisions

**The four options, against the two properties they must satisfy.** A stall needs recovery; a round needs a bound. Each
option is scored on whether it delivers either, and on what it costs the existing gate.

| Option                                                                  | Bounds a round's duration                                                                                                    | Recovers a stall                                                                                                                   | Cost to the gate                                                                             |
| ----------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------- |
| (a) Clarify the reviewer prompts only                                   | Partly — a clearer stop rule narrows what a pass reads, but nothing observable bounds it, and a stall is not a prompt defect | **No** — a stalled invocation still reports nothing and the loop still has no protocol                                             | Lowest; leaves the stall unaddressed                                                         |
| (b) Caller-side watchdog plus a portable handover                       | **No** — it changes what the caller does while waiting, not how long the pass runs                                           | **Yes** — the record exists before the wait, so a replacement starts from the caller's knowledge regardless of the stalled session | Adds a protocol and a handover obligation to the caller's invocation step                    |
| (c) A bounded screen with a fixed supplied list and caller risk focuses | **Yes** — the inputs become a list, and the pass's own contract forbids adding surfaces                                      | **No** — a bounded pass can still stall                                                                                            | Adds a supplied-inputs obligation and an escalation route                                    |
| (d) Move the work into thorough review                                  | Not by itself — it relocates depth rather than bounding it                                                                   | **No**                                                                                                                             | Reduces what the mandatory quick gate covers, which is the quality the task forbids reducing |

**Chosen: (c) plus (b), with (a) folded into (c) as the stop rule.** Neither (c) nor (b) delivers both properties, and
the pair is the smallest set that does: (c) is what makes a round predictable, and (b) is the only option that makes a
stall recoverable at all. (a) is not a separate option but part of (c) — a stop rule the pass's own contract carries is
what "do not expand scope" means in practice — so it is applied as (c)'s implementation rather than as a rival choice.
(d) is rejected: the routing decision already sends depth to `review-thorough`, so moving more work there would take
coverage away from the mandatory gate without bounding anything.

**The handover is written before the wait, not extracted from the stalled session.** This is the design's load-bearing
decision and it follows from the capability finding above: since no partial output is available and no cancellation
exists, anything the caller does not already hold is lost. So the invocation's handover — identity, base and current
revisions, supplied artifacts, scope, prior findings with their instance-level evidence, completed checks, known
limitations — is recorded by the caller _before_ it waits, and a replacement receives that record. The field set is this
change's, stated in the watchdog scenario; the deep-review scenario states the hand-over a routed deep-review session
receives, and the two overlap without being identical — a replacement needs the base and current revisions and the
completed checks, while a routed session needs whether the pass is an initial review or a fix verification. Unifying the
two enumerations is a consolidation this change does not attempt, since it would edit a clause no defect in this unit
points at.

**The replacement is a replacement, never a continuation, and it re-verifies independently.** The continuity rules stay
as they are: the saved session id is reused in the normal case, a continuation's returned identifier is compared with
the saved one, and only expiry, a definition change, or `stopped-responding` justifies a replacement. The new protocol
adds what was missing — the record, the ordering, and the reason — and states that the handover is context rather than
proof, so a persistent session never substitutes for independent re-verification.

**The deadline is the caller's, and the design says so.** The scenario states that the deadline comes from the
invocation or the caller's policy and never from a universal figure presented as measured. The repository has no
measurement to offer: `docs/review-rounds.tsv` records rounds, classes, and session transitions, and nothing that times
one.

**The escalation route is stated rather than implied.** The existing scenario already says depth beyond the supplied
scope "belongs to the deep review the routing decision below selects", but a reviewer reading it can still absorb the
depth instead. The delta makes it an instruction: a risk the scope cannot settle is escalated — through the routing
decision or by the caller raising it — so widening the quick pass is not an available response.

**Nothing is added to the ledger.** The replacement reason and both session identifiers already have fields, and the
round's outcome is the row itself, so the protocol records into the existing schema. A duration or token count is
explicitly excluded by the new scenario, because no gate can re-derive one — the same rule the repository already
applies to a verdict task whose inputs cannot produce its result.

## Risks / Trade-offs

- **A supplied-inputs list can under-specify the screen.** A caller who lists too little narrows what is screened while
  the report still reads as complete. Mitigated by requiring the report to name the list, so the omission is visible in
  the round's own output rather than only in a later thorough pass.
- **A deadline can be set too short, replacing a working reviewer.** The protocol makes the replacement carry the full
  handover and re-verify independently, so a needless replacement costs a session and not a review; and the deadline is
  the caller's stated policy rather than a repository-wide default, so the trade is made where it can be judged.
- **`stopped-responding` can be declared on a slow but live reviewer.** The stalled session is not cancelled (it cannot
  be), so both it and the replacement may report; the ledger records the replacement's transition, so the round's
  provenance stays readable rather than two rounds appearing to be one.
- **A MODIFIED block of this size is mostly a copy.** Mitigated by generating it from the current spec block and by
  asserting, as a task, that every pre-existing scenario and clause survives the edit — the validator checks scenarios,
  not clauses.

## Migration Plan

None: the definitions and `AGENTS.md` take effect on the next loop, and the delta syncs at archive. No ledger migration
is needed, because no field is added or removed.

## Claims table

| Claim                                                                                                                        | Source                                                                                                                                                   |
| ---------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The reviewer invocation has no timeout and no cancellation parameter                                                         | the `subagent` interface in use — its parameters are the agent, a description, the prompt, `sessionID`, `background`, and a model                        |
| A background invocation reports only on completion, with no partial output                                                   | the same interface's documented behaviour, as exercised in this repository's own loop                                                                    |
| `opencode session` offers no abort or stop                                                                                   | `opencode session --help` — `list`, `delete`, `export`, `import`                                                                                         |
| Continuation is supported by `sessionID` and `opencode --session <id>`                                                       | `opencode --help`; the session-continuity clause in `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`                                 |
| The replacement vocabulary already exists in three places                                                                    | the spec's continuity clause; `docs/review-rounds.tsv`'s header comment; `scripts/commit-hygiene.py`'s `SESSION_REPLACEMENT_REASONS`                     |
| The requirement the loop lives in is one block of 352 lines, and a `MODIFIED` block copies it whole                          | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`; the prior session changes' deltas (145, 193, and 355 lines), all `MODIFIED` blocks |
| The ledger has no field for a duration, a token count, or a session count                                                    | `docs/review-rounds.tsv`'s header comment and its declared fields                                                                                        |
| The quick screen's bounded-screen clauses already exist and are refined rather than replaced                                 | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`, the quick-review scenario                                                          |
| The routing decision already sends depth to the deep review                                                                  | the same spec's routing scenario and its `NOT_NEEDED` clause                                                                                             |
| The deep-review scenario enumerates its own hand-over, including whether the pass is an initial review or a fix verification | `git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`, the "routed deep review runs in its own session" scenario                          |
| The parked review-loop record rejects four levers and directs any further quick-loop change to the baseline comparison       | `git show HEAD:docs/ideas.md` (the 2026-10-15 entry); `docs/retrospectives/2026-10-15.md`'s "Recommended next direction"                                 |
| The delta's edits are the clauses the diff against the pre-change block shows, and the description sentence it adds          | the delta's own diff against the pre-change block (`git show HEAD:openspec/specs/showcase/quality/agent-skills/spec.md`)                                 |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per kind): **`[path]`**
candidates name files, paths, or commands rather than asserting facts; **`[cross-ref]`** candidates are names,
slash-joined phrases, and ratios rather than claims — the scanner's `/` heuristic catches each shape, and none asserts
anything about the repository; **`[count]`** candidates are the unit's own bookkeeping — the round it records, its task
and section numbering, and the results its notes report — rather than facts about the repository; **`[absolute]`**
candidates are modal, structural, or scope statements where they appear, or a claim that carries its own row above.
