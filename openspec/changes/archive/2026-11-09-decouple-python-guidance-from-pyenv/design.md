# Design — decouple the optional interpreter guidance from pyenv

## Context

The repository needs one thing from Python: a `python3` of 3.12 or newer on `PATH`, which the build, the commit hook,
and the setup resolve by that rule. Two archived decisions chose that deliberately rather than a runtime pin:
`adopt-python-3-12-minimum`'s D6 (no `.python-version` pin, `PATH` resolution, the file documented as personal) and
`reproduce-python-ide-setup` (the same resolution kept, a pyenv-specific resolver rejected). Nothing in the repository
requires pyenv to execute, and the scripts import only the standard library plus one local module.

What the guidance does not carry is that choice's consequence: the live pyenv mentions are conditional examples, but
they read as though pyenv were the mechanism, and every reader-facing one that gives a concrete command gives a
pyenv-only one. A reader on uv gets a remedy that does not apply to them. `uv python pin` writes the same
`.python-version`, but the repository itself selects `python3` from `PATH`, so a pin matters only where the local
environment integrates it into that selection — pyenv's shims do here — and an uninstalled pin can leave `python3` on a
different interpreter. That is why the guidance's own check (`python3 --version` in the environment running the tooling)
is the part that must survive.

## Decisions

### D1 — The guidance states the condition; one concrete example stays

The change removes pyenv as the named mechanism. It keeps `pyenv local` as the one concrete example, because it is the
command whose effect this machine demonstrates, and states the condition that example must satisfy — `python3` resolves
to that interpreter. It adds no uv-specific behavior: the repository has no uv-specific need (C8, C9), and a pin matters
only where the environment integrates it into the selection (C10, C11), so the README names uv only to say that its own
commands read the same file.

### D2 — uv gains no prerequisite, doctor probe, or Prerequisites row

The doctor's `python3` probe diagnoses the executable the build actually runs. An optional doctor entry covers a
distinct repository workflow, not another supplier of a prerequisite the probe already covers, so a uv row would widen
the supported toolchain without serving a demonstrated need.

### D3 — The remedy keeps a concrete command, so one example and the condition are stated

A vague remedy ("use whichever manager you have") costs the reader the step that made the original useful. One command
is stated with the condition it must satisfy, so a reader on another manager can satisfy the condition with their own
tool and is not excluded.

### D4 — The test pins the remedy's condition and example

`test-ensure-idea-settings.py` already asserts the remedy's wording, including the concrete command. It gains the
condition phrase, so the test pins the corrected promise rather than the old parenthetical.

### D5 — No spec delta

`showcase/quality/ide-config`'s requirement "The Python analysis interpreter is the one the build runs" specifies the
selection rule, the interpreter matching, and the `.python-version` inheritance without naming a manager; the delta
would restate the requirement to say the same thing. The change is `skip_specs: true`.

## Claims table

