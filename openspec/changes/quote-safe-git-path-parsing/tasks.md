# Tasks

## 1. Make the guard's path-enumerating calls quote-safe

- [x] 1.1 Fix `staged_paths` in `scripts/commit-hygiene.py` to read
      `git diff --cached --name-only -z --diff-filter=ACMR` and split the output on `"\0"`, filtering empty elements —
      matching the `-z` pattern `find_non_executable_scripts` already uses. Verify: `python3 -c` over a scratch repo
      with a staged `café.md` returns the real path, so `formatter_owned()` contains it (the pre-fix code returned `[]`
      for that input).
- [x] 1.2 Fix `find_staged_then_edited` to read `git status --porcelain -z` and split on `"\0"`, dropping the now-dead
      `strip('"')` (with `-z` the path is unquoted; keeping the strip would mangle a path ending in a quote). Preserve
      the existing rename handling (`" -> "` split) against the `-z` shape — note `-z` does **not** use the `" -> "`
      arrow but emits the two paths as separate NUL-terminated fields, so the rename branch must be re-derived from the
      real format, not carried over blindly. Verify: a staged-then-edited `café.md` is reported by the function (the
      pre-fix code reported the quoted literal, which no caller matches).
- [x] 1.3 Fix `find_tracked_ignored` to read `git ls-files --cached --ignored --exclude-standard -z` and split on
      `"\0"`, filtering empties — so `find_force_staged_artifacts`'s membership test compares real paths on both sides.
      Verify: a tracked-ignored `café`-named path appears in both this function's output and `staged_paths()`'s when
      staged.
- [x] 1.4 Read each fixed site's git invocation against `git <cmd> --help` (or a scratch run) before trusting the `-z`
      output shape — in particular confirm that `status --porcelain -z` reorders/handles the rename case differently
      from the newline form, since task 1.2 depends on it. Verify: the observed `-z` output for a rename and for a plain
      staged file is recorded in the change's report.

## 2. Cover each fixed site

- [x] 2.1 Add a case to `scripts/test-commit-hygiene.py` per **fixed function**, each exercising its function
      **directly** with a non-ASCII path: one adding a direct `staged_paths` assertion to `FormatterTests` (which drives
      `check_staged` today, so this is a new direct call there) confirming it returns the real non-ASCII path so
      `formatter_owned` selects it; one in `StagedThenEditedTests` asserting `find_staged_then_edited` reports the real
      path; and one in `TrackedIgnoredTests` asserting `find_tracked_ignored` reports the real path. Two of the three
      match an existing direct-call precedent (`TrackedIgnoredTests`/`StagedThenEditedTests` call their functions, and
      `ExecutableBitTests` has a `café.sh` case). Verify: each new case fails against the pre-fix code and passes after;
      run the suite with `python3 scripts/test-commit-hygiene.py` and record the result.
- [x] 2.2 Add a separate cross-site case in `ForceStagedArtifactTests` for a non-ASCII generated artifact, labelled as
      the **combined** check (it depends on `staged_paths` and `find_tracked_ignored` agreeing) rather than as an arm of
      either site — so a regression in one site is attributable, not masked by the other. Verify: the case passes, and
      confirm it fails when _either_ constituent site is reverted, since it is the one fixture that spans both.
- [x] 2.3 Cover the empty-element filtering that `-z` introduces, so a future over-filter is caught. **Confirmed already
      covered by the pre-existing ASCII cases** (`StagedThenEditedTests`' cleanly-staged case,
      `ForceStagedArtifactTests`' ordinary-file case, `TrackedIgnoredTests`' clean-repository case) rather than adding a
      duplicate; the suite passes with them.

## 3. Verification

- [x] 3.1 Prove the end-to-end effect through the guard, not only the unit functions: stage a deliberately
      non-conforming formatter-owned `café.md` in a scratch clone and confirm
      `python3 scripts/commit-hygiene.py --staged` refuses the commit, where the pre-fix code passed it. Verify: the
      guard's exit code and its message, before and after.
- [x] 3.2 Run the guard's suite `python3 scripts/test-commit-hygiene.py` (wired into `check` through
      `testCommitHygiene`) under the **system** interpreter as well, `/usr/bin/python3 scripts/test-commit-hygiene.py`,
      since a repo Python script runs under whichever `python3` PATH resolves and the checker is stdlib-only 3.9
      compatible. Verify: both invocations pass.
- [x] 3.3 Run `openspec validate --changes` and confirm the delta validates with all six existing scenarios of the guard
      requirement preserved in main-spec order, plus the new ones (the four non-ASCII scenarios and the clean-rename
      scenario). Verify: exit 0, `1 passed`, and the scenario count read from the delta rather than assumed — do not
      write a running total into the task, since it goes stale as scenarios are added.
- [x] 3.4 Run `./gradlew spotlessApply` after the final edit to a Spotless-owned file, then `./gradlew spotlessCheck`
      (`scripts/commit-hygiene.py` and `scripts/test-commit-hygiene.py` are Python, which Spotless does not own — check
      them against the manual 120-character rule instead). Verify: the Gradle task succeeds and no over-120 line was
      added to the Python files.
- [x] 3.5 Run the Docker-free gate `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`, which includes
      `testCommitHygiene` and the `verify*` tasks that invoke the checker. Verify: the task completes successfully.
- [x] 3.6 Remove the implemented idea from `docs/ideas.md`: the entry "Pass `-z` to the checker's remaining git
      path-parsing sites" is exactly this change, so it leaves the scratchpad in this change's PR (the docs-refresh
      convention). Verify: `git diff docs/ideas.md` shows the entry removed, and grep finds no other reference calling
      it proposed.
- [x] 3.7 Sweep `docs/ideas.md` for entries this change makes stale — the neighbouring "Widen the `commit-hygiene`
      test-coverage requirement to every check" entry is adjacent but **not** implemented by this change (it widens the
      spec requirement, which this change does not), so confirm it still reads as open and leave it, or refresh its
      prose if this change's new tests make its wording wrong. Verify: the report states the outcome for that entry.
- [x] 3.8 Run the `lesson-capture` subagent over the diff, apply the durable proposals the main agent judges worth
      keeping, and record the applied net `AGENTS.md` delta on this task. Verify: the subagent's verdict is recorded and
      any applied edit is visible in `git diff AGENTS.md`.
