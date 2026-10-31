# Design — clarify-python-ide-activation

## Context

The setup writes a Python analysis module for `scripts/*.py` into `.idea/`, and its hand-back told the contributor to
apply it with **File → Reload All from Disk**. An owner episode showed the module present on disk but absent from
IntelliJ's live model after a reload, and the probe recorded at
`docs/decisions/2026-10-31-python-module-activation-probe.md` produced a result that did not reproduce in a tree whose
`.idea/` had been deleted.

Further experiments in disposable worktrees replaced both the probe's verdict and this unit's first design. The record's
"Reopened — 2026-10-31" section carries the mechanism and the measurements; this design takes them as settled input:

- IntelliJ applies the project-local module list when the **project opens**; a reload of an open project does not add a
  module its model does not have.
- A setup run made while the project is closed, followed by opening the project, yields the module — measured in a fresh
  tree (83 modules including `scripts`, with the expected SDK) and in an already-imported tree after closing its window
  and re-running.
- In the fresh tree a Gradle sync and a later close/reopen left the module in place; in the already-imported tree the
  sync did.

## Decisions

### D1 — The activation procedure belongs to the module requirement

The requirement already promises Python analysis per working tree; analysis exists only once the project has read the
module, so the requirement states how that happens. The build-model scenario gains the precondition that the project has
opened the module, because its subject is what a later sync does to an active module.

- **Alternative — a new requirement for activation.** Rejected: one subject split across two requirements, and the
  `MODIFIED` route costs one delta block instead of a permanent corpus entry.

### D2 — The contract is an open, not a reload

The run reports: open the project after a run made while it was closed; if the project was open during the run, close
its window, run the setup again, and then open it. That is the measured procedure; a reload of an open project is
explicitly not the way the module is added.

- **Alternative — keep the reload as the primary instruction.** Rejected: measured not to activate the module, and the
  original incident's failure mode.

### D3 — The formatter requirement keeps its reload-or-restart sentence

The merged formatter and inspection settings are read at startup, so a reload or a restart applies them; the probe and
the experiments do not contradict that. Only the module's activation differs, and the guidance says so.

### D4 — The advisory is a distinct closing action section

The owner asked that the guidance be hard to miss. The block is separated by a blank line, carries the heading
`=== Next step in IntelliJ ===`, one clause per line, and the formatter note last — the repository's plain-text section
style. A capability spec covers a human-facing artifact's outcome, not its presentation, so the layout is owned by the
script and its committed control while the spec owns the procedure.

### D5 — No storage-mode change, no Gradle subproject, no manual import

Each alternative was measured and set aside, and the record carries the measurements:

- **Project-local module storage** (writing `ExternalStorageConfigurationManager enabled="false"`): on a live project
  the migration on close rewrites `modules.xml` from the model and drops the entry a run just wrote, so the setup must
  run again; written with the IDE closed it leaves the project without modules until a Gradle re-import. Five steps per
  tree for no gain.
- **A `scripts` Gradle subproject with `gradle-idea-ext-plugin`** (`rootModuleType = 'PYTHON_MODULE'`): the type is
  settable, the Python SDK is not (`JetBrains/gradle-idea-ext-plugin#134`, open), so the interpreter would still be
  assigned by hand, at the cost of a new build project.
- **Importing the generated module through Project Structure.** Rejected by the owner as a product flow; it also needs
  an IDE action the setup cannot perform.

### D6 — The script still writes the registration, and claims nothing about a live model

`.idea/python/scripts.iml` and its `.idea/modules.xml` entry remain what the project reads when it opens. The run
reports files prepared, not analysis verified — `CONFIGURED` already has that meaning. No automatic repair of an
already-open project's live model is promised.

## Purpose determination

The `showcase/quality/ide-config` Purpose names the classes the capability covers and says the setup reproduces them.
This change states how the IDE comes to read a module the run already writes; it neither widens nor removes a class, and
the run still writes the same setup. No Purpose refresh is owed.

## Claims table

