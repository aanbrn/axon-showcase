# Tasks

## 1. Checker mode

- [x] 1.1 Add a `--tracked-ignored` mode to `scripts/commit-hygiene.py` that reports every path
      `git ls-files --cached --ignored --exclude-standard` lists, update the module docstring and the argparse
      `description` to name the mode (both enumerate the existing modes), and add unit tests: a repository with a
      force-committed ignored file (reported) and a clean repository (passes).

## 2. Build wiring

- [x] 2.1 Register `verifyTrackedIgnoredFiles` in `build.gradle.kts` mirroring `verifyCapturedMarkers`: an `Exec` task
      running `pythonExecutable scripts/commit-hygiene.py --tracked-ignored`, declaring `scripts/commit-hygiene.py` as
      an input with `outputs.upToDateWhen { false }`, added to `tasks.named("check")`; verify with
      `./gradlew verifyTrackedIgnoredFiles`.
- [x] 2.2 Positive control: temporarily `git add -f` an ignored file (e.g. a `*.pyc`), confirm
      `./gradlew verifyTrackedIgnoredFiles` fails and names it, then `git rm --cached` it and delete it.

## 3. Documentation

- [x] 3.1 Name the new build check where the build gates are described: `README.md`'s guard paragraph (add
      `verifyTrackedIgnoredFiles` alongside `verifyCapturedMarkers`), and the two `AGENTS.md` root-check enumerations —
      the `# Check runs:` comment and the CI paragraph — which today name none of the three commit-hygiene tasks; add
      all three (`verifyCapturedMarkers`, `testCommitHygiene`, `verifyTrackedIgnoredFiles`), derived from the `check`
      dependency block in `build.gradle.kts`.

## 4. Verification

- [x] 4.1 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and confirm it is green, including
      `verifyTrackedIgnoredFiles`.
- [x] 4.2 Run `openspec validate verify-tracked-ignored-files --strict` and `openspec validate --specs`, and confirm
      both pass.
- [x] 4.3 Run `./gradlew spotlessApply` then `spotlessCheck` and confirm the tree is clean.
- [ ] 4.4 Refresh the `showcase/quality/commit-hygiene` `## Purpose` in the archive commit, since it enumerates the
      capability's checks and this change adds one (a delta cannot carry a Purpose).
