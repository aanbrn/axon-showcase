# clarify-generalization-parked-match — process defects

### clarify-generalization-parked-match#1 — A Code Mode tool name was guessed instead of looked up

- **Recorded** — 2026-10-28, main agent session, during the unit's premise investigation.
- **Stage** — implementation.
- **Expected** — the Code Mode catalog states that its listing is partial and that a tool is reached by the `path` a
  `search(...)` call returns, so an investigation read would call `search` first and then the returned path.
- **Observed** — the first investigation call passed `tools.read` and `tools.grep` to `execute` directly, without a
  `search` call; both failed as unknown tools, and the batch returned only error lines.
- **Evidence** — the `execute` result's log block:
  `ERR Error: Unknown tool 'read'. Did you mean tools.amplicode.read_class_file?` and
  `ERR Error: Unknown tool 'grep'. Did you mean tools.amplicode.list_spring_beans?`.
- **Consequence** — one wasted tool call and one re-run of the same three reads through the dedicated file tools. No
  artifact was affected, and the investigation's content was unchanged.
- **Knowledge boundary** — observed the two failures and the re-run. Did not check whether the harness's own guidance
  for `execute` names the dedicated `read`/`grep` tools as the preferred route, so the alternative explanation (the
  dedicated tools were the intended path all along) is unverified.

- **Decision** — no-change: one wasted tool call; the Code Mode catalog instruction already states that its listing is
  partial and that a tool is reached by the `path` a `search(...)` call returns.

### clarify-generalization-parked-match#2 — A decision's premise about `AGENTS.md` was asserted without reading it

- **Recorded** — 2026-10-28, main agent session, at review round 1.
- **Stage** — review.
- **Expected** — the design's decision 5 states what `AGENTS.md`'s generalization Procedure does, and the pre-write pass
  derives such a claim from the artifact rather than from recollection, adding a claims-table row for it.
- **Observed** — decision 5 said the Procedure "points at the command for the pass's steps; enumerating them at root
  would restate a command's procedure", which is false: the Procedure's steps 2-4 enumerate the hand-off, the apply
  step, and the `Decision` line. The claim had no claims-table row.
- **Evidence** — round 1's finding 1 (`claim-unreproducible`) quotes `AGENTS.md:505-516` as enumerating steps 2-4,
  against `design.md`'s decision 5. The Procedure text was also present in the session's loaded instructions, so the
  premise was checkable without a search.
