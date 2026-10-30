# Design

## Context

`scripts/ensure-idea-settings.py`'s Python stage reads a configuration's `options/jdk.table.xml`, expands each Python
SDK entry's `homePath`, and keeps the entry whose path equals the probe's interpreter — both sides put through
`Path.resolve()`. A virtual environment breaks that equality: its `bin/python` is a symlink chain to the base
interpreter it was created from, so both sides resolve to the same file.

Observed on the reporting machine (outside the repository, read from
`~/Library/Application Support/JetBrains/IntelliJIdea2024.3/options/jdk.table.xml`): the entry
`Python 3.12 (snapscreen-infra-as-code)` holds `$USER_HOME$/.pyenv/versions/snapscreen-infra-as-code/bin/python`, and

| Path                                      | Resolved binary                           | Parent directory resolved                    |
| ----------------------------------------- | ----------------------------------------- | -------------------------------------------- |
| the entry's interpreter                   | `…/.pyenv/versions/3.12.1/bin/python3.12` | `…/3.12.1/envs/snapscreen-infra-as-code/bin` |
| the probe's interpreter (`…/bin/python3`) | `…/.pyenv/versions/3.12.1/bin/python3.12` | `…/3.12.1/bin`                               |

The resolved binaries are identical and the install directories differ, so the old rule matched the venv's SDK to an
interpreter the venv does not belong to — in two configuration directories at once, producing
`NEEDS_ACTION — several Python SDK entries match interpreter …` and a non-zero exit with no module written, on a machine
where the right answer was "that interpreter is not registered".

With one such configuration named explicitly, the same run is worse rather than louder: the base rule wrote the module
with `SDK 'Python 3.12 (snapscreen-infra-as-code)'` for the 3.12.1 interpreter, a reference to an environment that
interpreter does not belong to. A/B on the same inputs, the base script and this change's script in scratch copies:

| Script      | Report with `--idea-config-dir …/IntelliJIdea2024.3`                                                        | Exit |
| ----------- | ----------------------------------------------------------------------------------------------------------- | ---- |
| base        | `CONFIGURED — … interpreter …/3.12.1/bin/python3.12 (3.12.1), SDK 'Python 3.12 (snapscreen-infra-as-code)'` | 0    |
| this change | `NEEDS_ACTION — interpreter …/3.12.1/bin/python3 (3.12.1) is not registered as a Python SDK in …`           | 1    |

The claims table's rows reproduce every figure above; this document explains the rule the delta states.

## Goals / Non-Goals

**Goals:**

- A registered SDK matches the probe when, and only when, the two interpreters belong to the same installation.
- The pairs the stage must tell apart keep working, each with its own committed case: a pyenv `bin/python3` symlinked to
  `bin/python3.12`, an install root that is itself a symlink (a Homebrew-style prefix), two spellings of the same home
  (`$USER_HOME$`, `~`), and an entry naming the target of the link the probe's path points through.
- The report keeps naming the interpreter the probe ran, so a contributor can see which install a tree resolved.

**Non-Goals:**

- Registering, renaming, or repairing an SDK entry in IntelliJ's table.
- Reading the IDE's per-project interpreter settings, `pyvenv.cfg`, or any file outside the documented SDK table.
- Changing the interpreter selection rule, the outcome vocabulary, or the exit-code contract.

## Decisions

### D1 — Match on the interpreter, the install it belongs to, and the probe's environment

An entry matches when its interpreter resolves to the same binary as the interpreter the probe reported and the entry is
installed in the install that binary belongs to: the directory of the path the probe reported or, outside a virtual
environment, the directory that path resolves to. `expand_home` stops resolving the entry's path; `probe_interpreter`
reports the path the interpreter names for itself (`sys.executable`), its version, and whether `sys.prefix` differs from
`sys.base_prefix`; and the predicate resolves the binaries and compares the directories itself.

