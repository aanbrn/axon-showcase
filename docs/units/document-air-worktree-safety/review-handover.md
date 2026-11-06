# Review hand-over — document-air-worktree-safety

One section per quick-review invocation, written before the caller waits on it, so a stalled session is recovered from
this record rather than from the session.

## Round 1 — proposal stage

- **Unit** — `document-air-worktree-safety`; change dir `openspec/changes/document-air-worktree-safety/`; record
  `docs/units/document-air-worktree-safety/unit.md`.
- **Base and current revision** — `origin/main` at `805f30ef` (the Air adoption, PR #853); `HEAD` at the same commit
  (nothing committed yet).
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-document-air-worktree-safety`, branch
  `document-air-worktree-safety`.
- **Supplied artifacts** — `proposal.md`, `design.md` (decisions D1–D4, the 11-row claims table, dismissals, non-goals,
  verification), `tasks.md` (sections 0–5), `.openspec.yaml`, the unit record, and the two targets the change will edit:
  `AGENTS.md`'s Workspace rule and `.opencode/skills/setup-agent-tools/SKILL.md`'s Air section; also
  `scripts/new-worktree.sh`, `scripts/commit-hygiene.py` and `docs/adr/0024-tree-write-right-is-not-session-bound.md` as
  the sources the claims rest on.
- **Scope** — proposal-stage screen: do the proposal, design, and tasks cohere; is the recorded rule placed where a
  reader acts on it; does the recovery procedure stay inside the process; do the claims reproduce; Structured Writing;
  code spans and the 120-character recipe. No implementation exists yet.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `openspec validate --all` reports 27 passed;
  `grep -rn "setup-agent-tools" openspec/specs/` returns no hits (the `skip_specs` evidence); `claim-scan.py` seeded the
  table.
- **Known limitations** — Air's worktree behaviour is recorded from the bundled plugin's own strings, not from a live
  session; task 1 runs the disposable exercise during implementation and its observation replaces any wording the
  exercise contradicts.
- **Subject packs** — none; this change edits guidance and a skill definition, with no JVM code, test tier, Gradle build
  decision, or deployment surface.
- **Deadline** — 20 minutes from this record's write.

## Round 2 — proposal stage

- **Unit** — `document-air-worktree-safety`; change dir `openspec/changes/document-air-worktree-safety/`; record
  `docs/units/document-air-worktree-safety/unit.md`.
- **Base and current revision** — `origin/main` at `805f30ef`; `HEAD` at the same commit, the unit staged and
  uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-document-air-worktree-safety`, branch
  `document-air-worktree-safety`.
- **Supplied artifacts** — the same planning artifacts as round 1 plus round 1's instances
  (`docs/units/document-air-worktree-safety/findings/round-1.md`), and the same targets and claim sources.
- **Scope** — re-screen of round 1's six instances: the tree statement made conditional (primary while free, otherwise
  the sibling worktree), the "nothing can see Air's tree" claim narrowed to "no record names it" with the guard's
  consequence, ADR-0024 rendered on its own terms, a claims row for PR #853's assignment and the "idea note" phrase
  dropped, D3's negative constraint added to task 3.1's verify, and the two packed sentences split into bullets and
  numbered steps.
- **Prior findings** — round 1's six, all applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `python3 scripts/commit-hygiene.py` exits 0;
  `--unique-rounds --expect document-air-worktree-safety:1` passes.
- **Known limitations** — Air's behaviour is still recorded from the bundled plugin's strings; the disposable exercise
  runs during implementation.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 3 — proposal stage

- **Unit** — `document-air-worktree-safety`; base `origin/main` at `805f30ef`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-document-air-worktree-safety`, branch
  `document-air-worktree-safety`.
- **Supplied artifacts** — the planning artifacts, rounds 1–2 instances, the targets and the claim sources.
- **Scope** — re-screen of round 2's three instances: the clone's guard is armed and asserted before the refusal is read
  (task 1.3 and the design's backstop bullet), task 3.2 mirrors the proposal's numbered recovery steps, and the
  proposal's guard sentence says what the guard does.
- **Prior findings** — round 2's three, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `commit-hygiene.py`, `--expect …:2` and
  `--session-continuity` exit 0.
- **Known limitations** — Air's behaviour is recorded from the bundled plugin's strings; the exercise runs during
  implementation.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 4 — proposal stage

- **Unit** — `document-air-worktree-safety`; base `origin/main` at `805f30ef`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-document-air-worktree-safety`, branch
  `document-air-worktree-safety`.
- **Supplied artifacts** — the planning artifacts, rounds 1–3 instances, the targets and the claim sources.
- **Scope** — the two corrections the technical advisor blocked agreement on: the proposal's guard sentence now states
  the missing-assignment check's conditional reach instead of an unconditional refusal, and exercise tasks 1.1–1.4 now
  commit inside Air's own tree, record index versus worktree state, arm the guard and a commit identity in the clone,
  assert the resolved hook file, and order the cleanup; `design.md`'s Verification and the proposal's Impact bullet were
  brought in line.
- **Prior findings** — round 3 clean; this round re-screens the corrected exercise and the changed sentences.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `commit-hygiene.py` exits 0.
- **Known limitations** — Air's behaviour is recorded from the bundled plugin's strings; the exercise itself runs during
  implementation.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 5 — implementation stage

