# Design

## Context

See `proposal.md` — Why. The mechanism the change extends is the setup that already reconciles `config/idea/*.xml` into
`.idea/`; the state it must produce is the one the owner configured by hand in the primary checkout (a `PYTHON_MODULE`
covering `scripts/` that references a Python SDK registered in IntelliJ's global table). Two facts shape the approach:
`.idea/` is ignored entirely, so nothing generated may be committed; and the interpreter lives in a table IntelliJ owns
per configuration directory, which a setup run can read but must not write.

The delta adds four requirements to `showcase/quality/ide-config`; this document explains how they are satisfied and
does not restate them.

Observed while planning, and the reason the stage reports the interpreter it used: in this unit's own worktree as forked
from `origin/main` — with no `.python-version`, which is ignored — `pyenv version-name` returned `3.12.1`, the global
version, where the primary checkout returned `3.12.16`, so `python3 -c 'import sys; print(sys.executable)'` reported the
3.12.1 binary. Task 7.2 then wrote this tree's own pin, and the same commands now report `3.12.16` here. The mechanism
reproduces per working tree; the exact patch version is the contributor's local pin, which the design deliberately does
not manage.

## Goals / Non-Goals

**Goals:**

- One command per working tree reproduces the Python analysis module, on any machine where IntelliJ has already
  registered the interpreter the build uses.
- Every input the stage depends on is read from the tree or from the IDE's own files, never guessed.
- A state the stage cannot complete is reported as such, with the exact action, and never written as a broken reference.

**Non-Goals:**

- Creating or repairing a Python SDK entry in IntelliJ's table.
- Choosing an interpreter other than the one `PATH` gives the build.
- Identical patch versions across working trees, or a shared runtime pin.
- Any `.venv`, any change to `.gitignore`, CI, or the toolchain doctor.
- A background daemon or file watcher: the stage runs when the setup runs.

## Decisions

### D1 — The stage lives in `ensure-idea-settings.py`, not in a second script

The Python stage is a further reconcile step of the same script `setup-idea.sh` already invokes.

- **Why:** one entry point keeps one exit-code contract and one test suite; the script already owns "reconcile the
  project's IDE files".
- **Alternative rejected — a second, dedicated settings script:** two entry points, two test tasks, and a second
  definition of "configured".
- **Alternative rejected — a Gradle task:** the setup is not a build step; it must run without a JDK-driven build and
  before the IDE's first reload.

### D2 — Interpreter selection copies the build's rule and resolves the real binary

The stage runs `python3 -c 'import sys; print(sys.executable)'` from the working tree root and uses the reported path,
after asserting `sys.version_info >= (3, 12)`.

- **Why:** `build.gradle.kts` picks the first `python3` on `PATH`, and the pyenv shim in front of it resolves per
  working directory — copying that rule keeps the IDE and `./gradlew check` on one interpreter without encoding a
  pyenv-specific rule the build does not have. `sys.executable` yields the interpreter the command actually ran, not the
  shim path, so the SDK reference and the IDE's own table entry agree.
- **Alternative rejected — `pyenv which python3`:** a selection rule the build does not use, which also fails where
  pyenv is absent (it is not a repository prerequisite).
- **Alternative rejected — a per-worktree `.venv`:** a second interpreter that Gradle does not use, plus `.venv` is not
  ignored today, so it would add a `.gitignore` change to an IDE-configuration change.
- **Alternative rejected — writing `.python-version` when absent:** it is ignored on purpose, and the version pins the
  repository commits are the doctor's `3.12` floor and CI's `3.12` line (the claims table's rows), neither of which is
  an installable pyenv version, so a script cannot derive the exact patch the owner chose.
- **Consequence accepted:** a worktree without a local `.python-version` resolves the machine's global `pyenv` version,
  so the stage reports that interpreter and the module references it — the reproduction promise is the mechanism, not
  one patch version across trees. The report is what tells the contributor which interpreter a tree got.

### D3 — The IntelliJ configuration is identified, never guessed from ordering

Candidates are the platform's configuration roots (`~/Library/Application Support/JetBrains/IntelliJIdea*` on macOS,
`~/.config/JetBrains/IntelliJIdea*` on Linux, `%APPDATA%\JetBrains\IntelliJIdea*` on Windows). The stage selects a
candidate only when exactly one carries a `jdk.table.xml` with a Python SDK entry matching the resolved interpreter;
`--idea-config-dir` overrides the search, and an unusable value fails the stage with no fallback.

- **Why:** zero candidates and several candidates are different states for the contributor, and picking "the newest"
  silently configures the wrong IDE installation.
- **Alternative rejected — a mandatory flag:** it would add a path argument to every run in every new working tree,
  against the objective that reproduction be one command.
- **Alternative rejected — newest-configuration glob:** nondeterministic across installed versions, and it also decides
  for a contributor who uses a custom `idea.config.path`.
- **Limitation:** only the macOS form is verified on this machine; the Linux and Windows roots follow JetBrains'
  documented layout and are unverified until a real example is exercised, which the spec's `SKIPPED` outcome keeps
  honest — they produce no module rather than a wrong one.
- **Argument parsing keeps the documented invocation:** `--idea-config-dir <path>` is an optional flag parsed alongside
  the existing optional positional launcher path (`./scripts/setup-idea.sh /path/to/idea`), so an existing invocation
  keeps working; the script's usage header gains the flag.

### D4 — The SDK name is read from the table, matched on the normalized home path

Within the identified configuration, the stage parses `options/jdk.table.xml`, expands `$USER_HOME$` and a leading `~`,
compares `realpath`s against the resolved interpreter, and uses the matching entry's `name` verbatim.

- **Why:** a module references an SDK by name, and the name is not derivable from the path by a documented rule —
  IntelliJ's own `suggestSdkName` produced the `~`-abbreviated form here, but that is one machine's observation, not a
  contract. Reading the table makes the reference exact by construction.
- **Alternative rejected — computing the name:** the failure mode is a module that looks configured and resolves
  nothing, which is the state the design forbids the setup to write.
- **Alternative rejected — writing the table:** it is IDE-owned, rewritten while the IDE runs, and per machine; a setup
  script must not edit it.

### D5 — The module file is reconciled in place, not regenerated

The target is `.idea/modules/scripts.iml`: `<module type="PYTHON_MODULE">`, a `NewModuleRootManager` with content root
`file://$MODULE_DIR$/../../scripts`, one `<orderEntry type="jdk" jdkType="Python SDK">` carrying the name from D4, and
`<orderEntry type="sourceFolder" forTests="false" />`. The stage parses an existing file, edits only those managed
elements, and writes it back; `.idea/modules.xml` gains or updates one `<module>` element keyed by its file path.

- **Why:** the shape is the one IntelliJ persisted for the hand-made module in the primary checkout, so the setup
  reproduces the working state rather than a lookalike; preserving other elements keeps a plugin's component (the
  CheckStyle one IntelliJ added) intact.
