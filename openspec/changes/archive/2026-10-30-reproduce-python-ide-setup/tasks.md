# Tasks

Workspace: unit=reproduce-python-ide-setup; path=/Users/afanasyev/Development/axon-showcase-reproduce-python-ide-setup;
branch=reproduce-python-ide-setup; writer=ses_e7545f286ffeYYnUnvdst4oiB0

## 1. Generate the Python module

- [x] 1.1 Add a `write_python_module(project_dir, sdk_name)` stage to `scripts/ensure-idea-settings.py` that writes
      `.idea/modules/scripts.iml`: module type `PYTHON_MODULE`, one `NewModuleRootManager` component, content root
      `file://$MODULE_DIR$/../../scripts`, one `<orderEntry type="jdk" jdkType="Python SDK">` with the given name, and
      `<orderEntry type="sourceFolder" forTests="false" />`. Verify with a unit test asserting the parsed elements of a
      fresh file in a temporary project directory.
- [x] 1.2 Register the module in `.idea/modules.xml` by upserting one `<module>` element keyed by its file path,
      creating the file with the standard `ProjectModuleManager` skeleton when it is absent. Verify with a test that
      runs the stage over a tree with no `modules.xml` and over one that already lists two other modules, asserting the
      entry count.
- [x] 1.3 Preserve IDE- and plugin-managed content: when `.idea/modules/scripts.iml` already exists with another
      component (the CheckStyle component IntelliJ adds), the stage edits only the managed elements and leaves the
      component in place. Verify with a test whose fixture carries a foreign component and asserts its survival.
- [x] 1.4 Make the stage idempotent: a second run over the first run's output writes nothing (byte-identical files).
      Verify with a test comparing both files before and after the second call.
- [x] 1.5 Restore drift: when the managed content root, the Python SDK order entry, or the `modules.xml` entry has been
      changed or removed, a run restores it and leaves unrelated content intact. Verify with three fixtures, one per
      drifted element.

## 2. Resolve the interpreter

- [x] 2.1 Implement interpreter resolution: run `python3 -c 'import sys; print(sys.executable)'` with the working tree
      root as the working directory, take the printed real path, and read `sys.version_info` in the same call. Verify
      with a test that stubs the subprocess, asserts the exact `-c` program, and asserts the parse of a fixed output;
      plus a case that runs the real `python3` from a temporary working tree and asserts the reported path is the
      interpreter that ran.
- [x] 2.2 Reject an interpreter below Python 3.12 with the `ERROR` outcome, a non-zero exit, and no module written.
      Verify with a test injecting a below-floor version and asserting the exit code and the absence of
      `.idea/modules/scripts.iml`.

## 3. Identify the IntelliJ configuration and the SDK name

- [x] 3.1 Discover candidate configuration roots per platform and select one only when exactly one carries a
      `jdk.table.xml` with a matching Python SDK entry; accept `--idea-config-dir` as an explicit override and reject an
      unusable value without falling back to the search. Verify with tests for: one candidate (selected), two candidates
      (needs action), none (skipped, exit 0), and an unusable explicit directory (error, exit non-zero).
- [x] 3.2 Parse `options/jdk.table.xml`, expand `$USER_HOME$` and a leading `~` in `homePath`, compare resolved paths
      against the resolved interpreter, and use the matching entry's `name` verbatim. Verify with a test whose fixture
      holds two entries — one `$USER_HOME$`-spelled, one `~`-spelled — and asserts both match their interpreter and that
      the written `.iml` carries the entry's own name.
- [x] 3.3 Report `NEEDS_ACTION` with a non-zero exit and write no module when no entry matches, several entries match,
      or several configurations qualify, naming the interpreter path and the candidates. Verify with three tests
      asserting the exit code, the message, and that no `.iml` exists afterwards.
- [x] 3.4 Treat an unparsable SDK table as `ERROR` rather than as "no match". Verify with a malformed-XML fixture.

## 4. Wire the CLI contract

- [x] 4.1 Give `scripts/ensure-idea-settings.py` a `main(argv)` entry point that accepts `--idea-config-dir`, runs the
      formatter merge, then the Python stage, and prints one summary line per stage carrying the outcome vocabulary
      (`CONFIGURED`, `SKIPPED`, `NEEDS_ACTION`, `ERROR`). Verify by calling `main` with each fixture and asserting the
      printed outcome word and the return code for all four outcomes, plus a fixture whose formatter merge fails (a
      template without its component) in which the Python stage still runs and both stages report.
