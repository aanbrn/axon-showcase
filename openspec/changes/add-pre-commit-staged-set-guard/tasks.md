# Tasks

## 1. Checker (`scripts/commit-hygiene.py`)

- [x] 1.1 Implement the staged-then-edited check: report each path whose index entry and working-tree content differ
      (`git status --porcelain` states such as `MM`/`AM`/`RM`), with unit tests in `scripts/test-commit-hygiene.py` that
      load the checker by path (`importlib.util.spec_from_file_location`, since the kebab-case filename is not an
      importable identifier) and exercise it against a fixture repository.
- [x] 1.2 Implement the force-staged generated-artifact check: report each staged path that is git-ignored or under a
      generated directory (e.g. `__pycache__/`, `build/`), with a unit test that force-stages a bytecode file.
- [x] 1.3 Implement the `captured:` marker check for `AGENTS.md`: a marker must lie within a rule block (a bold-lead
      bullet or paragraph) and never on a plain `- Text` bullet; ignore backticked prose mentions. Unit-test five
      fixtures — correctly placed (pass), marker on a plain `- Text` bullet (fail), wrapped marker whose token starts
      the next line (pass), backticked prose mention (ignored), and a merged-into bullet with the marker mid-item (pass)
      — and add a test asserting the check passes the repository's current `AGENTS.md`.
- [x] 1.4 Implement the format check delegating to a configurable formatter command (default `./gradlew spotlessCheck`),
      running only when a formatter-owned file is staged and refusing on non-zero exit, with a unit test that stubs the
      command both ways.
- [x] 1.5 Implement the CLI: `--staged` runs the index checks plus the marker check on the staged `AGENTS.md`;
      `--markers` runs only the marker check; both exit non-zero and name each offending path/reason on failure, with
      unit tests asserting exit codes and messages.

## 2. Tests (`scripts/test-commit-hygiene.py`)

- [x] 2.1 Run the suite with `python3 scripts/test-commit-hygiene.py` and confirm it passes.
- [x] 2.2 Confirm each check fails a test when disabled (positive control): temporarily neuter each of the four checks,
      confirm its test goes red, then restore it.

## 3. Hook and activation

- [x] 3.1 Add the tracked hook `scripts/git-hooks/pre-commit` (mode 100755) that runs
      `python3 scripts/commit-hygiene.py --staged` and propagates its exit code; verify with `git ls-files -s` that the
      mode is `100755`, and by running it directly on a defective staged set (non-zero) and a clean one (zero).
- [x] 3.2 Add `scripts/install-git-hooks.sh` (mode 100755) that sets `core.hooksPath` to `scripts/git-hooks`; verify it
      is idempotent and that `git config --get core.hooksPath` reports `scripts/git-hooks` afterwards.

## 4. Build wiring

- [x] 4.1 Register `verifyCapturedMarkers` (runs `python3 scripts/commit-hygiene.py --markers`) and `testCommitHygiene`
      (runs `python3 scripts/test-commit-hygiene.py`) in `build.gradle.kts`, resolving `python3` from `PATH`, declaring
      `AGENTS.md` and the scripts as task inputs, and adding both to `tasks.named("check")`; verify with
      `./gradlew verifyCapturedMarkers testCommitHygiene`.
- [x] 4.2 Confirm a marker moved onto a plain `- Text` bullet fails `verifyCapturedMarkers` (positive control), then
      restore the file.

## 5. Documentation and idea removal

- [x] 5.1 Document the guard and its activation in `README.md` (the human path) and `AGENTS.md` (the agent's commit
      discipline), naming the install step and the `--no-verify` bypass. In `AGENTS.md`, revise both `captured:` sites —
      the capture bullet's "at the end of the rule it records" clause and the programmatic-edits gotcha's "every marker
      at the end of the rule it names" — so the enforced invariant (a marker lies inside a rule block, never on a plain
      `- Text` bullet) is distinguished from the authoring preference to place it at the end, and the file no longer
      prescribes an invariant the guard does not enforce.
- [x] 5.2 Remove the "Pre-commit guard over the staged set" idea from `docs/ideas.md` (implemented by this change).
- [x] 5.3 Update the disposition of suggestion 6 in `docs/retrospectives/2026-09-16.md` (and suggestion 2 in
      `docs/retrospectives/2026-09-19.md`) to name this change.

## 6. Verification

- [x] 6.1 Run the guard end to end against a deliberately defective staged set (an unformatted staged file, a
      force-staged artifact, a staged-then-edited path, and a marker on a plain bullet) and confirm each is reported.
- [x] 6.2 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck` and confirm the tree is clean.
- [x] 6.3 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and confirm it is green, including
      `verifyCapturedMarkers` and `testCommitHygiene`.
- [x] 6.4 Run `openspec validate add-pre-commit-staged-set-guard --strict` and confirm it passes.
