# Design — owner authorization for a unit's start and its implementation

## Context

The lifecycle's procedure has five steps and starts at "Propose the change"; the only approval stop it names is the
review gate's manual pass, whose §5 runs it at the implementation stage. The specification states the approval once, for
shipping ("work uncommitted until the manual approval pass"), and a mutating command's preflight repeats that stop. So
an agent can begin a unit on its own judgement, and it can implement a proposal before the owner has seen that proposal.

The owner's rule, as stated: a discussion of what should be done is not authorization to do it; any unit starts only on
their permission; any proposal passes manual review before implementation; any unit passes manual review before
shipping.

The corpus already maps units without a separate proposal stage — a docs refresh, a standalone fix, a dependency bump —
onto the single pass the review-gate invariant states (the "The review gate covers every unit of work…" bullet in
"OpenSpec Workflow Agreement" → "Invariants"). The change amends that mapping so it applies only where the owner's
authorization itself names each direct edit the unit may make, rather than imposing a second pass on a unit that has no
proposal to review.

## Decisions

1. **The start gate is an invariant of the change lifecycle**, not a rule of individual commands: every unit — change,
   docs unit, audit application, dependency bump — begins only on the owner's explicit instruction to begin it.
2. **Authorization means an instruction, not an inference.** An explicit request to do the work, an approval of a
   proposal the agent presented, or the owner naming a unit to continue. A discussion, a question, an idea the owner
   explores, or the agent's own conclusion that the task is clear is not authorization; the agent states the proposal
   and waits.
3. **The manual review pass is stage-scoped.** The review gate's §5 already owns the ask; the change makes it explicit
   that it runs at each boundary a unit has — the proposal before implementation, the implementation before delivery —
   and that a unit with no separate proposal stage keeps the single pass that invariant maps. The existing preconditions
   apply to the stage the pass closes.
4. **The shipping stop is unchanged**: it belongs to the rule that already states it, and the new text points there
   rather than repeating it.
5. **The specification carries the start gate and the proposal stop**, because both are behaviour a reader of the corpus
   must be able to rely on; the mutating-command requirement gains the two stops in the lifecycle it makes a command
   state, and the thirteen command definitions that carry that preflight are updated with it.
6. **The owner's authorization is a precondition, not a reviewable artifact**: until the proposal is approved an agent
   writes only the unit's planning artifacts and its records under `docs/units/<unit>/`, changing no implementation
   target and no live rule file. The operative wording is the writable-scope invariant in the proposed wording below;
   this decision records why the boundary is drawn there rather than banning every write.

## Proposed wording

### `AGENTS.md` — "Workflow: Change lifecycle" → Invariants

> - **A unit starts only on the owner's instruction.** An agent that believes it has understood what the owner wants
>   states the proposal and waits. A unit begins only on an explicit instruction to begin it, on the owner's approval of
>   a proposal the agent presented, or on the owner naming the unit to continue. A discussion of what should be done, a
>   question the owner asks, an idea they explore, or the agent's own conclusion that the task is clear is not that
>   authorization.
> - **A proposal passes the owner's manual review before any implementation edit.** The review gate's pass runs at each
>   boundary a unit has: the proposal before implementation, the implementation before delivery. A unit takes the single
>   pass instead only where the owner's authorization itself names each direct edit the unit may make; presenting the
>   work as a proposed edit does not remove the proposal's pass. The delivery stop is that rule's and is unchanged.
> - **Until the approval its route requires, only the unit's own artifacts are writable.** A two-pass unit waits for the
>   proposal's review; a single-pass unit's writable scope begins with the owner's authorization that names each direct
>   edit the unit may make. Either way an agent may create and edit the unit's planning artifacts and its records under
>   `docs/units/<unit>/`, and changes no implementation target and no live rule file until that approval.

### `AGENTS.md` — "OpenSpec Workflow Agreement" → Invariants, the "The review gate covers every unit of work…" bullet

> Any other bullet of the same list is unchanged. The bullet's mapping sentence becomes:
>
> > There is no proposal/implementation/archive vocabulary for those, so map the rule onto what exists: run
> > `review-quick` over the diff against the repo (and the change dir when one exists), fix the findings, re-run until
> > clean, route the deep review, then ask the user for the manual pass — all _before_ committing, pushing, or opening
> > the PR. Only where the owner's authorization itself names each direct edit the unit may make does that mapping take
> > the manual pass once, before delivery; presenting the work as a proposed edit without that authorization does not.

### `AGENTS.md` — "Workflow: Change lifecycle" → Procedure

> 1. **Begin the unit only on that authorization** (see the invariants above), in the unit's assigned working tree.
> 2. **Propose the change**: create its artifacts (or, where the unit's authorization names each direct edit, the unit's
>    edits) and put them on a branch named after the change, created per the "Fork every new branch" invariant. Commit
>    nothing.
> 3. Leave the work uncommitted, staged as before (unchanged). After this step, a unit whose authorization itself names
>    each direct edit the unit may make proceeds to step 7's pass, without steps 4–6.
> 4. Run the review gate's **proposal stage** and fix every finding until it is clean.
> 5. Ask the user for the **manual review of the proposal**, and end the turn there: implementation begins only on their
>    approval of it.
> 6. Implement the approved proposal, then run the review gate's **implementation stage** and fix every finding until it
>    is clean.
> 7. Ask the user for the **manual review of the finished unit**, and end the turn there. Do not commit, push, open a
>    PR, archive, or merge before the user approves the implementation — except a commit the user explicitly asks for.
> 8. Ship only on the owner's delivery request… (unchanged)

### `AGENTS.md` — "Workflow: The review gate" → Stages

> - the manual review pass — the owner, asked at each boundary its unit has: the proposal before implementation, the
>   implementation before delivery.

### `.opencode/skills/review-gate/SKILL.md` — §5

> **§5 runs at each stage boundary**: the proposal's pass precedes any implementation edit, and the implementation's
> pass precedes delivery, while a unit takes the single pass that `AGENTS.md`'s review-gate invariant names only where
> the owner's authorization itself names each direct edit the unit may make. The preconditions below apply to the stage
> the pass closes.

## Consequences

- The unit's own work follows the rule it introduces: this proposal is presented for the owner's review before any rule
  edit, and the implementation follows only on their approval.
- The project-authored commands that carry the lifecycle preflight are updated with the two new stops, so the
  requirement and the commands do not disagree.
- An agent that starts work without that instruction is a process defect, not a style choice; the defect is recorded
  under the unit that observed it.
