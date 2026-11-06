---
name: setup-agent-tools
description:
  Set up this repo's tooling MCP servers — the GitHub MCP (core) and, for IntelliJ IDEA users, the IDE's built-in MCP
  server reached from a terminal or from JetBrains Air — by detecting what is already installed and wiring the rest into
  the user's own configuration. Use when the user asks to set up, install, or configure the agent tooling, the
  GitHub/gh-mcp MCP, or the IntelliJ IDEA MCP server, including OpenCode inside JetBrains Air.
license: MIT
---

# Set up the tooling MCP servers

This repo's agents get extra reach from MCP servers. Playwright is already configured in the project
(`.opencode/opencode.json`); the servers below are per-user, and each lives in its own configuration file: the GitHub
and client-config IDE entries in the reader's **global** OpenCode config (`~/.config/opencode/opencode.jsonc`), and an
Air registration in JetBrains' `~/.jetbrains/acp.json`.

- **GitHub — core.** Lets the agent read pull requests and CI checks.
- **IntelliJ IDEA's built-in MCP server — optional, IntelliJ IDEA only.** The IDE's own server, the agent's way to run
  inspections, refactorings, formatting, builds, run configurations, and the debugger through the live IDE. There are
  two ways to reach it: OpenCode in a terminal or the desktop app (an `intellij` entry in the global config), or
  OpenCode hosted inside JetBrains Air (a local ACP agent in `~/.jetbrains/acp.json`).
- **MCP Steroid — fallback, IntelliJ IDEA only.** The `devrig` bridge, for the cases the built-in server cannot serve:
  an IDE release without it, a headless backend provisioned through `devrig backend`, and screenshot or UI-input work,
  which the built-in server has no tools for.

Never commit these entries to the project config: they are auth- or IDE-bound, and would not work for anyone else
without their own credentials or IDE.

The recipe below was verified against IDEA 2026.3 (build `IU-263.7307.200`, its bundled MCP Server 263.7307.200),
JetBrains Air bundled with it, and OpenCode 2.2.5. Another release can rename a tool or change the ACP schema, so read
the installed server's tool list and the applicable ACP configuration before applying it, and report an unknown version
or schema as unsupported rather than assuming these names.

## Procedure

Work idempotently — detect first, act only on what is missing, and change nothing that already works. Confirm with the
user before reading or editing files outside this repo.

