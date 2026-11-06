# Review hand-over — adopt-builtin-ide-mcp-server

One section per quick-review invocation, written before the caller waits on it, so a stalled session is recovered from
this record rather than from the session — plus one section per recorded consultation, which is not a review round.

## Round 1 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `d420246cea8bdec1a9e0b57dcd8f852eb0131236`; `HEAD` at the same commit
  (nothing committed yet).
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — `openspec/changes/adopt-builtin-ide-mcp-server/proposal.md`, `design.md` (with the claims
  table and the per-kind dismissals), `tasks.md`, `.openspec.yaml`, the unit record, and the four current target files
  the change will edit: `AGENTS.md` (its "IDE inspections (optional)" and "Tooling-setup skill and command" bullets),
  `.opencode/skills/setup-agent-tools/SKILL.md`, `.opencode/commands/setup-agent-tools.md`, and `docs/ideas.md`.
- **Scope** — proposal-stage screen: do the proposal, design, and tasks cohere with one another and with the current
  files; does every behavior the change intends to ship have a task; do the referenced paths and tools exist; is the
  claims table complete against the facts the artifacts state; does the prose meet the Structured Writing Standard. No
  implementation exists yet, so there is no diff to screen.
- **Prior findings** — none; this is the unit's first round.
- **Completed checks** — `python3 scripts/claim-scan.py --base origin/main` (candidates resolved into the table and the
  dismissals); `./gradlew spotlessApply` and `./gradlew spotlessCheck` pass; `openspec validate --all` passes (27
  items); the worktree preparation reported the Python stage `CONFIGURED`.
- **Known limitations** — the OpenCode-side wiring of the built-in server is not verified in this unit: it needs the
  owner's global config and an OpenCode restart, so the artifacts describe the procedure and the IDE-side facts only.
  The IDE-settings wording is kept at the level this machine's plugin bundle establishes, not at a screenshot-verified
  menu path.
- **Subject packs** — none; the change edits guidance and a skill/command definition, with no JVM code, test tier,
  Gradle build decision, or deployment surface.
- **Deadline** — 20 minutes from this record's write.

## Round 2 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `d420246cea8bdec1a9e0b57dcd8f852eb0131236`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the same planning artifacts as round 1 (the staged tree), plus round 1's instances at
  `docs/units/adopt-builtin-ide-mcp-server/findings/round-1.md`.
- **Scope** — proposal-stage re-screen of the fixed artifacts against round 1's eleven instances, plus the same
  coherence, task-coverage, claims-table, and Structured Writing checks.
