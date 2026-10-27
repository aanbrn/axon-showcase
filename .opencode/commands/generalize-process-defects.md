---
description:
  Generalize the process-defect records the repository has accumulated — select the unanalyzed ones, hand them to the
  `lesson-capture` subagent, and record a decision per record — in a unit of its own, on the owner's decision.
---

Run the generalization pass `AGENTS.md`'s "Workflow: Generalize process defects" triggers.

**Trigger** — the owner invokes `/generalize-process-defects`, optionally naming the units whose records to analyze. The
moment is the owner's decision; nothing schedules this command, and no queue or drain guarantee is implied.

**Procedure**

1. Read every file under `docs/process-defects/` and list the records that carry no `Decision` line — a file may mix
   decided and undecided records, so list per record, not per file. Show the owner the set, with each record's unit,
   stage, and one-line title.
2. Take the selection: the units the owner named, or the whole set when none were named.
3. Invoke the `lesson-capture` subagent with the selected records, the existing guidance a candidate would extend, and
   the review evidence each record cites. Its contract — the promotion gate, the evidence threshold, the dispositions,
   the owning destinations, the retirement candidates, and the authoring-system recommendations — is the `agent-skills`
   requirement's; do not restate it here or in the hand-over.
4. Present the proposals, and apply only those the owner approves, in this unit.
5. Append one `Decision` line to each analyzed record: `no-change` with its reason, `proposed` with a link, or `shipped`
   with a merged pull request's link.
6. Keep this unit's own process defects in its own record, like any other unit's.

**Decision points**

- IF the selection is empty, THEN report that and stop — an empty selection is not a pass over the corpus.
- IF a record's evidence is missing or unreadable, THEN hand that candidate back with the limitation stated and withhold
  it; do not infer the missing evidence.
- IF a record already carries a `Decision` line, THEN read it as history rather than re-analyzing it.
- A link to an open pull request is recorded as `proposed`, never as `shipped`.
- The improvement is this unit's work: its own branch and working tree from a freshly fetched `origin/main`, consuming
  records that are already merged.
- The owner approves this unit like any other; being named the generalizer grants no self-review or merge authority.

**Failure handling** — a record set that cannot be read is reported with the paths named, and the pass does not start. A
`lesson-capture` run that returns nothing durable still ends with a `Decision` line per record.

**Verification** — every selected record carries a `Decision` line, each applied proposal states the rule it extends,
and the unit has been through the review gate.

**Completion criteria** — the owner has the proposals and their outcomes, every selected record is decided, and the
approved improvements are applied or routed.

**Repository lifecycle**

- A new unit starts on its own branch, created from a freshly fetched `origin/main`; a unit continuing an existing
  branch or pull request works on that branch.
- The unit's working tree — the primary checkout when no other unit holds it, otherwise the sibling worktree
  `../axon-showcase-<unit>` — is established and recorded before the first write, per `AGENTS.md`'s "Workflow: Change
  lifecycle" **Workspace** rule.
- Unrelated dirty-worktree changes are preserved.
- The work stays uncommitted until the manual approval pass.
- Nothing is committed, pushed, turned into a pull request, archived, or merged without that explicit approval.
- The full per-unit review gate runs over the result; `AGENTS.md` and the `review-gate` skill own its steps.
