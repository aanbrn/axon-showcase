# Process defects — surface-projector-backlog

### surface-projector-backlog#1 — The integration test's first version depended on case order

- **Recorded** — 2026-11-05, by the main agent.
- **Stage** — implementation.
- **Expected** — each integration case asserts its own broker state, so any order and any repetition run the same way.
- **Observed** — the first version shared one topic and one consumer group across the cases, so the offsets one case
  committed decided another's expectation: `committedSegment_reportsZero` saw a backlog of 1 and
  `consumedBacklog_drainsToZero` saw 8 where 5 was produced, both from state an earlier case left behind.
- **Evidence** — the two failures of the first full run (`expected: 0 but was: 1`, `expected: 5L but was: 8L`), and the
  same class passing once every case had its own topic and group.
- **Consequence** — repaired before the change was reported: each case creates its own topic and group in its setup, at
  the cost of a broker call per case.
- **Knowledge boundary** — observed: the two failures and their repair; not checked: whether the order that produced
  them is stable, since the cases' independence no longer depends on it.
- **Decision** — no-change: below the promotion gate — one occurrence, caught by the case's first full run and repaired;
  no existing rule covers it.

### surface-projector-backlog#2 — A build-file edit was made without loading `gradle-build`

- **Recorded** — 2026-11-05, by the main agent.
- **Stage** — implementation.
- **Expected** — a change to a module's `build.gradle.kts` (adding `libs.axon.extensions.kafka.springBootStarter` to the
  `integrationTest` suite) is a Gradle build decision, so `gradle-build` loads before it.
- **Observed** — the dependency was added while the module's integration sources failed to compile, with only
  `jvm-development`, `jvm-testing`, and `deployment-operations` loaded; the review then stopped at its classification
  step because the hand-over named no pack owning the build file.
- **Evidence** — the review's reply ("Missing input — `gradle-build`") and the empty pack list in its record line; the
  change itself is the documented `gradle-build` rule that a custom suite re-declares what it compiles against.
- **Consequence** — one review invocation was spent on the classification stop; the pack was loaded afterwards and the
  added edge confirmed as the rule's prescribed shape rather than a new decision.
- **Knowledge boundary** — observed: the missing pack in the hand-over and the stopped review; not checked: whether the
  dependency choice itself was wrong, since the pack's rule agrees with it.
- **Decision** — proposed: applied as P1 in `AGENTS.md`'s subject-pack section — the caller checks a change's planned or
  actual edited paths for a subject its decisions did not name.

### surface-projector-backlog#3 — The review hand-over section was written after the invocation, not before

- **Recorded** — 2026-11-05, by the main agent.
- **Stage** — review.
- **Expected** — the round's hand-over section is appended before the reviewer is invoked, so the review reads its
  inputs from the record.
- **Observed** — five consecutive implementation rounds (6, 7, 8, 9, 10) were invoked with no section for that round, or
  with a section whose heading named the previous round, and each review returned `missing-task` (or, at round 9, a
  `wording` finding) for it.
- **Evidence** — `findings/round-6.md` through `findings/round-10.md` each carry that finding; round 10 named the
  recurrence and asked for it to be re-derived rather than re-patched.
- **Consequence** — one finding and one fix pass per round; the fix each time was to write the section, which the review
  then re-checked, so no product defect escaped, but five rounds were spent on it.
- **Knowledge boundary** — observed: the five findings and the sections' headings; not checked: why the write was
  skipped — the capture is the record's, the cause analysis is the generalization's.
- **Decision** — no-change: already covered by `review-gate` §2's preflight, which records the invocation's hand-over
  before the caller waits; the pre-invocation check is parked in `docs/ideas.md` 2026-11-07.

### surface-projector-backlog#4 — An advisor pass's findings were recorded outside the unit's layout

- **Recorded** — 2026-11-06, retroactively, by the main agent while reconciling the unit records in
  `reconcile-review-records`.
- **Stage** — implementation.
- **Expected** — the unit's directory holds `unit.md`, `rounds.tsv`, `findings/round-<N>.md`, `process-defects.md`, and
  `review-handover.md`; a pass that is not a review round records its findings in the hand-over, which already carries a
  section for it.
- **Observed** — the advisor's two implementation passes were written to `findings/advisor-1.md` and
  `findings/advisor-2.md`, while the hand-over's "Control evidence" section carried the same passes' outcomes.
- **Evidence** — the two files' paths and their `[over-claim]`, `[wording]`, `[hypothesis]` and `[open question]`
  entries, beside `review-handover.md`'s "Control evidence — the deployed positive backlog (no review round)" section.
- **Consequence** — the findings' instances lived outside the regulated set for the life of the merged unit; they now
  sit in that hand-over section, and the files are deleted.
- **Knowledge boundary** — observed: the two files and the section summarizing the same passes; not checked: what the
  invocation's instructions said about where the findings should go.
- **Decision** — no-change: already covered by `showcase/quality/commit-hygiene`'s unit-layout requirement and the
  `verifyUnitLayout` gate that refuses an unadmitted path.
