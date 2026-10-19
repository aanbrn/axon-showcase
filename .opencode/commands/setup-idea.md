---
description: Set up the project's IntelliJ configuration (reconcile settings + formatters incl. web Prettier)
---

Set up the project's IntelliJ configuration so the IDE matches the build formatter.

**Trigger** — the owner invokes `/setup-idea`.

**Procedure**

1. Follow the `setup-idea` skill: run `./scripts/setup-idea.sh`. The script and the skill own what the merge touches,
   the plugins it installs, and the Prettier scope it enables — read them there rather than here.
2. Report what the script reports: what it restored, left unchanged, or skipped.

**Decision points**

- IF the script reports the IDE is running, THEN ask the owner to close it and re-run for the plugin install.
- Tell the owner how to apply the configuration, as the skill's hand-back step states.

**Failure handling** — a merge the script cannot complete is reported, with the state it found left intact.

**Verification** — the script's own report of what it restored.

**Completion criteria** — the configuration is merged, the plugins are installed or the owner has been asked to close
the IDE, and the owner knows how to apply it.
