# Reconnect a Failed MCP Server

## Why

An MCP server that fails to connect at startup stays failed: OpenCode records the failure and never retries it, so the
tool is missing for that workspace until OpenCode restarts. A laptop wake makes this likely — the network comes up after
the connect attempt, `npx` cannot reach the npm registry, and the attempt stalls for minutes. The running OpenCode
server exposes a per-server connect operation that repairs it in about a second, and nothing in the repository tells a
person or the agent how to reach it.

## What Changes

- **`scripts/reconnect-mcp.py`** — new on-demand tool: a read-only report of every server's status, or a repair of one
  named server or of the whole `failed` set; one connect attempt per server, a verified `connected` result, a bounded
  deadline per attempt, and a documented exit contract.
- **`scripts/test-reconnect-mcp.py`** — its suite, driving the tool's entry point against a fixture `opencode`
  executable so the real transport boundary is exercised.
- **`build.gradle.kts`** — a `testReconnectMcp` task, registered in the root `check`.
- **`.opencode/commands/reconnect-mcp.md`** — the `/reconnect-mcp` trigger, pointing at the tool rather than restating
  its procedure.
- **`README.md`** — the recovery note in "Tooling MCP Servers", the slash-command row, and the Python prerequisite row.
- **`AGENTS.md`** — one pointer in the agent-tooling section: a failed MCP server is recovered with the command, not by
  restarting OpenCode.
- **`docs/ideas.md`** — the upstream report this change surfaces is parked with its retirement condition, to ship as its
  own unit.
- **`openspec/changes/reconnect-failed-mcp/specs/showcase/quality/agent-skills/spec.md`** — two `ADDED` requirements.
- No change to `.opencode/opencode.json`, the MCP configuration, Gradle tracking, or the workflows.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills` — two `ADDED` requirements: "A failed MCP server can be recovered on demand" (a
  read-only report, one named target, or the whole `failed` set; a no-op when already connected; one attempt; a verified
  result) and "A recovery attempt is bounded and reports only what it confirmed" (one deadline per attempt, the exit
  contract, sanitized output). No existing requirement covers MCP recovery:
  `git grep -ni 'mcp' 02f839d9 -- openspec/specs` returns only the technical advisor's permission clauses.

## Impact

- **Build**: one new `Exec` verification task (`testReconnectMcp`), added to the root `check` alongside the existing
  Python suites; it runs the repository's declared Python minimum and needs no network.
- **Code, APIs, dependencies, deployment**: none.
- **Agent tooling**: a new `scripts/` tool with a command trigger, and the documentation that names it.
