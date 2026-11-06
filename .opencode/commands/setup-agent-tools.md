---
description: Set up the tooling MCP servers (GitHub core; IntelliJ IDEA's built-in MCP server, from a terminal or Air)
---

Set up this repo's tooling MCP servers for the current user.

**Trigger** — the owner invokes `/setup-agent-tools`.

**Procedure**

1. Detect the current state: which MCP servers the user's configuration already declares.
2. Wire the **GitHub** MCP (core), and — for IntelliJ IDEA users who want the agent working through their IDE — the
   IDE's **built-in MCP server**: in the global OpenCode config for OpenCode in a terminal or the desktop app, and in
   JetBrains' `~/.jetbrains/acp.json` for OpenCode hosted inside Air. The **Steroid** bridge stays the fallback for the
   cases the built-in server cannot serve.
3. Verify each wired path on its own surface: `opencode mcp list` and one IDE operation for the client entry; Air's
   agent list and one IDE operation in an Air session for the ACP registration.
4. Tell the owner which process to restart if the tools do not appear — OpenCode for the client entry, Air for the ACP
   registration.

**Decision points**

- IF the setup would read or edit one of the user's per-user configuration files — the global OpenCode config or
  `~/.jetbrains/acp.json` — or install an extension or run an external installer, THEN ask for confirmation first,
  naming what it would touch.
- The `setup-agent-tools` skill is the authority for the exact procedure — its detection order, the configuration shape,
  and the per-server wiring. Follow it rather than reconstructing the steps here.

**Failure handling** — a configuration the skill cannot write is reported, with the state it found left intact.

**Verification** — each wired path is confirmed on its own surface: `opencode mcp list` plus one IDE operation for the
client entry, and Air's agent list plus one IDE operation in a session for the ACP registration.

**Completion criteria** — the paths the owner chose are wired (or declined), each is verified on its own surface, and
the owner has been told which process to restart.
