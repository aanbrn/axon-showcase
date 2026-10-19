---
description: Set up the tooling MCP servers (GitHub core; Steroid optional for IDEA)
---

Set up this repo's tooling MCP servers for the current user.

**Trigger** — the owner invokes `/setup-agent-tools`.

**Procedure**

1. Detect the current state: which MCP servers the user's configuration already declares.
2. Wire the **GitHub** MCP (core), and optionally the **Steroid** MCP for IntelliJ IDEA users who want the agent working
   through their IDE.
3. Verify with `opencode mcp list`.
4. Tell the owner to restart OpenCode, since the server list is read at startup.

**Decision points**

- IF the setup would read or edit the user's **global** OpenCode configuration, or install an extension or run an
  external installer, THEN ask for confirmation first, naming what it would touch.
- The `setup-agent-tools` skill is the authority for the exact procedure — its detection order, the configuration shape,
  and the per-server wiring. Follow it rather than reconstructing the steps here.

**Failure handling** — a configuration the skill cannot write is reported, with the state it found left intact.

**Verification** — `opencode mcp list` shows the wired servers.

**Completion criteria** — the servers are wired (or the owner declined), verified, and the owner has been told to
restart OpenCode.
