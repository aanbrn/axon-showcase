# Design

## Context

The unit that merged before this one (`extract-deployment-operations-guidance`, #656) hit a process violation: after a
`/consult` returned a report with three corrections and a closing prompt, the invoking agent applied all three before
the owner decided, then presented the report with a disclosure. The advisor's own analysis ranked the cause as a routing
mistake at the moment the report arrived — the report was finding-shaped, the review gate's standing instruction is "fix
everything it finds", and nothing marked the consult boundary — with the imperative form of the requested prompt as the
amplifier. The rules already forbade the action: before this change, `/consult` step 7 said "do not apply its suggested
action without the owner's decision", and the advisor definition says "a recommendation is never authorization". The gap
is not a missing obligation; it is that the procedure does not require the turn to end at the hand-over, so the next
mode of work can begin inside the same turn.

The owner asked for the advisor's recommended single change: replace step 7 with a terminal handover. The unit's capture
then merged the rule's general form into the Structured Writing Standard, the surface that governs a procedure's shape.

## Goals / Non-Goals

**Goals:**

- Make the hand-over a step that ends the turn, so the next action cannot be chosen before the owner's decision.
- Keep the report and any prompt it carries available to the owner as a proposition, not as an instruction.

**Non-Goals:**

- A runtime control over a consultation's lifecycle (the advisor judged it infeasible at reasonable cost: no commit- or
  Gradle-level check can observe "a consult is open").
- Changing the report contract's sections, the handoff fields, or the advisor's read-only boundary.
- A new `AGENTS.md` **bullet**: the behavior stays owned by the `agent-skills` capability and the command. The capture
  nevertheless merged its general form into the Structured Writing Standard's existing "Numbered steps" bullet (+4/−1
  lines), because that rule governs how a procedure is authored; the merge is a capture, not a second statement of this
  command's order. With the two hand-back steps below, `git diff --numstat AGENTS.md` is 7/4.

## Decisions

| Decision                                                                                       | Alternatives considered                                                   | Why                                                                                                                                                                                                                                                                            |
| ---------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Put the terminal step in `/consult`'s step 7 and its decision points                           | `AGENTS.md`; the advisor definition alone; a new check                    | The command owns the calling procedure and is loaded when it runs; `AGENTS.md` is always loaded and its growth is bounded; the advisor definition owns the report's shape rather than the caller's next action; and no check can observe a consultation's authorization state. |
| Also state the proposal status in the advisor's report contract                                | Leave the output shape to the caller                                      | The requested prompt is the amplifier: the caller asks for it, and the contract otherwise says only "next action, with its first step", so one sentence keeps a requested prompt from reading as a runnable instruction.                                                       |
| Carry the hand-over into the command's direct-delegation paragraph                             | Rely on the delta's "invoking agent" wording                              | A direct delegation is the same caller path with the report arriving one hop earlier; leaving that paragraph on the old rule is where the instruction would be read as satisfied.                                                                                              |
| Extend the existing scenarios rather than add a requirement                                    | A new requirement for the hand-over and for the prompt's form             | The two scenarios already own the presentation rule and the next-action shape; a new requirement would duplicate each across two entries.                                                                                                                                      |
| State the stopping step at the two `AGENTS.md` hand-backs rather than narrow the captured rule | Narrow the rule to a subagent's report; leave the two steps as conditions | The change lifecycle's step 4 and the review gate's step 6 are the same hand-back shape, so the rule's first test is the file that holds it; two words each make them conform, where narrowing would fit the rule to the evidence and leave the class unstated.                |

## Risks / Trade-offs

| Risk                                                                              | Mitigation                                                                                                                       |
| --------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------- |
| A terminal step reads as forbidding the agent from answering a follow-up question | The step ends the turn after the report; the owner's next message starts whatever comes next, including work on the same report. |
| The agent treats a later, unrelated "please do" as the decision                   | Step 7 says the decision must be about that report; the delta's scenario says "a subsequent owner decision about that report".   |
| The advisor's sentence narrows its own independence                               | It governs the prompt's form, not the recommendation or its evidence.                                                            |

## Claims table

The pre-write derivation pass's record (`scripts/claim-scan.py --base origin/main` seeded it; each candidate resolves to
a row or a dismissal below).

| Claim (as stated here)                                                                        | Source / derivation                                                                                                                                                                                             |
| --------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The violation's sequence is the agent's account of one session                                | The repository does not record it: the predecessor's archived `tasks.md` records the corrective pass and its reviews, not that the pass ran before the owner's decision. Stated as an account, not repo history |
| The predecessor unit is #656, merged as `7eeb1afc`                                            | `gh pr view 656 --json state,mergeCommit`                                                                                                                                                                       |
| The old step 7 read "do not apply its suggested action without the owner's decision"          | `git show 7eeb1afc:.opencode/commands/consult.md`, step 7                                                                                                                                                       |
| The advisor definition says "a recommendation is never authorization"                         | `git show 7eeb1afc:.opencode/agent/technical-advisor.md`, "What you are not"                                                                                                                                    |
| The review gate's standing instruction is "Fix everything it finds; re-run"                   | `AGENTS.md`, "Workflow: The review gate", step 4                                                                                                                                                                |
| The report contract requires "exactly one action, with its first step" and asks for no prompt | `git show 7eeb1afc:.opencode/agent/technical-advisor.md`, the report contract's "Next action" bullet                                                                                                            |
| No existing check can observe a consultation's authorization state                            | The check inventory: `scripts/commit-hygiene.py`'s modes and the Gradle `verify*` tasks read file content and the staged set, not a runtime state                                                               |
| The two requirements and the scenario names the delta modifies                                | `git show 7eeb1afc:openspec/specs/showcase/quality/agent-skills/spec.md`                                                                                                                                        |
| The advisor's ranking of the causes                                                           | The advisor's own report (external to the repository), quoted in "Context" as its account                                                                                                                       |

**Dismissals.** The scan's candidates resolve by class: `count` — the "7" tokens are the step number "step 7", not a
count; `absolute` — scope words inside the rule ("never", "only"); `cross-ref` — `#656` is the PR reference above;
`path` — repository paths the row's own source resolves.

## Verification

`openspec validate --all`; a read of the two edited definitions against the delta; the quick review; and a four-run
definition smoke (the new and pre-change commands, each on a neutral fixture and on one carrying the review-gate loop
context). All four runs presented the report, edited nothing, and stopped for the owner's decision. The pre-change
control stops as well, so the fixture does not discriminate: the smoke evidences the intended behavior, not that the
wording reduces the failure — that stays an unverified hypothesis. Neither the neutral nor the review-gate-loop fixture
reached the bad state; reproducing it would need a fixture carrying a prior owner go-ahead for the work and a
finding-shaped report arriving inside that same turn, which these runs did not build.