- **Alternative rejected — writing a fixed file body:** a regeneration drops plugin-managed components and churns the
  file on every run.
- **Alternative rejected — committing the module into the repository:** `showcase/quality/ide-config` forbids tracking
  `.idea/` files, and an absolute interpreter name would make the committed copy machine-specific.

### D6 — The stage has its own outcome vocabulary and exit-code contract

| Outcome        | Condition                                                              | Exit |
| -------------- | ---------------------------------------------------------------------- | ---- |
| `CONFIGURED`   | the module was written or already conformed                            | 0    |
| `SKIPPED`      | no IntelliJ configuration directory was found and none was named       | 0    |
| `NEEDS_ACTION` | no SDK matches, several match, or several configurations qualify       | ≠0   |
| `ERROR`        | unusable `--idea-config-dir`, unparsable table, interpreter below 3.12 | ≠0   |

The `NEEDS_ACTION` report carries the interpreter path, the candidate configurations or entries, and the IntelliJ
settings page on which that interpreter is registered as an SDK — the contributor's action has to be doable from the
report alone.

`scripts/setup-idea.sh` runs under `set -euo pipefail` and currently invokes the helper before the plugin install, so a
propagated non-zero status would abort the run before the plugins are installed — on a fresh clone, before the setup's
primary function. The script therefore captures the helper's status, still attempts the plugin installation, treats an
installation skip (IDE running, no launcher) as legitimate rather than as a failure, and exits with the aggregate of
every stage's `NEEDS_ACTION` or `ERROR`; the helper itself runs the formatter merge and the Python stage in one pass and
reports both.

