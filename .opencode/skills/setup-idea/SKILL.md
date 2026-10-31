---
name: setup-idea
description:
  Set up this repo's IntelliJ configuration so the IDE matches the build formatter and analyzes scripts/*.py — merge the
  committed settings (config/idea/*.xml plus the test-tier naming inspection) into .idea/, reconcile the Python analysis
  module for the current working tree, install the palantir-java-format + ktfmt plugins, and enable IntelliJ's built-in
  Prettier for the web module. Use when the user asks to set up or fix their IntelliJ/IDE configuration, or when the
  configuration has drifted (e.g. IntelliJ rewrote .idea/).
license: MIT
---

# Set up the project's IntelliJ configuration

`./scripts/setup-idea.sh` is the primary, token-free path — a contributor runs it directly (repeatedly, when the
configuration has drifted). The script merges the committed settings (`config/idea/*.xml` and the test-tier naming
inspection) into `.idea/`, preserving IDE-managed content, reconciles the Python analysis module for the current working
tree, installs the palantir-java-format and ktfmt plugins, and enables IntelliJ's built-in Prettier for the web module
(run on reformat/save; the JS/TS indentation comes from `.editorconfig`). Applying the configuration needs neither the
IDE launcher nor a closed IDE; only the plugin install is skipped when IntelliJ is running or not found, and that skip
is legitimate.

The Python stage's selection rule, the module it writes, and the conditions behind each outcome are the delta spec's and
the script's; this skill carries what to do for the outcome it reports.

## Procedure

1. **Run the script** from the repo root: `./scripts/setup-idea.sh` (the merged configuration covers the JVM formatters
   and the web module's built-in Prettier).
2. **If it reports the IDE is running**: the plugin install needs the whole application closed — ask the user to exit
   IntelliJ (File → Exit), then re-run. Do not close it yourself. The Python module does not need that: it needs this
   project's window closed, so if the project was open during the run, close just its window, re-run, and open the
   project again; other projects may stay open.
3. **If the launcher is not found**: the configuration is still applied — tell the user to pass the launcher path
   (`./scripts/setup-idea.sh /path/to/idea`), set `IDEA_HOME`, or install the plugins from the IDE.
4. **Read the Python line** and act on its outcome:
   - `CONFIGURED` — the module is written; it appears when the project is opened, as step 5 states.
   - `SKIPPED` — pass `--idea-config-dir <path>` if an IntelliJ configuration exists outside the searched roots.
   - `NEEDS_ACTION` — hand the user the block the script printed: its first line states what is wrong, then the remedies
     — register the printed interpreter once per configuration (File → Project Structure → SDKs → Add SDK → Python SDK),
     or run the tree on an interpreter that is already registered (`pyenv local <version>` where pyenv supplies one) —
     and only then the registered SDKs with their interpreter paths and the configurations searched. Walk the user
     through the first remedy or the alternative, then re-run. Every working tree that resolves the same interpreter
     reuses it; the ambiguity case names the matching entries and the `--idea-config-dir` / keep-one-entry remedy.
   - `ERROR` — report the message; no module was written.
5. **Hand back**: report what the script restored, left unchanged, or skipped and both outcome lines. Then give the
   Python module's activation procedure: the module appears when the project is opened after a run made while the
   project was closed, so open the project now; if the project was open while the setup ran, close its window, run the
   setup again, and then open it — reloading an open project does not add the module. If IntelliJ offers to load the
   Gradle project when it opens, accept. The merged formatter and inspection settings are different: IntelliJ reads them
   at startup, so a reload or a restart applies those. A non-zero exit is a `NEEDS_ACTION`/`ERROR` stage or a failed
   plugin installation, not a failed merge: the other stages still ran.
