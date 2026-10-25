# Design

## Context

### Where the current floor comes from

- `README.md` documents the prerequisite as **`Python 3`** whose macOS install path is “Ships with macOS Command Line
  Tools”.
- macOS's Command Line Tools ship `/usr/bin/python3` **3.9.6**, so “the documented Python” is 3.9 by inheritance from
  Apple rather than by a decision recorded anywhere.
- `AGENTS.md`'s Python gotcha was added by `fbf6a940` (“Capture the pre-commit guard's lessons (#373)”) as a
  **verification** lesson: a script verified under a newer PATH interpreter imports on a contributor's machine only to
  raise `TypeError` on a PEP 604 union. Its subject is the _class_ of mistake, not a supported-version policy.

### Why no gate verifies any floor today

| Surface                               | Current state                                                                     |
| ------------------------------------- | --------------------------------------------------------------------------------- |
| `build.gradle.kts` `pythonExecutable` | the first executable `python3` on `PATH`; no version assertion                    |
| `scripts/doctor.sh` probe table       | `python3` is `optional`, floor column holds a **major** number (`3`)              |
| `scripts/doctor.sh` comparison        | compares `major` only, so `3.11` and `3.9` are indistinguishable from `3.12`      |
| `scripts/test-doctor.sh` required set | held literally against the spec, and it names `compose docker docker-daemon java` |

So the floor is stated in prose only, and the doctor cannot express a minor floor even if the declaration carried one.

### The trigger

The owner reported the IDE showing a mass of diagnostics on `scripts/`; the measured cause was that no Python
interpreter is configured for the project, so nothing resolves (the IDE then reports every import and builtin as
unresolved). The immediate remedy is a local interpreter selection, which is per-machine and unversioned. The durable
half is the one this change delivers: the repository should **choose** its Python minimum and verify it, so the
interpreter a contributor (or the IDE) selects has a declared floor to match.

### Why the number is unverifiable today

The floor lives in prose: the README's `Python 3` row, the doctor's declaration (`… | python3 | --version | 3`), the
`AGENTS.md` prerequisites bullet and its Python gotcha, and `scripts/setup-idea.sh`'s prerequisite line. None of them is
the declaration a check compares against, and no gate asserts an interpreter's version (claims row 17), so the change's
first decision is to declare the floor.

## Decisions

### D1 — The declared minimum is Python 3.12

| Candidate       | Verdict                                                                                                                                                                                            |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 3.9 (inherited) | Rejected: end-of-life 2025-10-31, and `actions/python-versions` carries **no** 26.04 build for any 3.9 release, so verifying it in CI breaks when `ubuntu-latest` moves.                           |
| 3.10            | Rejected: end-of-life 2026-10-01.                                                                                                                                                                  |
| **3.12**        | **Chosen**: Ubuntu 24.04's `python3` is 3.12.3, the `ubuntu-24.04` runner image's own `python3` is 3.12.3, setup-python has 24.04 and 26.04 builds (from 3.12.13), and support runs to 2028-10-31. |
| 3.13 / 3.14     | Not chosen: they buy a longer horizon but exclude contributors on 24.04 LTS and on 3.12 without installing another interpreter, while the scripts already run under 3.9.6 (claims row 15).         |

The floor is a **minimum**, not a pinned runtime: CI provisions the current 3.12 patch, and a developer may use any
interpreter at or above the floor.

### D2 — Both quality gates provision the floor; the fast gate asserts it

- `ci.yml` gains `actions/setup-python@v7` with `python-version: '3.12'` before the quality-gates step, which also
  removes the silent interpreter jump the image migration would otherwise cause.
- A following step resolves `command -v python3` — the binary `build.gradle.kts` picks first from `PATH` — and asserts
  its directory is the one setup-python installed and that its version is at least the installed version, taking both
  expectations from the setup step's own `python-version` and `python-path` outputs (one holds the interpreter's path,
  the other its version), so the workflow carries one copy of the number.
- The full gate on `main` provisions the same interpreter, so the integration path exercises the same floor.

### D3 — The spec owns the predicate; the declaration implements it; a check ties the copies together

`test-doctor.sh` already holds `expected_required` **against the spec** rather than against the declaration (“so the
comparison is against the spec rather than the declaration against itself”). The floor follows the same shape:

1. `showcase/quality/toolchain-check` states 3.12 — the one normative site.
2. The doctor's probe table carries the floor, the machine-readable implementation.
3. `test-doctor.sh` asserts the declaration's floor equals the spec's 3.12, that the README row names it, and that
   `ci.yml`'s `setup-python` input equals it.

