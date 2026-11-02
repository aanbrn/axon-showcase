# Design

## Context

`scripts/doctor.sh` is the repository's pre-build diagnostic: a POSIX-shell script that reports one status line per
declared probe and exits non-zero when a prerequisite of the default build-and-test path is unsatisfied. Its declaration
is a single table whose `class` column is the only thing deciding that exit code — `required` sets the failure flag, and
`optional` does not (`report()` in `scripts/doctor.sh`).

`actionlint` is declared `optional` (claims table row 1), yet the default `check` runs `workflowLint`, and that task
refuses to run without the tool: `actionlint is required to lint GitHub workflows` (rows 2 and 3). `AGENTS.md` and the
README already say so — `AGENTS.md` names it among the four prerequisites of the default build-and-test path, and the
README's Prerequisites row reads "The workflow-lint gate in `check`" (rows 8 and 9). So the declaration is the one
surface that disagrees with the other three.

The spec settles the class the declaration should carry: required means "needed to run and verify the application";
optional covers "deployment, security scanning, and the agentic workflow" (row 7). `actionlint` is neither a deployment
nor a scanning nor an agent-workflow tool — it is a member of the verification path, like the Python interpreter the
repository's own `check` scripts run under, which is already `required`.

The defect reproduces in one step. With a `PATH` that provides `java`, `docker`, and `python3` and a reachable daemon,
but no `actionlint`, the doctor exits **0** and reports `actionlint missing brew install actionlint`, while
`./gradlew workflowLint` under the same `PATH` fails with the message above (rows 3 and 4). A contributor follows the
doctor's clean bill of health into a failing `check`.

## Goals / Non-Goals

**Goals.**

- A machine without `actionlint` fails the default diagnosis, with the install hint the README's Prerequisites row
  already carries.
- The required-set control names `actionlint`, so a later class change fails the check rather than drifting unnoticed.
- The two spec scenarios that enumerate the required prerequisites name it.
- The below-floor fixture keeps attributing its verdict to Python alone, with `actionlint` required.

**Non-Goals.**

- Changing `workflowLint`'s behaviour, or removing it from `check`.
- Editing `README.md` or `AGENTS.md`: both already state the requirement (rows 8 and 9).
- The sibling parked idea about asserting the doctor's install hint against the README's install column: a different
  subject, and it touches every probe's wording rather than one class.
- A `## Purpose` refresh: the capability's Purpose describes the probe set generically ("the toolchain the build, tests,
  and deployment depend on"), and `actionlint` was already a probe — only its class changes (row 14).

## Decisions

**1. Flip the declaration's class, and nothing else in the doctor.**

- Chosen: `actionlint|optional|…` becomes `actionlint|required|…`. The detection, the version read, the install hint,
  and the report line are unchanged, so the only behavioural difference is the default run's exit code when the tool is
  absent — which is the whole point.
- Rejected — making `workflowLint` skip when `actionlint` is missing: `AGENTS.md` forbids toleration
  (`continue-on-error`, a skipped gate) because a check that passes falsely is worse than a failing one, and the
  code-quality capability already specifies that the lint gate fails with a message naming the tool.
- Rejected — removing `workflowLint` from `check`: workflow structure and expression errors would then surface only
  remotely, which the code-quality capability explicitly forbids.
- Rejected — leaving the class and changing the docs: the docs already agree with the intended class; it is the doctor
  that is wrong.

**2. The required-set control gains `actionlint`, and its comment follows the spec text it quotes.**

- Chosen: `expected_required` becomes `actionlint compose docker docker-daemon java python3` (the comparison sorts the
  declared `required` rows), and the comment's quotation of the required-tool scenario gains `actionlint`, since it
  quotes the scenario verbatim.
- Rejected — deriving the expected set from the declaration: that would compare the table against itself and pass a
  class change silently. The set is held by hand precisely so it is a second statement of the spec's list.

**3. The below-floor fixture gains an `actionlint` stub.**

- Chosen: the fixture that drives the below-floor verdict already stubs `java`, `docker`, and `python3` and prepends
  them to the host `PATH`; it gains an `actionlint` stub printing a version, so the fixture satisfies every required
  probe on any host. It also asserts that invariant rather than stating it: at the interpreter's floor, every key in
  `expected_required` must report `satisfied` in the fixture's own output, checked before the verdict, so a future class
  change fails naming the unstubbed probe instead of Python.
- Why it is not optional: with `actionlint` required and absent from the host, the `3.12.0` case — which asserts the
  default run exits **0** — would fail with a message naming Python, though Python is at its floor. The exit status
  would no longer be attributable to the interpreter alone, which is what the fixture's own comment claims.
- Rejected — letting the fixture inherit the host's `actionlint`: the test would then pass or fail on whether the
  contributor happens to have the tool, and its failure message would name the wrong subject.
