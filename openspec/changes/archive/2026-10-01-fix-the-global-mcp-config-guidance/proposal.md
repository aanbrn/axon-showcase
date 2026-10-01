# Proposal: Keep the global MCP config loadable by both OpenCode lines

## Why

IntelliJ IDEA's AI could not start: its ACP session launches OpenCode **1.18.15**, which rejects the v2-only spellings
in the user's global config — `Unrecognized key: permissions`, then `mcp.servers` (`Missing key mcp.servers.enabled`).
The repository's own setup path produces exactly that shape: the README's global snippet shows `mcp.servers`, and the
`setup-agent-tools` skill's `opencode mcp add <name> -- <command…>` writes `mcp.servers` (and, on 2.0.21, targets the
_project_ config unless `--global` is passed). A flat `mcp` map plus `permission` loads under both lines, so the docs
must lead there. Upstream, the CLI half is `anomalyco/opencode#49904` and the docs/schema half `#52177`/`#43748`.

## What Changes

- `.opencode/skills/setup-agent-tools/SKILL.md` — the global-config recipe: pass `--global`, and add the server to the
  flat `mcp` map rather than the `mcp.servers` nesting; correct the two claims about `opencode mcp add` (that it writes
  the global config by default, and that the `-- <command>` form is absent from its `--help`); and offer to flatten an
  existing `mcp.servers` nesting into the flat map (the shape 1.x refuses), citing `anomalyco/opencode#49904` as the
  retire-when condition.
- `README.md` — the global-config snippet: the GitHub server goes under the flat `mcp` map, not `mcp.servers`.
- `AGENTS.md` — every site that names the command or the shape: the tooling-setup bullet
  (`opencode mcp add <name> -- <command…>` → the `--global` form plus the flat shape); the V1-shape paragraph of the
  config gotcha (extended to the global config, naming the 1.x/ACP consumer); the `mcp add` probe passage in "A CLI's
  `--help` and its docs are not its contract" (the command defaults to the project config; the `-- <command>` form _is_
  listed in 2.0.21's help); and the permission-proof `debug config` gotcha's normalized-document paragraph (its
  v2.0.15-pinned `disabled` and credential-rendering claims re-stated for 2.0.21).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. No capability spec describes the `setup-agent-tools` skill or the global config's shape.

## Impact

- **Docs / definitions only**: the skill, the README, and `AGENTS.md`. No build, code, workflow, or runtime change.
- **Effect**: a contributor following the setup gets a global config both OpenCode lines load, so IDEA's AI (which runs
  the 1.x agent) starts; the skill stops advertising a shape that breaks a first-class consumer.