- **Why:** "nothing was written" and "the wrong thing was written" must not read the same, and a silent success is what
  let today's gap persist; the formatter merge stays usable on a machine without IntelliJ.
- **Alternative rejected — always exit zero:** it would report an incomplete Python setup as done.
- **Alternative rejected — always non-zero on any Python gap:** a machine without IntelliJ would fail a setup whose
  formatter half succeeded.

### D7 — Tests drive the entry point with injected probes and temp fixtures

`scripts/test-ensure-idea-settings.py` (stdlib `unittest`) builds a temporary working tree and a temporary IntelliJ
configuration directory per case, calls the script's `main(argv)` with an injected interpreter probe, and asserts both
the files written and the exit code. It is registered as `testEnsureIdeaSettings` and joins the root `check` block.

- **Why:** the mechanism's inputs are files, so the contract is testable without IntelliJ; driving `main` also covers
  the exit-code routing, which an assertion on a helper would leave untested.
- **Controls the suite must carry:** a module file that already holds a foreign component (the case a regenerating
  implementation fails), an existing `modules.xml` with other modules, a second candidate configuration (the ambiguous
  case), a table whose SDK home differs from the interpreter, both macro spellings (`$USER_HOME$`, `~`), a below-floor
  interpreter report, and a second run over the first run's output for idempotency.
- **The wrapper's continuation contract is covered by its own isolated control**, `scripts/test-setup-idea.sh`: it
  copies the wrapper, the helper and `config/idea/` into a temporary tree, stubs the launcher with a script that records
  its invocation, and covers a helper error with the launcher present (install attempted, non-zero exit), a helper error
  with no launcher (install skipped, non-zero exit) and a successful run (zero exit). The isolation is what keeps a test
  run from installing real plugins, and the stub's own record is the evidence that the install was attempted.
- **The real selection command is covered, not only an injected probe:** one case stubs the subprocess and asserts the
  exact `-c` program and the parse of its output, and one case runs the real `python3` from a temporary working tree and
  asserts the reported path is the interpreter that ran — an injected probe alone would pass while a mistyped program or
  a wrong parse broke the mechanism the whole decision rests on.
- **Alternative rejected — asserting the helpers only:** the authoring failure `check-and-test-authoring` records is a
  helper that passes while the entry point rejects a valid input.
- **Mutation proof (required, per the same rule):** removing the foreign-component preservation and the "nothing written
  when unmatched" branch each must turn a named test red before the change is reported done.

## Risks / Trade-offs

- **IntelliJ rewrites `modules.xml` while it runs** → the stage is a reconciler, not a daemon: a later rewrite is drift,
  and the documented remedy is re-running the setup. The README states this rather than promising protection.
- **The `scripts` content root nests inside the Gradle root module's content root** → the IDE resolves nested content
  roots to the inner module; the state was verified once in the primary checkout, and a Gradle re-import that drops the
  module is the same drift condition.
- **A non-zero exit on `NEEDS_ACTION` changes the script's contract** → nothing in CI invokes the script; the README,
  the `/setup-idea` command, and its skill state the new vocabulary and the action each outcome asks for.
- **The SDK-table read is IDE-version-specific** → the stage reads one documented file (`options/jdk.table.xml`), treats
  an unparsable table as `ERROR` rather than as "no match", and its test fixtures carry the macro forms actually
  observed.
- **Linux and Windows configuration roots are unverified** → the outcome vocabulary keeps them safe (no module rather
  than a wrong one) and the design records the limitation instead of claiming support.

## Migration Plan

Nothing is migrated. In the primary checkout the hand-made module already sits at the same path with the same shape, so
its first run reports `CONFIGURED` (or restores any drift). Every other working tree gains the file on its first setup
run. Reverting the change leaves `.idea/` untouched apart from the generated module, which is ignored.

## Claims

Base revision for every pre-change fact: `89224c730e63e773fcfdd882c9f6492c323d56ea`.