- [x] 4.2 Forward `--idea-config-dir` from `scripts/setup-idea.sh`, keep the existing optional positional launcher path
      working, and extend the script's usage header with the flag. Capture the helper's status instead of letting
      `set -e` abort, attempt the plugin installation after a failed Python stage, treat its own skip (IDE running, no
      launcher) as legitimate rather than as a failure, and exit with the aggregate of every stage's
      `NEEDS_ACTION`/`ERROR`. Verify by running `./scripts/setup-idea.sh --idea-config-dir /nonexistent` in a scratch
      tree and reading a non-zero exit with the `ERROR` line _and_ the plugin-install step still attempted, then
      `./scripts/setup-idea.sh` in the real working tree and reading `CONFIGURED`.
- [x] 4.3 Add `scripts/test-setup-idea.sh`, an isolated shell control for the wrapper: copy `scripts/setup-idea.sh`,
      `scripts/ensure-idea-settings.py` and `config/idea/` into a temporary tree, point `--idea-config-dir` at a
      fabricated `options/jdk.table.xml`, and stub the launcher with a script that records its invocation. Cover a
      helper `ERROR` with the launcher present (install attempted, exit non-zero), a helper `ERROR` with no launcher
      (install skipped, exit non-zero), and a `CONFIGURED` run (exit zero). Verify that the stub's record proves the
      install ran, that the temporary tree holds the generated module, and that no real plugin is installed.

## 5. Put the controls in the build

- [x] 5.1 Add `scripts/test-ensure-idea-settings.py` (stdlib `unittest`) with the fixtures tasks 1-4 name and register
      it as a `testEnsureIdeaSettings` Exec task in `build.gradle.kts`, mirroring `testReviewRounds`, and add it to the
      root `check` block. Verify with `./gradlew testEnsureIdeaSettings`.
- [x] 5.2 Register `scripts/test-setup-idea.sh` as a `testSetupIdea` Exec task (running it through `sh`, like
      `testDoctorCharter`) and add it to the root `check` block. Verify with `./gradlew testSetupIdea`.
- [x] 5.3 Prove each control can fail: remove the foreign-component preservation (task 1.3) and the "no module on an
      unmatched SDK" branch (task 3.3) one at a time, confirm the named test goes red, then restore; and make the
      wrapper abort on a helper failure again, confirming `test-setup-idea.sh` goes red. Verify by recording all three
      failures in the task's `Done` note. **Done** — regenerating an existing module made
      `test_existing_module_keeps_a_foreign_component` fail; writing on an unmatched SDK made
      `test_an_unmatched_interpreter_needs_action_and_writes_nothing` fail; the wrapper aborting made three
      `test-setup-idea.sh` cases fail (the running-IDE skip, the missing launcher, and the helper-error case).
