---
description:
  Report which MCP servers failed to connect and recover them — one named server, or the whole failed set — without
  restarting OpenCode
---

Report, and recover, the MCP servers in the current workspace.

**Trigger** — the owner invokes `/reconnect-mcp` (with no target, `--all`, or one server name), or an MCP tool is
missing after a network interruption and the owner asks for it back.

**Procedure**

1. Run `python3 scripts/reconnect-mcp.py` from the repository root with no target to see every server's status — it
   prints one line per server and changes nothing. `--help` owns the arguments, the deadline, and the exit contract;
   pass `--directory <path>` only when the servers belong to another checkout.
2. IF the report shows a failed server, THEN repair `--all` of them in one run, or name the one the owner asked about.
3. Report the tool's per-server result lines as they printed.
4. IF every targeted server is connected, THEN the recovery is complete. IF the tool reports anything else, THEN report
   those lines as they stand and do not retry in a loop: they name the reason (an unknown name, a status reconnection
   cannot repair, or an outcome the tool could not confirm), and `--help` prints the exit contract.

**Decision points**

- IF the owner asks to reconnect several servers, THEN run `--all` once; it repairs exactly the `failed` set, so a
  browser recovery never touches a server whose status is not `failed`.
- IF the tool reports an unconfirmed outcome, THEN the connect attempt may still be running in the server — wait before
  another attempt instead of firing a second one.
- Adding or editing MCP configuration is a different condition, which the README's Tooling MCP Servers section covers
  rather than this command.

**Completion criteria** — the owner knows every server's status the invocation covered, and the command printed the
tool's own result lines rather than a restatement of them.
