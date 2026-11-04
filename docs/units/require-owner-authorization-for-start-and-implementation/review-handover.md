# Review hand-over — require-owner-authorization-for-start-and-implementation

One section per quick-review invocation, recorded before the caller waits on it.

## Round 1 — proposal screen

- **Unit** — `require-owner-authorization-for-start-and-implementation`, an OpenSpec change. It adds the owner's
  authorization gate at a unit's start and a manual-review stop for the proposal before implementation, with the exact
  wording in `design.md`; no rule file is edited yet, since the proposal must pass the owner's review first.
- **Revisions** — base `origin/main` `66069e0e`; the work is uncommitted on the branch.
- **Workspace** — `/Users/afanasyev/Development/axon-showcase`, branch
  `require-owner-authorization-for-start-and-implementation`.
- **Packs** — `none`: agent-process guidance.
- **Supplied artifacts** — the change dir (`proposal.md`, `design.md` with the proposed wording, `tasks.md` with the
  claims table, and the delta), this unit's `unit.md` and `process-defects.md`.
- **Scope** — the proposal's coherence with the owner's three stops, whether design.md's wording covers every unit kind
  and no step begins work before the authorization, whether the delta's ADDED and MODIFIED blocks are shaped correctly,
  and whether any existing rule still permits the gap.
- **Prior findings** — none; the unit's first invocation.
- **Completed checks** — `spotlessApply`; `openspec validate --all` (27 items);
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; the claims table re-derived at the base.
- **Known limitations** — the implementation has not begun, by design; design.md's wording is proposed, not applied.
- **Deadline** — none fixed; the record exists so a stall is recoverable from it.

## Round 2 — proposal screen, after round 1's seven findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the change dir with round 1's fixes: the invariant split into two bullets; the mapping for a
  unit with no separate proposal stage; the root stage-list wording added to `design.md`; the delta's `ADDED`
  requirement trimmed of the delivery-stop restatement and its scenario; task 0 for the pre-write pass with the scan's
  three kinds; the claims row citing `at the implementation stage`; and task 3 for the twelve command definitions.
- **Prior findings with their instances** — all seven in `findings/round-1.md`.
- **Completed checks** — `spotlessApply`; `openspec validate --all` 27 items;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; the twelve-command set and the four claims rows re-derived
  at the base.
- **Known limitations** — the implementation has not begun, by design.

## Round 3 — proposal screen, after round 2's six findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the change dir with round 2's fixes: the command set derived from the requirement's predicate
  (sixteen `Repository lifecycle` blocks less the three that start no unit — thirteen, `flag-process-defect` included);
  Decision 6 narrowed to implementation edits and non-planning files with the planning-artifact exception; task 0's
  absolute scan counts replaced by one dismissal reason per kind; every claims row anchored at `66069e0e`; the mapping
  named as `AGENTS.md`'s review-gate invariant in the design and the delta; the delta's authoring note deleted.
- **Prior findings with their instances** — all six in `findings/round-2.md`.
- **Completed checks** — `spotlessApply`; `openspec validate --all` 27 items;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; the base-anchored rows re-run.
- **Known limitations** — the implementation has not begun, by design.

## Round 4 — proposal screen, after round 3's two findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the same proposal with round 3's fixes: the duplicated command count removed from the
  design's Consequences, and the mapping attributed to `AGENTS.md:170`'s bullet in "OpenSpec Workflow Agreement" → "###
  Invariants" in the design and the claims row.
- **Prior findings with their instances** — round 3's two (`findings/round-3.md`).
- **Completed checks** — `spotlessApply`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; ledger checks for
  rounds 1–3.
- **Known limitations** — the implementation has not begun, by design; this section was written after its invocation,
  which round 4's finding records.

## Round 5 — closing proposal screen

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the same proposal, plus this hand-over's round-4 section and the unit's process-defect record
  replacing "No process defect observed" with `#1`.