- [x] 5.4 Confirm the new `check` members run from the root gate:
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` must execute `testEnsureIdeaSettings` and
      `testSetupIdea` and stay green.

## 6. Documentation

Before editing, derive the site set with `git grep -n "setup-idea" -- ':!openspec/changes/reproduce-python-ide-setup'`
for the tracked set and
`grep -rn "setup-idea" . --exclude-dir=.git --exclude-dir=node_modules --exclude-dir=openspec/changes/reproduce-python-ide-setup`
for untracked files; reconcile the sites below with what those return, and give each derived site its own edit and its
own re-grep.

- [x] 6.1 Rewrite the README's Python-analysis passage — the text currently reading "`setup-idea.sh` does not provision
      or register an interpreter" — to state that the setup now writes the analysis module, names the one-time
      prerequisite per IDEA configuration and interpreter, and keeps the statement that this does not change what
      `./gradlew check` runs. Verify by grepping README.md for the old sentence (absent) and for the new wording
      (present).
- [x] 6.2 Widen the other two README passages that enumerate what the setup does: the `/setup-idea` row in the
      slash-command table, and the drift bullet in the IntelliJ section that lists only the merged settings and the
      installed plugins. Verify each with a re-read of its own paragraph, and keep the README's design intent (a
      human-facing onboarding path, not a reference dump).
- [x] 6.3 Update `.opencode/commands/setup-idea.md`: the new stage, `--idea-config-dir`, each outcome and what it asks
      of the contributor, and the non-zero exit cases — pointing at the script and skill for the mechanics instead of
      restating them. Verify by reading the command against the skill and confirming no procedure step is duplicated.
- [x] 6.4 Update `.opencode/skills/setup-idea/SKILL.md` with the Python stage's procedure, the one-time SDK
      prerequisite, and the `NEEDS_ACTION` hand-back. Verify by reading it against the script's own help text.
- [x] 6.5 Refresh the `AGENTS.md` bullet that describes what the setup merges, so it no longer reads as formatter-only.
      Verify by grepping `AGENTS.md` for the new stage wording and re-reading the surrounding rule for consistency.
- [x] 6.6 Confirm no live artifact restates the script's procedure steps, outcome table, or prerequisite text: read the
      four edited artifacts and the spec delta, and move any duplicate into a pointer to the script or the spec.

## 7. Verification and the review gate

- [x] 7.1 Record the pre-write derivation pass before anything in this tree changes the interpreter: re-derive the
      claims table and its dismissals in `design.md` after the last edit from the base
      `89224c730e63e773fcfdd882c9f6492c323d56ea`, re-running the commands the rows cite (`git show 89224c73:.gitignore`,
      `git show 89224c73:build.gradle.kts`, `git show 89224c73:.github/workflows/ci.yml`,
      `git show 89224c73:scripts/doctor.sh`, `git show 89224c73:README.md`,
      `git show 89224c73:openspec/specs/showcase/quality/ide-config/spec.md`,
      `git show 89224c73:scripts/ensure-idea-settings.py`, `git ls-files '*.py'`,
      `git grep -h -E '^import ' 89224c73 -- '*.py'`, `git grep -h -E '^from ' 89224c73 -- '*.py'`,
      `git ls-tree -r 89224c73 --name-only -- .idea`, `git check-ignore .venv`, `pyenv version-name` and the interpreter
      probe), and re-running `scripts/claim-scan.py --base 89224c730e63e773fcfdd882c9f6492c323d56ea` over this change
      directory to confirm each candidate class still has its dismissal. Verify by the re-derived rows matching what the
      commands printed, and record that the `pyenv version-name` row is the pre-pin value.
- [x] 7.2 Give this working tree its local interpreter pin (`pyenv local 3.12.16`, which writes the ignored
      `.python-version`, absent here by design), then run `./scripts/setup-idea.sh` and read `Python: CONFIGURED`.
      Verify by the script's own report naming the interpreter it used, and that it is the pinned one. **Done** —
      `Python: CONFIGURED — wrote module and registration`, naming the 3.12.16 interpreter and the SDK
      `~/.pyenv/versions/3.12.16/bin/python3` read from the IDE's configuration directory; a second run reported
      `already in place`.
- [x] 7.3 Open this worktree in IntelliJ and confirm Python analysis resolves for `scripts/review-routing.py`
      (interpreter, language level 3.12, and a run configuration whose `sdkHome` is the resolved interpreter). Record
      the observed values in the task's `Done` note. **Done** — this worktree opened as its own project: `findPythonSdk`
      for `scripts/review-routing.py` returned `~/.pyenv/versions/3.12.16/bin/python3`, the `scripts` module's content
      root was this worktree's `scripts/`, and the language level was 3.12.
- [x] 7.4 Write `docs/process-defects/reproduce-python-ide-setup.md` from what the unit observes, or state that no
      process defect was observed. Verify the file exists and carries the unit's name. **Done** — two records written (a
      mutation control that mutated an unreachable branch, and a shell-test isolation that let a real IDE launcher be
      invoked).
- [x] 7.5 Run `python3 scripts/commit-hygiene.py --review-ready` and the touched gates
      (`./gradlew spotlessApply spotlessCheck testEnsureIdeaSettings`), fixing what they report. Verify by a clean run.
- [x] 7.6 Run the quick-review loop to no new observations and append each round's row to `docs/review-rounds.tsv`,
      running `--unique-rounds` and `--session-continuity` after each round. Verify with
      `python3 scripts/commit-hygiene.py --unique-rounds --expect-clean reproduce-python-ide-setup`. **Done** — rounds
      6-11 on the implementation (9, 4, 1, 3, 0 findings), closing clean at round 11; `--unique-rounds`,
      `--session-continuity` and `--expect-clean` all exit 0.
- [x] 7.7 Derive the deep-review routing floor with
      `python3 scripts/review-routing.py --unit reproduce-python-ide-setup --change reproduce-python-ide-setup`, decide
      at or above it, record the outcome and its signals here, and run the review when it is `REQUIRED` (or when the
      owner resolves a `RECOMMENDED` one). **Done** —
      `python3 scripts/review-routing.py --unit reproduce-python-ide-setup --change reproduce-python-ide-setup` printed
      `ROUTING: REQUIRED` with the signal `requirement-block-added` (the delta's ADDED requirements), so
      `/review-thorough` ran before the manual pass. Its three findings are fixed: the shell test's launcher discovery
      reached absolute install roots (`find_idea` now searches the roots `IDEA_SEARCH_ROOTS` names, empty in the
      isolated run), the interpreter scenario now names the SDK the module references, and the fourth requirement scopes
      its vocabulary to the two reporting stages.
- [x] 7.8 Re-derive the claims table and the dismissals last, once every append to this unit's own record exists — the
      closing quick-review round, the process-defect record, this task's routing line, and any deep-review status — and
      re-run `scripts/claim-scan.py --base 89224c730e63e773fcfdd882c9f6492c323d56ea` over this change directory. Verify
      that each class still carries its dismissal and that no row cites a command whose output changed.
- [x] 7.9 Ask the owner for the manual review pass and end the turn without committing. **Done** — asked on the
      proposal and after the implementation; the owner approved shipping.

## Review hand-overs

- Round 1 (proposal stage) — reviewer `review-quick`, session `ses_e750d1ab0ffexqeuzSUDKmdqeg`, inputs: `proposal.md`,
  `design.md`, `tasks.md`, `specs/showcase/quality/ide-config/spec.md`; base `89224c730e63e773fcfdd882c9f6492c323d56ea`;
  bound 20 minutes; 13 findings, `compliance_misses` 10.
- Round 2 (proposal stage, continuation of the round-1 session) — same inputs and base; bound 20 minutes; the hand-over
  relays each round-1 finding's `file:line` instance and the fix applied, and asks for new observations; 7 findings.
- Round 3 (proposal stage, continuation of the round-1 session) — same inputs and base; bound 20 minutes; the hand-over
  relays each round-2 finding's instance and the fix applied.
- Round 4 (proposal stage, continuation of the round-1 session) — same inputs and base; bound 20 minutes; the hand-over
  relays each round-3 finding's instance and the fix applied; 4 findings.
- Round 5 (proposal stage, continuation of the round-1 session) — same inputs and base; bound 20 minutes; the hand-over
  relays the round-4 ordering finding and its fix.
- Round 6 (implementation stage, continuation of the round-1 session) — inputs: the four artifacts plus the
  implementation diff (scripts, build wiring, docs, process-defect record); base
  `89224c730e63e773fcfdd882c9f6492c323d56ea`; bound 20 minutes.
- Round 7 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  relays each round-6 finding's instance and the fix applied.
- Round 8 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  lists the four round-7 findings and their fixes.
- Round 9 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  reports the round-8 tasks.md formatting fix.
- Round 10 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  reports the deep-review fixes (launcher search roots, two delta wordings), the re-stated dismissals, and the task
  records.
- Round 11 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  reports the three round-10 fixes (the pre-pin `pyenv` row and Context, the widened `cross-ref` dismissal, and the
  collapsed code-span spacing).
- Round 12 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  lists the advisor's three pre-ship findings and their fixes (IntelliJ-only configuration discovery plus the
  missing-table action, the real-probe assertion, and the wrapper's truthful messages).
- Round 13 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  reports the neutral install-failure wording, the combined case, and the docstring wrap.
- Round 14 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  reports the exit-contract wording: the successful-run scenario now names the install outcome, a failed installation
  has its own scenario, and the command and skill list it as the third non-zero cause.
- Round 15 (implementation stage, continuation of the round-1 session) — same inputs; bound 20 minutes; the hand-over
  reports the two agent-facing sentences now naming a failed plugin installation as the third cause.

## Workflow follow-up

- Refresh `showcase/quality/ide-config`'s Purpose **in the archive commit**: the current text enumerates only the
  formatter settings and inspections, which this change's non-formatter artifact falsifies. Verify by reading the
  Purpose after the archive against the capability's full requirement list.
- After the owner approves: commit, push, run CI, archive with `openspec archive reproduce-python-ide-setup --yes`, and
  merge, per the `ship-change` skill.