- **Prior findings** — all eleven of round 1, with the fixes applied: (1) task 2.6 offers the switch and 2.4 asks the
  narrower question; (2) task 4.1 re-parks the JetBrains Air option under the same dated section; (3) task 2.1 now
  rewrites the skill's frontmatter description; (4) the sweep uses `git grep -ni` over the tracked set; (5) `.idea/` is
  out of row 13 and out of task 4.2; (6) task 2.3 and D3 name the `mcp.ide.script.toolset.enabled` gate and the settings
  page; (7) row 14 sources the dialog and the Brave Mode confirmations; (8) the stale-URL cause is a hypothesis in both
  artifacts; (9) row 12 is re-sourced on the name-grep and the three `mcp` passages; (10) tasks 6.1 and 6.3 name
  `python3 scripts/commit-hygiene.py` and the unit; (11) the five-space code span is fixed.
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` pass after the fixes;
  `python3 scripts/commit-hygiene.py --unique-rounds --expect adopt-builtin-ide-mcp-server:1` and `--session-continuity`
  pass.
- **Known limitations** — as in round 1: the OpenCode-side wiring and the exact settings-widget path are not verified
  here; the artifacts stay at the level the installed plugin bundle establishes.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 3 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `d420246cea8bdec1a9e0b57dcd8f852eb0131236`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records, and both prior rounds' instances
  (`findings/round-1.md`, `findings/round-2.md`), plus the four current target files.
- **Scope** — proposal-stage re-screen against round 2's six instances; same coherence, task-coverage, claims-table, and
  Structured Writing checks.
- **Prior findings** — round 2's six, with the fixes applied: (1) row 13 now excludes the change's own directory from
  its `git grep` and reproduces five files, while task 4.2 has the run's output recorded on its `Done` note instead of
  predicting line by line; (2) task 2.3 and D3 name the registry key (the Registry dialog) and the settings page as
  separate surfaces; (3) task 2.6 asks the file to state which server the guidance selects and that both may be
  connected; (4) the five-space code span is fixed and every inline code span in the changed files was re-swept for
  whitespace runs; (5) the proposal's skill bullet is split into sub-bullets; (6) round 1's instance file anchors tasks
  and rows by id instead of line numbers.
- **Completed checks** — at the time of writing: the code-span sweep was clean, the cited `git grep` reproduced five
  files, and `--unique-rounds --expect adopt-builtin-ide-mcp-server:2` and `--session-continuity` passed. The formatter
  check did not: this section's own append left the file unformatted, which round 3 reported, and the tree is formatted
  afterwards.
- **Known limitations** — as in rounds 1–2: the OpenCode-side wiring and the exact settings-widget path are unverified
  here.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 4 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `d420246cea8bdec1a9e0b57dcd8f852eb0131236`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records, all three prior rounds' instance files, and
  the four current target files.
- **Scope** — proposal-stage re-screen against round 3's two instances; same coherence, task-coverage, claims-table, and
  Structured Writing checks.
- **Prior findings** — round 3's two, both `claim-unreproducible` on coordinates the review could not reproduce: the
  round-3 section's formatter line (rewritten to state the run that failed and that the tree was formatted afterwards)
  and the defect record's row citation (`design.md:97` → the claims table's row 12). A second process-defect record
  captures the false-green mechanism.
- **Completed checks** — `--unique-rounds --expect adopt-builtin-ide-mcp-server:3` and `--session-continuity` passed
  before this section was appended; the tree is formatted and `spotlessCheck` re-run after it, so the section's own
  append cannot leave the file unformatted.
- **Known limitations** — as before: the OpenCode-side wiring and the exact settings-widget path are unverified here.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 5 — proposal stage, widened scope

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `d420246cea8bdec1a9e0b57dcd8f852eb0131236`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the widened `proposal.md`, `design.md` (D1–D7, claims rows 1–19, dismissals), `tasks.md`
  (sections 0–7), `.openspec.yaml`, the unit records, and the five current target files: `AGENTS.md` (its "IDE
  inspections (optional)" and "Tooling-setup skill and command" bullets), `README.md` (`#### Tooling MCP Servers` and
  the `/setup-agent-tools` slash-command row), `.opencode/skills/setup-agent-tools/SKILL.md`,
  `.opencode/commands/setup-agent-tools.md`, `docs/ideas.md`.
- **What changed since round 4** — on the owner's instruction the proposal now covers both hosting paths (client config
  and Air/ACP, chosen by the reader at setup) and adds a README entry plus the rewrite of the `AGENTS.md` rule that
  previously kept the README silent; `docs/ideas.md` now loses the whole entry rather than keeping its Air half.
  `design.md` gained D5 (rewritten), D7, new non-goals, and claims rows 15–19; `tasks.md` was restructured into sections
  1–7.
- **Scope** — full proposal-stage screen of the widened artifacts: coherence across proposal/design/tasks and the five
  targets, task coverage of every stated behaviour, existence of every referenced path/tool/flag, claims-table and
  dismissal completeness, the Structured Writing Standard, and the 120-character and code-span checks.
