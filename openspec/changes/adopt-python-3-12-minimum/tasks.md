# Tasks

Workspace: unit=adopt-python-3-12-minimum; path=/Users/afanasyev/Development/axon-showcase-adopt-python-3-12-minimum;
branch=adopt-python-3-12-minimum; writer=ses_e8ede802cffeqJu3lPJfhdQyis

> Incident record (2026-10-25, this unit). Three process mistakes, recorded here rather than as new guidance. The unit's
> capture (task 11.1) ran before the last two, so its verdict covers only the material it saw.
>
> - **Destructive refresh.** While refreshing this branch onto `origin/main` for the fifth time, a
>   `git checkout HEAD -- <paths>` over paths this unit had edited discarded every uncommitted edit to them (12 files,
>   the 14 ledger rows included) — the hazard the `git checkout -- <file>` gotcha names, and avoidable with the refresh
>   rule's `git merge --ff-only origin/main`. Recovered from IntelliJ Local History, which held the pre-discard
>   revisions for ten files; `.gitignore` and `docs/ideas.md` were re-applied by hand, and `AGENTS.md` plus the ledger
>   rebuilt from `HEAD` so the sibling `#684` merge was not reverted. Verified afterwards by the full gate set and both
>   ledger checks. Disposition: already covered; compliance miss.
> - **Markdown insertion, `docs/ideas.md`.** Inserting two parked ideas immediately after an existing entry's first line
>   left that entry's five continuation lines under the new entry. Caught by re-reading the section; repaired by moving
>   the continuation block back under its owner. Disposition: already covered by the "Programmatic edits to a structured
>   file" gotcha — an insertion made with a script is named there, and an item's end is its indented continuation;
>   compliance miss.
> - **Markdown insertion, this file.** Inserting task 7.8 after task 7.7's first line moved 7.7's `Done:` note into 7.8.
>   Caught by reading the section; repaired by re-deriving both blocks. Same disposition.

## 1. Planning and records

- [x] 1.1 Record the workspace assignment above before the first edit.
- [x] 1.2 Derive the pre-write claims table in `design.md` — each fact this change rests on, with the command that
      reproduces it — re-run every row's command at the recorded state, and resolve every `scripts/claim-scan.py`
      candidate into a row or a per-kind dismissal beside the table.
- [x] 1.3 Trace each normative clause of the two delta specs to its implementation task, in the table below. Done: the
      table above covers all six clauses.

| Clause (delta spec)                                                                                        | Task(s)                 |
| ---------------------------------------------------------------------------------------------------------- | ----------------------- |
| `toolchain-check`: a missing required tool fails the diagnosis, now naming the Python 3.12+ interpreter    | 3.1, 4.1, 7.1           |
| `toolchain-check`: a required tool below its floor fails, now including Python 3.11 against the 3.12 floor | 2.1, 2.2, 2.3, 2.4, 5.2 |
| `toolchain-check`: the repo-state scenario's required set names Python 3.12+                               | 4.1                     |
| `merge-governance`: the pull-request gate provisions the declared Python minimum before `check`            | 6.1, 6.2                |
| `merge-governance`: the gate asserts the interpreter it resolved reports at least that version             | 6.3                     |
| `merge-governance`: the main gate provisions the same Python minimum                                       | 6.1                     |

## 2. Doctor: express and compare a minor-version floor

- [x] 2.1 Rename the declaration's seventh column to `minimum-version` in the probe table's header comment, and accept
      `major[.minor]` there in `validate_probe_table`, failing a malformed value with the column's new name. Done: the
      column reads `minimum-version`, and the validator accepts `major[.minor]`.
- [x] 2.2 Compare the detected version against the floor in an **extracted helper**
      (`version_at_least <detected> <floor>`) that `check_tool` calls — as `validate_probe_table` is already extracted —
      comparing numeric components (major first, then minor only when the majors match) and treating an integer floor as
      `major.0`; the grammar stays `major[.minor]`, so a floor with a third component is rejected by the validator
      rather than silently read. The extraction lets `test-doctor.sh` exercise both functions directly. Done:
      `version_at_least` is extracted and `check_tool` calls it.
- [x] 2.3 Keep reporting `unknown` for an unreadable or non-numeric version — a missing reading must not read as
      satisfied — and name the floor in the too-old remedy text. Done: the unreadable path still reports `unknown`, and
      the too-old remedy names the floor.
