# Tasks

## 1. Add the unique-cron mode to the checker

- [x] 1.1 Add a `--unique-crons` mode to `scripts/commit-hygiene.py` (stdlib only, macOS Python 3.9 compatible —
      `typing.Optional`, never `str | None`) that scans `.github/workflows/*.yml` and `*.yaml` for `- cron:` entries,
      ignores commented-out lines, and fails when two schedules collide (minute and hour equal with overlapping
      day-of-week fields — a field of `7` treated as `0`, since both name Sunday — or an identical cron string when a
      restricted day-of-month/month is involved), naming the workflow file or files carrying each colliding pair and the
      shared cron. Verify by running `python3 scripts/commit-hygiene.py --unique-crons --repo .` on HEAD and reading its
      output (expect exit 0, no output).
- [x] 1.2 Update the checker's own documentation so both enumerate the new mode alongside the existing ones: the module
      docstring (`scripts/commit-hygiene.py`, lines ~4–8) and the argparse description (~line 192). Verify by reading
      both and confirming each names `--unique-crons`.
- [x] 1.3 Add cases to `scripts/test-commit-hygiene.py` covering an exact duplicate (fails), two schedules in one
      workflow file (fails), a daily and a weekly schedule sharing minute and hour (fails), two weekly schedules on
      different days sharing minute and hour (passes), a Sunday written `7` against one written `0` (fails), a pair of
      different schedules that both restrict day-of-month/month and share minute and hour (passes unless identical),
      identical restricted-day schedules (fails), a malformed cron (not a collision), a commented-out `cron` (not a
      schedule), an unquoted cron with an inline trailing comment (stripped), a clean set (passes), a `.yaml` workflow
      (scanned), and the mode naming the colliding files and shared cron. Verify with
      `python3 scripts/test-commit-hygiene.py` (51 tests).
- [x] 1.4 Prove both glob arms are real and the scan's file set is not vacuous: run a scratch command that lists the
      files the arms match:
      `python3 -c "from pathlib import Path; [print(p) for p in sorted(Path('.github/workflows').glob('*.y*ml'))]"` Then
      read its full output — confirm it enumerates every workflow file by path, including a known one such as
      `dependency-updates.yml`, rather than reporting only a count (`.y*ml` covers both arms; `.yaml` matches nothing
      today). Then exercise the `.yaml` arm: create a temporary `.github/workflows/__probe.yaml` whose `cron` duplicates
      a `.yml` workflow's, run `python3 scripts/commit-hygiene.py --unique-crons --repo .` and confirm it reports the
      collision, delete the fixture, and confirm `test ! -e .github/workflows/__probe.yaml` and an empty
      `git status --short .github/workflows/`.
- [x] 1.5 Prove the check hits a known bad and a known good input: temporarily perturb a workflow `cron` to duplicate
      another (for example set `.github/workflows/e2e.yml`'s line to `- cron: '40 19 * * 0'`, matching
      `helm-updates.yml`), run `python3 scripts/commit-hygiene.py --unique-crons --repo .` and read the output (expect
      failure naming both files), then restore the file (`git restore .github/workflows/e2e.yml`) and re-run to confirm
      it passes. Finish with `git diff --exit-code .github/workflows/` to prove the tree is clean; do not leave the
      perturbation in the tree.

## 2. Wire the check into the build and refresh the docs this change owns

- [x] 2.1 Register `verifyUniqueCronSchedules` in `build.gradle.kts` beside
      `verifyConflictMarkers`/`verifyExecutableBits` (`group = "verification"`,
      `inputs.file("scripts/commit-hygiene.py")`, `inputs.dir(".github/workflows")`, `outputs.upToDateWhen { false }`,
      `commandLine(pythonExecutable, "scripts/commit-hygiene.py", "--unique-crons")`) and add
      `dependsOn("verifyUniqueCronSchedules")` to the root `check` task. Verify the task is registered with
      `./gradlew tasks --group verification`.
- [x] 2.2 Re-run the Gradle task on the good tree to confirm the wiring, complementing the CLI proof at 1.5: run
      `./gradlew verifyUniqueCronSchedules` and confirm it passes.
- [x] 2.3 Add `verifyUniqueCronSchedules` where the commit-hygiene `check` tasks are enumerated: `AGENTS.md`'s two
      enumerations (the `# Check runs:` comment in the Build & Test section and the Continuous Integration paragraph),
      `README.md`'s build-side counterpart list (the Quality Gates section, ~lines 552–555), and the parked
      test-coverage widening idea's check list in `docs/ideas.md`. Then remove the parked unique-cron idea from
      `docs/ideas.md`. Verify by grepping `AGENTS.md`, `README.md`, and `docs/ideas.md` for `verifyUniqueCronSchedules`,
      and `docs/ideas.md` for `unique-cron` (no hits).

## 3. Verification

- [x] 3.1 Run `./gradlew spotlessApply` then `spotlessCheck` and confirm the change-dir markdown and touched files are
      clean.
- [x] 3.2 Run `openspec validate --changes` and confirm it reports the change valid.
- [x] 3.3 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (or at least
      `testCommitHygiene verifyUniqueCronSchedules spotlessCheck`) and confirm it is green.
- [ ] 3.4 Refresh the `commit-hygiene` `## Purpose` in the archive commit to include the unique-cron guarantee, since it
      enumerates the capability's checks and this change adds one (a delta cannot carry a Purpose).