The number also appears in prose this unit adds — `AGENTS.md`, two script docstrings — and in the assertion step's
output-derived comparison; those are not tied by the check and are sweep-maintained when the floor moves, which is why
the assertions cover the three surfaces a bump must not miss and not “every copy”.

### D4 — The floor stays in the existing declaration, in POSIX shell

The doctor must run with nothing but a POSIX shell, so the comparison stays in shell: the floor column accepts
`major[.minor]` (a third component is rejected, not read), and an extracted `version_at_least` helper compares numeric
major, then minor, with an integer floor read as `major.0`. A non-numeric or unreadable version keeps reporting
`unknown` rather than satisfied, the existing integer floors (Java 21, Helm 4) keep working unchanged, and the helper's
known-good and known-bad cases live in `test-doctor.sh` — a checklist task is not a control.

### D5 — Behavioural controls, not declaration assertions

The doctor's floor behaviour is proved over a **fixture directory prepended to the host `PATH`** — stub `java`, `docker`
(including `info` and `compose version`) and `python3` scripts, with the `java` stub reporting a supported version so
the satisfied rows can also exit zero — so the below-floor case can be produced on any machine without Docker. The
fixture is prepended rather than substituted because the doctor itself needs the host's `awk`, `grep`, `sed` and `git`:

| Fixture                  | Assertion                                                                                   |
| ------------------------ | ------------------------------------------------------------------------------------------- |
| `python3` reports 3.11.2 | the `python3` line reads `too-old`, names both versions, and the default run exits non-zero |
| `python3` reports 3.12.0 | the line reads `satisfied`, and the default run exits zero                                  |
| `python3` reports 3.15.1 | the line reads `satisfied` (a newer interpreter is above the floor)                         |

Each control is confirmed by running it against the **pre-change** doctor and seeing it fail, and by neutering the
comparison afterwards, since a control that never failed proves nothing.

### D6 — No runtime pin, no resolver change, no IDE automation

- No `.python-version`: the gate resolves `PATH` (`build.gradle.kts`), so a pin would not bind it, and a scan of the 63
  jars of the Python plugin as shipped in IDEA 2026.2 (build IU-262.8665.337) finds no reader for that file — only the
  `python-version-info` payload it reports. The scan cannot prove every integration ignores the file; the `PATH`
  resolution above is what makes the pin advisory regardless.

  Measured on the owner's machine (pyenv 2.8.13, installed `3.12.1`): with `.python-version` naming an **installed**
  version, `python3 --version` reports it; naming an **uninstalled** one, `pyenv version` warns on stderr while
  `python3 --version` still succeeds with a _different, newer_ interpreter from the next `PATH` entry — so such a file
  can read as a pin while running something else. This is one machine's measurement, not a claim about pyenv's
  mechanism, and the file's optional personal use is documented in the README instead.

- `pythonExecutable` keeps resolving `PATH`, so CI provisioning is what selects the interpreter on the runner and no
  local machine's resolution changes.
- `setup-idea.sh` keeps merging settings and installing formatter plugins; it does not provision interpreters. The
  README documents the selection instead, because it is per-machine and `.idea/` is ignored.

### D7 — `actionlint`'s classification is a separate unit; the documentation sentence is corrected here

`AGENTS.md`'s prerequisites bullet calls both `Python 3` and `actionlint` optional although `check` requires both. The
sentence is corrected in this change because leaving a falsified sentence in place is not an option; the doctor's
`actionlint` **classification** (declaration, required-set assertion, spec example) is the pre-existing drift Unit 2
fixes. The two are split because they share no outcome: this unit's subject is the Python floor.

### D8 — The workflow edit is verified by the pull request's own run

`ci.yml` triggers on `pull_request` and `push` to `main` and has no `workflow_dispatch`, so the dispatch procedure's
trigger (a dispatch-enabled scheduled workflow) does not apply. The change's own `build` run exercises the provisioning
step and the Python tasks; a dispatch would need a `workflow_dispatch` trigger added purely to satisfy a rule that does
not reach this file.

### D9 — No capability Purpose refresh is owed

`showcase/quality/toolchain-check`'s Purpose already describes a diagnostic probing “the toolchain the build, tests, and
deployment depend on”. Making Python required refines an item inside that scope without widening the capability, and
`showcase/quality/merge-governance`'s Purpose describes branch protection and CI gates rather than the interpreter a
gate provisions.

## Alternatives considered

