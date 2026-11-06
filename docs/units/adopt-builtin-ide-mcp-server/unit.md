# Unit: adopt-builtin-ide-mcp-server

Workspace:
`unit=adopt-builtin-ide-mcp-server; path=/Users/afanasyev/Development/axon-showcase-adopt-builtin-ide-mcp-server; branch=adopt-builtin-ide-mcp-server; writer=ses_e500773ebffePTHtVKvbxnk063`

State: implementation-review

The unit's plan and tasks live in `openspec/changes/adopt-builtin-ide-mcp-server/`.

## Resume checkpoint

- **Time and revision** — 2026-11-06, implementation review; the branch forked from `origin/main` at `d420246c` and was
  fast-forwarded to `0b236cde`.
- **Workspace** — the `Workspace:` record above.
- **Stage** — implemented, live checks passed, implementation-stage quick review running. The owner approved the
  proposal after the advisor's three-round review-to-agreement; `AGENTS.md`, `README.md`, the setup skill and command,
  and `docs/ideas.md` are edited; `check`, `spotlessCheck`, `openspec validate --all` and the commit guard pass; the
  client path and both Air configurations were exercised and the skill states the observed combination.
- **Next step** — the owner's explicit skip-or-run decision on the deep review (routing RECOMMENDED; the advisor, asked
  to settle it, recommends a skip), then the owner's manual pass over the finished unit.
- **Change set** — no commits yet; the six edited files, the change dir and this record's directory are staged and
  uncommitted.
- **Blockers and open decisions** — none; the IDE's MCP endpoint answered after the owner toggled the server, and the
  restored per-user configuration is the client entry plus `use_idea_mcp: false`.
- **Pending decision** — the owner's manual pass over the finished unit.
- **Owner's instructions (2026-11-06)** — the widened scope covers both hosting paths and the README entry; delivery
  also switches this machine's global OpenCode config from the `steroid` entry to the built-in server (task 6.4); and
  the owner pre-confirmed the `~/.jetbrains/acp.json` write and the Air launch for the live check (task 6.5).
- **Findings no record holds** — none.
- **Operations in flight** — none.
