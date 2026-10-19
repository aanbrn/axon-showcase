# Tasks

## 1. The delta spec

- [x] 1.1 `openspec/changes/make-reviews-predictable-and-recoverable/specs/showcase/quality/agent-skills/spec.md` — the
      `MODIFIED` block, copied whole from the current spec's requirement (description and every scenario) and then
      edited: the requirement's own statement of a bounded, recoverable invocation; the screen's inputs as a
      caller-supplied list its report names; the caller's risk focuses widening attention within that list; the
      escalation to `review-thorough`; the stop rule for both reviewers; the preflight clause's precondition, widened so
      a first-round stall has a path to replacement; and a new scenario for the caller-side watchdog. Verify:
      `openspec validate --changes` accepts it, and the block's `### Requirement:` header matches the main spec's
      verbatim. **Done** — `--changes` passes and the header matches byte-for-byte.
- [x] 1.2 Verify the `MODIFIED` block preserves what it must not weaken. Verify: every `#### Scenario:` header in the
      main spec's block appears in the delta's, in the same relative order, and a clause-level diff shows the only
      absent main-spec lines are the two rewritten clauses' continuations (the bounded-screen clause and the preflight
      clause, four lines in total) — the mandatory quick gate, the finding-evidence rules, the pre-write pass, the
      manual review, the routing decision, the two-pass budget, and the ledger's continuity rules all survive. **Done**
      — 19 pre-existing scenarios present in the same relative order plus the new one, and exactly four main-spec lines
      absent: the bounded-screen clause's continuation and the preflight clause's three lines.

## 2. The reviewer definitions and the loop's rules

- [x] 2.1 `.opencode/agent/review-quick.md` — state the three layers (the bounded screen over the caller's supplied
      list, the caller's risk focuses, the escalation to `review-thorough`), the stop rule (no scope expansion; a
      limitation rather than unbounded search; the best completed report), and the handover fields the invocation
      supplies. Verify: the definition names all three layers and the stop rule, and it does not restate a rule
      `AGENTS.md` or the spec owns. **Done** — the three layers, the stop rule, and the hand-over the invocation
      supplies are stated, and no rule `AGENTS.md` or the spec owns is restated.
- [x] 2.2 `.opencode/agent/review-thorough.md` — state the stop rule: the change's artifacts and its own contract's
      checks rather than the repository at large, and a limitation rather than unbounded investigation. Verify: the
      definition carries the rule and its existing obligations are intact. **Done** — the stop rule is stated and the
      definition's existing obligations are intact.
- [x] 2.3 `AGENTS.md` — the review gate's invocation step carries the watchdog protocol (the deadline, the handover
      recorded before waiting, the `stopped-responding` record, the replacement, the independent re-verification), and
      the session-continuity decision bullet points at the spec's rule rather than restating it. Verify: the step
      carries the two imperatives and the pointer, and no rule is stated in both places. **Done** — the preflight
      carries the deadline and the hand-over-recorded-before-waiting imperatives and points at the spec for the rule and
      the hand-over's fields; the continuity bullet already pointed at the spec, so its clause was verified as already
      satisfied rather than edited.

## 3. Verification

- [x] 3.1 `openspec validate --all` and `openspec validate --changes` pass. Verify: run both. **Done** — `--all` 27/27
      and `--changes` 1/1.
