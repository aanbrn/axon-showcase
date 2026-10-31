# Decision record — activating the Python analysis module in IntelliJ

Date: 2026-10-31

Status: a bounded exploration. It binds no standing rule; it records what one probe observed.

## The question

`scripts/setup-idea.sh` writes a Python module for `scripts/` and registers it. Two owner episodes showed the module
present on disk but absent from the IDE's live model, and several restarts and a Reload did not consistently activate
it. The unit `clarify-python-ide-activation` asked whether the registration must also be written to IDEA's external
project store, or whether the storage policy must change, to make activation reliable with the fewest user actions.

## The fixture

A disposable worktree, `axon-showcase-ide-probe`, branched from `origin/main` at `bff517eb`, opened in IntelliJ IDEA
2026.2.0.1 (IU-262.8665.337). The Gradle import completed (82 modules in the live model). External storage was enabled
(`ExternalStorageConfigurationManager enabled="true"`), and both registrations were present: project-local
`.idea/modules.xml` (`ProjectModuleManager`) and the cache-side
`…/Caches/JetBrains/IntelliJIdea2026.2/projects/axon-showcase-ide-probe.2ca70a31/external_build_system/project/modules.xml`
(`ExternalProjectModuleManager`, 82 entries whose `fileurl` values point into the project).

## The observations

1. **Failing control.** `setup-idea.sh` ran while the IDE was open:
   `Python: CONFIGURED — wrote module and registration`, the project-local list gained the `scripts` entry (1 entry),
   the external list stayed at 82. A programmatic invocation of the File → Reload All from Disk action (id
   `Synchronize`) left the live model at **82 modules, no `scripts`**.
2. **The registrations are read by a project-model reload.** Adding the `scripts` entry to the external list (83) and
   invoking `ProjectManagerEx.reloadProject` produced **83 modules, `scripts [PYTHON_MODULE]`**.
3. **Attribution.** Removing the entry from both registrations and reloading gave **82** — the reload re-reads the
   registrations rather than reusing the previous model.
4. **Decisive user-path test.** With only the **project-local** entry present (external list without `scripts`), the
   owner clicked File → Reload All from Disk in the UI: **83 modules, `scripts [PYTHON_MODULE]` with content root
   `<tree>/scripts`**. The external registration was unnecessary for that outcome.
5. **The instrumentation differs from the user action.** The programmatic `Synchronize` invocation does not reload the
   project model; the same menu item from the UI does. Why the two paths differ is not established (no source or trace
   evidence), so the difference is recorded as a measurement, not as a mechanism.

## The result

On this tested state, **one setup run followed by one File → Reload All from Disk activates the module with the existing
project-local registration**. No external write, no storage-policy change, and no close/reopen are needed. That is the
shortest demonstrated successful flow; it is not proven to be the minimum possible, and it was not measured across other
IDEA versions or before the formatter plugins are first installed.

**Superseded — see “Reopened — 2026-10-31” below:** the reload step did not reproduce in a tree whose `.idea/` had been
deleted; the activation is the project's open, not the reload.

## What it does not establish

- **Gradle-sync survival.** The probe did not run a normal Gradle sync after activation; the reopened experiments below
  did, in both starting states, so this record now carries that result.
- **First-time plugin installation**, which still needs the IDE closed, or its consequences for this flow.
- **Any mechanism** behind the programmatic-versus-UI difference, or the IDE's internal rewrite of the registrations.

## Reopened — 2026-10-31, and what the experiments established

The reopening condition fired. In the unit's own tree the owner closed the IDEA window, deleted `.idea/`, reopened the
folder, ran `scripts/setup-idea.sh`, and chose File → Reload All from Disk. The live model stayed at **82 modules**,
none with a content root of `<tree>/scripts`; `.idea/modules.xml` listed exactly the one `scripts` module. The earlier
result — a local-only entry activating on Reload — did not reproduce.

Further experiments, in disposable worktrees, established the mechanism and the working procedure.

### The mechanism

- With external project storage in use, the Gradle import keeps the model in the workspace-model cache
  (`…/projects/<hash>/project-model-cache/cache.data`), and `.idea/modules.xml` is a compatibility file rather than the
  model. Writing it while the project is open changes nothing the IDE reads.
- IntelliJ applies the project-local module list when the **project opens**. A reload of an open project does not add a
  module the model does not already have, and a restart or reopen of an already-imported project does not repair it.
- The owner's counterexample bounds the flag: the primary checkout reports
  `ExternalStorageConfigurationManager.isEnabled = true` **and** keeps its modules under `.idea/modules/` with a full
  `modules.xml`, because its model was written locally when it was imported. The flag is not by itself the predicate of
  where an existing project's model lives.
- When the model _is_ local, the script's write is picked up immediately: in the migrated probe tree, one setup run took
  the live model from 82 to 83 modules with `scripts [PYTHON_MODULE]` and the expected interpreter, with no reload and
  no reopen.

### The working procedure (measured)

1. **A fresh worktree with no `.idea`:** run `./scripts/setup-idea.sh`, then open the folder. IntelliJ offers to load
   the Gradle project; accepting it yields 83 modules, the Python module among them with the expected SDK. A Gradle sync
   afterwards and a close/reopen both left it in place.
2. **A project already imported:** close its window, run the setup, then open it. The reopened model held 83 modules,
   the Python module with its SDK, and a Gradle sync afterwards left it in place.

### The Java settings (observed, out of scope)

A fresh import of the setup-prepared project left the project-level SDK unset and the language level at the IDE default
(`JDK_26`), while each module carried its own SDK — `temurin-21` for 74 JVM modules, `temurin-25` for the other 8 JVM
modules, and the Python SDK for `scripts`. The project-level values are what a fresh Gradle import produces: the
migration probe's project, imported without a setup-created `.idea`, ended with the same `misc.xml` content. The setup
therefore does not remove them. Pinning them would be a separate capability; this record states only what was measured.

### The alternatives measured and set aside

- **Flip the project to local module storage (`misc.xml`, `externalStorage=false`).** Applied on a live project it takes
  effect on reload, and the migration happens when the project closes — but that migration rewrites `modules.xml` from
  the model and drops the entry a run had just written, so the setup has to run again after the reopen. Written while
  the IDE is closed, the same flag leaves the project without its modules until a Gradle re-import. Five steps per tree
  for no gain over the procedure above.
- **A Gradle subproject with `gradle-idea-ext-plugin` (`rootModuleType = 'PYTHON_MODULE'`).** The plugin can set the
  module type, but not the Python SDK (`JetBrains/gradle-idea-ext-plugin#134`, open: "Make SDK configurable (e.g. Python
  SDK)"), so the interpreter would still be assigned by hand, at the cost of a new build project.

The revised verdict: **the module is activated by opening the project after a run made while the project was closed**; a
run made while the project was open is completed by closing the project, running the setup again, and opening it. The
reload instruction is withdrawn.

## Reopening condition

Reopen this record if a project opened after a setup run made while it was closed fails to show the module, or if a
Gradle sync drops an active module — record the operation order, the registration state on disk, the live module type
and content root, and the project's module-storage mode.

The incident narrative is `docs/process-defects/clarify-python-ide-activation.md` #1; the correction that follows this
verdict is the unit `clarify-python-ide-activation`.