- **Consequence** — decision 5, the design's Context paragraph, and a Risks bullet all carried the same false premise
  and were rewritten; two claims-table rows were added (the Procedure's shape and the README row). The change stays
  command-only, with the root summary's overlap recorded as a Non-Goal.
- **Knowledge boundary** — observed the three false statements and their rewrite. Did not check whether any other unit
  rests on the same premise about root's Procedure.

- **Decision** — no-change: "Interrogate the premise" and review-gate §1 already require reading the artifact a claim
  describes and giving the claim its own claims-table row.

### clarify-generalization-parked-match#3 — The installer rewrote `.opencode/package.json` mid-session, unnoticed until review

- **Recorded** — 2026-10-28, main agent session, at review round 2.
- **Stage** — review.
- **Expected** — `AGENTS.md`'s `.opencode/package.json` rule: diff that pin against `HEAD` before staging, and read a
  change there as the startup install rather than a deliberate dependency change.
- **Observed** — the working tree gained `@opencode-ai/plugin` `1.19.4` → `1.19.3` after the unit began; the first
  `git status` of the session did not list the file. Nothing in the unit's own flow inspected it before the review did.
- **Evidence** — round 2's finding 1 (`impact-omission`) quotes `git status` listing ` M .opencode/package.json` and the
  diff's one-line pin change; `git diff -- .opencode/package.json` showed only that line, with no `overrides` edit.
- **Consequence** — reverted with `git checkout -- .opencode/package.json` before staging; the unit's tracked diff is
  the command and the review ledger row. One review finding was spent on it.
- **Knowledge boundary** — observed the change and its revert. Did not establish which process rewrote the pin, and did
  not check whether the same rewrite recurs on the next session start in this tree.

- **Decision** — proposed: applied as the earlier decision moment in the `.opencode/package.json` installer-state bullet
  ([`AGENTS.md`](../../AGENTS.md)), since the rule's own "before staging" deadline was later than the review read that
  kept finding the rewrite; https://github.com/aanbrn/axon-showcase/pull/733.

### clarify-generalization-parked-match#4 — Rounds 2 and 3 were handed over inside the invocation, not recorded before it

- **Recorded** — 2026-10-28, main agent session, after review round 3.
- **Stage** — review.
- **Expected** — the review gate's preflight records each invocation's hand-over **before** waiting on it, so a stall
  costs the loop a wait rather than the round's work.
- **Observed** — round 1's hand-over was written to `tasks.md` §4 before its invocation. Rounds 2 and 3 carried theirs
  only as the invocation prompt, so a stall in either round would have left no record outside the transcript.
- **Evidence** — `tasks.md` §4 held one hand-over table before this record was written; round 3's report names the same
  gap as a limitation ("`tasks.md` §4 records only round 1's hand-over; rounds 2 and 3 were supplied per invocation").
  Round 4's report notes the same gap for its own hand-over, so the miss repeated within this unit.
- **Consequence** — §4 gained a "Rounds 2 and 3" entry summarizing what each carried, written after the fact. No round
  stalled, so nothing was lost.
- **Knowledge boundary** — observed the missing pre-wait records. Did not check whether an earlier unit in this
  repository persisted only its first round's hand-over, so whether this is a recurrence is unestablished.

- **Decision** — no-change: review-gate §2's preflight already requires the invocation's hand-over to be recorded before
  the wait.

### clarify-generalization-parked-match#5 — `docs/ideas.md` was read from another unit's working tree, not the base revision

- **Recorded** — 2026-10-28, main agent session, at review round 6.
- **Stage** — implementation (premise investigation), surfaced at review.
- **Expected** — a claims-table row's cited line range comes from the revision it names (`git show <base>:<path>`), and
  the primary checkout, assigned to another unit, is not read as the base state.
- **Observed** — the first read of `docs/ideas.md` came from the primary checkout, whose working tree carried
  `retry-infra-verification-fetches`'s uncommitted edit to that file; the parked entry sat at line 60 there and at line
  58 at the base. The row's range was written from that read, so at the base it cited it began two lines into the entry.
- **Evidence** — round 6's finding 2 quotes `git diff --name-only c93607bd a4c3cd9e` listing `docs/ideas.md`;
  `git show c93607bd:docs/ideas.md | grep -n "Read a parked idea's own"` → 58, and the same at `a4c3cd9e` → 60.
- **Consequence** — the row reproduces at the new base, where its range covers the entry whole, so the row needed no
  correction; the `tasks.md` note that claimed no cited artifact changed in the merge was corrected to state the shift.
- **Knowledge boundary** — observed this file and its two revisions. Did not check whether any other artifact read
  during the premise investigation came from the primary's working tree rather than from the base.

- **Decision** — no-change: the premise and base-anchoring rules already require a pre-change claim to be read at the
  unit's base revision, never from another unit's working tree; this is the second occurrence the decided record
  `separate-lesson-capture-from-implementation#3` withheld, and the covering rule still determines the action.

### clarify-generalization-parked-match#6 — A rule was cited from memory to justify a decision, against its own boundary text

- **Recorded** — 2026-10-28, main agent session, at the second consultation.
- **Stage** — implementation, surfaced by the owner's second consultation and confirmed at review.
- **Expected** — a decision that declines work by invoking a rule quotes that rule's own boundary, read at the revision
  it cites, since the boundary is what decides whether the declined work is the rule's target or its exception.
- **Observed** — decision 5 declined the `AGENTS.md` pointer by citing the single-sourcing invariant ("copying the
  command's steps into root would add the third copy the invariant forbids") without reading the invariant's own
  imperative-versus-procedure split, which states that an imperative a reader must act on before a step is stated at
  every site where that step appears. Under that boundary the pointer is required, and the decision's premise was wrong.
- **Evidence** — the second consultation's confirmed problem 2 quotes `AGENTS.md:293–298` for the boundary and
  `AGENTS.md:508–511` for the hand-off step; the review then verified both against the base revision.
- **Consequence** — decision 5 was re-derived to add the one-line pointer at root, with the Procedure's summary overlap
  kept as a separate Non-Goal; `AGENTS.md` gained the pointer and two renumbered lines, and the proposal's Impact,
  Capabilities rationale, and claims table were updated.
- **Knowledge boundary** — observed this decision. Did not check whether other rules invoked elsewhere in this unit's
  artifacts were quoted from memory rather than read.

- **Decision** — no-change: the premise rule already requires reading the artifact a decision cites, and the
  single-sourcing invariant states the imperative-versus-procedure boundary the decision misquoted.

### clarify-generalization-parked-match#7 — Round 8's ledger row was never appended

- **Recorded** — 2026-10-28, main agent session, at review round 9.
- **Stage** — review.
- **Expected** — the review gate appends one row per round to `docs/review-rounds.tsv` when the round's report arrives,
  before the next round runs, so the sequence stays continuous.
- **Observed** — round 8's report arrived, the base advanced immediately afterwards, and the ledger row was never
  written; the work went straight into round 9, whose own hand-over check named `--expect …:8` against a file holding
  seven rows.
- **Evidence** — round 9's finding 1 (`missing-task`) states that the ledger carries rounds 1-7 while round 8 ran and
  reported three classes; the file's tail showed seven rows for this unit before the correction.
- **Consequence** — round 8's row was appended before round 9's, in order, and the ledger checks re-run. Task 3.5's Done
  note was false for the interval between the two rounds.
- **Knowledge boundary** — observed this omission. Did not check whether an earlier unit in this repository missed a row
  the same way, since the ledger's continuity check reports only the current boundary.

- **Decision** — no-change: already parked — the `docs/ideas.md` 2026-10-22 "reviewer wrapper" entry, whose
  `Revisit when:` condition is not met.

### clarify-generalization-parked-match#8 — An invented session identifier was written into the change record

- **Recorded** — 2026-10-28, main agent session, at the deep-review stage.
- **Stage** — review.
- **Expected** — a review record carries the session identifier the invocation returned, never an invented one; the
  `review-quick` contract states it, and the deep-review command has the pass's session recorded on the review task.
- **Observed** — the §5 pass-1 block was written before the invocation, with a session identifier no invocation had
  returned. The next edit replaced it with a statement that the identifier is recorded once the invocation returns, and
  the pass's line now carries the identifier the invocation actually returned.
- **Evidence** — the first version of the "Invoked" line named a session id; the corrected version reads "first pass
  (its session identifier is recorded here once the invocation returns)"; the final line carries
  `ses_e7f84836bffeOXwhIemuaUXYeb`, which is the value the invocation returned.
- **Consequence** — the invented identifier lived in one edit cycle and reached no committed state; nothing else was
  affected.
- **Knowledge boundary** — observed this instance. Did not check whether an earlier unit's records carry an identifier
  no invocation returned.

- **Decision** — no-change: the deep-review procedure requires each pass's session to be recorded from the invocation,
  and the authored-claims evidence rule already requires a recorded identifier to be the one the invocation returned.

### clarify-generalization-parked-match#9 — An inline code span was authored across a line break, and the reflow left a whitespace run inside it

- **Recorded** — 2026-10-28, main agent session, at quick-review round 12.
- **Stage** — review.
- **Expected** — an inline code span stays on one source line, since the markdown formatter joins the lines and leaves
  the continuation line's indentation inside the span; a defect inside a span passes `spotlessCheck` and only
  proofreading catches it.
- **Observed** — task 3.4's Done note was written with the signal `[repeated-class] 5 rows repeat a prior class` split
  across two source lines; after `spotlessApply` the span read `[repeated-class] 5 rows repeat a prior     class`, with
  a five-space run inside the code text. `spotlessCheck` stayed green, and quick-review round 11 did not report it.
- **Evidence** — the reflowed line in `tasks.md` before the fix, and the corrected line, which keeps the span on one
  source line: "signal `[repeated-class] 5 rows repeat a prior class`".
- **Consequence** — fixed in the same round; the command it quotes (`review-routing.py`) emits the single-space form, so
  the recorded text disagreed with its source.
- **Knowledge boundary** — observed this instance. Did not sweep the unit's other artifacts for the same shape, and did
  not check whether the gate could detect a whitespace run inside a span.

- **Decision** — no-change: the inline-code-span entries this pointed at are closed by
  `docs/decisions/2026-10-29-inline-span-whitespace.md`, which rejects the proposed whitespace gate on a probe over the
  gated markdown; the formatting convention already forbids authoring a span across a line break.
