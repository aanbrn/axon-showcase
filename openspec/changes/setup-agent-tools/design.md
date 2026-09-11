## Context

See proposal.md — Why. The repo already ships Playwright as a project MCP (`.opencode/opencode.json`); the
auth-/IDE-bound MCPs (`github`, `steroid`) live in each contributor's global `~/.config/opencode/opencode.jsonc`, which
the repo must not commit. OpenCode can add a local server non-interactively with `opencode mcp add <name> -- <command…>`
— it writes the global config and preserves JSONC comments, so the agent does not hand-edit it.

## Goals / Non-Goals

**Goals:**

- A contributor can type a natural-language request (or `/setup-agent-tools`) and have the agent wire up the GitHub MCP
  — the core tool — with the interactive bits handed back.
- Idempotent: re-running detects what is already installed/configured and changes nothing.
- Never break a contributor's global config.

**Non-Goals:**

- Committing the auth-/IDE-bound MCP entries (they stay per-user).
- Automating `gh auth login` (inherently interactive) or the final OpenCode restart.
- Touching Playwright (already project-configured).

## Decisions

**D1: Wire the config with `opencode mcp add <name> -- <command…>`, not a hand edit or a script.** OpenCode's own
command adds a local server to the global config and preserves JSONC comments, so the agent drives the blessed primitive
rather than editing JSONC by hand; a `scripts/setup-mcp.sh` would duplicate it. An entry with no `enabled` key is
enabled by default, so the command's output needs no manual config fix-up. _Alternatives considered:_ a direct JSONC
edit (kept as a fallback if `mcp add` is unavailable — it would need an external-directory grant, which the user
confirms); a setup script (rejected — unnecessary). _Caveat:_ the `-- <command>` form is not shown in `--help` (which
implies an interactive flow), so the skill documents it explicitly.

**D2: Ship both a skill and a command.** The skill (auto-loaded by its description) is what makes "just ask the agent to
set up the tools" work without knowing a command; the `/setup-agent-tools` command is the discoverable, documented entry
point. The command points at the same procedure rather than duplicating it.

**D3: GitHub is core; Steroid is opt-in (IntelliJ IDEA only).** Per the maintainer: most contributors don't need
Steroid. The procedure always covers GitHub, and only adds `steroid` when the user uses IDEA.

**D4: Detect before acting (idempotency).** Use `opencode mcp list` (existing servers + status), `gh extension list`
(the `shuymn/gh-mcp` extension), `gh auth status`, and `which devrig` to decide each step, skipping what exists.

**D5: Hand off the interactive steps and the restart.** `gh auth login` is run by the user (the agent
triggers/instructs); the final message tells the user to restart OpenCode because MCP config is read at startup.

## Risks / Trade-offs

- [A mis-typed server name or a wrong command leaves the server failing to connect] → the agent verifies the result with
  `opencode mcp list`, which reports each server's connection status, right after adding it.
- [A contributor's config may already carry these entries, possibly with different names] → detection via
  `opencode mcp list` is by server name; if a `github`/`steroid` server already exists, the agent leaves it and says so.
- [Installing `devrig` executes a downloaded shell script] → the skill presents the vendor's documented one-command
  installer (`curl -fsSL https://devrig.dev/install.sh | sh`, idempotent, into `~/.mcp-steroid`) and runs it only after
  the user confirms; the IDE-plugin step (`devrig install plugin`) uses the IDE's own confirmation dialog.