- [x] 2.4 Add the changed validator's and helper's known-good and known-bad cases as **committed controls** in
      `test-doctor.sh` (a checklist task is not a control), stated as a fourth group in that file's header beside
      SHAPE/BEHAVIOUR/AGREEMENT: extraction of both functions, then acceptance of `""`, `21` and `3.12` and rejection of
      `.`, `3.`, `3.x`, `3.12.` and `3.12.0`; and `version_at_least` for `3.9.6 < 3.12`, `3.12.0 >= 3.12`,
      `21.0.12+1 >= 21`, `3.13.0 >= 3.12` and `4.0 >= 3.12`. Show the discriminating cases failing against the
      pre-change code — the `3.12` acceptance, which the old validator rejects, and `3.9.6 < 3.12`, which the major-only
      comparison passes — and show every remaining case can fail by mutating the rule it pins (widen the grammar so
      `3.12.0` is accepted; use `-gt` instead of `-ge` on the minor so `3.12.0 >= 3.12` flips). Done: the controls live
      in `test-doctor.sh`'s §4; falsified — the pre-change validator rejects `3.12`, a `-gt` mutation flips
      `3.12.0 >= 3.12`, and a widened grammar accepts `3.12.0`.

## 3. Doctor: the `python3` probe

- [x] 3.1 Declare `python3` as `required` with the floor `3.12` in the probe table. Done: the row reads
      `python3|required|tool|yes|python3|--version|3.12`.
- [x] 3.2 Give the `python3` probe an install hint for each documented platform that leaves a qualifying `python3` on
      `PATH` — `brew install python3` on macOS (Homebrew's `python@3.12` alone installs `python3.12` and puts the
      unversioned symlinks outside `PATH`), and on Debian/Ubuntu the requirement rather than a bare
      `apt install python3`, because the `debian` class spans releases whose `python3` predates 3.12. Done: macOS names
      `brew install python3`; Debian names the 3.12+ requirement and the Ubuntu 24.04 package.
- [x] 3.3 Run `./scripts/doctor.sh` on this host and confirm the line reports the installed interpreter's version as
      satisfied against the new floor. Done: `./scripts/doctor.sh` reports `python3 satisfied 3.15.1` and exits 0.

## 4. `test-doctor.sh`: declare-to-declare agreements

- [x] 4.1 Add `python3` to `expected_required` and update the comment so the literal still reads as the spec's set.
      Done: `expected_required` now reads `compose docker docker-daemon java python3`.
- [x] 4.2 Hold `expected_python_floor="3.12"` against the spec (not against the declaration) and assert the
      declaration's `python3` floor equals it. Done: `expected_python_floor=3.12` is asserted against the declaration's
      `python3` floor.
- [x] 4.3 Assert the README Prerequisites row documenting Python names the declared floor, and update the row mapping so
      the renamed row still maps to the `python3` probe. Done: the row must read `Python 3.12+`, and the mapping matches
      `Python *`.
- [x] 4.4 Assert that the `setup-python` step in `.github/workflows/ci.yml` — located by its `with:` block, never by the
      first `python-version` occurrence in the file — carries a `python-version` equal to the declared floor, so the
      workflow cannot drift from the declaration. Done: the step is located by `uses: actions/setup-python@`.
- [x] 4.5 Confirm each new assertion fails on the pre-change artifacts (a scratch copy with the old row, the old floor
      and no `setup-python` step), then passes on the changed ones. Done: all three fail on `HEAD`: zero matches for the
      README row, the `3.12` floor and the setup step.

## 5. `test-doctor.sh`: below-floor behavioural controls

- [x] 5.1 Build a fixture directory prepended to the host `PATH` — stub `java`, `docker` (answering `--version`, `info`
      and `compose version`) and `python3` — scoped to the fixture invocations only, so the default run's exit code is
      driven by the fixture, not the host, and the `java` stub reports a supported version so the satisfied rows can
      exit zero. Done: the fixture directory is prepended for the fixture invocation only.
- [x] 5.2 Assert a fixture `python3` reporting 3.11.2 yields the `too-old` status naming both versions **and** a
      non-zero default exit, capturing the status under `set +e` before `set -e` is restored and asserting the anchored
      `python3` status line as well as the exit code. Done: `3.11.2` yields the `too-old` line naming both versions and
      a non-zero exit.
- [x] 5.3 Assert a fixture `python3` reporting 3.12.0 yields `satisfied` and a zero exit, and one reporting 3.15.1
      yields `satisfied` — a newer interpreter is above the floor. Done: `3.12.0` and `3.15.1` yield `satisfied` and a
      zero exit.