| Alternative                                            | Why not                                                                                                                                                                           |
| ------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Keep 3.9 and verify it in CI                           | No 26.04 build exists for 3.9 (0 of 32 releases), so the verification breaks as the label migrates; 3.9 is also a year past end-of-life.                                          |
| Leave the floor undeclared and document IDE setup only | Treats the symptom: the interpreter has no declared floor to match, so any selection may be below what the scripts are verified against.                                          |
| A version matrix over several Pythons                  | Doubles the gate's cost to prove what one run at the floor plus each developer's newer interpreter already covers.                                                                |
| `pyproject.toml` / `.python-version`                   | Nothing binds the gate and the IDE does not read them, so neither resolves the incident; a `.python-version` would also pin one installed patch rather than the declared minimum. |
| Wire the doctor into `check`                           | The host is not a build input; the existing rule keeps the doctor out of `check` and asserts its declaration instead.                                                             |

## Risks

- **macOS contributors must install Python.** The documented prerequisite stops being “nothing to install”, which is a
  real onboarding cost; the README states the floor and the doctor's install hint names a command that leaves a
  qualifying `python3` on `PATH`.
- **The doctor now fails without a suitable Python.** That is the honest reading of the default path: `check` runs the
  Python suites unconditionally, so a machine without them cannot verify a change.
- **The README/declaration/workflow numbers drift.** Closed by the three agreements D3 adds, each with a control.
- **A newer runner interpreter silently changes what the gate verifies.** Closed by provisioning the floor explicitly
  and asserting the resolved interpreter's version.

## Claims table

Each row states a fact this change rests on, with the command that derives it, re-run at the recorded state.