- **Prior findings** — rounds 1–4 are recorded in `findings/` and closed; the closing round 4 was clean. This round
  screens the widened text, so a recurrence of an earlier class is possible and should be reported as a repeat.
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` were run after this section was
  appended; `openspec validate --all` passes (27 items); `--unique-rounds` and `--session-continuity` pass for round 4.
- **Known limitations** — the OpenCode-side wiring and the Air session are not exercised in this unit yet: task 6.4 runs
  the live check with the owner during implementation, and until then D7's `use_custom_mcp` semantics stay a stated
  hypothesis.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 6 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `d420246cea8bdec1a9e0b57dcd8f852eb0131236`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (all five prior rounds' instance files), and
  the five current target files.
- **Scope** — proposal-stage re-screen against round 5's three instances; same coherence, task-coverage, claims-table,
  and Structured Writing checks over the widened scope.
- **Prior findings** — round 5's three, with the fixes applied: (1) the four location statements are now tasked — the
  skill introduction in 3.1, the `AGENTS.md` bullet in 1.2, the command's confirmation trigger in 4.1, and the closing
  verify/hand-back step as new task 3.8; (2) task 3.4's combination is provisional, task 6.4 rewrites it from the
  observation, and D7 states the provisional default for the "both" choice; (3) claims row 20 sources what the Air
  fields are parsed into and read by, and D7 hedges `use_idea_mcp`, `use_custom_mcp`, and `idea_mcp_allowed_tools`
  alike, with the proposal bullet matched to it.
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` were run after this section was
  appended; the code-span sweep is clean; `--unique-rounds --expect adopt-builtin-ide-mcp-server:5` and
  `--session-continuity` pass.
