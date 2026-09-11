---
description: Set up the project's IntelliJ configuration (reconcile settings + formatter plugins)
---

Set up the project's IntelliJ configuration so the IDE matches the build formatter.

Follow the `setup-idea` skill: run `./scripts/setup-idea.sh`, which merges the committed settings
(`config/idea/*.xml` and the test-tier naming inspection) into `.idea/`, preserving IDE-managed content, and installs
the palantir-java-format + ktfmt plugins when IntelliJ is closed. If the script reports the IDE is running, ask the
user to close it and re-run for the plugins. Report what was restored, left unchanged, or skipped, and tell the user to
apply the configuration with **File → Reload All from Disk** (or restart IntelliJ), since IDEA reads `.idea/` settings
at startup.
