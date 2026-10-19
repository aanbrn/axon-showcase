---
description:
  Run a thorough review of a change against its specs, tasks, and surrounding code — the deep-review procedure the
  review gate's routing decision selects, or a manual explicit run
---

Run a deep review pass of a change. This command owns the **procedure**; the review gate owns **whether** a deep review
runs (its routing decision — `REQUIRED`, `RECOMMENDED`, or `NOT_NEEDED`). A manual invocation is an **explicit run**: it
does not route, and it is never a substitute for a `REQUIRED` outcome.

**Trigger** — the owner invokes `/review-thorough`, or the review gate's routing decision selects a deep review.

**Prerequisites** — a change to review, resolved below.

**Procedure**

1. Determine the change to review: use the argument after `/review-thorough` as the change name if provided (e.g.
   `/review-thorough add-actionlint-gate`), otherwise use the current active change, or the most recent uncommitted work
   if none is active.
2. Create or reuse the change's dedicated `review-thorough` session: create one on the first pass, and on a follow-up
   pass continue the same session by passing its recorded session id — a different change starts a fresh one.
3. Gather the change identity, its planning artifacts (proposal, design, tasks, delta specs), the current diff, the
   prior findings with their statuses, the current pass, the known limitations, and whether this pass is the initial
   review or a fix verification — and hand over only that.
4. Invoke the `review-thorough` subagent (`.opencode/agent/review-thorough.md`) with that context.
5. Present its findings in the report contract it returns — the verdict line first, then findings budgeted per item and
   grouped by severity. Do not re-summarize it.
6. Record the pass on the change's review task in its `tasks.md`: the routing outcome and its signals, the pass number,
   and each finding's location and status — `OPEN`, `FIXED_AND_VERIFIED`, `REJECTED` with the evidence, or `ACCEPTED` by
   an explicit owner decision recorded with the reason and what it accepts. That record is the deep-review status's one
   authoritative source; do not also append it to `docs/review-rounds.tsv`, whose rows are the quick loop's.
7. Ask the owner which findings to fix. Do not edit any files without the owner's go-ahead.

**Decision points**

- IF the argument names no change and no active change can be determined, or the "most recent uncommitted work" is
  ambiguous (more than one candidate, or none), THEN stop and ask the owner which change to review — never guess.
- Honor the active OpenSpec store and root context: resolve the change through the OpenSpec CLI's own store rather than
  assuming a repository-local `openspec/changes/` path.
- IF creating, continuing, handing over, parsing the output, or validating the session fails, THEN report the failure
  rather than reading it as a successful review.
- Where the session must be replaced, transfer the current findings, their statuses, the diff, and the objective, record
  the lost continuity, and review the current state independently.
- Where no safe recovery exists, stop and report the blocker.

**Failure handling** — a failed session, hand-over, or output parse is a reported failure, not a passed pass.

**Verification** — the pass is recorded on the change's review task with each finding's status.

**Completion criteria** — the owner has the findings and has chosen which to fix; the pass and its statuses are
recorded.

**Pass budget** — hold the default of two full passes: the initial review, then an independent verification after the
valid findings are fixed. A third full pass needs a recorded decision resting on the residual material risk and the
expected value. At the budget limit, an unresolved blocking finding prevents normal completion, and an unresolved
should-fix finding needs an explicit resolution or a recorded acceptance. Incomplete verification and uncertainty stay
visible, and the limit never accepts residual material risk by itself.
