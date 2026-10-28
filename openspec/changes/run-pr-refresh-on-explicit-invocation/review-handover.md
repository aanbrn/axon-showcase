# Review hand-over

## Round 1 — the proposal

- **Unit** — change `run-pr-refresh-on-explicit-invocation`, branch of the same name, base `5a2895b5` (the
  generalization unit's merge); no commits; the change directory is untracked.
- **Supplied artifacts** — `proposal.md`, `specs/showcase/quality/agent-skills/spec.md` (a `MODIFIED` block),
  `design.md` (Context, six decisions, the claims table, risks, non-goals), `tasks.md`; plus the evidence the design
  cites: `.opencode/commands/sync-branch.md`, `openspec/changes/archive/2026-10-27-add-sync-branch-command/design.md`,
  `openspec/specs/showcase/quality/agent-skills/spec.md` (the current requirement), `README.md`'s row, `docs/ideas.md`,
  `docs/process-defects/bump-setup-pack-action-2026-10-28.md#2`, `AGENTS.md` (the branch-refresh rule and the ship
  stage).
- **Scope** — the proposal round: whether the delta, design, and tasks are coherent with the proposal and the
  repository; the implementation has not run, so there is no behavior to review.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `design.md` carries the pre-write pass's claims
  table (nine rows, each with its command).
- **Known limitations** — the design leaves the exact `git fetch`/fast-forward invocation to implementation; claim 8
  rests on the merged incident record rather than a fresh run; the current `gh pr update-branch` help was read on the
  installed CLI, not pinned in the repository.
- **Deadline** — 15 minutes, the caller's policy.

## Open questions

none

## Round 2 — the revised proposal

- **Prior findings and their instances (round 1), with fixes** —
  1. The ordered guard left steps 4–6 matching an open pull request's branch → task 1.7 makes step 3 terminal for that
     case and restricts steps 4–6 to a branch with no open pull request.
  2. Scenarios 3 and 6 narrowed "push" to "push of the unit's own work", which the body did not → both now keep "push"
     unqualified; the exception clause carries the carve-out, and scenario 3 names the exception instead.
  3. The exception was keyed to a command class the requirement's subject does not reach (`/sync-branch` starts no unit)
     → the requirement gains a scope sentence: a command that carries the preflight without starting a unit owes the
     same lifecycle and approval boundary, and scenario 6's WHEN names that command.
  4. "The command is the only site that refuses it" had no row → claims row 10 reproduces the grep.
  5. The bounded re-read and the "requested but unconfirmed" report were not in a task → task 1.1 and the completion
     criteria (1.6) carry them.
  6. The proposal promised a conflict report "with the paths" the route cannot produce → it now says "its reason".
  7. The amended clauses were not quoted beside their replacements → D5 carries the old/new pair for the approval clause
     and for scenario 3's THEN, with the reason each changed, plus the new scope sentence.
  8. The design lacked the schema's `Goals / Non-Goals` section and its Context did not reference the proposal → both
     added; the former trailing Non-Goals section is merged into it.
  9. Claims row 6 cited `AGENTS.md` lines 1641/2114 → now 1643/2116, as the row's command returns.
  10. The idea-removal task did not name the entry → task 3.2 names "Run the refresh on an explicit `/sync-branch`
      invocation".
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply` then `spotlessCheck` green; the
  claims table's rows were re-run at the base.
- **Scope** — still the proposal round; no implementation exists.
- **Known limitations** — same as round 1, plus: the widened scope sentence is a deliberate widening of the
  requirement's subject, recorded in D5's clause pairs.
- **Deadline** — 15 minutes, the caller's policy.

## Round 3 — the scope decision (no spec delta)

The owner approved direction A with a `MODIFIED` delta, on the advisor's advice. Round 2's finding showed the premise
was wrong: the lifecycle requirement's subject is a command that starts a mutating unit, and `/sync-branch` starts none,
so the requirement never bound the command. The advisor withdrew the delta premise, and the scope is now:

- **No delta**: `.openspec.yaml` carries `skip_specs: true`; the `specs/` artifact is removed; the proposal's Modified
  Capabilities reads `none`; D5 records why (the requirement's subject, `git grep sync-branch` over the specs, the
  explicit-request invariant at `AGENTS.md:135`, and the definition-edit rule that permits `skip_specs`); rows 11–12
  derive the two new claims; the design's stale delta references are gone and the tasks lose their delta group.
- **The command carries the boundary**: step 3, the decision point, the lifecycle line, and the completion criteria
  state the remote update the invocation authorizes and the stops on the unit's own work.
- **Prior findings** — round 2's two findings are resolved by this scope decision: the proposal bullets no longer
  describe an exception that is not owed, and the self-satisfying scope sentence is gone with the delta.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed (the change validates with `skip_specs` and no
  delta); `spotlessApply`/`spotlessCheck` green.
- **Scope** — still the proposal round; no implementation exists.
- **Known limitations** — the owner's decision on the reduced scope is requested with the manual pass; the advisor
  withdrew the earlier delta recommendation.
- **Deadline** — 15 minutes, the caller's policy.

## Round 4 — the reconciled premise

- **Prior findings and their instances (round 3), with fixes** —
  1. The no-delta premise contradicted the archived D3's "already govern[s] it" clause, and Context paraphrased D3
     without it → Context now quotes the clause and points at D5, and D5 reconciles it: the clause describes the shape
     the command adopts, not a SHALL whose antecedent it meets, since the requirement binds "a command that starts a
     mutating unit that ships"; adopting a template does not make its requirement binding. The reversal of the earlier
     delta plan is recorded with that reading.
  2. Row 12 attributed the `skip_specs` clause to the docs-refresh convention → it now names the "A recurring unit type"
     bullet.
  3. Task 1.3 left the decision point's unqualified push stop → it now replaces that sentence with the qualified stops
     ("push of the unit's own work"), matching proposal bullet 3 and task 1.4.
- **Boundary re-derived once** — the decision point (task 1.3), the lifecycle line (task 1.4), and the completion
  criteria (task 1.6) all name the same authorized operation and the same unchanged stops.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — the owner's decision on the reduced scope is requested with the manual pass.
- **Deadline** — 15 minutes, the caller's policy.

## Round 5 — the site set closed

- **Prior findings and their instances (round 4), with fixes** —
  1. Row 12's label still said "docs-refresh convention" → it now names the "A recurring unit type" bullet, applied with
     an asserted replacement this time.
  2. The boundary's site list included the completion criteria, which no task carried → Goals and D4 now name the two
     sites the command will carry it in (the decision point and the lifecycle section); the completion criteria keep the
     refreshed head and the local branch's state, as tasks 1.6 states.
  3. The proposal's idea-removal bullet did not name the entry → it now quotes "Run the refresh on an explicit
     `/sync-branch` invocation", as task 2.2 does.
- **Process observation, recorded for this unit** — two round-3 fixes were declared applied while one replacement had
  not matched (an unasserted string replacement), which is the editing rule this repository records. The unit's
  process-defect record will carry it; every replacement in this round asserts its anchor and re-reads the result.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; each
  fixed line re-read after the formatter.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — the owner's decision on the reduced scope is requested with the manual pass.
- **Deadline** — 15 minutes, the caller's policy.

## Round 6 — closing

- **Prior finding and its instance (round 5)** — the Risks section still carried the withdrawn three-site list
  ("decision point, lifecycle section, and completion criteria") → corrected to the two sites the design and tasks now
  share. The statement's other occurrences were swept: `design.md` and `tasks.md` name only the decision point and the
  lifecycle section for the boundary, and the only remaining "completion criteria" occurrence (`proposal.md:22`) is the
  report fields, which is what task 1.6 carries.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green.
- **Scope** — the proposal round; no implementation exists.
- **Known limitations** — the owner's decision on the reduced scope is requested with the manual pass.
- **Deadline** — 15 minutes, the caller's policy.

## Round 7 — the implementation

- **Unit** — change `run-pr-refresh-on-explicit-invocation`, branch of the same name, base and `HEAD` `5a2895b5`; no
  commits; the change directory and the unit's record are uncommitted.
- **Supplied artifacts** — the implementation diff (`.opencode/commands/sync-branch.md`, `README.md`, `docs/ideas.md`,
  `design.md`'s narrowed Non-Goal, the ticked `tasks.md`,
  `docs/process-defects/run-pr-refresh-on-explicit-invocation.md`) and the proposal artifacts already reviewed in rounds
  1–6.
- **Scope** — the implementation round: the command's new steps 3–9, the decision point, the lifecycle exception, the
  failure handling, and the completion criteria; the README row; the idea removal; the unit's record.
- **Prior findings** — rounds 1–6 closed clean at round 6; the reduced scope (no delta, `skip_specs`) is the owner's
  pending decision at the manual pass.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  Docker-free `./gradlew check`: `BUILD SUCCESSFUL`.
- **Known limitations** — the `gh pr update-branch` head re-read is prose, not a script; the local fast-forward's exact
  failure modes are left to the executing agent; the unit's record is new and unreviewed.
- **Deadline** — 15 minutes, the caller's policy.

## Round 8 — the dirty-tree guard scoped

- **Prior findings and their instances (round 7), with fixes** —
  1. The hoisted dirty-tree stop blocked an open pull request's update → step 3 now fires only when **no** pull request
     is open; an open pull request's update runs regardless, and its local fast-forward reports a refusal instead of
     pre-blocking (the rule's own sentence: a branch with uncommitted work fast-forwards, keeping its changes).
  2. The record cited `agent-skills/spec.md:1332` for the requirement's antecedent → `:1331`.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  Docker-free `check` was green before this round's edits, which touch only the command's prose and the record.
- **Scope** — the implementation round.
- **Known limitations** — same as round 7.
- **Deadline** — 15 minutes, the caller's policy.

## Routing

`python3 scripts/review-routing.py --change run-pr-refresh-on-explicit-invocation` → `RECOMMENDED`, signal
`[repeated-class] 5 rows repeat a prior class` (the proposal rounds repeated `coherence`, `wording`,
`claim-unreproducible`, and the implementation round 7 repeated `contradiction`). The owner resolved the outcome to
**skip the deep review** in favour of the independent consultation, and authorized shipping on its no-objection.

## Round 9 — the shipping consultation's correction

- **Prior finding (external, from the shipping consultation)** — round 8 had scoped the dirty-work guard to "no pull
  request is open"; the root rule states that a branch holding both its own commits and uncommitted work stops until
  that work is resolved, with no exception for an open pull request, and the pre-change step 3 was inert (a hand-off),
  so its ordering never had to respect that stop. Now that step 4 mutates, the guard precedes both routes again.
- **Fix** — step 3 is restored unscoped: "IF the branch carries its own commits beside uncommitted work, THEN stop and
  report what has to be resolved first: both routes below run only on a tree whose work is settled."
- **Recorded decisions** — the owner resolved the `RECOMMENDED` routing outcome to skip the deep review in favour of the
  independent consultation (recorded above) and authorized shipping on its no-objection; task 3.4 is ticked with that
  outcome.
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green; the
  Docker-free `check` was green before this prose edit.
- **Scope** — the implementation round.
- **Known limitations** — the shipping consultation's other conditions are recorded (archive after CI, stage both sides
  of the move, green CI on the archive commit).
- **Deadline** — 15 minutes, the caller's policy.

## Round 10 — the step references aligned

- **Prior findings and their instances (round 9), with fixes** —
  1. The design, proposal, and ticked tasks still named "step 3" for the pull-request update, which is now step 4 (step
     3 is the dirty-work stop) → D1 says step 4 and states the stop's precedence, D2 says step 5, the proposal bullets
     name steps 4 and 5, and tasks 1.1/1.2/1.7 name steps 4, 5, and 6–8.
  2. Step 3's "both routes below" left its referent ambiguous (three routes are gated) → "every route below".
- **Completed checks** — `openspec validate --all`: 27 passed, 0 failed; `spotlessApply`/`spotlessCheck` green.
- **Scope** — the implementation round.
- **Known limitations** — same as round 9.
- **Deadline** — 15 minutes, the caller's policy.
