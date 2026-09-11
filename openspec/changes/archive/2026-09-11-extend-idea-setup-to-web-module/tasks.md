## 1. Prettier for the web module

- [x] 1.1 Derive the project Prettier template (`config/idea/prettier.xml`) by configuring Prettier in IDEA and reading
      the `.idea/prettier.xml` it writes (built-in integration, using `showcase-web-ui`'s `prettier`); strip any
      machine-specific absolute paths (Prettier package / Node interpreter) so the committed template stays portable
- [x] 1.2 Merge it in `ensure-idea-settings.py` so `.idea/prettier.xml` enables Prettier on reformat / format-on-save
- [x] 1.3 Extend IDEA's Prettier scope to the types the module formats: the bundled default
      (`**/*.{js,ts,jsx,tsx,cjs,cts,mjs,mts,json,vue,astro}`) omits `css`/`html`, so `myFilesPattern` is set to the
      default plus `css,html` (observed live: without it a CSS reformat fell back to IDEA's built-in formatter and broke
      the gate)

## 2. JS/TS indentation

- [x] 2.1 Source the JS/TS indentation from `.editorconfig` (`indent_size = 2`, `indent_style = space`) — no separate
      IDEA code-style scheme (Prettier is the formatter, and the indent comes from a read-only file)
- [x] 2.2 Audit the "wildcard imports" concern for TS: TS imports are explicit (there is no Java-style on-demand
      wildcard threshold), so no setting is needed — recorded in the audit
- [x] 2.3 Audit IDEA's Optimize Imports for the web module: it removes unused/duplicate imports and preserves
      side-effect imports, and its reordering is gate-neutral (no import-order rule; Prettier does not reorder imports)
      — documented, no configuration added

## 3. Extend the setup

- [x] 3.1 Add the new `config/idea/*.xml` templates/components to `ensure-idea-settings.py` (merge, preserving other
      content)
- [x] 3.2 Adjust `scripts/setup-idea.sh` if the flow or plugin behavior changes (expected: no plugin install needed)

## 4. Audit the remaining parked-idea items

- [x] 4.1 Record the findings and any follow-ups for the `@/` alias (IDEA reads `tsconfig.json` paths), the Node
      interpreter Prettier needs, the Vitest test-file naming (inspection if one exists, otherwise note the omission),
      and the `installPlugins` flow

## 5. Docs and spec

- [x] 5.1 README "Formatting and IDE Setup": cover the web module (Prettier + JS/TS style)
- [x] 5.2 AGENTS.md: extend the IntelliJ setup paragraph
- [x] 5.3 Add the `ide-config` delta (new requirement + scenarios)
- [x] 5.4 Remove the implemented 2026-09-03 `docs/ideas.md` entry in this change's PR (bundled — commit it on the change
      branch rather than leaving it in the uncommitted review diff, so a rebase/stash cannot conflict with a `main` that
      also edited the file)
- [x] 5.5 Refresh the `ide-config` main spec's `## Purpose` at archive to mention the web module — a delta cannot carry
      a `## Purpose`, so this is applied in the archive commit
- [x] 5.6 Update the `/setup-idea` skill and command to mention the web module's Prettier (they still described the
      setup as JVM-only)

## 6. Verify

- [x] 6.1 Confirm the Prettier configuration is applied and IDEA-accepted (IDEA itself wrote `.idea/prettier.xml` when
      Prettier was enabled, and the setup merges that exact template), with the JVM side unaffected
- [x] 6.2 Sandbox-test the merge (restores drift, preserves other content, idempotent)
- [x] 6.3 `./gradlew spotlessCheck` and `openspec validate --all` pass
- [x] 6.4 Live check: `Reformat Code` on a deliberately non-Prettier `styles.css` restored Prettier's canonical form
      (leaving it `prettier --check`-clean), confirming the extended scope routes CSS through Prettier; TS/TSX already
      matched
