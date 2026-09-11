---
name: setup-idea
description: Set up this repo's IntelliJ configuration so the IDE matches the build formatter — merge the committed settings (config/idea/*.xml plus the test-tier naming inspection) into .idea/ and install the palantir-java-format + ktfmt plugins. Use when the user asks to set up or fix their IntelliJ/IDE configuration, or when the configuration has drifted (e.g. IntelliJ rewrote .idea/).
license: MIT
---

# Set up the project's IntelliJ configuration

`./scripts/setup-idea.sh` is the primary, token-free path — a contributor runs it directly (repeatedly, when the
configuration has drifted). The script merges the committed settings
(`config/idea/*.xml` and the test-tier naming inspection) into `.idea/`, preserving IDE-managed content, and installs
the palantir-java-format and ktfmt plugins. Applying the configuration needs neither the IDE launcher nor a closed
IDE; only the plugin install is skipped when IntelliJ is running or not found.

Prefer telling the user to run the script themselves — it is mechanical and cheap. Use this skill when they ask you
to do it, or when the script needs a handshake a terminal cannot give: quitting a running IDE so a wanted plugin
install can proceed.

## Procedure

1. **Run the script** from the repo root: `./scripts/setup-idea.sh`.
2. **If it reports the IDE is running**: ask the user to close IntelliJ (File → Exit), then re-run so the plugin
   install proceeds. Do not try to close the IDE yourself.
3. **If the launcher is not found**: the configuration is still applied — tell the user to pass the launcher path
   (`./scripts/setup-idea.sh /path/to/idea`), set `IDEA_HOME`, or install the plugins from the IDE.
4. **Hand back**: report what the script restored, left unchanged, or skipped, and tell the user to apply the
   configuration with **File → Reload All from Disk** (or restart IntelliJ) — IDEA reads `.idea/` settings at startup,
   so the change takes effect only after a reload.
