# Design

## Context

See `proposal.md` — Why. The approach was interrogated against the installed v2 tooling before it was drafted.

`opencode debug config` (v2.0.15) shows v2 **auto-migrates** the repo's v1 `permission.bash` object into a v2
`permissions` array (`{"action":"shell","resource":"*git push*","effect":"ask"}`) — the binary's embedded config
normalizer maps the v1 `bash` action onto v2's `shell` — so the existing push-guard still works and the only broken
surface is the plugin, which fails to load with
`Plugin must export a default definition with an id and an effect or setup function`.

The v2 plugin typings (`.opencode/node_modules/@opencode-ai/plugin/dist/v2/{promise,effect}/context.d.ts`) expose
`options`, `agent`, `aisdk`, `catalog`, `command`, `integration`, `plugin`, `reference`, and `skill` — **no permission
or config hook** (the `filesystem`, `path`, `event`, `npm`, and `location` modules exist but are not `PluginContext`
members). So no _typed_ v2 plugin hook can inject `external_directory` grants; the declarative `permissions` array is
the replacement, and the v2 permissions docs give its syntax: rules are `{action, resource, effect}`,
`external_directory` is the action, and `~`/`~/`/`$HOME`/`$HOME/` expand (nothing else does).

`opencode debug paths` reports `tmp <tmpdir>/opencode` — v2's managed temporary directory, which the permissions docs
say v2 allows for `external_directory` already — so the plugin's scratch grant is redundant.

## Goals / Non-Goals

**Goals:**

- Keep the grants the plugin still needs — the `gh` config dir and the `openspec` package root — working for unattended
  runs under v2.
- Leave no v1 plugin that fails to load, and no v1/v2 shape mixing in the config file.
- Keep each grant scoped to its target directory (the wildcard scoped to the package tree rather than a root).

**Non-Goals:**

- Migrating the other v1 keys in `.opencode/opencode.json` (`model`, `small_model`, `mcp`) — v2 reads them today (the
  binary maps `small_model` onto `agents.title.model`), so a migration is its own concern.
- Changing the scratch-files convention's path.
- Any code, build, or deployment change.

## Decisions

**Retire the plugin; move the grants to `permissions`.** No _typed_ v2 plugin hook adds permissions (verified against
the v2 typings), so a supported v2 rewrite cannot grant anything; the config is the surface. The binary's runtime may
carry an untyped hook, but an undocumented shape is not a foundation. _Alternative rejected:_ a v2-stub plugin for
discovery — it could not grant.

**Put the grants in `.opencode/opencode.json`, not the global config.** Both the local project and the cloud action load
the project config, so one checked-in file covers the unattended path that motivates the grants. _Alternative rejected:_
the user's global config — per-machine and not in the repo, so the cloud run would still hang.

**Drop the scratch grant as redundant.** v2 auto-allows its managed temporary directory, and `opencode debug paths`
shows it _is_ `<tmpdir>/opencode`. Adding a `*/opencode/*` rule would both duplicate that and widen the grant past the
plugin's exact path, against the repo's own rule that a temp grant is scoped to a named scratch subdirectory, never the
whole OS temp root. _Alternative rejected:_ keep a matching rule for safety — it would only broaden access.

**Migrate the file's `permission.bash` to `permissions` in the same edit.** v2 auto-migrates it, but leaving the v1
object beside a new v2 array risks two shapes merging unpredictably; the migration preserves the same rule (`*` allow,
`*git push*` ask) with no behavior change. _Alternative rejected:_ add `permissions` and leave `permission`.

**Express the paths with what v2 expands.** `$HOME` covers the `gh` config dir (the `$GH_CONFIG_DIR`/`$XDG_CONFIG_HOME`
overrides are not expandable and are recorded as a limitation). The `openspec` package root is machine-specific, so it
is matched by the wildcard `*/@fission-ai/openspec/*`.

**Delete `.opencode/tsconfig.json` with the plugin.** It names only `plugin/**/*.ts`; once the plugin is gone the file
configures nothing — and `.opencode/package.json`'s `@types/node` devDependency follows it, since that tsconfig's
`types: ["node"]` was its only consumer (the `@opencode-ai/plugin` dependency stays, as the v2 binary installs it at
startup).

## Risks / Trade-offs

- **The wildcard's efficacy is unverified until reproduced** (the `*` matching semantics for an `external_directory`
  resource are documented, not yet exercised) → a verification task reproduces the grant and records the result; a
  pattern that does not match is corrected before the change is done.
- **The `gh` env overrides are not covered** (`$GH_CONFIG_DIR`, `$XDG_CONFIG_HOME`) → recorded; an unattended run that
  sets them would still prompt, and the `$HOME` default is the common case.
- **A wildcard is broader than a resolved path** (`*/@fission-ai/openspec/*`) → still scoped to the package tree, and no
  `deny` is weakened.
- **An untyped v2 hook might exist** → the change relies only on the documented typed API and declarative config; if a
  supported permission hook ships, a plugin can replace the config later.

## Migration Plan

Delete the plugin and `.opencode/tsconfig.json`, and edit the config in one change; the config takes effect on the next
OpenCode reload (as does the plugin's removal). Verified by `opencode debug config` (the project document carries the
rules) and by a real external read under each granted directory resolving without a prompt — v2 no longer emits the
`evaluated permission` line that would name the matched rule, so `debug config` is the static proof. Rollback is
reverting the commit.
