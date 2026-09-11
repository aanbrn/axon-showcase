# Proposal: Rework the IntelliJ setup to reconcile, not just install

## Why

`./scripts/setup-idea.sh` configures IntelliJ to match the build formatter, but it has two problems:

- **It does not repair drift.** The template step is _create-if-absent_ (`ensure_config_file` copies only when the
  target is missing), so re-running the script cannot bring an already-present but stale `.idea/ktfmt.xml`,
  `.idea/codeStyles/codeStyleConfig.xml`, etc. back to the committed settings — only the inspection-profile upsert
  reconciles. The drift is real and observable: on this repo's own `.idea/`, palantir was `enabled=false` and ktfmt was
  missing its Custom-style options, because the old script never applied them cleanly (it aborts before the config step
  when IntelliJ is running).
- **A running IDE blocks the whole setup.** The script aborts before applying any configuration when the IDE is running,
  even though only `installPlugins` needs the IDE closed — copying/upserting configuration does not.

## What Changes

- Replace the copy-the-template step with a **merge/upsert** that writes only our elements (`<component>`/`<option>` and
  the inspection block) into IntelliJ's files, preserving everything else — generalizing the existing
  `ensure-idea-inspection.py` pattern. `config/idea/*.xml` stays the committed source of our settings.
- **Split the flow**: apply the configuration (merge) regardless of IDE state; gate only `installPlugins` on the IDE
  being closed (install when closed, otherwise warn and skip, leaving the configuration applied).
- Keep the setup script as the **primary, token-free path** — a contributor runs `./scripts/setup-idea.sh` directly when
  the configuration drifts (a mechanical, frequent operation not worth spending agent tokens on) — and add an **agent
  `/setup-idea` command** as an optional convenience (mirroring `/setup-agent-tools`) for those who prefer to ask.
- Update `README.md` and `AGENTS.md`.

## Capabilities

### Modified Capabilities

- `showcase/quality/ide-config` — the "setup script configures a formatter-matched IDE" requirement gains reconcile
  semantics (merge, preserving IDE-managed content) and the IDE-state behavior (configuration applies regardless; only
  the plugin install needs the closed IDE).

## Impact

- **Scripts**: `scripts/setup-idea.sh`; `scripts/ensure-idea-inspection.py` generalized and renamed into a
  settings-merge script (e.g. `scripts/ensure-idea-settings.py`).
- **Agent tooling**: a new `.opencode/skills/setup-idea/` + `.opencode/commands/setup-idea.md`.
- **Docs**: `README.md`, `AGENTS.md`.
- **Spec**: `showcase/quality/ide-config` delta.
- **Hygiene**: `.gitignore` (ignore `__pycache__/` created by the new Python settings-merge script).
- **Out of scope**: extending the setup to the web module (Prettier / TS code style / Vitest naming) — that remains
  parked in `docs/ideas.md` and is a follow-up change.
- **Parked idea**: this change implements only part of the 2026-09-03 `docs/ideas.md` entry (the durability and
  IDE-running ordering) — the web-module extension stays open, so the idea is revised rather than removed, as its own
  docs PR.