| Claim                                                                                                                                         | Source / command                                                                                                                | Result                                                                                                                                                  |
| --------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The module requirement states no activation step, while the formatter requirement says reload or restart                                      | `git show <base>:openspec/specs/showcase/quality/ide-config/spec.md`                                                            | module block `:127–160`, five scenarios and no activation clause; the formatter clause at `:54` says reload or restart                                  |
| Four live guidance sites carried a reload instruction; two scoped it to the settings and two applied it to the module's configuration as well | `git grep -n "Reload All from Disk" <base> -- README.md AGENTS.md .opencode scripts openspec/specs/showcase/quality/ide-config` | six hits: the skill `:44`, `AGENTS.md` `:2183`, `README.md` `:953`, the script `:134` (all changed); the spec `:54`, `:84` (formatter)                  |
| The activation is the project's open, measured in both starting states                                                                        | `docs/decisions/2026-10-31-python-module-activation-probe.md`, "Reopened — 2026-10-31"                                          | fresh tree 83 modules including `scripts`, surviving sync and a later close/reopen; already-imported tree 83 after close → setup → open, surviving sync |
| A fresh import leaves the project-level Java defaults unset, while each module carries its own SDK                                            | the record's "The Java settings"                                                                                                | project SDK none, language level `JDK_26`; 74 modules with `temurin-21`, 8 with `temurin-25`, and `scripts` with its Python SDK                         |
| The change alters only the script's closing echoes                                                                                            | `git diff --cached -U0 -- scripts/setup-idea.sh`                                                                                | the `echo` block before `exit "$status"` changes; no stage or Python logic                                                                              |
| `AGENTS.md`'s applied net delta                                                                                                               | `git diff --numstat <base> -- AGENTS.md` after `spotlessApply`                                                                  | +13/−10, net +3 lines                                                                                                                                   |
| The delta validates and the corpus totals                                                                                                     | `openspec validate --all` and `openspec validate --changes`                                                                     | 27 passed, 0 failed; the change validates                                                                                                               |
| The shell test passes and each control fails for its own reason                                                                               | `sh scripts/test-setup-idea.sh`                                                                                                 | all cases passed                                                                                                                                        |
| The Docker-free gate passes over the touched paths                                                                                            | `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`                                                                       | BUILD SUCCESSFUL                                                                                                                                        |

Base revision: `1e53298888045138bf188be3baa52f3fea3cf5ed`.

## Dismissals (claim-scan candidates)

- **`count`** — every hit is a value the change itself declares or a record reports — a task number, a declared floor,
  an exit status, a diffstat — never a corpus tally or an unattended count.
- **`absolute`** — every hit is a scope word in the change's own artifacts (its planning files and the guidance it
  ships), a record's own measured observation, or the proper name of the UI action, File → Reload All from Disk.
- **`path`** — every hit is a path, command, ref, or path-shaped token in the change's prose or code, and every one
  names something the change reads, runs, or writes.
- **`cross-ref`** — every hit is a slash-bearing literal in prose or code (a path, a ratio, an identifier), never a
  reference to another artifact's section.

## Risks

- **The measured state is one IDEA build and one storage mode.** The experiments ran on IntelliJ IDEA 2026.2.0.1 with
  external project storage; the record states the bound, and its reopening condition names what a later run must
  collect.
- **The Gradle-project load prompt depends on the IDE.** Opening a prepared project may prompt to load the Gradle
  project; accepting it kept every module in the measurement, and the guidance names it rather than assuming it.
- **Project-level Java defaults are out of scope.** A fresh Gradle import leaves the project SDK unset and the language
  level at the IDE default, while the modules carry their own SDKs (74 `temurin-21`, 8 `temurin-25`, and the Python SDK
  for `scripts`), so Java analysis exists. Pinning the project-level values would be a separate capability, matched from
  the local JDK table like the Python interpreter.
- **Attention is unmeasured.** The committed control proves the section exists, is placed last, and carries every
  clause; it cannot prove a contributor notices it.