- **Known limitations** — the Air session and the OpenCode client wiring are unexercised until task 6.4; D7's field
  semantics stay marked unverified until then.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 7 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `d420246cea8bdec1a9e0b57dcd8f852eb0131236`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (all six prior rounds' instance files), and
  the five current target files.
- **Scope** — proposal-stage re-screen against round 6's two instances; same coherence, task-coverage, claims-table, and
  Structured Writing checks.
- **Prior findings** — round 6's two, with the fixes applied: (1) D7 now accounts for the file-level
  `default_mcp_settings` — the skill reads and reports them, asks before changing them, and the live check records them
  so a doubled tool list is attributable; tasks 3.4 and 6.4 read them; (2) task 4.1 now widens the command's
  verification and completion criteria per surface (OpenCode and Air), and names one confirmation decision point
  covering both files.
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` were run after this section was
  appended; `--unique-rounds --expect adopt-builtin-ide-mcp-server:6` and `--session-continuity` pass.
- **Known limitations** — the Air session and the OpenCode client wiring are unexercised until task 6.4.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 8 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `d420246cea8bdec1a9e0b57dcd8f852eb0131236`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (all seven prior rounds' instance files), and
  the five current target files.
- **Scope** — proposal-stage re-screen against round 7's two instances; same coherence, task-coverage, claims-table, and
  Structured Writing checks.
- **Prior findings** — round 7's two, with the fixes applied: (1) D7 and task 3.4 now state one contradiction path — the
  skill reports the file's `default_mcp_settings`, offers to set them to match the reader's choice, and leaves them as
  found if declined; task 3.4's verify covers the confirm and decline branches; (2) D7's scope claim is weakened to the
  reading row 20 supports ("may inject … for a registered agent").
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` were run after this section was
  appended; `--unique-rounds --expect adopt-builtin-ide-mcp-server:7` and `--session-continuity` pass.
- **Known limitations** — the Air session and the OpenCode client wiring are unexercised until task 6.4.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 9 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (all eight prior rounds' instance files), and
  the five current target files.
- **Scope** — proposal-stage re-screen of the two changes made after the clean round 8: task 6.4 now records the owner's
  pre-confirmation of the ACP write and the Air launch, new task 6.5 carries the owner's instruction to switch this
  machine's global config in this unit, and the proposal's Impact states that delivery. Same coherence, task-coverage,
  claims-table, and Structured Writing checks.
- **Prior findings** — round 8 was clean; these are additions the owner asked for, not fixes.
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` were run after this section was
  inserted; `--unique-rounds --expect adopt-builtin-ide-mcp-server:8` and `--session-continuity` pass.
- **Known limitations** — the Air session and the client switch are unexercised until tasks 6.4 and 6.5.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 10 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (all nine prior rounds' instance files), and
  the five current target files.
- **Scope** — re-screen of round 9's two fixes: the design's Non-goals and Verification now cover both per-user writes
  and the client switch's own verification, the unit record carries the owner's two instructions, and the hand-over
  header covers the consultation records.
- **Prior findings** — round 9's two, both applied.
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` were run after this section was
  inserted; `--unique-rounds --expect adopt-builtin-ide-mcp-server:9` and `--session-continuity` pass.
- **Known limitations** — the Air session and the client switch are unexercised until tasks 6.4 and 6.5.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 11 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (all ten prior rounds' instance files), and
  the five current target files.
- **Scope** — re-screen of round 10's finding: the seven instance files were re-shaped to the store's one-token location
  form, and the default guard mode now passes over the staged set.
- **Prior findings** — round 10's `missing-task`, fixed; the guard invocation joins this record's completed checks.
- **Completed checks** — `./gradlew spotlessApply` and `./gradlew spotlessCheck` were run after this section was
  inserted; `python3 scripts/commit-hygiene.py` (the guard's default mode over the staged set) exits 0;
  `--unique-rounds --expect adopt-builtin-ide-mcp-server:10` and `--session-continuity` pass.
- **Known limitations** — the Air session and the client switch are unexercised until tasks 6.4 and 6.5.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 12 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (eleven prior rounds' instance files plus the
  consultation records), and the five current target files.
- **Scope** — re-screen of the advisor's three confirmed problems, applied: the live-check tasks reordered and
  strengthened, the version boundary added as D8, the upstream disposition as D9 and task 6.7, and the proposal's What
  Changes updated.
- **Prior findings** — none open; the advisor's three corrections are recorded as consultation C3.
- **Completed checks** — `./gradlew spotlessApply`/`spotlessCheck` were run after this section was inserted;
  `python3 scripts/commit-hygiene.py` exits 0; `--unique-rounds --expect adopt-builtin-ide-mcp-server:11` and
  `--session-continuity` pass.
- **Known limitations** — the Air session and the client switch are unexercised until tasks 6.4–6.7.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 13 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (twelve prior rounds' instance files plus the
  consultations), and the five current target files.
- **Scope** — re-screen of round 12's three instances: the six task-number citations follow the new order, claims rows
  21 and 22 source the IDE build id and the tracker coordinate, task 6.7 resolves the owning project before searching,
  and the five-space code span is fixed with every span re-swept.
- **Prior findings** — round 12's three, applied.
- **Completed checks** — `./gradlew spotlessApply`/`spotlessCheck` were run after this section was inserted; the
  code-span sweep is clean; `python3 scripts/commit-hygiene.py` exits 0;
  `--unique-rounds --expect adopt-builtin-ide-mcp-server:12` and `--session-continuity` pass.
- **Known limitations** — the Air session and the client switch are unexercised until tasks 6.4–6.7.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 14 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (thirteen prior rounds' instance files plus
  the consultations), and the five current target files.
- **Scope** — re-screen of the advisor's round-2 correction: the Air acceptance set is now three cases, including Air
  with the `intellij` entry disabled, and the finalize condition covers every case.
- **Prior findings** — round 13 was clean; this round screens the advisor's correction (C4).
- **Completed checks** — `./gradlew spotlessApply`/`spotlessCheck` were run after this section was inserted;
  `python3 scripts/commit-hygiene.py` exits 0; `--unique-rounds --expect adopt-builtin-ide-mcp-server:13` and
  `--session-continuity` pass.
- **Known limitations** — the three live cases are unexercised until tasks 6.4–6.6.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 15 — proposal stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the tree's own files staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the staged planning artifacts and unit records (fourteen prior rounds' instance files plus
  the consultations), and the five current target files.
- **Scope** — re-screen of round 14's single finding: D7 now states the Air-alone case as the disabled entry, matching
  the Verification and task 6.5, and the Verification bullet no longer frames the client case as an Air run.
- **Prior findings** — round 14's one, applied.
- **Completed checks** — `./gradlew spotlessApply`/`spotlessCheck` were run after this section was inserted;
  `python3 scripts/commit-hygiene.py` exits 0; `--unique-rounds --expect adopt-builtin-ide-mcp-server:14` and
  `--session-continuity` pass.
- **Known limitations** — the three live cases are unexercised until tasks 6.4–6.6.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 16 — implementation stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the whole unit staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the change dir (`proposal.md`, `design.md` D1–D9 with claims rows 1–23, `tasks.md`), the unit
  records, and the implementation diff against `origin/main`: `AGENTS.md` (two bullets), `README.md` (the server list,
  the "one to set up first" sentence, the `/setup-agent-tools` row), `.opencode/skills/setup-agent-tools/SKILL.md`,
  `.opencode/commands/setup-agent-tools.md`, `docs/ideas.md` (one entry removed, one parked).
- **Scope** — implementation-stage screen: does every behavior the plan states appear in the diff and in the artifact
  that owns it; do the tasks' Done notes match the work; the claims table against the implementation's new statements
  (rows 23); the README/`AGENTS.md` boundary; the skill's procedure against the tasks; Structured Writing; the 120-char
  and code-span checks.
- **Prior findings** — round 15 was clean; the advisor's three corrections (C3–C5) are applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
  BUILD SUCCESSFUL; `openspec validate --all` 27 passed; `python3 scripts/commit-hygiene.py` exits 0; live checks 6.4
  and 6.5 ran with the owner (client: connected plus `get_project_modules`; Air: both configurations, one agent, no
  doubling).
- **Known limitations** — the Air observation records one machine and one IDE build; the skill states the tested
  versions for that reason.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 17 — implementation stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the whole unit staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the change dir with claims rows 1–24, the unit records, and the same six implementation files
  as round 16.
- **Scope** — re-screen of round 16's five instances: the reproduction string, the flag generalisation, the provisional
  wording, the unit record's state, and task 5.2's Done note and expected set.
- **Prior findings** — round 16's five, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass after the fixes; `python3 scripts/commit-hygiene.py` exits
  0; `--unique-rounds --expect adopt-builtin-ide-mcp-server:16` and `--session-continuity` pass.
- **Known limitations** — one machine and one IDE build back the live observations; the skill states the tested
  versions.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 18 — implementation stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `0b236cde6292f74ec756db22cf89eefd342a6d3b`; `HEAD` at the same
  commit, the whole unit staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the change dir with claims rows 1–24, the unit records, and the same six implementation files
  as rounds 16–17.
- **Scope** — re-screen of round 17's two instances: the Air case's mechanism aligned across D7, Verification and task
  6.5, and the restart sentence split into its two facts.
- **Prior findings** — round 17's two, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass after the fixes; `python3 scripts/commit-hygiene.py` exits
  0; `--unique-rounds --expect adopt-builtin-ide-mcp-server:17` and `--session-continuity` pass.
- **Known limitations** — one machine and one IDE build back the live observations.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 19 — implementation stage, after the base refresh

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `f7597477becde888aae80dfdb850abf243841bbd`; `HEAD` at the same
  commit, the whole unit staged and uncommitted. The branch was refreshed twice after round 18 (`#850`, `#851`), and the
  refresh touched `docs/ideas.md`, so this round screens the reconciled tree as well as the fixes.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the change dir with claims rows 1–24, the unit records, and the six implementation files.
- **Scope** — the advisor's two implementation findings (C6) and the refreshed base: the switch moved from the skippable
  fallback step into the IDE step, the Air step reuses only a matching registration and offers to align a differing one,
  and `docs/ideas.md` now carries the base's removals beside this unit's parked entry.
- **Prior findings** — round 18 clean; C6's two problems applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `python3 scripts/commit-hygiene.py` exits 0;
  `--review-ready` reports the base `f7597477` contained in `HEAD` with the index current.
- **Known limitations** — one machine and one IDE build back the live observations.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 20 — implementation stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `f7597477becde888aae80dfdb850abf243841bbd`; `HEAD` at the same
  commit, the whole unit staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the change dir with claims rows 1–24, the unit records, and the six implementation files.
- **Scope** — round 19's single finding: the IDE step now asks the fallback question before offering the removal, so the
  offer never contradicts the fallback step and never churns a bridge the reader needs; task 3.6 matches.
- **Prior findings** — round 19's one, applied.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `python3 scripts/commit-hygiene.py` exits 0;
  `--unique-rounds --expect adopt-builtin-ide-mcp-server:19` and `--session-continuity` pass.
- **Known limitations** — one machine and one IDE build back the live observations.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 21 — implementation stage

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `f7597477becde888aae80dfdb850abf243841bbd`; `HEAD` at the same
  commit, the whole unit staged and uncommitted.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the change dir with claims rows 1–24, the unit records, and the six implementation files.
- **Scope** — round 20's finding: the two-step block was rewritten once from its decision model — ask the fallback need
  unconditionally, run the fallback step only on a yes, offer removal only on a no — instead of patching a clause again.
- **Prior findings** — round 20's one, applied by rewriting the block.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `python3 scripts/commit-hygiene.py` exits 0;
  `--unique-rounds --expect adopt-builtin-ide-mcp-server:20` and `--session-continuity` pass.
- **Known limitations** — one machine and one IDE build back the live observations.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Round 22 — implementation stage, after the second base refresh

- **Unit** — `adopt-builtin-ide-mcp-server`; change dir `openspec/changes/adopt-builtin-ide-mcp-server/`; record
  `docs/units/adopt-builtin-ide-mcp-server/unit.md`.
- **Base and current revision** — `origin/main` at `3dac4109cf301950e6452bdc027e170ad9a19883`; `HEAD` at the same
  commit, the whole unit staged and uncommitted. The branch was fast-forwarded a third time (`#852`), which changed
  `AGENTS.md` — one of this unit's edited files — so this round re-screens the merged result.
- **Workspace** — tree `/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server`, branch
  `adopt-builtin-ide-mcp-server`.
- **Supplied artifacts** — the change dir with claims rows 1–24, the unit records, and the six implementation files.
- **Scope** — the post-refresh tree: the base's "OpenSpec Workflow Agreement" introduction now sits beside this unit's
  two edited bullets, and `docs/ideas.md` reconciles both sides again.
- **Prior findings** — round 21 clean; the advisor's C7 agreement records a skip recommendation for the deep review.
- **Completed checks** — `spotlessApply`/`spotlessCheck` pass; `python3 scripts/commit-hygiene.py` exits 0;
  `--review-ready` reports base `3dac4109` contained in `HEAD` with the index current.
- **Known limitations** — one machine and one IDE build back the live observations.
- **Subject packs** — none.
- **Deadline** — 20 minutes from this record's write.

## Consultations

Screens that are not review rounds, recorded here rather than in a round file.

### C1 — one integration path or both (technical advisor)

- **Question** — whether the change should ship the client-config IDE entry alone or also register OpenCode as an ACP
  agent in JetBrains Air, and how `/setup-agent-tools` should decide.
- **Recommendation** — support both workflows with an explicit user choice rather than an unconditional setup or a
  silent default; verify coexistence and Air's existing-agent discovery before writing the procedure; replace the ACP
  non-goal with tasks; remove the combined idea rather than re-parking ACP; keep the narrow Steroid fallback and the
  README server-list omission at that time.
- **Owner's disposition** — accepted; the proposal was widened to both paths, and the README point was raised separately
  by the owner (C2).

### C2 — whether the README should name the agent tooling (technical advisor)

- **Question** — whether the README should name the IDE server among the tooling MCP servers, and in what shape.
- **Recommendation** — name it in the existing server list and sharpen the `/setup-agent-tools` row; rewrite the
  `AGENTS.md` restriction rather than adding an exception; keep tool counts, ports, registry keys, and JSON recipes out
  of the README; keep the Steroid fallback and the Air hosting instructions in the skill.
- **Owner's disposition** — accepted; the README entry, the command-row change, and the `AGENTS.md` rewrite are part of
  the amended proposal (tasks 1.3 and 2.1–2.2).

### C3 — proposal review to agreement (technical advisor, round 1)

- **Question** — should the proposal be implemented as written, or after which corrections, given the owner's
  review-to-agreement instruction.
- **Verdict** — agreement withheld pending three corrections: (1) the acceptance check cannot establish every hosting
  path it promises — it tests one Air configuration while the plan offers others, it needs the client entry task 6.5
  created, and it reads connection status instead of exercising an operation; (2) the portable procedure lacks a
  version/capability boundary — one build's schema becomes general guidance and an unknown release would silently
  inherit it; (3) excluding an upstream report contradicts `AGENTS.md`'s upstream-report rule, where a third-party
  defect is the reason to report rather than an exemption.
- **Disposition** — all three applied: tasks 6.4–6.7 now order the client switch first, exercise one harmless IDE
  operation per host and per configuration, stop finalization on a failure, and carry the upstream disposition; D8 and
  task 3.9 add the version boundary and discovery step; D9 replaces the upstream non-goal.

### C4 — proposal review to agreement (technical advisor, round 2)

- **Question** — whether round 1's three corrections are met, and the agreement verdict.
- **Verdict** — agreement withheld on one remaining point, the unresolved part of round 1's first problem: the Air
  acceptance case still ran only with the global `intellij` entry present, so a working operation could come through
  that entry rather than through Air's injection, and the Air-only promise in D7 was never established.
- **Disposition** — applied: D7 and the design's Verification now name three cases — the client path alone, Air with the
  `intellij` entry disabled for the test, and the reader's final configuration — and task 6.5 runs them with the same
  operation, restoring the owner's final config; task 6.6's stop condition covers every case.

### C5 — proposal review to agreement (technical advisor, round 3)

- **Question** — whether the Air-only acceptance case is now established, and the agreement verdict.
- **Verdict** — agreement to implement the proposal as written, subject to the owner's manual approval: the three cases
  are stated identically across D7, the Verification and task 6.5, the disabled-entry isolation is reversible and
  restored, and the stop-on-failure condition covers every case; the earlier corrections remain intact.
- **Withdrawal condition** — a required live case failing, or the plan weakening isolation, restoration, version
  qualification, or its stop-on-failure condition.
- **Disposition** — recorded; the next step is the owner's manual pass (task 7.2), and the agreement is neither
  implementation authorization nor delivery approval.

### C6 — implementation review to agreement (technical advisor, round 1)

- **Question** — is the implementation sound enough for the manual pass, and should the deep review run or be skipped.
- **Verdict** — not ready; run the focused deep review. Two confirmed problems: the offer to remove an existing
  `steroid` entry sat inside the skippable fallback step, so a reader adopting only the built-in server never met it;
  and an existing Air registration was reused "change nothing", so one whose flags contradict the chosen hosting mode
  would keep injection beside the new global entry. It also flagged that the tree had moved past the round-18 base
  without a re-screen.
- **Disposition** — both applied: the switch now sits in the IDE step, the Air step reuses only a matching registration
  and offers to align a differing one, the Notes say so, and tasks 3.4/3.6 match. The post-refresh tree and these fixes
  go through round 19; the deep review runs as recommended.

### C7 — implementation review to agreement (technical advisor, round 2)

- **Question** — whether the two implementation problems are resolved and whether the deep review should run.
- **Verdict** — agreement for the owner's manual pass. Both problems are resolved: the fallback question is asked
  unconditionally and the removal offer sits outside the skippable install step (all four reader states compose), and a
  differing Air registration is reported and aligned with confirmation. The advisor **reversed** its earlier run
  recommendation: the two concrete workflow gaps that motivated a deep pass are repaired and branch-checked, and the
  historical repeated-class signal alone does not establish another unresolved risk.
- **Withdrawal condition** — those branches regressing, a required hosting check failing, or the reviewed tree changing
  materially.
- **Disposition** — recorded; the deep-review decision is the owner's to confirm (the routing stays RECOMMENDED), and
  the manual pass is the next step.
