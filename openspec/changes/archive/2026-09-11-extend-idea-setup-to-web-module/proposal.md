# Proposal: Extend the IntelliJ setup to the web module

## Why

The IntelliJ setup configures the IDE for the **Java/Gradle** side only — the palantir + ktfmt plugins and the Java
import layout. The web module (`showcase-web-ui`) is a first-class part of the build with its own formatter gate:
`prettier --check` (`printWidth: 120`, `singleQuote`, `semi`, `trailingComma: all`, 2-space indent). Nothing configures
the IDE for it, so IDEA reformats TS/TSX/CSS/JSON with its own defaults (4-space indent, double quotes) and
`prettier --check` then rejects the result — the same "IDE vs build gate" drift the script already prevents for
Spotless, hit the moment a contributor edits the UI in the IDE.

## What Changes

- Make IDEA format the web module with the project's **Prettier** (IDEA's built-in integration, using the repo's
  `prettier` package — no plugin install), so Reformat Code / format-on-save matches `prettier --check`.
- Extend IDEA's Prettier file scope (`myFilesPattern`) to the types the module's gate covers — the bundled default omits
  CSS and HTML — so reformatting those files uses Prettier rather than IDEA's built-in formatter.
- Extend `.editorconfig` with the JS/TS indentation IDEA reads from it (`2`, matching Prettier) — the same read-only
  mechanism the setup already uses for the Java import layout — rather than authoring an IDEA code-style scheme.
- Audit and decide the remaining parked-idea items: the `@/` alias (IDEA reads `tsconfig.json` paths), the Node
  interpreter Prettier needs, the Vitest test-file naming, the TS wildcard-import concern, and whether the
  `installPlugins` flow still holds.
- Extend the merge script to cover the new artifacts, and update `README.md` / `AGENTS.md`.

## Capabilities

### Modified Capabilities

- `showcase/quality/ide-config` — add a requirement that the setup configures a formatter-matched IDE for the web module
  (Prettier), mirroring the existing JVM formatter requirement.

## Impact

- **Scripts**: `scripts/ensure-idea-settings.py` (new components/templates); `scripts/setup-idea.sh` if the flow
  changes.
- **Config**: `config/idea/` (new templates), `.editorconfig`.
- **Docs**: `README.md`, `AGENTS.md`.
- **Spec**: `showcase/quality/ide-config` delta.
- **Out of scope**: replacing Prettier/ESLint as the gates (they remain the source of truth), and any per-user
  `.idea/workspace.xml` settings.
