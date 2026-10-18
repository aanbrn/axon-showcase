# Design

## Context

The `review-quick` subagent (`.opencode/agent/review-quick.md`) does the per-unit review pass. Its behavior is owned by
`showcase/quality/agent-skills`'s "Per-change quality-gate and analysis subagents are available" requirement — the
"Quick review runs after proposal and implementation" scenario. Several prior changes widened it
(`give-the-review-a-canonical-class-list` added the classes, `reuse-review-loop-session` the session rule,
`establish-structured-writing-standard` the standard's clause).

Its checks are compressed into "any obvious correctness problems visible at a glance", and its report contract names the
issue, its `file:line`, and one line of evidence — not the class or the correction. The task spec adds the proposal,
implementation, and conventions checklists, the per-class evidence requirements, the prioritization order, the finding
shape, and the moved-defect recurrence clause.

## Goals / Non-Goals

**Goals** — make each check and each finding's evidence explicit within the existing quick pass, so the review is
actionable in one round.

**Non-Goals:**

- No depth increase — the pass stays _quick_; `review-thorough` owns depth.
- No new finding class.
- No change to the record line or the class list.
- No widening of the 120-character check (a separate parked idea).

## Decisions

**The pass stays quick — the checklists are subjects, not a protocol.** Each checklist names what to look at in one line
rather than a step-by-step procedure: the depth belongs to `review-thorough`, and a quick review's value is cheap
convergence.

**The per-class evidence requirements are stated, not implied.** Each class's evidence is named (an enumeration → its
corpus; a contradiction → both statements; a missing scenario → the behavior and the omitting delta; a `test-control` →
the expected behavior and the failure it should detect; an `impact-omission` → the affected site), so a suspicion the
evidence cannot establish is not reported as a confirmed defect.

**The finding shape gains its class and its correction.** The report contract's budget becomes the class, the
`file:line`, the issue, one line of evidence, and the required correction — the class is already required by the
canonical list, and the correction is what makes a finding actionable.

**No `## Purpose` refresh is owed.** The Purpose names the per-unit quality-gate subagents; the change sharpens the
review-quick's method within that scope, so the Purpose's own text is not falsified.

## Risks / Trade-offs

- **A longer definition could slow the pass** → the checklists are one line each; the review's cost is the search, not
  the reading, and the added lines name subjects it already checks.
- **The checklists could drift toward thorough** → the Non-Goal and the first Decision bound it; a depth request routes
  to `review-thorough`.
- **A definition edit is served from the pre-change copy until a reload or a new session** → the smoke-run of the edited
  definition is a precondition for the archive.

## Migration Plan

None: the change takes effect on the next review. No `## Purpose` refresh is owed, so the implementing PR's diff carries
no main-spec edit beyond the delta and the archive commit carries only the delta→main sync.

## Claims table

| Claim                                                                                                            | Source                                                                                                                       |
| ---------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| The definition compresses the checks into "any obvious correctness problems visible at a glance"                 | `git show HEAD:.opencode/agent/review-quick.md`                                                                              |
| The report contract names the issue, its `file:line`, and one line of evidence — not the class or the correction | `git show HEAD:.opencode/agent/review-quick.md` (the report contract)                                                        |
| The behavior is owned by the per-change-subagents requirement's review-quick scenario                            | the main spec's requirement ("Quick review runs after proposal and implementation")                                          |
| Several prior changes widened the review-quick                                                                   | the archive (`give-the-review-a-canonical-class-list`, `reuse-review-loop-session`, `establish-structured-writing-standard`) |
| A definition's behavior is only exercised by a run, which needs a reload or a new session                        | `AGENTS.md`'s subagent-reload gotcha (`git show HEAD:AGENTS.md`)                                                             |
| No `## Purpose` refresh is owed                                                                                  | the `## Purpose` names the per-unit quality-gate subagents, which the change does not widen                                  |

**Dismissals** (scan candidates resolved to neither a row nor a fact, grouped by kind, one reason per kind): **absolute
quantifiers and counts** are modal or structural wordings, not facts about the repository; **the `spec.md` delta's
copied requirement bodies and retained scenarios** are the main spec's text, so their quantifiers and cross-references
are pre-existing, not claims this unit states; **paths, commands, and cross-references** are name, path, or command
references rather than claims about the repository; **the `docs/review-rounds.tsv` candidates** are this unit's own
appended record rows, a round number and a class label in a one-line record, not a claim.