| Claim                                                                                       | Source or command                                                                                                                                                                                                                                                |
| ------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `.idea/` is ignored entirely and no `.idea/` path is tracked                                | `git show 89224c73:.gitignore` lines 15-18; `git ls-tree -r 89224c73 --name-only -- .idea` (empty); the requirement that forbids tracking it: `git show 89224c73:openspec/specs/showcase/quality/ide-config/spec.md` ("IntelliJ configuration is not versioned") |
| `.python-version` is ignored; `.venv` is not                                                | `git show 89224c73:.gitignore` lines 9-12; `git check-ignore .venv` on that revision exits 1                                                                                                                                                                     |
| The setup merges four components and one inspection, nothing else                           | `git show 89224c73:scripts/ensure-idea-settings.py` (`COMPONENTS`, `ensure_inspection`)                                                                                                                                                                          |
| The setup currently aborts before the plugin install when the helper exits non-zero         | `git show 89224c73:scripts/setup-idea.sh`: `set -euo pipefail`, then the `python3 "$SCRIPT_DIR/ensure-idea-settings.py"` call preceding the `installPlugins` loop                                                                                                |
| The build resolves `pythonExecutable` as the first `python3` on `PATH`, as an absolute path | `git show 89224c73:build.gradle.kts` lines 487-493                                                                                                                                                                                                               |
| CI provisions Python 3.12                                                                   | `git show 89224c73:.github/workflows/ci.yml` line 69                                                                                                                                                                                                             |
| The doctor declares Python 3.12 as the tool floor                                           | `git show 89224c73:scripts/doctor.sh` line 156                                                                                                                                                                                                                   |
| The repository's Python is stdlib-only, with 12 tracked `.py` files                         | `git ls-files '*.py'` at 89224c73 (12); its 16 distinct top-level imported modules are all in `sys.stdlib_module_names` (derive with `git grep -h -E '^import ' 89224c73 -- '*.py'` and `git grep -h -E '^from ' 89224c73 -- '*.py'`)                            |
| The README states the setup does not provision or register an interpreter                   | `git show 89224c73:README.md` lines 972-976                                                                                                                                                                                                                      |
| A worktree without `.python-version` resolves the global `pyenv` version                    | `pyenv global` (3.12.1) is what this worktree resolved before task 7.2 pinned it: `pyenv version-name` there returned 3.12.1 against the primary's 3.12.16, and `python3 -c 'import sys; print(sys.executable)'` printed the matching 3.12.1 directory           |
| IntelliJ stores the entry with a `$USER_HOME$` home path and a `~`-abbreviated name         | read of `~/Library/Application Support/JetBrains/IntelliJIdea2026.2/options/jdk.table.xml` (outside the repo)                                                                                                                                                    |
| The persisted module shape a generated file must reproduce                                  | read of the primary checkout's `.idea/modules/scripts.iml` (outside the repo): `PYTHON_MODULE`, `NewModuleRootManager`, `$MODULE_DIR$/../../scripts`, `jdk`/`sourceFolder` entries                                                                               |
| A `PYTHON_MODULE` covering `scripts/` resolves the interpreter and survives a re-import     | live IntelliJ IDEA 2026.2.0.1 probe in the primary checkout (outside the repo, not reproducible in CI)                                                                                                                                                           |

### Dismissals

`scripts/claim-scan.py --base <base>` is the pass's lead list, not a gate: it reports candidate shapes, and the rows
above carry the facts the artifacts actually rest on. Candidates are dismissed by class.

- **`absolute`** — a prose quantifier inside decision, task or documentation text ("only when exactly one candidate
  matches", "no module is written"). A claim of that shape the artifacts rest on is a row above, and the review screens
  the unit's diff for any that is not.
- **`count`** — this change's own step, task and round identifiers, which no repository command derives, or a figure the
  rows already carry (the tracked `.py` files, the components, the imported modules).
- **`cross-ref`** — a reference the artifacts already carry: a UI label, a path in the workspace record, a document
  whose content claim is a row above, or a path in the implementation and its tests, evidenced by those files and their
  suites; none is a fact the table must source separately.
- **`path`** — a subject this change creates or edits (the scripts, the build file, the generated `.idea` files), whose
  existence is the change's own work; the pre-change paths the design _rests_ on are rows above, the
  `showcase/quality/ide-config` reference is row 1's second half, and the unverified Linux and Windows configuration
  roots are the limitation Risks records rather than a path the change creates.