| #   | Claim                                                                                                                    | Source or command                                                                                                                                   | Result                                                                                                                                                         |
| --- | ------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| C1  | uv honors a `.python-version` and resolves the interpreter it names                                                      | `d=$(mktemp -d); printf '3.12.16\n' > "$d/.python-version"; (cd "$d" && uv python find)`                                                            | `/Users/afanasyev/.pyenv/versions/3.12.16/bin/python3.12`                                                                                                      |
| C2  | `uv python pin` writes the version into `.python-version`                                                                | `d=$(mktemp -d); (cd "$d" && UV_PYTHON_DOWNLOADS=never uv python pin 3.12.16); cat "$d/.python-version"`                                            | `Pinned \`.python-version\` to \`3.12.16\``; the file holds `3.12.16`                                                                                          |
| C3  | uv reports `~/.local/bin` as the directory it installs Python executables into                                           | `uv python dir --bin`, whose `--help` reads "Show the directory into which `uv python` will install Python executables"                             | `/Users/afanasyev/.local/bin`                                                                                                                                  |
| C4  | On this machine pyenv's shims precede `~/.local/bin` on `PATH`                                                           | `echo "$PATH" \| tr ':' '\n' \| grep -n -e '\.pyenv/shims' -e '\.local/bin'`                                                                        | `1:/Users/afanasyev/.pyenv/shims`, `5:/Users/afanasyev/.local/bin`                                                                                             |
| C5  | A pin naming an uninstalled version does not fall through to that version                                                | `d=$(mktemp -d); printf '3.13.99\n' > "$d/.python-version"; (cd "$d" && uv python find)`                                                            | exit 2, `pyenv: version '3.13.99' is not installed (set by …/.python-version)` — the error is pyenv's shim's, which uv's discovery consulted                   |
| C6  | No capability spec names pyenv                                                                                           | `git grep -n "pyenv" bfe30a08 -- openspec/specs`                                                                                                    | no hits (exit 1)                                                                                                                                               |
| C7  | Every live pyenv mention is one this change edits, outside the dated records                                             | `git grep -n "pyenv" bfe30a08 -- ':!openspec/changes/archive' ':!docs/units'`                                                                       | `README.md` 1030, 1043, 1045; `setup-idea/SKILL.md` 40, 59; `ensure-idea-settings.py` 653; `test-ensure-idea-settings.py` 463                                  |
| C8  | The repository carries no Python package manifest, requirements file, or lockfile                                        | `git ls-tree -r --name-only bfe30a08 \| grep -E 'pyproject\.toml\|requirements.*\.txt\|setup\.py\|\.lock'`                                          | no hits                                                                                                                                                        |
| C9  | The scripts import only the standard library plus one local module                                                       | `git grep -h -E '^(import\|from) ' bfe30a08 -- 'scripts/*.py'`                                                                                      | every top-level module is in `sys.stdlib_module_names` except `review_vocabulary`, which `git ls-tree bfe30a08 scripts/review_vocabulary.py` shows in the tree |
| C10 | A `.python-version` affects a bare `python3` only where the environment integrates it into the selection                 | `d=$(mktemp -d); printf '3.12.16\n' > "$d/.python-version"; (cd "$d" && PATH="$HOME/.local/bin:/opt/homebrew/bin:/usr/bin:/bin" python3 --version)` | `Python 3.15.2` — the next `PATH` entry, not the pinned version; with the login `PATH` the same command prints `3.12.16`                                       |
| C11 | An uninstalled pin can leave a bare `python3` on a different interpreter, an observation rather than a claimed mechanism | `d=$(mktemp -d); printf '3.13.99\n' > "$d/.python-version"; (cd "$d" && python3 --version)`                                                         | `Python 3.15.2`, exit 0; the archived `adopt-python-3-12-minimum` D6 records the same observation                                                              |
| C12 | The build, the commit hook, and the setup resolve the first `python3` on `PATH`                                          | `git grep -n "python3" bfe30a08 -- build.gradle.kts scripts/git-hooks/pre-commit scripts/setup-idea.sh`                                             | `build.gradle.kts` 492–498 (`firstOrNull { it.canExecute() }`), `scripts/git-hooks/pre-commit` 17 (`exec python3 …`), `scripts/setup-idea.sh` 108–112          |

`bfe30a08` is the unit's base (`git merge-base HEAD origin/main` at the branch fork); the dated records under
`openspec/changes/archive/` and `docs/units/` are history and are excluded by the criterion C7 states.

## Dismissals

Resolved from `python3 scripts/claim-scan.py --base origin/main`; each kind's candidates that are not a row above. The
reasons are the artifact's own claim kinds rather than the scanner's regex kinds, because a list of the candidates or of
an output's lines is incomplete by construction — the artifact stating it adds candidates to the scanned set — while
these four classes are fixed by what the artifacts can state.

| Scanner kind | The claim kind each candidate falls into                                                                                                                                                                                                                    | Disposition                                                                      |
| ------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| count        | a task, round, or line number; a literal a task prescribes; a figure a check or the hand-over reports; the review deadline                                                                                                                                  | no row: none is a total over a corpus or an enumerated list                      |
| absolute     | a statement about one file or one command's result, a repository claim a row carries (C6, C7, C8, C9, C12), or a non-claim the artifacts state (a design decision, a task instruction, an artifact schema answer, a record's own statement about its round) | no row beyond those                                                              |
| cross-ref    | a script this change invokes, or a record field, record id, or shorthand a record uses for its own findings or artifacts                                                                                                                                    | no row: each resolves in the tree or in the unit's own records                   |
| path         | a command's own text or the path it printed; one of the four files this change edits; a path or workspace field of the unit's records; a cited capability path                                                                                              | no row: each is produced by the row's own command or exists at the base revision |

The one corpus claim no row carries is the Context's statement that nothing requires pyenv to execute, which C7's grep
establishes.

## Non-goals

- Adding uv to the prerequisites, the doctor's probe table, or the README Prerequisites table.
- Changing the interpreter resolution, the `.python-version` inheritance, or the CI provisioning.
- Provisioning an interpreter from `setup-idea.sh`.

## Verification

- `python3 scripts/test-ensure-idea-settings.py` passes, including the changed assertion.
- `./gradlew spotlessApply` and `./gradlew spotlessCheck` pass.
- `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes.
- `openspec validate --all` accepts the change's `skip_specs`.