- [x] 5.4 Show the controls can fail: run them against the pre-change doctor (3.11 reads satisfied under the major-only
      comparison) and, separately, neuter the new comparison and confirm they fail again. Done: the pre-change doctor
      reports `python3 satisfied 3.11.2`, so the new line assertion fails there; a `-gt` mutation flips the at-floor
      case.

## 6. CI: provision the floor and assert the resolved interpreter

- [x] 6.1 Add an `actions/setup-python@v7` step with `id: python` and `python-version: '3.12'` before the quality-gates
      step, so both the pull-request and the `main` gate run the Python tooling at the floor. Done: the step carries
      `id: python` and `python-version: '3.12'`.
- [x] 6.2 Keep the provisioning step's comment to what it does, and place it so the gate's later steps inherit the PATH.
      Done: the comment states what the step does, and the step precedes the gates.
- [x] 6.3 Add an assertion step that resolves `command -v python3` and fails unless its **directory** equals the
      directory of `steps.python.outputs.python-path` (that output is the `python` executable, so the paths differ in
      their last segment) and its version is at least `steps.python.outputs.python-version` — so the workflow holds one
      copy of the number and asserts the binary the build resolves, not merely one setup-python installed. Done: the
      assertion compares the resolved `python3`'s directory with `python-path`'s and the version with `python-version`.
- [x] 6.4 Run `./gradlew workflowLint` and confirm the edited workflow lints. Done: `check` ran `workflowLint` green
      over the edited workflow.

## 7. Documentation