| #   | Claim                                                                                                                                   | Source / command                                                                                                                                                                                                                                                                    | Observed                                                                                                                                                                |
| --- | --------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `check` depends on four Python test tasks (and the doctor charter, which is shell)                                                      | `grep -n 'dependsOn("test' build.gradle.kts`                                                                                                                                                                                                                                        | five `dependsOn` lines, four of them Python suites                                                                                                                      |
| 2   | The gate's interpreter is the first `python3` on `PATH`                                                                                 | `sed -n '469,475p' build.gradle.kts`                                                                                                                                                                                                                                                | `System.getenv("PATH") … firstOrNull { it.canExecute() }`                                                                                                               |
| 3   | The doctor's floor is major-only                                                                                                        | `sed -n '159,162p;288,300p' scripts/doctor.sh`                                                                                                                                                                                                                                      | `^[0-9]+$` validation; `[ "$major" -ge "$min_major" ]`                                                                                                                  |
| 4   | `test-doctor.sh` holds the required set against the spec                                                                                | `sed -n '145,157p' scripts/test-doctor.sh`                                                                                                                                                                                                                                          | `expected_required="compose docker docker-daemon java"`, comment names the spec                                                                                         |
| 5   | The Python 3.9 floor is stated in two live sites: the `AGENTS.md` Python gotcha and `scripts/review-routing.py`                         | `git grep -nE '(3\.9\)\|Python 3\.9)' -- AGENTS.md scripts/`                                                                                                                                                                                                                        | the gotcha's “`/usr/bin/python3` (3.9)” and `review-routing.py`'s “Python 3.9-compatible”                                                                               |
| 6   | Ubuntu 24.04 ships Python 3.12.3                                                                                                        | `curl -s 'https://api.launchpad.net/devel/ubuntu/+archive/primary?ws.op=getPublishedSources&source_name=python3.12&exact_match=true&status=Published&distro_series=https%3A//api.launchpad.net/devel/ubuntu/noble&ws.size=1'`                                                       | `python3.12 3.12.3-1ubuntu0.17 in noble`                                                                                                                                |
| 7   | setup-python has 3.12 builds for 24.04 and 26.04; no 3.9 build exists for 26.04                                                         | `curl -s https://raw.githubusercontent.com/actions/python-versions/main/versions-manifest.json` then filter `platform_version`                                                                                                                                                      | 3.12.15 has `22.04`/`24.04`/`26.04`; 3.9: 0 of 32 releases carry `26.04`                                                                                                |
| 8   | Python 3.12 is supported to 2028-10-31; 3.10 ended 2026-10-01                                                                           | `curl -s https://endoflife.date/api/python.json`                                                                                                                                                                                                                                    | `3.12 eol 2028-10-31`, `3.10 eol 2026-10-01`                                                                                                                            |
| 9   | `ubuntu-latest` moves to Ubuntu 26.04 in November 2026                                                                                  | `curl -s https://raw.githubusercontent.com/actions/runner-images/main/images/ubuntu/Ubuntu2404-Readme.md`                                                                                                                                                                           | announcement row linking `actions/runner-images#14748`                                                                                                                  |
| 10  | macOS's Command Line Tools ship Python 3.9.6, the “documented Python” the old gotcha derived its floor from                             | `/usr/bin/python3 --version`                                                                                                                                                                                                                                                        | `Python 3.9.6`                                                                                                                                                          |
| 11  | The IDE's Python plugin (IDEA 2026.2, build IU-262.8665.337) carries no reader for `.python-version`                                    | `find "$PLUGIN" -name '*.jar' -print0 \| xargs -0 -n1 sh -c 'unzip -p "$0" 2>/dev/null \| strings \| grep -q "python-version" && echo "$0"'`, where `$PLUGIN` is the loaded `python-ce` plugin path (63 jars; NUL-delimited because the path holds a space)                         | one jar: `lib/modules/intellij.python.psi.impl.jar`, whose only hit is the string `python-version-info`                                                                 |
| 12  | The Python gotcha entered `AGENTS.md` with commit `fbf6a940` (“Capture the pre-commit guard's lessons (#373)”) as a verification lesson | `git log -S 'documented prerequisite' --format=%h -- AGENTS.md`                                                                                                                                                                                                                     | `fbf6a940`                                                                                                                                                              |
| 13  | `actions/setup-python` exposes the installed version and path as outputs, so the assertion step needs no second literal                 | `gh api 'repos/actions/setup-python/contents/action.yml?ref=v7.1.0' --jq .content \| base64 -d \| sed -n '/^outputs:/,/^runs:/p'`                                                                                                                                                   | `python-version`, `cache-hit`, `python-path`                                                                                                                            |
| 14  | Homebrew's `python@3.12` does not put `python3` on `PATH`; its unversioned symlinks land in `libexec/bin`                               | `brew info python@3.12` (Caveats)                                                                                                                                                                                                                                                   | “python3 … installed into /opt/homebrew/opt/python@3.12/libexec/bin”; `brew install python3` is the formula the caveat names for a `PATH` `python3`                     |
| 15  | The scripts run unchanged on Python 3.9.6, so raising the floor needs no migration                                                      | `/usr/bin/python3 scripts/test-claim-scan.py` (and `test-commit-hygiene.py`, `test-review-rounds.py`, `test-review-routing.py`), plus `/usr/bin/python3 -c "import runpy; runpy.run_path('scripts/ensure-idea-settings.py', run_name='not_main')"` for the one script with no suite | all five exit 0 under `Python 3.9.6`                                                                                                                                    |
| 16  | The `ubuntu-24.04` runner image's own `python3` is 3.12.3                                                                               | `curl -s https://raw.githubusercontent.com/actions/runner-images/main/images/ubuntu/Ubuntu2404-Readme.md \| grep -n '^- Python'`                                                                                                                                                    | `- Python 3.12.3`                                                                                                                                                       |
| 17  | No gate asserts the interpreter's version                                                                                               | `git grep -n 'python' -- .github/workflows/ci.yml build.gradle.kts`                                                                                                                                                                                                                 | `ci.yml` holds one inline `python3 - <<'PY'` heredoc and no setup or version check; `build.gradle.kts` only resolves `pythonExecutable` and passes it to the Exec tasks |

### Claim-scan dismissals

`python3 scripts/claim-scan.py --base origin/main` over this change dir yields candidates in four kinds; each is
dismissed by kind, and the reason is checked against the artifacts rather than asserted:

| Kind        | Dismissal                                                                                                                                                                                                                                                                                                    |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `count`     | The numeral heuristic matches task and section numbering (`1.1`, `9.3`), version numbers (`3.11`, `21`, `26.04`) and cross-reference numbers — none is a count of a corpus, and the table's own counted figures (releases, jars) are rows 7 and 11.                                                          |
| `path`      | Backticked paths and commands name the files and tasks this change reads or edits, plus the change's own artifacts; they are the sites the tasks act on, not claims about a corpus.                                                                                                                          |
| `absolute`  | The quantifiers are prose qualifiers that quantify no set — `only to raise`, `major-only`, `cannot prove every integration`, `re-run every row's command` — plus requirement text copied verbatim from the main specs; the quantified claims the change does make about a set are carried by their own rows. |
| `cross-ref` | Path-like prose compounds — `Debian/Ubuntu`, `README/declaration/workflow`, `required/optional`, the Homebrew `libexec` path, and the workspace path — plus the PR reference `#373`, which row 12 resolves. None is an unresolved artifact reference; the scanner reads them out of prose.                   |

## Non-Goals

- `actionlint`'s required/optional classification, its required-set assertion and its spec example (Unit 2).
- Modernizing the scripts' syntax to anything newer than the floor.
- Pinning a developer runtime (`.python-version`, `pyproject.toml`), a version matrix, or changing `ubuntu-latest`.
- Changing `pythonExecutable`'s resolution, the pre-commit hook, or `setup-idea.sh`'s behaviour.
- Automating the IDE interpreter selection.
