---
description: Verifies a process defect the owner reports and appends a record only for a confirmed outcome.
---

# Flag a process defect

The entry point that turns an owner's report — "the agent missed something here" — into a verified outcome. It is a
check, not a recorder: a record is written only after the episode's evidence establishes the defect.

**Trigger** — the owner invokes `/flag-process-defect [anchor]`. The anchor names the episode: a step, a decision, a
file, a command, or a point in the conversation. Without an anchor, the episode is the nearest action in the current
context that an expectation could have governed.

**Procedure**

1. Resolve the episode.
   - With an anchor, the episode is the action or decision it points at.
   - Without one, it is the nearest action in the current context that a rule or a plan could have governed.
   - IF the anchor and the context do not resolve one episode, THEN ask one clarifying question and stop. Do not pick
     the most plausible candidate.
2. Establish the rule in force at the episode.
   - The rule is the instruction, convention, plan, or tasking that governed the action when it was taken.
   - IF the episode is past and the text has changed since, THEN read the rule from that episode's revision rather than
     from today's text.
   - IF no rule can be established, THEN the outcome is "not establishable" — continue at step 4.
3. Compare what the rule required against what the sequence shows, within the episode.
   - Read the episode's immediate sources: its messages, the tool output and results, the diff, and any artifact it
     produced.
   - State the required action and the observed one, each with the fragment that supports it.
   - Do not widen the episode to support a conclusion, and do not search the repository for another violation.
4. Report exactly one outcome.

   - **Confirmed** — the required action, the observed one, and the fragment that establishes the difference.
   - **Refuted by the available evidence** — the required action, and the fragment showing it was taken.
   - **Not establishable from it** — what was checked, and which fragment is missing.

   A resolved episode yields one of the three. An episode that never resolved yields the clarifying question from step 1
   instead, and no outcome.

5. On a confirmed outcome only, append the record.
   - Append it to `docs/process-defects/<unit>.md` for the unit the episode belongs to — the live one, the merged one,
     or a slug naming the work for a unit with no change dir. Never to whichever unit this session happens to work in.
   - Fill the fields `AGENTS.md`'s recording workflow states, with these specifics: `Observed` is the action you
     established, not the owner's description of it; `Evidence` names the owner's report as the signal beside the
     verified fragment, and that fragment may be an excerpt, a command's output, or the episode's own event sequence —
     it need not be re-runnable; `Knowledge boundary` separates what you verified from what the owner reported and what
     you could not reach.
   - Extend an existing record that already covers this episode rather than adding a second one.
   - Write no `Decision` line. The record enters the generalization queue like any other, and
     `/generalize-process-defects` owns what happens there.

**Decision points**

- IF the owner's report is the only thing that attests the episode, THEN the outcome is "not establishable": a report is
  a signal, not evidence. Name what would establish it — a command's output, a report excerpt, the sequence of actions.
- IF the evidence establishes a defect different from the reported one, THEN record the established defect, and say in
  the report that it differs from the report.
- IF the evidence refutes the report, THEN say so with its grounding and write nothing: no record is the correct result.
- IF more than one rule could have governed the episode, THEN use the one in force at the time and say which it is.
- Diagnose nothing: no cause, no proposed improvement, no cost estimate. The record carries facts.

**Failure handling**

- An episode whose sources cannot be read — a lost session, a pruned log — is reported as "not establishable" with the
  unreadable source named, never as confirmed or refuted.
- IF the record cannot be written, THEN report that beside the outcome; do not substitute a summary in the conversation
  for the record.

**Verification**

- Exactly one outcome is reported, and it names its evidence.
- A confirmed outcome has its record appended, and a refuted or unestablished one wrote nothing.
- `git status` shows only the record file, beyond the unit's own work.

**Completion criteria** — the owner has one outcome with its evidence, and a confirmed defect has its record in the file
of the unit the episode belongs to.

**Repository lifecycle**

- This command edits the record file only; it starts no code work.
- WHEN the episode belongs to a live unit, THEN append in that unit's assigned working tree: the primary checkout when
  no other unit holds it, otherwise the sibling `../axon-showcase-<unit>` worktree. Establish the tree before the first
  write, and treat an ownership you cannot establish as occupied.
- WHEN the episode belongs to a unit with no live tree — merged, or archived — THEN the append is a documentation unit
  of its own: its own branch from a freshly fetched `origin/main`, unrelated dirty changes preserved, the work
  uncommitted until the owner's approval, and no commit, push, pull request, archive, or merge without it.
- Never commit to `main`. Write only into the tree assigned to the episode's unit, or — when that unit has none — into
  the documentation unit's own tree carrying the append.
- `AGENTS.md`'s "Workflow: Change lifecycle" owns the branch, workspace, and approval steps — its **Workspace** rule
  owns the assignment itself — and "Workflow: The review gate" owns the review a documentation unit owes.