- **Why the binary and a directory:** each alone is wrong in a different direction. Resolving the binary only erases a
  virtual environment — every venv resolves to its base interpreter — while comparing directories only erases a sibling
  interpreter, because one directory can hold several versions (Homebrew's `bin` holds `python3.13` and `python3.15`).
- **Why the probe's directory comes from the reported path, not only from the resolved binary:** the directory is the
  install the build selected. A Homebrew interpreter invoked as `bin/python3.13` resolves to a file under `Cellar/…`, so
  taking only the resolved directory would stop matching the SDK registered for the path on `PATH`; a venv's interpreter
  reports `venv/bin/python`, whose resolved form is the base interpreter. The report therefore names the invoked path
  and the version, which is what the contributor gave the build.
- **Why the resolved target's directory is accepted only outside a virtual environment:** an SDK is often registered by
  the binary the path points at, and that is the same install — unless the path is a virtual environment's, whose
  interpreter resolves to the base binary too. Accepting the target's directory there configures the base installation
  for a venv, or turns a table holding both SDKs into an ambiguity the contributor can only clear by deleting one, which
  is the change's own confusion in the other direction. `sys.prefix != sys.base_prefix` is the interpreter's own report
  of that fact, so no file is read to learn it.
- **Alternative rejected — resolved-binary equality alone:** the reported defect.
- **Alternative rejected — install-directory equality alone:** this change's first direction, and a defect a
  technical-advisor consultation caught before delivery; it matches a different interpreter installed in the same
  directory.
- **Alternative rejected — only the reported path's directory:** it stops matching an entry registered by the target of
  a link, which the base rule matched; the target-entry case fails under it.
- **Alternative rejected — the target's directory unconditionally:** a second consultation's confirmed problem; a venv's
  interpreter then matches the base installation's SDK, which the two environment cases now reject.
- **Alternative rejected — resolving the probe before taking its directory:** it stops matching a Homebrew-style entry,
  whose target lives outside the directory it was invoked from, and a venv probed through its own interpreter.
- **Alternative rejected — accepting the entry's resolved directory as well, symmetrically:** for a venv that directory
  is the base interpreter's, so the venv's SDK would match the base interpreter again — the reported defect.
- **Alternative rejected — comparing the unresolved paths as written:** it breaks the pairs that must match — a
  `bin/python3` entry against a `bin/python3.12` interpreter, and an install root that is a symlink.
- **Alternative rejected — reading the venv's `pyvenv.cfg`:** a further file read for a fact the interpreter reports
  about itself.
- **Consequence accepted:** the rule is asymmetric for a link-spelled entry. An SDK registered through a link matches
  when the build runs that link or its target, but not when the build runs the target's own path while the SDK is
  spelled through the link; the stage reports its action, and the remedy is registering the path the setup prints.
- **Consequence accepted:** an entry whose `homePath` names a directory rather than an interpreter file fails the binary
  condition, so it does not match; IntelliJ stores a Python SDK's `homePath` as the interpreter file.

### D2 — The state model the rule has to preserve

The probe's columns are the path it reported, the file that path resolves to, and whether it ran from a virtual
environment. "In the probe's install" means the entry sits in the reported path's directory or, when the probe ran
outside a virtual environment, in the resolved file's directory.

| SDK entry's interpreter                                            | Probe's interpreter     | Same binary | In the probe's install | Outcome                             | Case     |
| ------------------------------------------------------------------ | ----------------------- | ----------- | ---------------------- | ----------------------------------- | -------- |
| venv `bin/python` (symlink chain to the base)                      | base `bin/python3`      | yes         | no                     | not a match; the action is reported | new      |
| a sibling interpreter in the probe's directory                     | base `bin/python3`      | no          | yes                    | not a match; the action is reported | new      |
| venv entry beside the base entry                                   | base `bin/python3`      | one of two  | one of two             | `CONFIGURED`, the base entry's name | new      |
| `bin/python3` symlinked to `bin/python3.12`                        | `bin/python3.12`        | yes         | yes                    | `CONFIGURED`, the entry's name      | new      |
| the install reached through a symlinked prefix                     | the install's real path | yes         | yes                    | `CONFIGURED`, the entry's name      | new      |
| an entry invoked from `bin` with its target outside that directory | the same invoked path   | yes         | yes                    | `CONFIGURED`, the entry's name      | new      |
| the entry naming the target of the link the probe's path points at | that link's path        | yes         | yes                    | `CONFIGURED`, the entry's name      | new      |
| the venv probed through its own interpreter                        | the venv's `bin/python` | yes         | yes                    | `CONFIGURED`, the venv's name       | new      |
| the base binary's SDK, for a probe inside a virtual environment    | the venv's `bin/python` | yes         | no                     | not a match; the action is reported | new      |
| both SDKs, for a probe inside a virtual environment                | the venv's `bin/python` | one of two  | one of two             | `CONFIGURED`, the venv's name       | new      |
| the probe's own path, `$USER_HOME$` or `~`                         | that path               | yes         | yes                    | `CONFIGURED`, the entry's name      | existing |
| `/opt/other/python3`                                               | any interpreter         | no          | no                     | `NEEDS_ACTION`, nothing written     | existing |
| two entries for one install                                        | one probe               | yes, twice  | yes, twice             | `NEEDS_ACTION`, nothing written     | existing |

Every row is one committed case in `scripts/test-ensure-idea-settings.py`; task 1.5 records which case each mutation of
the rule fails.

### D3 — The regression cases are built from real links and assert the aliasing they exercise

Each case creates the fixture's directories and symlinks on disk and asserts the property its outcome depends on before
reading the stage's result: the venv case that the entry resolves to the probe's binary, the sibling-interpreter case
that the two share a directory, the Homebrew-shaped case that the entry's target lives outside the directory it is
invoked from, the target-entry case that the registered path is the link's target, and the environment case that the
base binary is the probe's resolved target. Those assertions are the cases' positive controls: they fail if the fixture
stops reproducing the condition the case exists for.

- **Why not string fixtures:** the rule reads the filesystem, so the fixtures must exist there; and a case whose paths
  stopped aliasing would pass while testing nothing.
- **Why several cases rather than one:** each mutation of the rule fails a different subset, so a suite that dropped any
  of them would leave a component unexercised. Task 1.5 is the record of which mutation fails which case; this document
  does not restate that set.

### D4 — The module file sits outside the directory the Gradle import owns

The module is written to `.idea/python/scripts.iml` and registered in `.idea/modules.xml`; a reconcile also deletes
`.idea/modules/scripts.iml` and any entry naming it.

- **Why:** that earlier location is the directory IntelliJ's Gradle import generates module files into and rebuilds on
  every sync. Observed here: the setup wrote the module there, a Gradle reload rewrote `modules.xml` from the build
  model with no `scripts` entry — the IML left orphaned on disk — and the Python analysis disappeared, which a reload
  cannot repair because the reload is the import. The same module written under `.idea/python/` survived a full Gradle
  sync and the file then resolved to its Python SDK in the live project.
- **Alternative rejected — keeping the old path and documenting a reload:** a reload re-imports the build model, so it
  reproduces the loss rather than repairing it.
- **Alternative rejected — a Python project SDK:** measured in the live project,
  `PythonSdkUtil.findPythonSdk(rootModule)` stays empty for a Gradle module that carries its own JDK, so the project SDK
  never reaches `scripts/`.
- **Consequence accepted:** `.idea/python/` is a path IntelliJ does not manage; an import that decides to clean unknown
  module files would drop it again, and the reconciler restores it on the next setup run.

### D5 — `NEEDS_ACTION` reports a short outcome line, then an action-first block

The actionable outcomes keep their one-line header — `Python: NEEDS_ACTION — <what is wrong>.` — and continue with an
indented block that states the remedies first and the inventories last: the interpreter to register with the settings
path and the re-run step, the alternative of running the tree on a registered interpreter, the registered SDKs with
their interpreter paths and configurations, and the configurations searched. The ambiguity case mirrors it: what to
keep, then the matching entries. `CONFIGURED`, `SKIPPED`, and `ERROR` stay single-line.

- **Why:** the owner's first run printed one long line whose only remedy sat at the end, after every configuration path
  and SDK name; the second version added the alternative remedy but kept it at the end of a longer line. A contributor
  acts from this report, so the remedy comes first and the evidence is indented beneath it. The outcome contract asks
  for one outcome per stage, and the stage's line still carries it; the detail lines are that outcome's explanation,
  which the contract does not forbid.
- **Why the full interpreter path per SDK, not only its name:** an SDK name is a label IntelliJ chooses, so it cannot be
  matched back to an interpreter the contributor might register or pin; the path can.
- **Alternative rejected — a single long line with the remedy first:** it keeps the scanning problem the owner raised;
  indentation and short lines are what make the block readable in a terminal.
- **Alternative rejected — a separate reporting stage or an `--explain` flag:** the former complicates the outcome
  contract and the exit status, and the latter hides the help behind another invocation for a contributor who is already
  stuck.
- **Consequence accepted:** `NEEDS_ACTION` outputs several lines, so a consumer that assumed exactly one line per stage
  reads the block's first line as the outcome; the wrapper only forwards the text and the exit status, and the tests
  assert the block's structure.

## Risks / Trade-offs

- **An interpreter whose parent directory is a symlink to another install's directory matches that install.** Both paths
  describe one directory after resolution, which is also how the IDE sees a Python SDK's home, and the binary condition
  still requires the same file; no case in the repository distinguishes such a pair from the same install.
- **The probe's path is whatever the interpreter reports as `sys.executable`.** A wrapper that reports a path in another
  directory than the one it was invoked from would move the probe's install directory; the stage reports the path it
  used, so the contributor sees which directory a mismatch was judged against.
- **The environment is known only from the interpreter's own report.** `sys.prefix` against `sys.base_prefix` is how
  CPython and virtualenv mark a virtual environment; an implementation that sets them equal inside one would have its
  base installation configured, so the design records the signal rather than inferring it from paths.
- **An entry that reaches the same binary from any other directory does not match.** The rule accepts the reported
  path's directory and, outside a virtual environment, the resolved file's directory — never the entry's own resolved
  directory, which for a venv is the base interpreter's. So a link-spelled entry against a build running the target's
  own path, a second link to the same binary outside both accepted directories, and the base installation's SDK inside
  an environment each report the action rather than configuring. The remedy is registering the path the setup prints.
- **A future IntelliJ that stores an SDK home as a directory would stop matching** → the stage reports the contributor's
  action (no module written) rather than a wrong reference, and the observed table shape is the interpreter file.
- **The rule is filesystem-dependent, so its tests must create real links** → each case builds them under a temporary
  directory and the venv case asserts the aliasing itself, so the only platform assumption is that `os.symlink` works
  (the suite already treats a POSIX temporary directory as given).

## Migration Plan

Nothing is migrated. An entry keeps matching when it resolves to the interpreter's own binary and sits in the directory
the probe's path names or, outside a virtual environment, in the one that path resolves to — which covers a table
holding the install the build runs. Three classes change state, each to the report it owed: a tree whose only match was
a virtual environment's entry, which the change exists to reject; a tree whose entry reaches the same binary from any
other directory, such as a link-spelled entry when the build runs the target's own path or a second link beside the
build's own; and a tree whose build runs inside a virtual environment while the table holds only the base installation's
SDK, which now reports that environment's interpreter instead of configuring the base. Each reports `NEEDS_ACTION`
naming the interpreter and where to register it.

## Claims

Base revision for every pre-change fact: `289b562caf1dbac593653d4e2f40a7b37c7bf9e6`.

| Claim                                                                                                       | Source or command                                                                                                                                                                                                                                                                                                                                                                                                                     |
| ----------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The stage compares the entry's resolved home with the probe's resolved binary                               | `git show 289b562c:scripts/ensure-idea-settings.py` — `resolve()` at lines 163 (`probe_interpreter`) and 203 (`expand_home`), `if home == interpreter` at 353                                                                                                                                                                                                                                                                         |
| The suite carries 22 cases at the base                                                                      | `git show 289b562c:scripts/test-ensure-idea-settings.py \| grep -c '^    def test_'` → 22                                                                                                                                                                                                                                                                                                                                             |
| The suite runs inside `check`                                                                               | `git show 289b562c:build.gradle.kts` — `testEnsureIdeaSettings` registered at line 552, `dependsOn` at line 769                                                                                                                                                                                                                                                                                                                       |
| The two spec clauses this delta amends are the only live statements of the matching rule                    | `git grep -n "real binary path\|resolved interpreter" 289b562c` — outside `openspec/changes/archive/`, hits are `openspec/specs/showcase/quality/ide-config/spec.md` lines 167, 172, 183, 188 only; the `setup-idea` skill, its command, and the README state no matching rule                                                                                                                                                        |
| A virtual environment's interpreter resolves to the base interpreter it was created from                    | read of `~/Library/Application Support/JetBrains/IntelliJIdea2024.3/options/jdk.table.xml` plus `Path.resolve()` on both sides (outside the repository)                                                                                                                                                                                                                                                                               |
| pyenv spells `bin/python3` as a symlink to `bin/python3.12` in the same directory                           | `ls -l ~/.pyenv/versions/3.12.16/bin/python3` → `python3 -> python3.12` (outside the repository)                                                                                                                                                                                                                                                                                                                                      |
| The interpreter names the path it was invoked from, not the file that resolves to                           | `INTERPRETER_PROGRAM` prints `sys.executable`; the scratch A/B reports read `interpreter …/3.12.1/bin/python3 (3.12.1)` while `Path(resolve())` on that path and on the venv entry both name `…/3.12.1/bin/python3.12`                                                                                                                                                                                                                |
| IntelliJ's Gradle import rebuilds `.idea/modules/` and rewrites `modules.xml`                               | read of this worktree: the setup wrote the module entry at 16:10, a Gradle reload rewrote `modules.xml` at 16:15 with 43 entries and no `scripts` line while `.idea/modules/scripts.iml` stayed on disk                                                                                                                                                                                                                               |
| A module under `.idea/python/` survives a Gradle sync and keeps Python analysis                             | a full `ExternalSystem.RefreshAllProjects` sync kept the entry (44 entries, one `scripts`), and the live project reported module `scripts`, type `PYTHON_MODULE`, SDK `~/.pyenv/versions/3.12.16/bin/python3` for `scripts/review-routing.py`                                                                                                                                                                                         |
| A Python project SDK does not reach a Gradle module that carries its own JDK                                | live probe: with the project SDK set to the Python SDK, `PythonSdkUtil.findPythonSdk(rootModule)` returned none; the project SDK was restored to `temurin-21`                                                                                                                                                                                                                                                                         |
| Two different interpreters are installed in one directory on this machine                                   | `/opt/homebrew/bin/python3.13` and `/opt/homebrew/bin/python3.15` both sit in `/opt/homebrew/bin` and resolve to different `Cellar/…` files (outside the repository)                                                                                                                                                                                                                                                                  |
| A virtual environment reports itself through `sys.prefix` and names its own interpreter in `sys.executable` | `~/.pyenv/versions/3.12.1/envs/snapscreen-infra-as-code/bin/python -c 'import sys; print(sys.prefix, sys.base_prefix, sys.executable)'` printed the environment's prefix against the base prefix and `…/envs/snapscreen-infra-as-code/bin/python`, while `python3` printed equal prefixes and `…/3.12.1/bin/python3` (outside the repository)                                                                                         |
| A non-existent interpreter path still yields its parent directory under the pinned Python                   | `python3 -c "from pathlib import Path; print(Path('/nonexistent/a/b/python3').parent.resolve())"` → `/nonexistent/a/b` (3.12.16)                                                                                                                                                                                                                                                                                                      |
| The base rule writes a venv's SDK for the base interpreter when one configuration is named                  | scratch A/B on the same inputs: `git show 289b562c:scripts/ensure-idea-settings.py` in a temporary tree holding a copy of `config/`, run with `--idea-config-dir "$HOME/Library/Application Support/JetBrains/IntelliJIdea2024.3"`, printed `CONFIGURED … SDK 'Python 3.12 (snapscreen-infra-as-code)'` and exited 0; this change's script on the same inputs printed `NEEDS_ACTION … is not registered as a Python SDK` and exited 1 |
| A tree pinned to the registered interpreter still configures against the current configuration              | the same scratch tree carrying `.python-version` = `3.12.16`, run with `--idea-config-dir "$HOME/Library/Application Support/JetBrains/IntelliJIdea2026.2"`, printed `CONFIGURED … SDK '~/.pyenv/versions/3.12.16/bin/python3'` and exited 0, writing `jdkName="~/.pyenv/versions/3.12.16/bin/python3"` into its `.idea/python/scripts.iml`                                                                                           |

### Dismissals

`scripts/claim-scan.py --base 289b562caf1dbac593653d4e2f40a7b37c7bf9e6` is the pass's lead list, not a gate. Candidates
are dismissed by class:

- **`absolute`** — a quantifier in the artifacts' prose, each of which restates a row above rather than adding a fact.
- **`count`** — the change's own step, task, and round identifiers, which no repository command derives.
- **`cross-ref`** — a reference the change's own artifacts already carry, evidenced by the artifact that holds it rather
  than by an external source; a cross-reference whose substance is a row above is that row's.
- **`path`** — a path the change creates or edits, or a pre-change path a row above already reads.
