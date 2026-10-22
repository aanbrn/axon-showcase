# Tasks

## 1. Apply the procedure

- [x] 1.1 Add the terminal handover to `.opencode/commands/consult.md` step 7: after presenting the report the agent
      ends its turn, and a report's closing prompt or named corrections are propositions that start only on a subsequent
      owner decision about that report. Verify the step quotes the boundary the command already states, so the addition
      reads as a completion rather than a second rule. **Done**: step 7 now ends the turn, with the proposition and the
      later-decision condition as bullets.
- [x] 1.2 Add the matching decision point to the command's list: IF the report closes with a prompt or names
      corrections, THEN present it, ask for the owner's decision, and stop. **Done**: the decision point covers any
      report that returns a recommendation, not only one with a prompt.
- [x] 1.3 Add one sentence to `technical-advisor.md`'s report contract: a requested prompt is delivered as a proposal to
      the owner, not as an instruction the invoking agent may run immediately. Verify it sits with "Next action" rather
      than in the advisor's independence or read-only statements. **Done**: the "Next action" bullet says a requested
      prompt is a proposal the caller presents and waits on.

- [x] 1.4 State the stopping step at the two approval hand-backs the captured rule governs: the change lifecycle's step
      4 and the review gate's step 6 both end the turn, so `AGENTS.md` conforms to its own rule. **Done**: both steps
      read "and end the turn there"; `git diff --numstat AGENTS.md` is 7/4 (the captured bullet +4/−1 and the two
      hand-backs).

## 2. Record and verify

- [x] 2.0 Run the pre-write derivation pass and record its claims table in `design.md`; seed it with
      `scripts/claim-scan.py` and resolve every candidate as a row or a class-level dismissal. **Done**: the claims
      table is in `design.md`, seeded by `claim-scan.py`; the incident row is labelled as the session account.
- [x] 2.1 Confirm the delta mirrors the requirement's path (`specs/showcase/quality/agent-skills/spec.md`), keeps the
      scenario names and the requirement's other scenarios, and states the hand-over in the existing scenario. **Done**:
      the delta mirrors the path, keeps both requirements' scenario names and order, and amends the two named scenarios.
- [x] 2.2 Run `openspec validate --all` and `spotlessApply`/`spotlessCheck`; read the two definitions against the delta.
      **Done**: `openspec validate --all` 27 passed, `--strict` valid, `spotlessCheck` green.
- [x] 2.3 Run the review gate: `review-quick` over the planning artifacts and the implementation, appending each round's
      row to `docs/review-rounds.tsv` with the identifier the invocation returns; fix every finding and re-run until a
      round reports no new observations; then derive the deep-review routing outcome with
      `python3 scripts/review-routing.py --change consult-terminal-handover` and record the outcome and its signals on
      this task. **Done**: the per-round state is `docs/review-rounds.tsv`'s, which records the rounds and their
      classes; routing is `RECOMMENDED` with the `repeated-class` signal, whose count the ledger owns and the script
      re-derives after the final append. Resolution: the deep pass is declined — the unit edits two definition
      paragraphs, two scenario bodies, one captured root rule and two root hand-back steps, with no code or schema, and
      both delta blocks were diffed mechanically against their main-spec originals.
- [x] 2.4 Run the `lesson-capture` subagent (the diff touches `.opencode/`, so the skip condition does not hold) and
      record its applied net delta or skip reason. **Done**: one durable proposal applied — the `AGENTS.md` Structured
      Writing Standard's "Numbered steps" bullet now states that a hand-back step ends the turn
      (`captured: consult-terminal-handover`, +4/−1 lines, net +3). The ledger-append recurrence was judged an
      instruction-following failure with no durable change.
- [x] 2.5 Run the definition smoke after an OpenCode reload: one short `/consult` in a fresh session whose report
      carries a closing prompt, observing that the caller presents the report, edits nothing, and stops for the owner's
      decision; a control with the pre-change command on the same fixture. **Done, with a limitation**: four isolated
      runs (the new and the pre-change command, each on a neutral fixture and on one carrying the review-gate loop
      context) all presented the report, edited nothing, and stopped for the owner's decision. The pre-change control
      stops as well, so the fixture does not discriminate: the run evidences the intended behavior, not that the wording
      reduces the failure, which stays an unverified hypothesis. Neither the neutral nor the review-gate-loop fixture
      reached the bad state; reproducing it would need a fixture carrying a prior owner go-ahead for the work and a
      finding-shaped report arriving inside that same turn, which these runs did not build.
- [x] 2.6 Ask the owner for the manual review pass; commit, push, archive, and merge only after approval. **Done**: the owner approved on 2026-10-22 ("Одобряю").
