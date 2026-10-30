---
description: Set up the project's IntelliJ configuration (settings + formatters incl. web Prettier + Python analysis)
---

Set up the project's IntelliJ configuration so the IDE matches the build formatter.

**Trigger** — the owner invokes `/setup-idea`.

**Procedure**

1. Follow the `setup-idea` skill: run `./scripts/setup-idea.sh`. The script and the skill own what the merge touches,
   the plugins it installs, the Prettier scope it enables, and the Python module it reconciles — read them there rather
   than here.
2. Report what the script reports: the `Settings:` line and what it restored, left unchanged, or skipped, plus the
   `Python:` line and its outcome.

**Decision points**

- IF the script reports the IDE is running, THEN ask the owner to close it and re-run for the plugin install. That skip
  is not a failure.
- IF the Python line reports `NEEDS_ACTION`, THEN hand the owner the interpreter path and the settings page the message
  names: registering that interpreter is a one-time step per IntelliJ configuration, and the script exits non-zero until
  it is done.
- IF the owner works in several working trees, THEN note that each tree needs its own setup run — the module lives in
  the ignored `.idea/` — while the interpreter registration is shared by trees that resolve the same interpreter.
- Tell the owner how to apply the configuration, as the skill's hand-back step states.

**Failure handling** — a merge the script cannot complete is reported, with the state it found left intact; a non-zero
exit means a stage reported `NEEDS_ACTION` or `ERROR`, or the plugin installation failed — not that nothing ran.

**Verification** — the script's own report: one `Settings:` line and one `Python:` line, each carrying its outcome.

**Completion criteria** — the configuration is merged, the plugins are installed or the owner has been asked to close
the IDE, and the owner knows how to apply it.