- Rejected — leaving the stub list to the comment's assurance: the list is a hand-copied duplicate of the declaration's
  `required` rows, so the next reclassification re-breaks the fixture silently; the assertion is what makes the
  duplicate fail loudly.

**4. Two `MODIFIED` blocks, one per requirement that enumerates the required set.**

- Chosen: the required/optional requirement's scenario gains `actionlint` in its parenthetical list, and the repo-state
  requirement's Git-hooks scenario gains it in its own list. Both blocks are copied whole from the main spec, so no
  scenario is dropped, and no requirement description changes: the classes are defined generically and the scenarios
  carry the examples.
- Rejected — a new `ADDED` requirement: the behaviour refines the existing requirement rather than introducing a subject
  no requirement covers.
- Rejected — also editing the `actionlint` install hints or version floor: the tool is presence-only today, and this
  change is about the class.

**5. The parked idea is removed in this change.**

- Chosen: delete the entry from `docs/ideas.md`, since the change implements it.
- Rejected — annotating it as promoted: the idea is not deferred to a later change; it is this one.

## Risks

- **A contributor without `actionlint` now gets a non-zero default doctor run.** That is the intent, and the report line
  already carries the per-platform install command, so the failure is actionable rather than merely loud.
- **`--all` behaviour is unchanged.** It already failed on a missing `actionlint`; only the default run's verdict moves,
  so nothing that depended on `--all` changes.
- **The doctor is not part of `check`.** The build asserts the declaration and its reporting (`testDoctorCharter`), not
  the host's toolchain, so CI stays green without `actionlint` on the runner's default `PATH` — except that `check`
  itself runs `workflowLint`, which the CI workflow installs `actionlint` for.
- **The class is a one-word surface with three readers** — the doctor's exit code, the required-set control, and the two
  spec scenarios. The control and the delta keep the three in step; a future probe reclassification still has to touch
  all three, as before.
- **The fixture is not hermetic.** Its stubs are prepended to the host `PATH` rather than replacing it, so a required
  tool the fixture omits still resolves from the host. The baseline assertion covers attribution — an omitted stub the
  host lacks fails naming the probe — and no control here asserts hermeticity, which is this decision's accepted
  limitation rather than a gap in the guard.

## Claims

Base revision for the before-state rows: `git merge-base HEAD origin/main` = `ee7bdc58`.

