# Proposal: Root Prettier for markdown

## Why

The repo formats Java (Spotless) and the web UI (Prettier) automatically, but markdown still relies on a **manual
120-char wrapping convention** — enforced by the maintainer (and the build agents) by hand across `docs/`, `AGENTS.md`,
`README.md`, and OpenSpec specs. This is error-prone and consumes effort (as seen across recent changes). This automates
markdown formatting the way the web UI already does, ending the manual convention.

## What Changes

- Format root markdown (`docs/`, `AGENTS.md`, `README.md`, `openspec/specs/`, and active `openspec/changes/*/`) with
  `proseWrap: "always"` and `printWidth: 120` (matching the repo's 120-char convention). Prettier's `printWidth` is a
  preference, not a hard limit — backtick-dense lines may still exceed 120, the accepted trade-off of automating
  markdown wrapping.
- Add a `markdown` format to the **existing root Spotless** config using its built-in `prettier()` step — Spotless
  provisions Prettier itself, so there's no new plugin, no root npm install, and no coupling to the web-UI's
  `node_modules`. The Prettier options are declared **inline** via `prettier().config(...)` (the single source of truth;
  no root `.prettierrc`, since Spotless does not reliably auto-read one). The markdown gate runs via the existing
  `spotlessCheck` in `check`, like Java and the web-UI.
- **Scope**: live markdown only — `docs/**/*.md`, `AGENTS.md`, `README.md`, `openspec/specs/**/*.md`, and active
  `openspec/changes/*/` (~30 files). The `openspec/changes/archive/` historical record is **excluded** (no reflow of
  archived changes).
- **YAML**: markdown only for now — Helm values, workflows, and docker-compose keep their own conventions (a separate
  decision).
- **One-time reflow**: the first `format` run reflows the ~30 in-scope files (may adjust long inline code, tables, and
  `→`/`—` sequences); afterwards the gate keeps them stable.

## Capabilities

### New Capabilities

- None (this is tooling, not a runtime/deployment behavior).

### Modified Capabilities

- `showcase/quality/code-quality` — the formatting gate grows from Java/web-UI to include root markdown.

## Impact

- **Build**: extend the existing root **Spotless** config with a `markdown` format using its built-in `prettier()` step
  (Spotless provisions Prettier itself; no new plugin, no root npm install, no web-UI coupling); `check` gains the
  markdown gate via the existing `spotlessCheck`.
- **Code**: reflow of ~30 in-scope markdown files on the first `format` run.
- **Docs**: `AGENTS.md` / `README.md` — update the "manual 120-char wrapping" convention to reference the automated
  Prettier gate; the OpenSpec specs are reflowed to the Prettier output.
- **Behavior**: no runtime change; CI gates markdown formatting.
