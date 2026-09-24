# Proposal

## Why

`.opencode/opencode.json` still carries two **v1 legacy shapes** that OpenCode v2 reads only through its v1→v2
compatibility normalizer: `small_model` (whose v2-native slot is the `title` agent's model) and a flat `mcp.playwright`
entry (v2-native is `mcp.servers.playwright`). v2 documents V1 config as continuing to work and normalizes both
silently, so this is future-proofing rather than a fix — but the normalizer **drops** a value it cannot map
(`omitted unsupported legacy …`, only a log line), and the shim can be retired. `retire-opencode-permission-plugin`
retired the plugin that could no longer load, and migrated `permission.bash` to `permissions` to avoid mixing the v1 and
v2 shapes; this completes the file's remaining legacy keys.

## What Changes

- **Rewrite `small_model` to its native slot** — `agents.title.model`, the shape `opencode debug config` already
  normalizes it to.
- **Rewrite the flat MCP entry** to `mcp.servers.playwright` (the v2 docs' shape), dropping the v1 `enabled` flag (the
  server stays enabled by default).
- **Reconcile the docs** — `AGENTS.md`'s model-pin bullet names the config's `model` and `small_model` keys; the
  README's global-config MCP snippet shows the v1 flat shape; and the `setup-agent-tools` skill's fallback says to add
  an entry under the `mcp` object, with its Notes naming the v1 `enabled` key.
- **State the v2 requirement in the README** — the config now carries the v2-only `permissions` key, which v1 rejects
  outright (`V2 permissions are not supported by OpenCode V1`), and the README's OpenCode row still installs v1
  (`brew install opencode` → 1.18.30) — so the row requires v2 (or later) and its install points at the v2 package.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — no capability spec covers the config's key migration. The `code-quality` spec's requirement covers only that
  `.opencode/opencode.json` is formatting-gated, which this change does not alter (the file stays Prettier-formatted),
  so no delta is owed and the change declares `skip_specs`.

## Impact

- `.opencode/opencode.json` — the two legacy keys rewritten to their native shapes.
- `AGENTS.md` — the model-pin bullet's key enumeration.
- `README.md` — the global-config MCP snippet's shape and the OpenCode prerequisite row's v2 requirement.
- `.opencode/skills/setup-agent-tools/SKILL.md` — its MCP fallback shape and `enabled` wording.
- No code, build, runtime, or deployment change.
