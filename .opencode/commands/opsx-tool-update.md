---
description: Regenerate OpenSpec commands and skills after a new openspec CLI release
---

Regenerate the OpenSpec instruction files when a new `openspec` CLI version has been installed. This keeps the
`.opencode/commands/opsx-*.md` commands and `.opencode/skills/openspec-*.md` skills in sync with the CLI's built-in
templates (the same files `openspec update` regenerated in commit `ad53bb6`).

Run `openspec --version` and record the installed version, then run `openspec update` from the repository root. Do not
pass `--force` unless the user explicitly asks — the plain command already detects when the instruction files are up
to date and skips the regeneration.

Then inspect `git status` under `.opencode/commands/` and `.opencode/skills/`:

- If nothing changed, report that the instruction files are already current for the installed CLI and stop.
- If files changed, list which commands/skills were added, removed, or modified. If the command or skill inventory
  changed (not just their bodies), check whether `AGENTS.md` and `README.md` still describe the available commands
  accurately (e.g. `opsx-*`/`openspec-*` references) and update them before committing.
- Commit the regenerated instruction files as a standalone change (e.g. "Regenerate openspec commands and skills with
  openspec update") — do not mix in unrelated changes. Do not push unless asked.