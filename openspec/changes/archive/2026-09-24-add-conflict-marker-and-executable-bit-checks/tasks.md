# Tasks

## 1. Conflict-marker check

- [x] 1.1 Add a conflict-marker check to `scripts/commit-hygiene.py` — `git grep -I` for `^(<<<<<<< |>>>>>>> )` over the
      index and the tracked tree — exposed as a `--conflict-markers` build mode and included in the `--staged` guard
      mode; update the module docstring and argparse description; add tests (a staged/tracked file with a branch marker
      is reported; a clean repository passes).

## 2. Executable-bit check

- [x] 2.1 Add an executable-bit check to `scripts/commit-hygiene.py` — filter `git ls-files -s` (root-relative paths) to
      `scripts/git-hooks/*`, `*.sh` at any depth, and `gradlew`, excluding the vendored `axon4to5-*` and generated
      `openspec-*` skill subtrees, and report any mode other than `100755` — exposed as an `--executable-bits` mode;
      update the module docstring and argparse description; add tests (a `100644` shell script and a `100644` hook are
      each reported; a clean repository passes).

## 3. Build wiring

- [x] 3.1 Register `verifyConflictMarkers` and `verifyExecutableBits` in `build.gradle.kts` mirroring
      `verifyTrackedIgnoredFiles`, and add both to `tasks.named("check")`; verify with
      `./gradlew verifyConflictMarkers verifyExecutableBits`.
- [x] 3.2 Positive controls: stage a file containing a conflict branch marker and confirm the guard (`--staged`)
      refuses; set a tracked `scripts/*.sh` to `100644` and confirm `verifyExecutableBits` fails; restore both.

## 4. Documentation

- [x] 4.1 Name the new checks and update every enumeration the new class stales: `README.md`'s guard paragraph and its
      "catches four commit-hygiene slips" count (four → five); the `AGENTS.md` guard-class lists (the Prerequisites
      guard line and the `git add <dir>` gotcha's guard sentence); the `scripts/git-hooks/pre-commit` header comment;
      and the `AGENTS.md` root-check enumerations (the `# Check runs:` comment and the CI paragraph), deriving each
      root-check list's members from `build.gradle.kts`'s `check` block.

## 5. Verification

- [x] 5.1 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and confirm it is green, including
      `verifyConflictMarkers` and `verifyExecutableBits`.
- [x] 5.2 Run `openspec validate add-conflict-marker-and-executable-bit-checks --strict` and
      `openspec validate --specs`, and confirm both pass.
- [x] 5.3 Run `./gradlew spotlessApply` then `spotlessCheck` and confirm the tree is clean.
- [x] 5.4 Refresh the `showcase/quality/commit-hygiene` `## Purpose` in the archive commit, since it enumerates the
      capability's checks and this change adds two.
