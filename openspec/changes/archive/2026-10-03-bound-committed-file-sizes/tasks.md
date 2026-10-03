# Tasks

## 1. The size check

- [x] 1.1 Add `config/commit-hygiene/large-files.properties` declaring `maxBytes` (a value above the current largest
      tracked file, `AGENTS.md` at ~263 KiB — e.g. 524288 = 512 KiB) with a comment naming the current largest file and
      the rationale. Verify the file parses and the value is stated once.
- [x] 1.2 In `scripts/commit-hygiene.py`, add a `--large-files` mode: read `maxBytes` from the properties file, scan
      `git ls-files -z`, report each file over the limit as its path, size, and the limit, and print the number of files
      checked. Register the mode in the `argparse` group and dispatch it in `main`, matching the sibling modes (stdlib
      only; `from typing import Optional`, no `str | None`). Verify
      `/usr/bin/python3 scripts/commit-hygiene.py     --large-files` runs over the current tree and exits 0 (no file
      exceeds the default), and that the system interpreter (3.9) accepts the script.
- [x] 1.3 Add tests to `scripts/test-commit-hygiene.py`: an over-limit file is reported naming it, an at/under-limit
      file is not, and the limit is read from the properties file. Run `python3 scripts/test-commit-hygiene.py` (or the
      Gradle `testCommitHygiene` task) and confirm green; confirm at least one test fails against the pre-change script.
- [x] 1.4 Register `verifyLargeFiles` in the root `build.gradle.kts`
      (`commandLine(pythonExecutable, "scripts/commit-hygiene.py", "--large-files")`, with the script and the properties
      file as inputs) and add `dependsOn("verifyLargeFiles")` to `check`. Verify with `./gradlew verifyLargeFiles` and
      by confirming it runs inside `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`.

## 2. Documentation

- [x] 2.1 Refresh every live prose enumeration of the `check` members (derived from the root `build.gradle.kts`'s
      `check` `dependsOn` set — 12 tasks plus `build-logic`'s test — not from memory), adding `verifyLargeFiles`:
      `AGENTS.md`'s `check`-members comment (lines 499-503) and its Build & Test `check` note (line ~500); `README.md`'s
      verification-command block (lines 776-779) and its commit-hygiene build-checks paragraph (lines 613-618); and the
      parked `docs/ideas.md` idea listing the five build-check names (lines 56-57). Deliberately **unchanged**:
      `README.md`'s "five commit-hygiene slips" paragraph (line ~610, the pre-commit guard's `--staged` set, not the
      build checks); `AGENTS.md`'s Continuous Integration `check` note (line ~651, which re-lists no member); and
      `openspec/config.yaml`'s `context:` block (verified to carry no such enumeration). Remove the implemented idea
      from `docs/ideas.md` (the 2026-09-24 "Bound the size of committed files" entry). Verify by reading each edited
      enumeration and `grep -n "verifyLargeFiles" AGENTS.md README.md docs/ideas.md` hitting all three at their
      enumeration sites, and `grep -n "Bound the size of committed files" docs/ideas.md` returning nothing. Record in
      the change's report that this member-add hand-updates several copies of one enumeration — the duplication the
      2026-09-28 spec-audit flagged (`docs/audits/2026-09-28.md`) — so the next member-add repeats it.
- [x] 2.2 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file, then `./gradlew spotlessCheck` and
      `openspec validate --changes`, and confirm all pass.

## 3. Verification

- [x] 3.1 Prove the check with a known-bad and a known-good input: temporarily add a tracked file larger than the limit
      (e.g. `head -c 600000 /dev/zero > /tmp/blob && git add -f`), confirm `./gradlew verifyLargeFiles` (and `check`)
      fails naming it, remove it, and confirm the task passes. Record both runs' output.
- [x] 3.2 Confirm the check runs inside `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and that the standard
      `check` is green.
- [x] 3.3 Run the per-unit `lesson-capture` subagent over the change and apply its durable proposals; record the applied
      net `AGENTS.md` delta on this task. Applied: one merge — the "the only X" gotcha's grep host list now names
      `README.md` and `docs/ideas.md` (the two sites this unit's loop hit), net 0 lines; captured:
      bound-committed-file-sizes.
- [x] 3.4 Refresh the `commit-hygiene` capability's `## Purpose` in the **archive commit** (a delta cannot carry a
      Purpose, so it is edited directly when the change is archived): its sentence enumerates the build checks and must
      gain the size check alongside the conflict-marker, executable-bit, tracked-ignored, and cron-collision checks.
      Verify the archived main spec's Purpose names the size check.
