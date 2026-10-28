# Review hand-over

## Round 1 — the proposal

- **Unit** — change `route-the-ledger-signal-per-unit`, branch of the same name, base `4475a8e4`; no commits; the change
  directory is untracked; the branch name is the change name, so no claim file is needed.
- **Supplied artifacts** — `proposal.md`, `design.md` (five decisions, seven claims rows, risks, non-goals), `tasks.md`,
  `.openspec.yaml` (`skip_specs: true`); plus the evidence the design cites: `scripts/review-routing.py`,
  `scripts/test-review-routing.py`, `.opencode/skills/review-gate/SKILL.md` (the routing section),
  `openspec/specs/showcase/quality/agent-skills/spec.md` (the routing requirement), `.opencode/agent/review-quick.md`
  (the record line), `docs/ideas.md` 2026-10-26 (the parked defect), and the archived generalization unit's review
  hand-over with its `NOT_NEEDED`→`RECOMMENDED` sequence.
- **Scope** — the proposal round: the two identities, the failure boundary, the controls, the skill correction, and the
  no-delta determination; no implementation exists.
- **Prior findings** — none; the unit's first round.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  claims table's seven rows were re-run at the base.
- **Known limitations** — the scratch-repository CLI control is planned, not written; the unit-identity source for a
  continued cloud branch is stated in the design (the recorded name, supplied by the caller) but not yet exercised.
- **Deadline** — 15 minutes, the caller's policy.

## Open questions

none

## Round 2 — the revised proposal

- **Prior findings and their instances (round 1), with fixes** —
  1. The central preservation promise had no CLI control → tasks 2.3 (`--change`-only, `RECOMMENDED`) and 2.4 (`--unit`
     ≠ `--change`: rows from the unit, block kinds from the change) name it; the helper cases are task 2.2.
  2. The `docs/ideas.md` entry this change implements was not removed → task 3.2 and the proposal's Impact name it.
  3. Claims row 6 cited an archived hand-over that does not exist (the generalization unit had no change dir) → it now
     cites the 2026-10-26 ideas entry and the unit's ledger rows, which carry the sequence.
  4. "Every existing invocation keeps its outcome" over-claimed → now "every invocation that names a change or a unit;
     an invocation that names neither fails".
  5. The differing-name mismatch was claimed mitigated twice, neither of which can fire → the risk is stated as
     residual, the skill instructs `--unit` whenever the names differ, and task 2.4 controls it.
  6. The promised visible failure could be skipped by `floor`'s `REQUIRED` short-circuit → D2 validates the identity in
     `main` before the floor, and task 2.5 asserts the failure fires alongside a `REQUIRED` path signal.
  7. A second `## Non-Goals` duplicated the template's section → folded into one, with the change-directory and
     precedence clause moved there.
  8. Five controls sat in one checkbox → each control is its own task (2.1–2.5).
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; each
  changed line re-read.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — same as round 1; the differing-name case is a stated residual risk.
- **Deadline** — 15 minutes, the caller's policy.

## Round 3 — the control set derived from the code's branches

