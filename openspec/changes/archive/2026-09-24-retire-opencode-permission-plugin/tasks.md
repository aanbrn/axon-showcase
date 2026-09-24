# Tasks

## 1. Replace the plugin with v2 permissions

- [x] 1.1 Delete `.opencode/plugin/grant-cli-config-dirs.ts` (the v1 plugin shape that v2 rejects).
- [x] 1.2 In `.opencode/opencode.json`, replace the v1 `permission.bash` object with a v2 `permissions` array carrying
      the same rule (`{"action":"shell","resource":"*","effect":"allow"}` then
      `{"action":"shell","resource":"*git push*","effect":"ask"}`), and add the two `external_directory` grants:
      `$HOME/.config/gh/*` and `*/@fission-ai/openspec/*`. Do not add a scratch-directory rule — v2's managed temporary
      directory is already allowed and is `<tmpdir>/opencode`.
- [x] 1.3 Confirm the file is valid JSON, carries no v1 `permission` object, and that no v1 key it replaces is dropped.
- [x] 1.4 Delete `.opencode/tsconfig.json` (its `include` names only the deleted plugin's sources, and nothing else
      configures TypeScript there), and confirm nothing else imports from the plugin directory.

## 2. Docs the change owns

- [x] 2.1 Updated the `AGENTS.md` scratch-files bullet (its "a plugin — not a path pattern — grants that directory"
      claim, the plugin mechanics, and the `{env:TMPDIR}` trailing-separator discussion) to say v2 allows its managed
      temporary directory, which is the scratch path, with no grant.
- [x] 2.2 Swept `AGENTS.md` for every reference this change invalidated and reconciled each: the `gh`-config-dir
      paragraph, the `permission.bash` key name (now the `shell` action, with the v1 name kept as a parenthetical), the
      "a plugin-supplied rule exists in no config file" / `plugin_origins` examples (the grants are now
      config-supplied), the `anomalyco/opencode#48100` close-out (v2 pre-approves the machine-specific
      `<tmpdir>/opencode`; the issue's portable-default ask remains open), and the `evaluated permission` log-line
      gotcha (v2 emits no such line after the 2026-09-23 migration); the `{env:TMPDIR}` cross-reference was reworded
      since the caveat is gone.
- [x] 2.3 Named the scratch dir for a subagent's own probe scratch in the same bullet (`$TMPDIR/opencode` rather than
      `/tmp`), so the v2 `external_directory` guardrail is not tripped by a review subagent's temp probes — the
      `/private/tmp/oc-*` directories a `review-quick` run created (visible in the spawn log), which prompted the owner.
- [x] 2.4 (Folded capture) Folded the `lesson-capture` run's log self-match rule — the log's
      `message="spawning process"` lines match a token search, so exclude them before reading a count — into the
      `opencode debug config` gotcha, which carries `captured: retire-opencode-permission-plugin`. Its other proposed
      rule (a grant's path must be the canonical spelling because v2 matches the `realpath`) was **dropped**: a
      read-tool A/B on both spellings of a file under `$TMPDIR/opencode` both resolved, so the scratch dir is allowed
      either way and the spelling rule is unnecessary — a `review-quick` probe reporting the non-canonical spelling
      rejected did not reproduce.

## 3. Verify the surrounding artifacts (no edit expected — confirm, do not assume)

- [x] 3.1 Confirmed no capability spec covers the plugin or config (grep of `openspec/specs/` for `external_directory`,
      `grant-cli-config-dirs`, `opencode/plugin` returned nothing), so `skip_specs` is correct.
- [x] 3.2 `.opencode/package.json` keeps `@opencode-ai/plugin` — the v2 binary installs it into each `.opencode/` at
      startup — while its `@types/node` devDependency is removed as orphaned (its only consumer, the deleted
      `.opencode/tsconfig.json`'s `types: ["node"]`, is gone); the file is documented in the scratch-files bullet.
- [x] 3.3 `docs/audits/2026-09-21.md`'s citation of `grant-cli-config-dirs.ts` is a historical record, left as recorded
      (noted in the change's report).

## 4. Verification

- [x] 4.1 The config is watched, so it reloaded without a restart: `opencode debug config` shows the project document
      carrying the migrated `shell` rules and the two `external_directory` grants, with no "failed to load plugin"
      warning for our plugin.
- [x] 4.2 Probed both grants: reading `/Users/afanasyev/.config/gh` (a directory listing) and
      `/opt/homebrew/Cellar/openspec/1.13.1/libexec/lib/node_modules/@fission-ai/openspec/package.json` both resolved
      with no prompt, and `opencode debug config` shows the matching rules; the `message=evaluated permission=…` line
      for these reads was not found in `opencode.log` (the running server's evaluations do not appear there), so the
      static half is the evidence and neither pattern needed correcting.
- [x] 4.3 Ran `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` after the final edit — BUILD SUCCESSFUL; the
      change is agent tooling, so no code tests are affected.
- [x] 4.4 Ran `review-quick` over the implementation diff to clean (six rounds; findings fixed each round), and the
      manual review pass is being requested before committing.
