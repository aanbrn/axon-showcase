# Review hand-over — record-unit-change-layering

## Round 1 — proposal screen

- **Unit, tree, branch, base** — `record-unit-change-layering`; tree `/Users/afanasyev/Development/axon-showcase` (the
  primary checkout, this unit's tree); branch of the same name, forked `--no-track` from `origin/main` `0b236cde`;
  nothing committed, the unit's four new files staged.
- **Packs** — none. The unit edits one ADR amendment and its own planning artifacts: it changes no JVM code, no test, no
  Gradle build decision, and no deployment, so no subject pack's row matches.
- **Supplied artifacts** — `openspec/changes/record-unit-change-layering/proposal.md`, `tasks.md` (its plan, the
  pre-write pass's claims table, and its dismissals), `.openspec.yaml` declaring `skip_specs`; the unit record
  `docs/units/record-unit-change-layering/unit.md`; and `docs/adr/0029-unit-state-lives-outside-the-change-plan.md`, the
  file the amendment will be appended to (unchanged so far).
- **The proposed change** — one dated `## Amendment` appended to ADR-0029, recording that a unit is the unit of work and
  an OpenSpec change one form it may take; naming the Decision's relocation of the record as superseding the Context's
  clause "the record cannot outlive the change directory", with a pointer at that sentence; and recording, as considered
  and declined, the alternative of carrying every unit as an OpenSpec change with the record generated inside the change
  directory. No record location, field, check, requirement, or code path changes.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `openspec validate --all` passes (27 items; the long-requirement warnings belong to existing
  specs); `./gradlew verifyWorkspaceRecords verifyUnitLayout` passes; `spotlessApply` has run; the unit's planning files
  are staged; `python3 scripts/claim-scan.py` has run and its candidates are rows or class-level dismissals in
  `tasks.md`.
- **Known limitations** — the amendment's wording was shaped by an advisor consultation whose corrections this proposal
  has adopted (the pointer at the falsified sentence, the workspace-record/process-defect-record distinction, the "each
  archive" overstatement, the `skip_specs` framing of the declined alternative); the consultation is not a review round
  and its findings are recorded here rather than in a round file. The decision the amendment records is the owner's
  question answered once, not a re-derivation of ADR-0029's own analysis.

## Round 2 — proposal screen, after round 1's six findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0b236cde`; nothing committed; every unit file staged,
  and `spotlessApply` has run over all of them.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 1's fixes: the amendment is specified as recording the Decision's relocation as
  superseding the Context clause, with a pointer at that sentence, and the specification now cites
  `docs/adr/README.md`'s amendment convention in task 2.1's Done condition; the `skip_specs` justification is narrowed
  to the amendment convention's absence from the spec corpus with its own row, and `docs/adr/README.md` is named as the
  owner; three rows join the claims table (a unit may take another form, the Decision's survival statement, the
  amendment convention's owner); the layering is stated as "a unit is the unit of work and an OpenSpec change is one
  form it may take", dropping the "process-core" term that collided with ADR-0025; the dismissals gain the `cross-ref`
  class and a widened `count` reason.
- **Prior findings with their instances** — round 1's six, in `findings/round-1.md`.
- **Completed checks** — `openspec validate --all` passes; `./gradlew verifyWorkspaceRecords verifyUnitLayout` passes;
  `spotlessApply` has run after the last edit; `commit-hygiene --unique-rounds --expect record-unit-change-layering:1`
  and `--session-continuity` both pass.
- **Known limitations** — none newly; round 1's limitation on the advisor-shaped wording stands, with its findings
  recorded here.

## Round 3 — proposal screen, after the owner's scope decision

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0b236cde`; nothing committed; every unit file staged and
  formatted.
- **Packs** — none, for the same reason as round 1.
- **Scope change since round 2** — the owner asked whether the process would clearly treat the unit as its base unit of
  work. I proposed a second edit (`AGENTS.md`) plus a new `agent-skills` requirement; the advisor's consultation found
  the requirement duplicative of four existing owners and recommended the narrowed shape, and the owner chose the
  narrowing. The unit now carries two edits and keeps `skip_specs: true`.
- **Supplied artifacts** — the updated `proposal.md` and `tasks.md`: the second edit replaces `AGENTS.md:126-128` with
  the definition (a unit is the process's base unit of work, one branch intended for one pull request; the rules govern
  every unit whether or not it carries an OpenSpec change directory; "change" reads as "unit of work"), the `skip_specs`
  justification now names both edits and the ownership model, task 2.2 carries the replacement's Done condition, and
  four claims rows were added (the replaced rule's citation sweep, a rejected unit's non-shipping scenario, the existing
  owners of the oriented behavior, ADR-0025's ban on a second copy).
- **Prior findings with their instances** — rounds 1 and 2, in `findings/`.
- **Completed checks** — `openspec validate record-unit-change-layering` passes with `skip_specs`; `spotlessApply` ran
  after the last edit; the staged set is current; the claims table has been re-derived for the new rows and the claim
  scan's candidates all fall in the four dismissed classes.
- **Known limitations** — the ADR amendment is still unwritten by design; the owner's proposal-stage pass is the next
  step.

## Round 4 — proposal screen, after round 3's seven findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0b236cde`; nothing committed; every unit file staged and
  formatted.
- **Packs** — none, for the same reason as round 1.
- **Supplied artifacts** — round 3's fixes: the `AGENTS.md` definition now states that an OpenSpec change is one form
  and that every other form the section names is equally a unit, so the replaced paragraph's enumeration survives as a
  pointer rather than a second copy, and task 2.2's Done condition lists that fact and names the citation command with
  its paths and base; the Why now says the paragraph carries a scope sentence and a reading rule rather than only a
  reading rule; the citation row runs against `origin/main` and states its scope; the claims row that named an absent
  "commit-assignment requirement" now names `A pre-commit guard blocks a mechanically defective staged set`;
  "requirement" in the scope bullet is now "capability-spec requirement"; and the dismissal reasons are stated by the
  class the scan emits — a task or review-round numeral or a reported total; a role, artifact, or root document named in
  prose.
- **Prior findings with their instances** — rounds 1–3, in `findings/`.
- **Completed checks** — `openspec validate record-unit-change-layering` passes with `skip_specs`; `spotlessApply` ran
  after the last edit; `commit-hygiene --unique-rounds --expect record-unit-change-layering:3` and
  `--session-continuity` pass; the claim scan's candidates all fall in the four dismissed classes.
- **Known limitations** — neither edit is written yet; the owner's proposal-stage pass is the next step.

## Round 5 — implementation screen

- **Unit, tree, branch, base** — `record-unit-change-layering`; tree `/Users/afanasyev/Development/axon-showcase` (the
  primary checkout); branch of the same name from `origin/main` `0b236cde`; the owner approved the proposal and the two
  edits are written; nothing committed; every unit file staged.
- **Packs** — none: the implementation edits `AGENTS.md` prose, one ADR, and the unit's own records. No JVM code, test,
  Gradle build decision, or deployment is touched.
- **The implementation** — (1) `AGENTS.md:126-129`: the "OpenSpec Workflow Agreement" paragraph is replaced by the
  definition (a unit is the process's base unit of work, one branch intended for one pull request; an OpenSpec change is
  one form and every other form the section names is equally a unit; the rules govern a unit whether or not it carries a
  change; "change" reads as "unit of work"), +3/−2 lines, net +1; (2)
  `docs/adr/0029-unit-state-lives-outside-the-change-plan.md` gains the dated `## Amendment` — the layering, the
  supersession of the Context clause with a pointer at it, and the declined alternative — with Status unchanged; (3)
  `docs/units/record-unit-change-layering/process-defects.md` records two observed process defects; tasks 1.1, 1.2, 1.3,
  2.1, 2.2, and 2.3 are ticked.
- **Prior findings with their instances** — rounds 1–4, in `findings/`.
- **Completed checks** — `openspec validate --all` 27 items;
  `spotlessCheck verifyWorkspaceRecords verifyUnitLayout verifyUniqueReviewRounds verifyFindingInstances` exit 0 after
  the last edit; the citation sweep returns `AGENTS.md` in the working tree plus the two `tasks.md` lines that quote the
  command, and only the replaced paragraph at `origin/main`; `git diff --numstat AGENTS.md` reports `+3/−2`; the claims
  table was re-checked against the written text.
- **Known limitations** — the amendment's claims are checked against the repository, not against a run: nothing in the
  build executes an ADR. The definition's effect on future classification is a hypothesis (a later misclassification
  would be its evidence), as the consultation recorded.

## Round 6 — implementation screen, after round 5's four findings

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0b236cde`; nothing committed; every changed file staged
  and formatted.
- **Packs** — none, for the same reason as round 5.
- **Supplied artifacts** — round 5's fixes: the amendment now grounds the record's earliness in "before the unit's first
  content write", the fact ADR-0029's Decision states, instead of "before any change artifact", which a continued unit
  falsifies; the claims row for that fact now also cites ADR-0029's Decision; the hand-over's citation result states the
  working tree's three hits and the base revision's one; the tick list names all six ticked tasks; and the dismissal
  classes are restated as the properties the scan tests — a numeral from any identifier or a command's reported output;
  a name, identifier, or heading locating a repository artifact, binding, or record.
- **Prior findings with their instances** — rounds 1–5, in `findings/`.
- **Completed checks** — `openspec validate --all` 27 items;
  `spotlessCheck verifyWorkspaceRecords verifyUnitLayout verifyUniqueReviewRounds verifyFindingInstances` exit 0 after
  the last edit; `--unique-rounds --expect record-unit-change-layering:5` and `--session-continuity` pass; the claim
  scan's candidates all fall in the four dismissed classes.
- **Known limitations** — the definition's effect on future classification remains a hypothesis; nothing exercises an
  ADR in the build.

## Round 7 — implementation screen, after round 6's limitation

- **Unit, tree, branch, base** — unchanged; base `origin/main` `0b236cde`; nothing committed; every changed file staged
  and formatted.
- **Packs** — none, for the same reason as round 5.
- **Supplied artifacts** — round 6's one limitation, acted on: the declined alternative no longer says the record would
  be generated "by a project-local schema". That phrase presupposed an OpenSpec CLI capability the repository does not
  evidence (it exists only in a scratch probe outside the tree), and a durable artifact may assert only what the
  repository can evidence; the alternative is now stated as "with the record carried inside the change directory", which
  keeps the co-location being declined and drops the un-evidenced mechanism.
- **Prior findings with their instances** — rounds 1–6, in `findings/`.
- **Completed checks** — `spotlessApply` ran after the edit;
  `commit-hygiene --unique-rounds --expect record-unit-change-layering:6` and `--session-continuity` pass; the amendment
  remains a pure append with its Status unchanged.
