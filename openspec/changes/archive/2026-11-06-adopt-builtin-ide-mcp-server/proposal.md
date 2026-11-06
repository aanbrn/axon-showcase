# Adopt the IDE's built-in MCP server and let the reader choose how to reach it

## Why

Steroid 0.102.0 cannot run a script on IDEA 2026.3: every `steroid_execute_code` call fails before the script body
executes, with `NoClassDefFoundError: com/fasterxml/jackson/databind/ObjectMapper` raised in
`McpScriptContextImpl.<init>` — so the repository's IDE reach (inspections, refactors, semantic searches) is dead on the
IDE version the owner runs, and no local setting can repair it. IDEA 2026.3 bundles its own MCP Server
(`com.intellij.mcpServer` 263.7307.200), and it already exposes the IDE operations this repository uses — inspections,
refactoring, formatting, builds, run configurations, the debugger, coverage, the profiler, VCS. The guidance therefore
has to name that server first, offer both ways of running the agent against it, and keep the Steroid bridge only where
the built-in server cannot serve.

## What Changes

- **`AGENTS.md`** — the rules that name the IDE server:
  - "IDE inspections (optional)": the optional inspection pass goes through the built-in server's `lint_files` and
    `get_file_problems` tools, not through `steroid_execute_code` and its `runInspectionsDirectly` helper;
  - "Tooling-setup skill and command": the optional per-user IDE server is the built-in one, with the Steroid bridge as
    its fallback, and the README now names the integrations while the skill owns their procedures;
  - the README restriction inside that bullet is rewritten rather than bypassed: the README names the integrations and
    their user-facing benefit, the skill owns configuration, fallback and hosting, and the Steroid bridge stays a
    fallback documented in the skill.
- **`README.md`** — the `Tooling MCP Servers` subsection gains an **IntelliJ IDEA's built-in MCP server** entry in its
  server list, stating what it gives the agent and pointing at `/setup-agent-tools`; the `/setup-agent-tools` row in the
  slash-command table replaces "optional extras" with the IDE connection it offers; the "one that matters" sentence
  distinguishes GitHub setup, the optional IDE connection, and the already-configured Playwright.
- **`.opencode/skills/setup-agent-tools/SKILL.md`** — the procedure gains an IDE section:
  - it asks where the reader wants to run OpenCode — terminal/desktop, inside the IDE through JetBrains Air, or both;
  - terminal/desktop: enable the IDE's MCP Server, copy the URL the IDE shows, and wire that URL as an `intellij` entry
    in the flat `mcp` map, pinning the port so the URL survives a restart;
  - Air: detect an existing OpenCode registration first, then — with confirmation — register it as a local ACP agent in
    `~/.jetbrains/acp.json`, with `use_idea_mcp` and `use_custom_mcp` set per the combination the live check settles;
  - both: both wirings, with the same coexistence rule;
  - around it the skill states that OpenCode is not one of the IDE's auto-detected clients, that the IDE-script toolset
    (`run_ide_script`, the built-in counterpart of `steroid_execute_code`) is off by default behind the
    `mcp.ide.script.toolset.enabled` registry key and confirms every script unless Brave Mode is on, that a `failed`
    server plausibly carries a stale URL, and that a machine still wiring the Steroid bridge is offered the switch
    rather than left with both;
  - the versions the recipe was verified against are recorded, and the reader is told to read the installed server's
    tool list and ACP schema first, with an unknown release reported unsupported rather than inheriting these
    assumptions.

  The Steroid steps remain for the cases the built-in server cannot serve.

- **Upstream** — the reproduced Steroid defect gets a disposition `AGENTS.md`'s upstream-report rule requires: an
  existing report cited in the skill's fallback section, or a drafted report for the owner's decision and the review
  gate, never a "third-party" exemption.

- **`.opencode/commands/setup-agent-tools.md`** — its description and procedure step name the built-in server, the Air
  hosting option, and the Steroid fallback.
- **`docs/ideas.md`** — the 2026-11-06 entry is removed: both halves of it are delivered here.

### New Capabilities

None.

### Modified Capabilities

None. No capability spec names the per-user agent-tooling MCP setup — `grep -rn "setup-agent-tools" openspec/specs/`
returns no hits, and the corpus's `mcp` mentions (the `agent-skills` Purpose, the technical advisor's tool-deny list,
and the failed-server recovery requirement) are none of them altered here — and `showcase/quality/code-quality`'s
"Quality verification does not require an IDE" is unchanged, since the IDE path stays optional. The change is therefore
`skip_specs: true`, with its evidence in the change's own artifacts.

## Impact

- **Files**: `AGENTS.md`, `README.md`, `.opencode/skills/setup-agent-tools/SKILL.md`,
  `.opencode/commands/setup-agent-tools.md`, `docs/ideas.md`, and the change's own artifacts.
- **Code, APIs, dependencies**: none — the change edits guidance and a skill/command definition.
- **Build and tests**: no task changes. The build's definition-inventory assertion keeps proving every command and skill
  definition still resolves with its metadata after the edit, and the markdown formatter owns the wrapping.
- **Out of the repository**: the reader's global `~/.config/opencode/opencode.jsonc` and `~/.jetbrains/acp.json` are not
  committed and are written only through `/setup-agent-tools` with confirmation; on the owner's instruction this unit's
  own delivery switches **this machine's** global config from the Steroid bridge to the built-in server (task 6.4) and
  runs one Air session (task 6.5), and both wirings are proven only in those live runs.
- **Verification**: the built-in server's identity and tool list are read back over its HTTP endpoint, its IDE-script
  gating is read from the bundled plugin, the ACP configuration file's location and schema are read from the bundled Air
  plugin, and one interactive session in Air settles the coexistence question and the agent listing.