- [x] 3.2 The build's gates pass. Verify: `./gradlew spotlessApply` then `./gradlew spotlessCheck`, the 120-character
      recipe over the changed set (the delta spec's markdown is formatter-owned), and
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`. **Done** — `spotlessCheck` clean, the 120-character
      recipe clean over the changed set, and `check` BUILD SUCCESSFUL.
- [x] 3.3 The edited definitions still load. Verify: the `build` gate's OpenCode probe (`./gradlew check` runs it for
      `.opencode/` changes, asserting every agent, command, and skill definition resolves with its metadata intact), and
      `opencode debug config` exits 0 with the three edited definitions' metadata present. **Done** — `check` green,
      `opencode debug config` exit 0, and both edited definitions read back with the new clauses in place.
- [x] 3.4 **Smoke-run `review-quick` against a seeded unit, after an OpenCode reload.** A definition edited mid-session
      is served from the pre-change copy until the agent list reloads, so this runs after a restart, and its evidence is
      the registered body rather than the file on disk. Seed it with positive controls: a supplied-list omission it must
      report as a limitation, and a supplied risk focus it must not exceed. Verify: the run names the three layers and
      the stop rule, reports the seeded limitation, and does not expand beyond the supplied list. **This is a
      precondition for archiving, not a task to defer across the merge** — an unchecked box inside `openspec/changes/`
      `archive/` is invisible and no gate reads it. **Done** — run after an OpenCode restart (session
      `ses_eada6e951ffefGaQHh2OQBhf6D`), against a seeded unit whose supplied list deliberately omitted the file its
      assertion needed. The registered body was confirmed first (`opencode debug agents` carries all five new clauses),
      since a subagent can read its file from disk. The run named its scope and its bounded-screen status, honoured the
      caller's risk focus, **reported the seeded limitation rather than chasing it** ("this pass did not search for it,
      so the assertion stays unreproducible here"), and did not expand beyond the supplied list — it even disclosed a
      single existence probe and refused to let a finding rest on an unscreened surface. Its record line was not
      appended to the ledger: it reviews a seeded unit, not this change.

## 4. The change's own records

- [x] 4.1 Record the pre-write derivation pass: the claims table is in `design.md`, its rows derived from the interface
      in use, the CLI's own help, the pre-change spec, the ledger's declared fields, and the prior session changes, with
      the `scripts/claim-scan.py` candidates each resolved into a row or dismissed beside the table. **Done** — the
      table's rows derived from the interface in use, the CLI's help, the pre-change spec, the ledger's fields, and the
      prior session changes, with the dismissals re-derived from the scan's candidates.
- [x] 4.2 Run the `review-quick` subagent over the planning artifacts, then the implementation, repeating until it
      reports no new observations; record each round's row in `docs/review-rounds.tsv` from the actual invocation,
      continuing the reviewer session the first round created. **Done** — the loop's rounds, their classes, and their
      session transitions are the rows `docs/review-rounds.tsv` holds for this unit, including the round-4 `replaced`
      for `definition-changed`. Through round 7 the class `incomplete-enumeration` recurred in every round, its repeat
      column set from round 2, and round 7's remedy ended it. The loop was then escalated to the owner per the spec's
      record-artifact clause, and they resolved to continue.
- [x] 4.3 Derive the deep-review routing floor with
      `python3 scripts/review-routing.py --change make-reviews-predictable-and-recoverable`, decide at or above it, and
      record the outcome and its signals here; run a `REQUIRED` deep review, or record the owner's explicit resolution
      for a `RECOMMENDED` one. **Done** — the derivation reports `RECOMMENDED` on the `[repeated-class]` signal. No
      path, declaration, or requirement-block signal matched. **The owner's resolution is the recorded decision**: the
      loop was escalated to them per the spec's record-artifact clause — a record has no shipped behaviour to converge
      on — and they resolved to continue the review rather than run a deep review. No deep-review session is created.
- [x] 4.4 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta or the skip and its reason here. **Also report** any rule this change obsoletes or makes
      redundant. **Done** — the subagent ran (session `ses_eadce50b8ffe17IBTxd0is6UFv`). Applied: the self-records
      bullet gains the dismissal-repair rule; the pointers to the reviewer-session rule collapse to one statement at the
      preflight; and `openspec/config.yaml`'s design rule gains the dismissal half of the reason-form rule. The applied
      net `AGENTS.md` delta is the figure `git diff --numstat AGENTS.md` reports. **One routed item is recorded, not
      applied**: extending the delta's watchdog scenario with the case the design's Risks names — a superseded
      invocation that reports after its replacement. It edits the delta after the loop closed by escalation, so the
      owner's manual pass decides it. **No retirements.**
- [x] 4.5 Record the `## Purpose` decision for `showcase/quality/agent-skills`: whether this change falsifies the
      Purpose's own text. Verify: state either that a refresh is owed (and apply it in the archive commit, since a delta
      cannot carry a Purpose) or that it is not, with the reason. **Done — no refresh is owed.** The Purpose enumerates
      the capability's coverage: the vendored skills, the authoring and structured-writing standards, the subagents, the
      audits and their scheduled variant, the reduction unit, and the command definitions. This change refines the
      per-change quality-gate requirement's clauses and adds a scenario; it adds no subagent and widens no class the
      Purpose names, so its text is not falsified.
- [x] 4.6 Ask the user for the manual review pass once 4.2 is clean and 4.4 is done; do not commit, push, or archive
      before they approve. **Done** — requested.