1. **Detect the current state.**
   - `opencode mcp list` — which servers exist, and their status.
   - `gh auth status` — is the GitHub CLI installed and authenticated?
   - `gh extension list` — is `shuymn/gh-mcp` installed?
   - `opencode acp --help` (or the CLI's subcommand list) — does this OpenCode serve ACP?
   - `which devrig` — is the Steroid bridge present?

2. **GitHub (core).**
   - If `gh` is missing, point the user at https://cli.github.com and stop this step.
   - If not authenticated, ask the user to run `gh auth login` (it is interactive — you cannot complete it) and wait.
   - If `shuymn/gh-mcp` is absent from `gh extension list`, run `gh extension install shuymn/gh-mcp`.
   - If `opencode mcp list` shows no `github` server, add it to the **flat** `mcp` map of the user's global config
     (`~/.config/opencode/opencode.jsonc`) — beside the existing servers, as
     `"github": { "type": "local", "command": ["gh", "mcp"] }`. An entry is enabled unless the configuration disables
     it. Write the flat map, not an `mcp.servers` nesting: OpenCode 1.x consumers (the npm `latest` line, and the agent
     IntelliJ IDEA's AI launches) reject `mcp.servers`, while the flat map loads under both lines.

3. **IntelliJ IDEA's built-in MCP server (optional, IntelliJ IDEA only).**
   - Ask where the reader wants to run OpenCode: **terminal/desktop**, **inside the IDE through Air**, or **both**. If
     they do not want the agent working through their IDE at all, skip this step — nothing in the repo requires it.
   - **Terminal/desktop, or both:** enable the IDE's MCP Server on its settings page (search the settings for "MCP
     Server"; the IDE shows a consent dialog), pin its `Port:` there so the URL survives a restart, and copy the URL the
     IDE shows — the status-bar MCP Server widget's "Copy server URL", or the settings page. Then add it to the flat
     `mcp` map of the global config: `"intellij": { "type": "remote", "url": "<the URL the IDE shows>" }`. OpenCode is
     not one of the IDE's auto-detected clients, so the entry is added by hand.
   - **Air, or both:** check `~/.jetbrains/acp.json` first. If it already registers an OpenCode agent whose flags match
     the reader's choice below, reuse it and change nothing; if it registers one whose flags differ — an Air-only
     registration used for "both" keeps the IDE server injected beside the new global entry — report the difference and
     offer, with confirmation, to set it to the tested combination, leaving it as found if they decline. Otherwise
     register it there, with confirmation:
     `"OpenCode": { "command": "<absolute path to opencode>", "args": ["acp"], "use_idea_mcp": <see below>, "use_custom_mcp": true }`,
     preserving every other key in the file. Read the file's `default_mcp_settings` before writing and report it; when
     it contradicts the reader's choice, offer to set it to match, and leave it as found if they decline. Set the two
     flags from the tested combination for the reader's choice — Air alone: `use_idea_mcp: true` (Air injects the IDE
     server, and the global config need not carry an `intellij` entry); both: `use_idea_mcp: false` with the `intellij`
     entry present (Air uses the same global config as the terminal, so one IDE server reaches both hosts). Both
     combinations were checked in Air on IDEA 2026.3: with the flag on and no `intellij` entry, the session reached the
     IDE through Air's injected server — it shows up there as `jetbrains_air_ide`, whose `execute_tool` routes to the
     IDE's tools — and with the flag off and the entry present, one server served both hosts with no doubling.
   - The IDE-script toolset (`run_ide_script`, the built-in counterpart of `steroid_execute_code`) is off by default:
     its gate is the `mcp.ide.script.toolset.enabled` registry key (the IDE's Registry dialog), and the MCP Server
     settings page is where the IDE's exposed tools are chosen. Each script asks for its own confirmation unless Brave
     Mode is on — the same switch that skips terminal-command and run-configuration confirmations.
   - A server that reads `failed` plausibly carries a stale URL: `/reconnect-mcp` reconnects a server, but it cannot
     retarget a changed port, so re-copy the URL when the IDE's port moved.
   - Ask whether the reader needs the Steroid fallback the next step installs — an IDE release without the built-in
     server, a headless backend, or screenshot and UI-input work. If they do, keep any existing `steroid` entry (the
     next step adds one if there is none), and say that both may be connected and that this guidance tells the agent to
     use the built-in server for IDE work. If they do not, offer to remove an existing `steroid` entry rather than
     leaving it wired.

4. **Steroid bridge (fallback, IntelliJ IDEA only).** Run this step only when the reader said in the previous step that
   they need it, for an IDE release whose built-in server cannot serve, a headless backend, or screenshot and UI-input
   work.
   - If `devrig` is not on `PATH`, offer its one-command installer (`curl -fsSL https://devrig.dev/install.sh | sh`);
     run it only after the user confirms — it executes a downloaded script and installs into `~/.mcp-steroid` — then
     re-check.
   - Install the MCP Steroid plugin into the running IDE with `devrig install plugin`, or point the user at the
     JetBrains Marketplace; the IDE shows its own confirmation dialog.
   - If `opencode mcp list` shows no `steroid` server, add
     `"steroid": { "type": "local", "command": ["devrig", "mcp"] }` to the same flat `mcp` map (use the absolute
     `devrig` path if the agent runs from a GUI, whose `PATH` may be minimal).
   - The bridge's script runtime fails on IDEA 2026.3 with
     `NoClassDefFoundError: com/fasterxml/jackson/databind/ObjectMapper` in `McpScriptContextImpl`; the owning project
     (`jonnyzzz/mcp-steroid`) publishes no writable tracker — its GitHub issues and discussions are disabled and its
     home page lists no contact — so there is nowhere to file it yet. Re-check that venue before reporting the next
     occurrence.

5. **Verify and hand back.**
   - For the client entry: confirm `opencode mcp list` shows `intellij` present and `connected`, then invoke one IDE
     tool from a session (for example `get_project_modules` with the project path, or `lint_files` on one file) and
     confirm it returns a result. A connected server alone does not prove the wiring. OpenCode reads the config at
     startup, and a running instance can pick a new entry up without a restart — on 2.2.5, `mcp list` and the session's
     tool list both reflected it without one — so restart only when the tools do not appear. If the endpoint accepts
     connections but never answers, toggle the IDE's MCP Server off and on in its status-bar widget: an IDE 2026.3
     server was once stuck that way and answered immediately after the toggle.
   - For an Air registration: `opencode mcp list` does not show it — Air reads the file at its own start. Confirm the
     agent appears in Air's own list, then invoke one IDE tool in an Air session the same way.
   - Hand back only what you could not do for the reader (an interactive login, a GUI confirmation, the IDE's own
     install dialog), naming the surface that reads each file.

## Notes

- Prefer editing the flat `mcp` map directly. `opencode mcp add` also works, but it defaults to the **project** config —
  pass `--global` — and it writes the `servers` nesting, which 1.x consumers reject; if you use it, move the entry out
  of `servers` into the flat map afterwards. (`anomalyco/opencode#49904`, which closed `not_planned`; retire this caveat
  when `mcp add` emits the flat shape.)
- If a `github`, `intellij`, or `steroid` server already exists as a flat `mcp` entry, or an `agent_servers` entry
  already registers OpenCode, leave it and say so; do not duplicate it — except that an Air registration whose flags
  contradict the reader's hosting choice is aligned with confirmation, as the Air step says. If it sits under an
  `mcp.servers` nesting (a config written before this change), offer to move it into the flat map — that nesting is what
  makes the 1.x consumers refuse the file.