| #   | Claim                                                                                         | Source                                                                                                                   | Result                                                                                                                                                                                                                                                                                                                                          |
| --- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | The doctor declares `actionlint` as an optional probe                                         | `grep -n '^actionlint' scripts/doctor.sh` at `ee7bdc58`                                                                  | `actionlint\|optional\|tool\|yes\|actionlint\|--version\|` — one row of the declaration; its `class` column is what an unsatisfied probe does to the default run's exit code                                                                                                                                                                    |
| 2   | The default `check` runs `workflowLint`, which requires `actionlint`                          | `build.gradle.kts:834` and `build-logic/src/main/kotlin/workflow-lint-conventions.gradle.kts`                            | `check` depends on `workflowLint`; the task fails with `actionlint is required to lint GitHub workflows` when `actionlintExecutable()` returns an empty string                                                                                                                                                                                  |
| 3   | A machine without `actionlint` fails `check` today                                            | `PATH=<stub: java, docker, python3> ./gradlew workflowLint --offline --console=plain`                                    | `Task :workflowLint FAILED` — `actionlint is required to lint GitHub workflows. Install it from https://github.com/rhysd/actionlint (brew, go install, or a release binary)`                                                                                                                                                                    |
| 4   | The same machine passes the doctor, which reports `actionlint` missing                        | `PATH=<the same stub> sh scripts/doctor.sh; echo $?`                                                                     | exit `0`; `actionlint missing brew install actionlint`; `java`, `docker`, `python3`, `docker-daemon` satisfied, and `actionlint` absent from that `PATH`                                                                                                                                                                                        |
| 5   | The required set is a hand-held control compared against the declaration                      | `sed -n 150,156p scripts/test-doctor.sh`                                                                                 | `expected_required="compose docker docker-daemon java python3"`, compared to the declaration's `required` rows (sorted); a class change fails until the control names it                                                                                                                                                                        |
| 6   | Two spec scenarios enumerate the required prerequisites                                       | `sed -n '96,97p;128,129p' openspec/specs/showcase/quality/toolchain-check/spec.md`                                       | lines 96–97: "(Java, Docker with / Compose, or the Python 3.12+ interpreter the build's Python tooling runs under)"; lines 128–129: "(Java, / Docker with a live daemon, Compose v2, Python 3.12+)" — each parenthetical spans two source lines, and the delta adds `actionlint` to both lists, which reaches the main spec at the archive sync |
| 7   | The spec's optional class covers deployment, scanning, and the agent workflow — not this tool | the requirement's description (lines 81–82) and the declaration's `optional` rows                                        | optional = "deployment, security scanning, and the agentic workflow"; the optional probes are `pack`, `helm`, `kubectl`, `snyk`, `gh`, `opencode`, `ast-grep`, `git-hooks`                                                                                                                                                                      |
| 8   | `AGENTS.md` already places `actionlint` on the default build-and-test path                    | `grep -n 'Only Java, Docker with Compose' AGENTS.md`                                                                     | line 809 names it among the four prerequisites, and line 810 says it runs `workflowLint`, "also part of `check`"                                                                                                                                                                                                                                |
| 9   | `README.md` already documents it as a `check` prerequisite                                    | `grep -n actionlint README.md`                                                                                           | the Prerequisites row reads "The workflow-lint gate in `check`"; line 663 reads "actionlint and Python 3.12+ are needed for the full `check`"                                                                                                                                                                                                   |
| 10  | The below-floor fixture stubs three tools and inherits the rest of the host `PATH`            | `sed -n '250,272p' scripts/test-doctor.sh`                                                                               | stubs for `java`, `docker`, `python3`; `PATH="$fixture_dir:$PATH"`; the `3.12.0` case asserts the default run exits `0`, so an absent required tool on the host would fail it naming Python                                                                                                                                                     |
| 11  | The fixture keeps its attribution once `actionlint` is required                               | `bash scripts/test-doctor.sh` under a `PATH` that lacks `actionlint`, with and without the fixture's stub                | with the stub the suite exits `0` and prints `14 probes; required: actionlint compose docker docker-daemon java python3`; with the stub removed the same run exits `1` on `fixture leaves required probe 'actionlint' unsatisfied at the floor — the verdict is not Python's alone`                                                             |
| 12  | A probe's class is the only input to the default run's exit code                              | `report()` in `scripts/doctor.sh`                                                                                        | `fail_required` is set only for an unsatisfied `required` probe (and an unreadable required version); every unsatisfied probe sets `fail_any`, which `--all` acts on                                                                                                                                                                            |
| 13  | Only `test-doctor.sh` holds the required set by hand                                          | `grep -rn 'expected_required\|required set' scripts build.gradle.kts openspec/specs README.md AGENTS.md .github`         | the control at `scripts/test-doctor.sh:155`; the spec references "the spec's required set" generically and enumerates it only in the two scenarios above                                                                                                                                                                                        |
| 14  | The capability's Purpose does not enumerate classes, so no Purpose refresh is owed            | `sed -n 3,8p openspec/specs/showcase/quality/toolchain-check/spec.md`                                                    | "it probes the toolchain the build, tests, and deployment depend on, reports each prerequisite's status" — generic, and `actionlint` was already a probe                                                                                                                                                                                        |
| 15  | The parked idea names exactly this change                                                     | `grep -n 'Classify .actionlint' docs/ideas.md`                                                                           | line 275: "Classify `actionlint` as a required probe in the doctor, with its required-set assertion and the spec example"                                                                                                                                                                                                                       |
| 16  | The live tree after the change passes the doctor and its control                              | `PATH=<the stub> sh scripts/doctor.sh; echo $?`, `bash scripts/test-doctor.sh`, and the class flipped back to `optional` | the doctor exits `1` with `actionlint missing brew install actionlint` and `at least one required prerequisite is unsatisfied`; the control exits `1` naming both required sets when the class is reverted and `0` restored; on this machine the doctor exits `0`                                                                               |
| 17  | The fixture's baseline assertion names the probe a stub change drops, not Python              | the `actionlint` stub removed again after the assertion landed, under a `PATH` that lacks the tool                       | the run exits `1` on `fixture leaves required probe 'actionlint' unsatisfied at the floor — the verdict is not Python's alone`; restored, it exits `0`                                                                                                                                                                                          |

Dismissals, one reason per candidate kind `scripts/claim-scan.py` reports:

- `[count]` — a numeral in these artifacts labels a step, a requirement, a declared table row, a version, or the line of
  a cited source; none is a total of a corpus, so none needs a deriving command of its own.
- `[absolute]` — a universal or absence qualifier is normative text (a requirement, a decision, a scenario) or a rule's
  own wording; every factual instance is a claims row, derived by the source its Source cell names.
- `[cross-ref]` — the candidates are slash-joined identifiers (script variables, term pairs) and the paths of files the
  change reads, edits, or records as a task's destination; the change cites no tracker reference.
- `[path]` — every candidate is a code span carrying a command, a reported message, a script variable, or a path the
  change reads or edits; each path exists in the tree or is created by this change.
