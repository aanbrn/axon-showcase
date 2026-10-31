# Process defects — clarify-python-ide-activation

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### clarify-python-ide-activation#1 — The IDE-setup hand-back advised a reopening as the activation step, and it did not activate the module

- **Recorded** — 2026-10-31, by the writing session (`ses_e6f985602ffeUDJ4DtV0zmSDfc`), after the owner's correction of
  the episode's order.
- **Stage** — implementation — applying standalone IDE setup; no OpenSpec change existed for that task.
- **Expected** — after `scripts/setup-idea.sh` reports `Python: CONFIGURED`, the persisted module registration reaches
  the IDE's live project model through the activation step the guidance names (File → Reload All from Disk), and
  reopening the project is the equivalent the same guidance offers.
- **Observed** — with the project already open, the first setup run wrote the registration; a Reload All from Disk did
  not add the module to the live model; on the IDE's exit the file was rewritten from that model and the entry dropped
  (44 → 43 module entries); a second run with the IDE closed restored the entry and installed the plugins; reopening the
  project still showed 82 modules with none covering `scripts/`; only a further Reload All from Disk activated it (83
  modules, `scripts` of type `PYTHON_MODULE`).
- **Evidence** — the script's own outcome lines (`Python: CONFIGURED — wrote module and registration`, then
  `Python: CONFIGURED — wrote registration`); the module count in `.idea/modules.xml` before and after the IDE's exit
  (44 → 43) and after the second run (44); the live model read through the IDE API (82 modules, no content root ending
  in `/scripts`; then 83, with `scripts [PYTHON_MODULE]`). Owner-reported: the order of the two Reload attempts and the
  reopen; the first Reload's failure was not reproduced by the agent independently.
- **Consequence** — two extra owner actions (closing and reopening the IDE), one agent statement that reopening was
  enough, and one Reload that could not have helped; the module is active now.
- **Knowledge boundary** — observed the disk states and the live-model counts after the reopen and after the successful
  Reload. Did not isolate why the first Reload and the reopen failed (import timing, model persistence, or another
  mechanism), and did not test restart-only activation of the merged settings.

### clarify-python-ide-activation#2 — The planning pass declared `skip_specs` from a name search, and the behavior is specified under another capability

- **Recorded** — 2026-10-31, by the writing session (`ses_e6f985602ffeUDJ4DtV0zmSDfc`), during the proposal stage.
- **Stage** — proposal.
- **Expected** — before declaring a correction `skip_specs`, search the spec corpus for the behavior's name **and** for
  the behavior itself, since an owning requirement need not name the file or skill the change edits.
- **Observed** — the agent searched the corpus for `setup-idea` only (no hits) and told the owner the correction owed no
  delta; `showcase/quality/ide-config` owns the behavior without naming the skill.
- **Evidence** — `grep -rln "setup-idea" openspec/specs/` returns no file;
  `openspec/specs/showcase/quality/ide-config/spec.md` states the activation behavior at `:54` and `:84` ("takes effect
  on its next reload (File → Reload All from Disk) or restart") and owns the Python module at `:127–134`.
- **Consequence** — corrected in the follow-up consultation before any implementation; the unit now owes a `MODIFIED`
  delta on `showcase/quality/ide-config`.
- **Knowledge boundary** — observed the grep's empty result and the spec's clauses. Did not check whether another
  capability also names module activation.

### clarify-python-ide-activation#3 — The probe's activation flow did not reproduce in a tree whose `.idea` had been deleted

- **Recorded** — 2026-10-31, by the writing session (`ses_e6f985602ffeUDJ4DtV0zmSDfc`), at the manual-review stage,
  after the owner ran the experiment and reported its outcome.
- **Stage** — review (the owner's manual pass).
- **Expected** — one setup run followed by File → Reload All from Disk activates the module, as the probe recorded and
  as the unit's spec delta, its closing block, and its guidance stated.
- **Observed** — in the unit's tree the owner closed the IDEA window, deleted `.idea/`, reopened the folder, ran
  `scripts/setup-idea.sh`, and chose File → Reload All from Disk; the live project model still held 82 modules and none
  whose content root is `scripts/`. `.idea/modules.xml` listed exactly one module — the `scripts` one.
- **Evidence** — read through the IDE API: `ModuleManager` 82 modules, no module named `scripts`, no content root ending
  in `/scripts`; `cat .idea/modules.xml` shows the single `scripts` entry; the IDE log records `modules.xml` opened at
  15:37:50, 15:38:03 and 15:38:13 with no module added and no load error.
- **Consequence** — the unit's premise was falsified; the delta, the closing block and the guidance were rewritten, and
  the manual pass was suspended.
- **Knowledge boundary** — the mechanism and the working flow were established later, in disposable worktrees, and are
  recorded in the decision record; the reason the probe's own project behaved differently is not established.

### clarify-python-ide-activation#4 — The activation contract was designed twice from unverified mechanisms

- **Recorded** — 2026-10-31, by the writing session (`ses_e6f985602ffeUDJ4DtV0zmSDfc`), during the manual-review stage.
- **Stage** — review.
- **Expected** — a contract about IntelliJ's activation of a generated module rests on a measured flow in the state a
  fresh tree gets, and the spec delta, the guidance, and the committed control state it.
- **Observed** — the unit first wrote the probe's reload verdict into the delta, the closing block, the guidance, and a
  test; the owner's experiment falsified it. A second design (a one-time manual import through Project Structure) was
  prepared with the advisor on the same unverified footing and was rejected by the owner as a product flow. A third
  (flipping the project to local module storage) was measured to cost five steps per tree and to drop the entry during
  migration. Each design change rewrote the same artifacts before the working flow — setup with the project closed, then
  open — was measured in both starting states.
- **Evidence** — #3 and the decision record's "Reopened — 2026-10-31" section; the ledger rows of the review rounds that
  followed each rewrite.
- **Consequence** — three rewrites of the delta, the block, the guidance, and the tests, and a review loop restarted
  from the top; the artifacts now state the measured procedure.
- **Knowledge boundary** — observed the rewrites and their causes in this session; did not measure how much of the work
  a single up-front measurement of both starting states would have saved.

## Resume checkpoint

- **Time and revision** — 2026-10-31, at base and HEAD `fe3121795c0069a603e37db35227639bb6b1a86e`, with the unit's work
  uncommitted in its tree.
- **Workspace** —
  `unit=clarify-python-ide-activation; path=/Users/afanasyev/Development/axon-showcase-clarify-python-ide-activation; branch=clarify-python-ide-activation; writer=ses_e6f985602ffeUDJ4DtV0zmSDfc`.
- **Stage** — the review gate is complete through the manual-pass request: the quick loop closes with a clean round, the
  routed deep review ran and its findings are fixed, and the gate re-ran green on the current tree.
- **Next step** — the owner's manual review pass; then the ship sequence on their approval.
- **Change set** — staged: the delta, the proposal, the design, the tasks, the two records, the `setup-idea` skill, the
  setup script's closing section, its test, `README.md`, `AGENTS.md`, and the ledger rows.
- **Blockers and open decisions** — none; the routing decision and the deep review's outcome are recorded on tasks
  7.2/7.3.
- **Pending decision** — the owner's approval of the implementation (the manual pass).
- **Findings no record holds** — none; the closing round reported nothing.
- **Operations in flight** — none.