- [x] 7.1 `README.md`: change the Prerequisites row to `Python 3.12+`, with install columns that leave a qualifying
      `python3` on `PATH` (`brew install python3` on macOS; on Debian/Ubuntu a 3.12+ package for the release, named as a
      requirement because older releases' `python3` predates it), and state that the build's Python tooling runs under
      it. Done: the row reads `Python 3.12+` with a qualifying install column per platform.
- [x] 7.2 `README.md`: in “Formatting and IDE Setup”, state that Python analysis needs an interpreter selected per
      project, that a 3.12+ interpreter satisfies the floor, and that `setup-idea.sh` does not provision one. Done:
      “Formatting and IDE Setup” states the per-machine selection, the floor, and that `setup-idea.sh` does not provide
      it.
- [x] 7.3 `AGENTS.md`: correct the prerequisites bullet so Python and `actionlint` read as part of the default
      build-and-test path rather than optional tools. Done: the bullet names Java, Docker with Compose, Python 3.12+ and
      `actionlint` as the default path's prerequisites.
- [x] 7.4 `AGENTS.md`: replace the Python gotcha's macOS-specific floor with the declared minimum, keeping its lesson
      (an interpreter newer than the floor does not verify the floor) and the stdlib-only rule. Done: the gotcha now
      cites the declared minimum and the gate that pins it, keeping the stdlib-only rule and the class lesson.
- [x] 7.5 `AGENTS.md`: add the reason the gate pins the interpreter to the `ubuntu-latest` bullet — the image's own
      `python3` moves with the label, so an unpinned gate would silently change what it verifies. Done: the
      `ubuntu-latest` bullet states why the gate pins the interpreter.
- [x] 7.6 `scripts/review-routing.py`: state the declared minimum in its module docstring’s compatibility sentence.
      Done: the docstring cites the declared 3.12 minimum.
- [x] 7.7 `scripts/setup-idea.sh`: state the declared minimum in its prerequisite line. Done: the prerequisite line
      reads 3.12+.
- [x] 7.8 Ignore a personal `.python-version` (`.gitignore`) and document the optional pyenv route in the README, since
      a shared pin cannot express the minimum and the owner asked for the analogous file. Done: `.python-version` is
      ignored — unanchored, like every other entry in that file, because `pyenv local` writes it into the current
      directory — and the README states that the file is a personal selection to keep `python3 --version` honest, not a
      repository runtime pin.

## 8. Sweeps

- [x] 8.1 Sweep every live statement of the old floor: `git grep -n '3\.9' -- . ':!openspec/changes/archive'` and
      `git grep -n 'Python 3'`, fixing each live site (historical records stay as recorded). Done: two Python 3.9 sites
      existed and both were rewritten; the sweep now finds none.
- [x] 8.2 Check `openspec/config.yaml`'s `context:` block and `docs/ideas.md` for a statement this change falsifies.
      Done: neither `openspec/config.yaml` nor `docs/ideas.md` states a false Python claim.
- [x] 8.3 Fix the README statements this change falsifies, keeping the application-runtime and repository-verification
      sets apart: the `/check-tooling` row (“Java, Docker, and the optional tools”) describes the doctor's prerequisite
      set, which now includes Python; the paragraph after the Prerequisites table stays correct about what is needed
      **to run the application** and gains Python only in its `check` clause (“actionlint is needed for the full
      `check`”). Do not add Python to the sentence about running the application. Done: the `/check-tooling` row names
      Python; the application-runtime sentence changed only in its `check` clause.
- [x] 8.4 Confirm no `README` or `AGENTS.md` sentence still calls Python optional for `check`. Done: no README or
      `AGENTS.md` sentence calls Python optional for `check`.

## 9. Verification

- [x] 9.1 `./gradlew spotlessApply` after the last edit, then `./gradlew spotlessCheck`. Done: `spotlessCheck` runs
      inside `check` and is green.
- [x] 9.2 `./gradlew testDoctorCharter` passes and its output reports the new probe count and required set. Done:
      `testDoctorCharter` is green: 14 probes, required includes `python3`.
- [x] 9.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes on this host. Done:
      `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` succeeded.
- [x] 9.4 `openspec validate --all` passes, and `openspec validate --changes` accepts both deltas. Done:
      `openspec validate --all` reports 27 passed, 0 failed.
- [x] 9.5 Manually run the below-floor fixture command from task 5 and paste its output as the control's evidence. Done:
      the fixture run prints `  python3        too-old   3.11.2 (needs >= 3.12) — brew install python3` and exits
      non-zero.
- [x] 9.6 Declare `.github/workflows/ci.yml` among `testDoctorCharter`'s inputs and update its description, since the
      script now reads that file. Done: `build.gradle.kts` lists the input and names the new assertions;
      `./gradlew testDoctorCharter --rerun-tasks` succeeded.

## 10. Review gate

- [x] 10.1 Record the proposal round and each implementation round in `docs/review-rounds.tsv`, running
      `python3 scripts/commit-hygiene.py --unique-rounds --expect adopt-python-3-12-minimum:<round>` and
      `python3 scripts/commit-hygiene.py --session-continuity` after every append. Done: rounds 1–14 are appended, the
      last append verified after the final edit, and both ledger checks
      (`--unique-rounds --expect adopt-python-3-12-minimum:14`, `--session-continuity`) pass.
- [x] 10.2 Derive the routing floor with `python3 scripts/review-routing.py --change adopt-python-3-12-minimum`, record
      its outcome here, and run the deep review when it is `REQUIRED` (or when a `RECOMMENDED` outcome is resolved to
      run). Done: `REQUIRED` on the `[infrastructure-or-deployment] .github/workflows/ci.yml` signal. Deep-review pass 1
      (session `ses_e8e619817ffe3qYqDqz5vCd1KD`) returned one nitpick — `testDoctorCharter`'s declared inputs and
      description do not cover the workflow agreement the script now reads (`build.gradle.kts:521–529`) — status
      `FIXED_AND_VERIFIED` by task 9.6; every other traced area passed, and the two extensions the closeout discussion
      surfaced (the cross-tool install-guidance agreement check, and `actionlint`'s classification) are parked in
      `docs/ideas.md` rather than left as an unnamed follow-up.
- [ ] 10.3 Ask the owner for the manual review pass and stop there.

## 11. Capture

- [x] 11.1 Run `lesson-capture` over the diff and the review rounds, or record the skip and its reason here. Done: the
      skip condition failed, so the subagent ran. Three durable proposals applied — two `AGENTS.md` rules (an install
      hint/column is a claim that following it satisfies the probe and is re-derived when a floor moves; a workflow with
      no `workflow_dispatch` trigger is verified by its own event-triggered run rather than by adding a trigger) and one
      `check-and-test-authoring` bullet widened (the mutation battery covers a `case` glob and a numeric comparison, and
      its fixtures sit at the rule's boundaries) — net `+7` AGENTS.md lines; no retirement proposed. Two items stayed
      routed rather than promoted: a check asserting the doctor's install hint equals the README's column per platform
      (the spec already requires that agreement, and it is automatable), and a change-dir note on the stub-`PATH`
      fixture technique.

## 12. After approval

- [ ] 12.1 Verify the change's own pull-request `build` run: the provisioning step reports 3.12, the assertion step
      prints the resolved interpreter, and the four Python suites run in it.
- [ ] 12.2 Archive the change and confirm the delta sync is staged with the change-dir move.
