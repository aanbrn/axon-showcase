# Review hand-over — make-unit-lifecycle-explicit

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `make-unit-lifecycle-explicit`; tree `/Users/afanasyev/Development/axon-showcase` (the
  primary checkout, this unit's tree); branch of the same name, forked `--no-track` from `origin/main` `805f30ef`;
  nothing committed; the unit's files staged.
- **Packs** — `gradle-build` and `jvm-testing`: decision 5 of the design introduces a Gradle `Task` + rules object +
  `*RulesTests` and wires it into `check`, so a build decision and a test-shape decision are being taken at proposal
  stage.
- **Supplied artifacts** — `openspec/changes/make-unit-lifecycle-explicit/`: `proposal.md`, `design.md` (nine decisions
  with their rejected alternatives, risks, migration plan, verification, the claims table and its dismissals),
  `tasks.md` (six task groups), `.openspec.yaml` (no `skip_specs`), and the two delta specs
  `specs/showcase/quality/agent-skills/spec.md` (7 added, 2 modified requirements) and
  `specs/showcase/quality/commit-hygiene/spec.md` (2 added).
- **The proposed change** — make the unit lifecycle the explicit, published process: seven stages (Begin, Plan, Proposal
  review, Implement, Implementation review, Deliver, Settle) with their transitions and exits; a new ADR recording the
  model; a `begin-unit` skill and command replacing `/new-worktree` and owning the whole workspace establishment;
  conditional archive; a declaration block in every project-authored command with `verifyCommandLifecycle` enforcing it;
  the `closed`-does-not-prove-delivery boundary; and the narrow publication exceptions stated rather than implied.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate make-unit-lifecycle-explicit` and `--strict` both pass; `spotlessApply` has
  run; the claims table's rows were re-run against the base revision; the claim scan's candidates are rows or
  class-level dismissals in `design.md`.
- **Known limitations** — no implementation exists yet, so the check's controls and the composed-route scenarios are
  planned rather than run; the Begin skill's step set is specified from the retired command's decision points and
  failure branches, and its final wording is the implementation's work.

## Round 2 — proposal screen, after round 1's seventeen findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `805f30ef`; nothing committed; the unit's files staged
  and formatted.
- **Packs** — `gradle-build` and `jvm-testing`, for the same reason as round 1.
- **Supplied artifacts** — round 1's fixes: the Impact states the build-gate movement and leaves dependency, API,
  deployment, and service unchanged; the retained "work stays uncommitted" scenario names the exceptions its description
  now carries; task 4.2 leaves the checkpoint's `Stage` operation-scoped; the command counts read 31 files with 6
  generated and 25 project-authored; `design.md` gains an **Amended clauses** section quoting each changed clause beside
  its replacement and why, and decision 2 no longer claims the states are unchanged; two Purpose tasks join the plan;
  the commit-hygiene delta gains the generated-mapping predicate and its scenario; task 5.3's controls cover all six
  predicates and the wiring is verified by `check --dry-run` plus a deliberately undeclared command; the claims table
  anchors base `805f30ef` and reads its sources at that revision; the Begin requirement points at the preflight
  requirement's workspace rule instead of restating it; the declaration requirement is named by title rather than "the
  previous requirements"; the script row cites the line that states the fact; the Lifecycle value set matches the
  requirement in both artifacts; decisions 3 and 5 are bulleted; the two two-behaviour requirements are split; and the
  table's pipes are escaped.
- **Prior findings with their instances** — round 1's seventeen, in `findings/round-1.md`.
- **Completed checks** — `openspec validate make-unit-lifecycle-explicit --strict` passes; `spotlessApply` ran after the
  last edit; `commit-hygiene --unique-rounds --expect make-unit-lifecycle-explicit:1` and `--session-continuity` pass;
  `spotlessCheck`, `verifyWorkspaceRecords`, `verifyUnitLayout`, `verifyUniqueReviewRounds`, `verifyFindingInstances`,
  and `verifyTableRows` pass.
- **Known limitations** — unchanged from round 1: no implementation exists, so the controls and composed routes are
  planned.

## Round 3 — proposal screen, after round 2's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `805f30ef`; nothing committed; the unit's files staged
  and formatted.
- **Packs** — `gradle-build` and `jvm-testing`, for the same reason as round 1.
- **Supplied artifacts** — round 2's fixes: the wiring verification now separates graph membership (`check --dry-run`)
  from the failing execution (a real `./gradlew check` with a deliberately undeclared command), in the design and in
  task 5.3; the Begin requirement no longer restates the state requirement's `active`-at-Begin transition; the
  publication row's second range is `AGENTS.md` 408-410; and the amended-clause rationale says the write was unstated
  rather than that the record carried no state.
- **Prior findings with their instances** — rounds 1 and 2, in `findings/`.
- **Completed checks** — `openspec validate make-unit-lifecycle-explicit --strict` passes; `spotlessApply` ran after the
  last edit; the ledger's `--expect 2` and `--session-continuity` pass; the record, layout, finding-instance, and
  table-row verifications pass.
- **Known limitations** — unchanged: no implementation exists, so the controls and composed routes are planned.

## Round 4 — implementation screen

- **Unit, tree, branch, base** — unchanged; base `origin/main` `805f30ef`; nothing committed; the implementation is
  complete and every changed file is staged.
- **Packs** — `gradle-build` and `jvm-testing`, and they were loaded before this implementation's build and test work.
- **The implementation**
  - `docs/adr/0030-the-unit-lifecycle-is-the-enclosing-process.md` — the decision, its consequences, and six rejected
    alternatives.
  - `AGENTS.md` — the lifecycle section replaced: a seven-stage table with their normal exits, transitions and exits,
    the scope and owner map, the Workspace block keeping its authorization/occupancy/preservation/release rules and
    pointing at the Begin skill, stage entries, the generated change-entry mapping, the command-declaration paragraph, a
    conditional-archive completion criterion, and the placement example retargeted to `/begin-unit`.
  - `.opencode/skills/begin-unit/SKILL.md` and `.opencode/commands/begin-unit.md` — the Begin procedure and its entry
    point; `.opencode/commands/new-worktree.md` retired; `scripts/new-worktree.sh` unchanged.
  - `docs/contracts/process-bindings.md` — the three heading values retargeted and the two Begin components added to the
    consumer trace.
  - `review-gate`, `pause-resume-unit`, and `ship-change` — the boundary names, the checkpoint's `Stage` wording, and
    delivery/settlement with a conditional archive.
  - Twenty-five command declarations (stage or scope, invocation, effect, owner) across the project-authored commands.
  - `build-logic/src/main/kotlin/CommandLifecycleRules.kt`, `VerifyCommandLifecycleTask.kt`,
    `build-logic/src/test/kotlin/CommandLifecycleRulesTests.kt` (eleven cases, one known-bad per predicate), and
    `verifyCommandLifecycle` wired into `check`.
  - `README.md` — the unit lifecycle stated before the change diagram, which stays as the nested change path; the
    command row retargeted.
  - `docs/ideas.md` — the implemented parked entry removed.
  - `docs/units/make-unit-lifecycle-explicit/process-defects.md` — one record.
- **Prior findings with their instances** — rounds 1–3, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green (2m 03s); the new task's wiring
  twice: `check --dry-run` lists `verifyCommandLifecycle`, and removing one command's declaration made the task fail
  naming that command before it was restored; `openspec validate --all` 27 items; `spotlessApply` after the last edit.
- **Known limitations** — task 6.4's composed routes (free primary, occupied primary, failed preparation, continued
  unit, unanswered approval, change-less delivery, archived-but-unmerged continuation) were not run in disposable
  repositories; the check's narrow claim is bounded in its requirement and its tests. The two Purpose refreshes remain
  scheduled for the archive commit.

## Round 5 — implementation screen, after round 4's ten findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `805f30ef`; nothing committed; every changed file staged
  and formatted.
- **Packs** — `gradle-build` and `jvm-testing`.
- **Supplied artifacts** — round 4's fixes: the ship section's Ordering bullet is conditional; the check no longer
  caches (it re-derives its verdict each run, so a moved owner cannot leave it UP-TO-DATE); its anchors are the eight
  the preflight names, its stage set is read from `AGENTS.md`'s stage table, and each of its other vocabulary values is
  asserted present in `AGENTS.md` so a rename there fails; the Begin skill's no-`.idea/` readiness branch is restored
  and its verification line conditions on `.idea/`; the stage table and the Plan entry agree on the named-direct-edit
  route; the Owners paragraph scopes its claim to the entries that name a skill; task 1.3 is ticked; and ADR-0030's
  `closed` clause is the pre-merge write.
- **Prior findings with their instances** — rounds 1–4, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green after the last edit; the rules
  tests pass (thirteen cases, one known-bad per predicate plus a vocabulary-rename case and a stage-rename case);
  `verifyCommandLifecycle` verifies 25 declarations and 6 generated mappings; `openspec validate --all` 27.
- **Known limitations** — task 6.4's composed routes remain unexercised in disposable repositories; the owner's explicit
  acceptance or a later run is owed. The two Purpose refreshes remain scheduled for the archive commit.

## Round 6 — implementation screen, after round 5's three findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `805f30ef`; nothing committed; every changed file staged
  and formatted.
- **Packs** — `gradle-build` and `jvm-testing`.
- **Supplied artifacts** — round 5's fixes: the vocabulary-presence guard now reads the lifecycle section only, so a
  value renamed elsewhere in `AGENTS.md` cannot keep the check green; the rules tests gain the vocabulary-rename and
  stage-rename cases, thirteen in all; and task 6.4's composed routes stay open for the owner's decision, unchanged.
- **Prior findings with their instances** — rounds 1–5, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green after the last edit; the rules
  tests pass (thirteen cases); `verifyCommandLifecycle` verifies 25 declarations and 6 generated mappings;
  `openspec validate --all` 27.
- **Known limitations** — task 6.4's composed routes remain unexercised; the owner's explicit acceptance or a later run
  is owed. The Purpose refreshes remain scheduled for the archive commit.

## Round 7 — implementation screen, after round 6's findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `805f30ef`; nothing committed; every changed file staged
  and formatted.
- **Packs** — `gradle-build` and `jvm-testing`.
- **Supplied artifacts** — round 6's fixes: the vocabulary-rename fixture now carries a decoy `host operation` outside
  the lifecycle section, so a whole-file read fails the test; the `lifecycleSection` KDoc names the third-level heading.
  Task 6.4's composed routes stay open, unchanged, for the owner.
- **Prior findings with their instances** — rounds 1–6, in `findings/`.
- **Completed checks** — the rules tests pass with the decoy; `verifyCommandLifecycle` verifies 25 declarations and 6
  generated mappings; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green after the last edit;
  `openspec validate --all` 27.
- **Known limitations** — task 6.4's composed routes remain unexercised; the owner's explicit acceptance or a later run
  is owed.

## Round 8 — implementation screen, after the composed-route run

- **Unit, tree, branch, base** — unchanged; base `origin/main` `805f30ef`; nothing committed; every changed file staged
  and formatted.
- **Packs** — `gradle-build` and `jvm-testing`.
- **Supplied artifacts** — task 6.4's run, recorded in `tasks.md`: a disposable repository built from this working tree,
  with a bare origin. A free primary wrote the unit record and the claim; an occupied primary produced the sibling tree
  on its own branch; a failed preparation kept the tree, reported `NEEDS_ACTION` and its remedy; a continuation took a
  tree on the existing branch; a change-less unit's archive was refused, which is the delivery step's conditional; an
  archived-but-unmerged unit continued on its branch with its archive present and its record intact. The
  unanswered-approval route has no filesystem fixture and rests on the boundary rule and the requirement's scenario.
- **Prior findings with their instances** — rounds 1–7, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green after the last edit; the rules
  tests pass (13 cases); `verifyCommandLifecycle` 25/6; `openspec validate --all` 27; the ledger's `--expect 7` and
  `--session-continuity` pass.
- **Known limitations** — the unanswered-approval route is scenario-verified only; the Purpose refreshes remain
  scheduled for the archive commit.

## Round 9 — implementation screen, after the README reframing

- **Unit, tree, branch, base** — unchanged; base `origin/main` `805f30ef`; nothing committed; every changed file staged
  and formatted.
- **Packs** — `gradle-build` and `jvm-testing`.
- **Supplied artifacts** — the README reframed after the advisor's review of its fidelity to the model: the section now
  opens with the unit as the organizing concept; the unit paragraph names the seven stages and the named-direct-edit
  route; the change diagram is explicitly labelled as the nested path, not the whole lifecycle; the phase narrative
  places the artifacts in Plan and Implement; the review paragraph states boundaries rather than universal phases and
  drops the branch-conflict procedure its owner carries; the PR paragraph separates approval from the delivery request
  and makes the archive conditional with Settle after the merge; the iterative paragraph speaks of the lifecycle and a
  material scope change; the entry-point paragraph says the source does not change the authorization; `Parallel Units`
  connects Begin and Settle; and the worked scenario gains the Begin and Settle bookends.
- **Prior findings with their instances** — rounds 1–8, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green after the edit;
  `openspec validate --all` 27; the ledger's `--expect-clean` and `--session-continuity` pass; the narrative read
  continuously from Spec-Driven Development through Parallel Units to the worked scenario tells one story (units
  enclosing the optional change path, review boundaries with the single-pass route, approval distinct from the delivery
  request, archive conditional and inside Deliver, settlement after merge).
- **Known limitations** — unchanged: the unanswered-approval route is scenario-verified only; the Purpose refreshes
  remain for the archive commit.

## Round 10 — implementation screen, after round 9's two findings

- **Unit, tree, branch, base** — base now `origin/main` `f45ef735`: the branch was fast-forwarded to it, and the
  incoming `AGENTS.md` Workspace bullet ("Air-hosted unit sessions use Local mode …") was reconciled into the rewritten
  block rather than dropped; nothing committed; every changed file staged and formatted.
- **Packs** — `gradle-build` and `jvm-testing`.
- **Supplied artifacts** — round 9's fixes: the README now says `AGENTS.md` owns the lifecycle model and the entry
  points to its stage procedures (the skills own the procedures), and the named-direct-edit exception says it skips the
  proposal review and keeps the implementation one. The base refresh changed `AGENTS.md` (the Air bullet, now in the
  Workspace list), `.opencode/skills/setup-agent-tools/SKILL.md`, and added that unit's records and archive; the
  reconciliation kept both sides.
- **Prior findings with their instances** — rounds 1–9, in `findings/`.
- **Completed checks** — `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` green after the reconciliation; the
  marker check passes; the ledger's `--expect 9` and `--session-continuity` pass; `--review-ready` confirms the new base
  and the index.
- **Known limitations** — unchanged: the unanswered-approval route is scenario-verified only; the Purpose refreshes
  remain for the archive commit.