- **Prior findings and their instances (round 2), with fixes** —
  1. The `--unit`-only CLI control was dropped → task 2.4 restores it (scratch repository, no change directory,
     `RECOMMENDED`, the `repeated-class` line, exit 0).
  2. One run was asked to assert both the unit's rows and the change's block kinds, which `floor`'s short-circuit makes
     impossible → split into 2.5 (the change's delta has no block kind; the unit's rows decide) and 2.6 (an `ADDED`
     block decides, the signal names the change's spec).
  3. D2's `main`-level validation had no task → task 1.2 now names it, with `ledger_signal` as the second layer, and
     task 2.7 asserts the failure fires alongside a `REQUIRED` path signal.
  4. Row 6's repeated rounds were wrong → re-run: `/` on round 1, repeats on 2, 3, 4, 6 and 7; the row now says so.
  5. Task 2.2 claimed to add helper cases that exist → it now says to keep and adapt them to the renamed parameter.
- **Derivation** — the controls were walked against `review-routing.py`'s branches: unit-only, change-only, both,
  neither, and a pre-empting `REQUIRED` path or block signal; the claims row was re-run rather than restated.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — same as round 2; the differing-name case is a stated residual risk.
- **Deadline** — 15 minutes, the caller's policy.

## Round 4 — the claims re-derived and the control list aligned

- **Prior findings and their instances (round 3), with fixes** —
  1. Row 4's source could not reproduce two of its quoted facts and was not base-anchored → it now runs
     `git show 4475a8e4:…spec.md | sed -n '2032,2086p'`, whose lines 2034, 2071 and 2076 carry the three facts. Every
     row's command was run verbatim at the base and its quoted output diffed, not just the row a round named.
  2. The proposal's "one case per control" list omitted the `--unit`-only run and described the identity split as one
     case → it now lists each case tasks 2.1–2.7 carry (helper cases; change-only; unit-only; no-block change delta with
     a different unit; `ADDED` block with a different unit; neither identity, with and without a pre-empting `REQUIRED`
     signal), and D4 points at the task list instead of restating it.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — same as round 3.
- **Deadline** — 15 minutes, the caller's policy.

## Round 5 — closing

- **Prior finding and its instance (round 4)** — D4 claimed every added CLI run asserts the outcome, the signal line,
  and the exit status, while only task 2.4 carried all three → tasks 2.3, 2.5, 2.6 and 2.7's second run now assert their
  signal line or failure message and the exit status, so the checklist carries the design's requirement.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; every
  claims row was re-run at the base in round 4.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — same as round 4.
- **Deadline** — 15 minutes, the caller's policy.

## Round 6 — the implementation

- **Unit** — change `route-the-ledger-signal-per-unit`, branch of the same name, base and `HEAD` `4475a8e4`; no commits;
  the change directory, the unit's record and the ledger rows are uncommitted.
- **Supplied artifacts** — the implementation diff (`scripts/review-routing.py`, `scripts/test-review-routing.py`,
  `.opencode/skills/review-gate/SKILL.md`, `docs/ideas.md`, the ticked `tasks.md`,
  `docs/process-defects/route-the-ledger-signal-per-unit.md`) and the proposal artifacts reviewed in rounds 1–5.
- **What the implementation does** — `--unit` selects the ledger identity and defaults to `--change`; `main` validates
  the effective identity before `floor`, so a run naming neither fails as `REQUIRED`; `ledger_signal` refuses a missing
  unit; the suite replaces the missing-signal test, keeps the helper cases, and adds eight CLI controls (unit-only with
  and without its own repeats, change-only, no-block delta with a different unit, `ADDED` block with a different unit,
  neither identity, a `REQUIRED` path signal with no identity, and an explicit unit with a nonexistent change); the
  review-gate section states the two flags; the idea is removed.
- **Control evidence** — the suite fails as it should: with `main` passing `args.change` to `floor` (the old wiring),
  three CLI controls fail (the unit-only pair and the different-unit case); restored, all 32 pass. The file's hash was
  verified before and after the perturbation.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green;
  `./gradlew testReviewRouting` green (32 tests); the Docker-free `check`: `BUILD SUCCESSFUL`.
- **Known limitations** — the wrong-identity residual risk is stated in the design; the CLI controls use a scratch git
  repository, not the live ledger.
- **Deadline** — 15 minutes, the caller's policy.

## Round 7 — the omission scoped and the line wrapped

- **Prior findings and their instances (round 6), with fixes** —
  1. The skill's "so an omission is never a lower floor" over-claimed: omitting `--unit` for a change unit whose ledger
     name differs silently reads zero rows → the sentence now scopes the `--change` omission and states that the
     `--unit` omission lowers the signal silently, to zero rows.
  2. `scripts/test-review-routing.py`'s fixture docstring was 122 characters → shortened; the >120 sweep over both
     scripts is clean.
- **Completed checks** — `spotlessApply`/`spotlessCheck` green; `scripts/test-review-routing.py` 32 tests OK; the
  Docker-free `check` was green before this round's two prose edits.
- **Scope** — the implementation round.
- **Known limitations** — the wrong-identity residual risk is stated in the design and the skill.
- **Deadline** — 15 minutes, the caller's policy.

## Routing

`python3 scripts/review-routing.py --change route-the-ledger-signal-per-unit` → `RECOMMENDED`, signal
`[repeated-class] 4 rows repeat a prior class` (four of the unit's eight rounds repeated a prior class; the ledger's
rows carry them). The owner resolved it as **skip, by explicit decision**, on the independent consultation's
recommendation: no mandatory-risk criterion is established (the change restores existing identity plumbing without
changing thresholds, signal classes or review policy), the correctness boundaries carry focused scratch-repository
controls, and the reported mutation check shows their sensitivity. The residual risk — a caller supplying a valid but
incorrect ledger identity — is stated in `review-gate/SKILL.md`; the decision is not a completed deep-review pass.

## Round 9 — the record corrections

- **Prior findings (from the shipping consultation), with fixes** —
  1. The routing paragraph enumerated the repeated rounds as 1, 2, 4 and 6; the ledger's repeats are 2, 3, 4 and 6 → the
     enumeration is removed: "four of the unit's eight rounds repeated a prior class; the ledger's rows carry them".
  2. Task 4.4's quoted signal carried a whitespace run inside the inline code span (`4 rows repeat     a prior class`),
     which the formatter cannot repair → the span is on one source line and reads the signal verbatim.
- **Completed checks** — `spotlessApply`/`spotlessCheck` green; `openspec validate --all` 27/0; `testReviewRouting` 32
  OK; the Docker-free `check` was green before these two record edits.
- **Scope** — the implementation round; the skip decision and its residual risk are recorded in task 4.4 and the routing
  paragraph.
- **Deadline** — 15 minutes, the caller's policy.