- **Prior findings with their instances** — round 4's `missing-task` (`findings/round-4.md`).
- **Completed checks** — `spotlessApply` after these edits; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`;
  ledger checks for rounds 1–4.
- **Known limitations** — the implementation has not begun, by design.

## Round 6 — closing proposal screen, after round 5's fix

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the same proposal with round 5's fix: the unit's process-defect record now states the three
  occurrences with the intervening unit named, rather than claiming three consecutive ones.
- **Prior findings with their instances** — round 5's `over-claim` (`findings/round-5.md`).
- **Completed checks** — `spotlessApply`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; ledger checks for
  rounds 1–5.
- **Known limitations** — the implementation has not begun, by design; the rule this change introduces means it begins
  only on the owner's approval of this proposal.

## Round 7 — proposal screen, after the advisor's two corrections

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with the advisor's corrections: the single-pass route now requires the owner's
  authorization itself to name the direct edits (a "proposed edit" label does not skip the pass, with its own scenario);
  the writable scope before approval is an invariant — planning artifacts and `docs/units/<unit>/` records, no
  implementation target and no live rule file — and Decision 6 records why rather than stating the ban; task 6.2
  measures the applied `AGENTS.md` delta and prefers replacing the existing lifecycle wording.
- **Prior findings with their instances** — round 6 was clean; this round covers the advisor-driven edits.
- **Completed checks** — `spotlessApply`; `openspec validate --all` 27 items; the delta's new scenarios present.
- **Known limitations** — the applied net delta is unavailable until implementation.

## Round 8 — proposal screen, after round 7's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with round 7's fixes: the single-pass condition now amends its owner (task 2.3
  on `AGENTS.md`'s "The review gate covers every unit of work…" bullet), the writable scope is keyed to the approval the
  unit's route requires, task 1.1 adds all three bullets, Decision 6 points at the proposed wording below, and the delta
  gained the positive-branch scenario.
- **Prior findings with their instances** — all four in `findings/round-7.md`.
- **Completed checks** — `spotlessApply`; `openspec validate --all` 27 items;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; ledger checks for rounds 1–7.
- **Known limitations** — the implementation has not begun, by design.

## Round 9 — proposal screen, after round 8's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with round 8's fixes: the delta defines both routes' writable scope; the
  design's proposed wording carries the amended "The review gate covers every unit of work…" bullet that task 2.3
  applies; and the Procedure's single-pass skip sits at step 2, where the edits are created, leaving step 4 to two-pass
  units.
- **Prior findings with their instances** — all three in `findings/round-8.md`.
- **Completed checks** — `spotlessApply`; `openspec validate --all` 27 items; ledger checks for rounds 1–8.
- **Known limitations** — the implementation has not begun, by design.

## Round 10 — proposal screen, after round 9's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with round 9's fixes: the amended review-gate bullet now shows the base bullet's
  procedure retained with the condition folded in, under a heading shaped like its siblings; the Procedure is renumbered
  1–8 with the single-pass skip after step 3 pointing to step 7; the ledger carries round 8's row.
- **Prior findings with their instances** — all three in `findings/round-9.md`.
- **Completed checks** — `spotlessApply`; `openspec validate --all` 27 items; ledger checks for rounds 1–9.
- **Known limitations** — the implementation has not begun, by design.

## Round 11 — proposal screen, after round 10's fix

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with round 10's fix: the single-pass condition is one phrase — "names each
  direct edit the unit may make" — in the invariant, Procedure step 3, §5, the delta, task 2.3 and the amended mapping
  bullet.
- **Prior findings with their instances** — round 10's `single-sourcing` (`findings/round-10.md`).
- **Completed checks** — `spotlessApply`; `openspec validate --all` 27 items;
  `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; ledger checks for rounds 1–10.
- **Known limitations** — the implementation has not begun, by design.

## Round 12 — closing proposal screen, after round 11's fix

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the proposal with round 11's fix: the comma before "proceeds" removed, so the restrictive
  clause and its verb read as one sentence.
- **Prior findings with their instances** — round 11's `wording` (`findings/round-11.md`).
- **Completed checks** — `spotlessApply`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; ledger checks for
  rounds 1–11.
- **Known limitations** — the implementation has not begun, by design.

## Round 13 — implementation screen

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`. The owner's approval of the proposal
  authorized this implementation; its own text is `design.md`'s "Proposed wording", applied verbatim.
- **Packs** — `none`.
- **Supplied artifacts** — `AGENTS.md` (three invariant bullets, the Procedure reshaped 5→8 steps, the review-gate
  invariant's mapping conditioned, the stages bullet naming both boundaries), `review-gate` §5's stage-scoping sentence,
  the thirteen command definitions' lifecycle blocks (one added bullet each; `flag-process-defect`'s block worded
  differently and patched in its own phrasing), and `docs/ideas.md` with the 2026-11-04 entry withdrawn.
- **Prior findings with their instances** — rounds 1–12; the closing proposal round was clean.
- **Completed checks** — `spotlessApply`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  (`BUILD SUCCESSFUL`); `openspec validate --all` 27 items; applied `AGENTS.md` delta `git diff --numstat` 33/11 (net
  +22 — three new invariant obligations plus the reshaped Procedure; the existing wording was replaced rather than
  layered).
- **Known limitations** — the delta's amendment to the review-gate invariant is stated in `AGENTS.md`; the archived spec
  lands at archive time.

## Round 14 — implementation screen, after round 13's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the implementation with round 13's fixes: step 2 is conditioned ("or, where the unit's
  authorization names each direct edit, the unit's edits") in `AGENTS.md` and the design alike; the applied Procedure
  now matches the approved text including its bold lead-ins and without the added pointer; the thirteen blocks and the
  delta's `MODIFIED` body say "the implementation's manual approval pass".
- **Prior findings with their instances** — all three in `findings/round-13.md`.
- **Completed checks** — `spotlessApply`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`;
  `openspec validate --all` 27 items; applied `AGENTS.md` delta 32/9 (net +23).
- **Known limitations** — the delta's archive lands at archive time.

## Round 15 — closing implementation screen, after the deep review

- **Unit, tree, branch, base** — unchanged; base `origin/main` `66069e0e`.
- **Packs** — `none`.
- **Supplied artifacts** — the implementation with the deep review's single should-fix: the `ship-change` skill's
  Trigger no longer cites "the Change lifecycle's step 5" (now the proposal's manual-review ask) and names the shipping
  step instead, so the cross-reference survives the Procedure's renumbering.
- **Prior findings with their instances** — the deep review's one finding; quick rounds 1–14 are recorded in
  `findings/`.
- **Completed checks** — `spotlessApply`; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`;
  `openspec validate --all` 27 items; the dead-reference sweep over `.opencode/` and `AGENTS.md` returns no remaining
  "lifecycle's step".
- **Known limitations** — the delta lands in the main spec at archive time.
