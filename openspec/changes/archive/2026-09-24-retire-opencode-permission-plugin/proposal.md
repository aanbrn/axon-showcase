# Proposal

## Why

OpenCode v2 (now installed as v2.0.15, and the `opencode.yml` / `audit.yml` workflows pin
`anomalyco/opencode/github@latest`, so cloud runs are v2 too) replaced the v1 plugin contract: a plugin must export
`{ id, setup }`, and the **typed** `PluginContext` exposes no permission or config hook. So
`.opencode/plugin/grant-cli-config-dirs.ts` fails to load —
`Plugin must export a default definition with an id and an effect or setup function` — and the `external_directory`
grants it injected for unattended runs are gone: a cloud run reading the `gh` config dir or the globally-installed
`openspec` package now hits an unanswerable `external_directory` prompt and hangs. v2's declarative `permissions` array
is the replacement surface.

## What Changes

- **Retire `.opencode/plugin/grant-cli-config-dirs.ts`** — its v1 shape cannot load under v2, and no typed v2 plugin
  hook contributes permissions, so the file has no supported v2 equivalent.
- **Move the two grants it still needs into `.opencode/opencode.json`**, as v2 `permissions` rules on the
  `external_directory` action, expressed with v2's path syntax (`~`/`$HOME` expand; `*` matches across `/`):
  - the `gh` config directory as `$HOME/.config/gh/*`;
  - the `openspec` package root as a `*/@fission-ai/openspec/*` wildcard, since its path is machine-specific.
- **Drop the scratch-directory grant** — v2 already allows its managed temporary directory, and `opencode debug paths`
  reports it as `<tmpdir>/opencode`, i.e. the same directory the plugin granted.
- **Migrate the same file's `permission.bash` rule to the v2 `permissions` array** (`shell`, same effects), so the file
  is not left mixing the v1 and v2 shapes.
- **Update `AGENTS.md`** — the scratch-files bullet and the plugin references describe the retired plugin and must
  describe the config that replaces it; the bullet also names the scratch dir for a subagent's own probe scratch
  (`$TMPDIR/opencode`, not `/tmp`), since a `review-quick` run's temp probes tripped the `external_directory` guardrail.
- **Fold the capture's rule in** — the `lesson-capture` run's log self-match rule (the log's `spawning process` lines
  match a token search) merges into the `debug config` gotcha, tagged with this change's marker; its other proposed rule
  (a grant's canonical path spelling) was dropped as unnecessary — a read-tool A/B resolves the scratch dir under either
  spelling.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- none — no capability spec covers the OpenCode permission plugin or config (verified by grepping `openspec/specs/` for
  `external_directory`, `grant-cli-config-dirs`, and `opencode/plugin`), so the change declares `skip_specs`.

## Impact

- `.opencode/plugin/grant-cli-config-dirs.ts` — deleted.
- `.opencode/opencode.json` — the two grants plus the `permission.bash` → `permissions` migration.
- `.opencode/tsconfig.json` — its `include` names only the plugin's sources and is orphaned by the deletion.
- `.opencode/package.json` — the orphaned `@types/node` devDependency is dropped (its only consumer was the deleted
  tsconfig's `types: ["node"]`); `@opencode-ai/plugin` stays.
- `AGENTS.md` — the scratch-files convention and the plugin references.
- No code, build, runtime, or deployment change; the affected surface is the agent's own tooling.
