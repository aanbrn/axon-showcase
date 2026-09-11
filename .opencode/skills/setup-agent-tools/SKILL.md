---
name: setup-agent-tools
description: Set up this repo's tooling MCP servers — the GitHub MCP (core) and, for IntelliJ IDEA users, the Steroid MCP — by detecting what is already installed and wiring the rest into the user's global OpenCode config. Use when the user asks to set up, install, or configure the agent tooling, the GitHub/gh-mcp MCP, or the JetBrains/Steroid MCP.
license: MIT
---

# Set up the tooling MCP servers

This repo's agents get extra reach from MCP servers. Playwright is already configured in the project
(`.opencode/opencode.json`); the two below are per-user and live in the reader's **global** OpenCode config
(`~/.config/opencode/opencode.jsonc`).

- **GitHub — core.** Lets the agent read pull requests and CI checks.
- **Steroid — optional, IntelliJ IDEA only.** JetBrains IDE tools the agent can use for IDE-accurate operations
  (refactors, searches, the IDE's inspections). Nothing in the repo requires it — offer it only if the user wants the
  agent working through their live IDE.

Never commit these entries to the project config: they are auth- or IDE-bound, and would not work for anyone else
without their own credentials or IDE.

## Procedure

Work idempotently — detect first, act only on what is missing, and change nothing that already works. Confirm with the
user before reading or editing files outside this repo.

1. **Detect the current state.**
   - `opencode mcp list` — which servers exist, and their status.
   - `gh auth status` — is the GitHub CLI installed and authenticated?
   - `gh extension list` — is `shuymn/gh-mcp` installed?
   - `which devrig` — is the Steroid bridge present?

2. **GitHub (core).**
   - If `gh` is missing, point the user at https://cli.github.com and stop this step.
   - If not authenticated, ask the user to run `gh auth login` (it is interactive — you cannot complete it) and wait.
   - If `shuymn/gh-mcp` is absent from `gh extension list`, run `gh extension install shuymn/gh-mcp`.
   - If `opencode mcp list` shows no `github` server, run `opencode mcp add github -- gh mcp`. This writes the user's
     global config; the `-- <command>` form is not shown in `opencode mcp add --help`, but it is the non-interactive
     path and preserves JSONC comments. An entry with no `enabled` key is enabled by default.

3. **Steroid (optional — IntelliJ IDEA only).**
   - Ask whether the user wants the agent to work through their live IDE. If not, skip this step — nothing in the repo
     requires it.
   - If `devrig` is not on `PATH`, offer its one-command installer
     (`curl -fsSL https://devrig.dev/install.sh | sh`); run it only after the user confirms — it executes a downloaded
     script and installs into `~/.mcp-steroid` — then re-check.
   - Install the MCP Steroid plugin into the running IDE with `devrig install plugin`, or point the user at the
     JetBrains Marketplace; the IDE shows its own confirmation dialog.
   - If `opencode mcp list` shows no `steroid` server, run `opencode mcp add steroid -- devrig mcp` (or use the absolute
     `devrig` path if the agent runs from a GUI, whose `PATH` may be minimal).

4. **Verify and hand back.**
   - Run `opencode mcp list` and confirm each server you added is present and `connected`.
   - Tell the user to **restart OpenCode** — MCP config is read at startup, so the tools appear in the next session.

## Notes

- If `opencode mcp add` is unavailable, fall back to adding the entry under the `mcp` object in the user's
  `~/.config/opencode/opencode.jsonc` directly (with their confirmation).
- If a `github` or `steroid` server already exists, leave it and say so; do not duplicate it.
