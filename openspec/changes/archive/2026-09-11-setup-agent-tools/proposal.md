# Proposal: Agent-driven tooling setup

## Why

The README documents the tooling MCP servers, but wiring them up is still a manual, discovery-heavy chore: a fresh
contributor must know to install the `gh-mcp` extension, authenticate, and hand-edit their global
`~/.config/opencode/opencode.jsonc`. The point of the agentic workflow is that you _ask_ and it happens — so the setup
should be one request away.

## What Changes

- Add a **`setup-agent-tools` skill** (auto-loads when someone asks to set up the tooling) and a matching
  **`/setup-agent-tools` command** (an explicit trigger) that walk the agent through the setup.
- The agent performs the setup (installing the tooling and writing the global config with `opencode mcp add`):
  - **GitHub (core)** — check `gh`; if unauthenticated, hand the interactive `gh auth login` to the user; install the
    `shuymn/gh-mcp` extension; wire it into the global config with `opencode mcp add github -- gh mcp`.
  - **Steroid (optional, IntelliJ IDEA only)** — only if the user uses IDEA: install `devrig` if missing (its
    one-command installer), add the MCP Steroid IDE plugin, and wire it with `opencode mcp add steroid -- devrig mcp`;
    otherwise skip it.
  - **Playwright** — already project-configured; nothing to do.
  - Verify with `opencode mcp list` and tell the user to restart (MCP config is read at startup).
- Refresh the README's MCP section to present the agent-driven setup as the **preferred, easiest path** (today the
  README documents only the manual wiring — the agent-driven path is not mentioned), distinguish **GitHub (the one that
  matters for the agent's PR/CI flow)** from **Steroid (only if you use IDEA)**, and surface the on-request setup in the
  intro and the Technologies/In the Process list.
- Add a short `AGENTS.md` note so the agent knows the command/skill exist.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- None. This is tooling/documentation only — it adds a contributor setup path and does not change any product behavior.

## Impact

- **New**: `.opencode/skills/setup-agent-tools/SKILL.md`, `.opencode/commands/setup-agent-tools.md`.
- **Docs**: `README.md` (MCP section + slash-command table), `AGENTS.md`.
- **Per user, at run time**: `opencode mcp add` writes the contributor's global `~/.config/opencode/opencode.jsonc` (no
  hand edit), and the agent installs the `gh-mcp` extension. No repository config is committed for the auth-/IDE-bound
  MCPs.