- **Unit** — `document-air-worktree-safety`; base `origin/main` at `805f30ef`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-document-air-worktree-safety`, branch
  `document-air-worktree-safety`.
- **Supplied artifacts** — the implementation diff (`AGENTS.md`'s Workspace rule,
  `.opencode/skills/setup-agent-tools/SKILL.md`'s new Air section), the reconciled planning artifacts,
  `docs/decisions/2026-11-06-air-worktree-exercise.md` (the exercise's two runs and the guard check), `unit.md`,
  `review-handover.md`, `rounds.tsv`, and the round 1–4 findings.
- **Scope** — implementation-stage screen: does the shipped wording state only what the exercise observed, does the
  `AGENTS.md` bullet scope unit sessions and point at the skill, does the skill's Air section carry before-start rules,
  the recovery steps, and the ADR-0024 boundary, are tasks ticked where work is done, and does the record reconcile with
  the artifacts.
- **Prior findings** — rounds 3 and 4 clean; the exercise contradicted the proposal's copy premise, and the wording was
  reconciled (see the record).
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `commit-hygiene.py`, `--expect-clean`,
  `--session-continuity` and `--review-ready` pass; the root `check` is running.
- **Known limitations** — the exercise ran twice on one IDEA build and one project clone; the wording is run-bounded.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 6 — implementation stage

- **Unit** — `document-air-worktree-safety`; base `origin/main` at `805f30ef`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-document-air-worktree-safety`, branch
  `document-air-worktree-safety`.
- **Supplied artifacts** — the implementation diff, the planning artifacts,
  `docs/decisions/2026-11-06-air-worktree-exercise.md`, `unit.md`, `review-handover.md`, `rounds.tsv`, and the round 1–5
  findings.
- **Scope** — re-screen of round 5's three instances: the exercise record moved out of the unit directory to an admitted
  `docs/decisions/` home with every reference repointed; run 2's source state, diffs, sizes, and fresh-marker content
  now recorded; the skill's ADR sentence opens with ADR-0024's finding.
- **Prior findings** — round 5's three, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `--unit-layout`, and `commit-hygiene.py` pass; the root
  `check` is running again after the layout failure it exposed.
- **Known limitations** — the clone is deleted, so its byte counts and marker contents are quoted from the run-2
  observation rather than re-measured.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 7 — implementation stage

- **Unit** — `document-air-worktree-safety`; base `origin/main` at `805f30ef`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-document-air-worktree-safety`, branch
  `document-air-worktree-safety`.
- **Supplied artifacts** — the implementation diff, the planning artifacts,
  `docs/decisions/2026-11-06-air-worktree-exercise.md`, `unit.md`, this hand-over, `rounds.tsv`, and the round 1–6
  findings.
- **Scope** — re-screen of round 6's two instances: the decision record gained a `## Reopening condition` naming each
  clause's artifact and signal, and its `.gitattributes` figure now states the append's bytes (39 + 1 + 40 + 1 = 81, the
  size `wc -c` reported).
- **Round 6's second finding, verified rather than applied**: it proposed 80 bytes from the quoted marker line and its
  newline alone; the append was a blank line plus the line, and `wc -c` printed 81 both before the edit's deletion and
  in the clone's own status, so the figure stands and the arithmetic is now written down.
- **Prior findings** — round 6's first applied; its second refuted at the source of the figure.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `--unit-layout`, `--finding-instances`, `--unique-rounds`, and
  `commit-hygiene.py` pass; the root `check` passed (`BUILD SUCCESSFUL`, 411 actionable tasks) and
  `openspec validate --all` reports 27 passed; tasks `4.2` and `4.3` are ticked after the claims rows were re-read
  against the plugin bundle, the guard source, ADR-0024 and the archived PR #853 design.
- **Known limitations** — the clone is deleted, so the record's byte counts are quoted from the run-2 observation; the
  arithmetic is reproducible from the recorded content.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 8 — implementation stage

- **Unit** — `document-air-worktree-safety`; base `origin/main` at `805f30ef`; everything staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-document-air-worktree-safety`, branch
  `document-air-worktree-safety`.
- **Supplied artifacts** — the implementation diff, the planning artifacts,
  `docs/decisions/2026-11-06-air-worktree-exercise.md`, `unit.md`, this hand-over, `rounds.tsv`, and the round 1–7
  findings.
- **Scope** — re-screen of round 7's single instance: the record's intro said the probe measured the behaviour "on two
  disposable clones" while the body, the skill and this hand-over describe one clone and two runs; it now says "in two
  runs on a disposable clone".
- **Prior findings** — round 7's, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck`, `--unit-layout`, `--finding-instances`, `--unique-rounds` and
  `commit-hygiene.py` pass; the root `check` is running again over the final text.
- **Known limitations** — the clone is deleted; the record's byte counts and marker contents are quoted from the run-2
  observation.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Consultation round 5 — technical advisor, finished unit

Not a quick-review round; its findings are recorded here, never in a round file.

- **Scope** — the technical advisor reviewed the shipped `AGENTS.md` bullet, the skill's Air section, and the decision
  record before the owner's manual pass.
- **Finding (confirmed)** — the record drew conclusions beyond its observations: (a) the fresh-marker paragraph said
  "the copy is not merely lagging behind the IDE's change list", which opening/indexing the project does not establish;
  and (b) the first reopening clause treated a non-empty snapshot patch as the transfer signal, though a patch
  establishes capture, not transfer into the destination tree.
- **Fix** — the paragraph now says the fresh marker was absent and why Air captured no changes remains undetermined; the
  reopening clause requires the destination-tree observation and labels the patch an investigation trigger.
- **Agreement** — the advisor agreed on the corrected text (2026-11-06); his withdrawal condition is contradictory
  exercise evidence or a material change to the agreed rule or recovery procedure.
