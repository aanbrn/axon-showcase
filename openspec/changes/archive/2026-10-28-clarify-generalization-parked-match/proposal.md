# Proposal

## Why

`AGENTS.md`'s generalization **Decision points** require the generalizing unit to dispose each record against the
recurrence sites the repository already records — a `captured:` rule, a `docs/ideas.md` entry whose own `Revisit when:`
condition the incident meets, or an already-parked problem — and that bullet states why it exists: no other reader tests
a parked entry's condition. `/generalize-process-defects`, the command that runs the pass, never names the step. A pass
following the command alone selects the records, hands them to `lesson-capture`, presents the proposals, and decides
every record without testing a parked entry's condition. The subagent cannot supply the missing half: `docs/ideas.md` is
in neither its inputs nor its destinations. The obligation is real and was once executed by the calling session rather
than by the command (`extend-owner-reply-language-scope`, record #2's `Decision`).

## What Changes

- **Add** a step to `.opencode/commands/generalize-process-defects.md` that disposes each selected record against the
  recurrence sites `AGENTS.md`'s generalization **Decision points** name, before the `lesson-capture` hand-off.
- **Point at** that bullet rather than restating its conditions, so the routing stays single-sourced where the spec
  already places it.
- **Split** the command's presentation step: it presents each record's recurrence disposition, and a `Decision points`
  rule withholds a proposal the disposition records as an already-parked problem.
- **Require** every selected record's disposition in the command's `Verification`, a record that produced no proposal
  included, so a pass that skips the match fails a stated check.
- **Point at** the match from `AGENTS.md`'s Procedure as a one-line pointer, since that Procedure enumerates the
  hand-off step a reader may work from.
- **Record** in the design why the match stays caller-side, and why no capability spec covers the command's step set.

## Capabilities

### New Capabilities

none

### Modified Capabilities

none — **`skip_specs: true`**: the routing this command executes is `AGENTS.md`'s by the spec's own delegation, since
the `agent-skills` generalization requirement states that "the owning destinations and their routing are `AGENTS.md`'s
generalization workflow's", and its one clause naming the command describes selection and hand-off, both of which the
command still performs. The change adds no behavioral contract; it makes an existing obligation explicit at the sites
that execute it.

## Impact

- `.opencode/commands/generalize-process-defects.md`: the match step, the split presentation and apply steps, a
  `Decision points` rule, and the `Verification` clause.
- `AGENTS.md`: one pointer line in the generalization Procedure, with the steps after it renumbered.
- New: `openspec/changes/clarify-generalization-parked-match/` and
  `docs/process-defects/clarify-generalization-parked-match.md`.
- No spec delta, and no `README.md`, ADR, or `lesson-capture` edit: the design records why each was verified as not
  owed.
- No code, build, test, or deployment impact.
