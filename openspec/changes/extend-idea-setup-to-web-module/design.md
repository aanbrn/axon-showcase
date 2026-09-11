## Context

See proposal.md — Why. Today the setup covers the JVM formatter only: the palantir + ktfmt plugins and the Java import
layout from `.editorconfig`. The web module has no IDE configuration at all — `.idea/` has no `prettier.xml` and no
`codeStyles/Project.xml` (only the scheme pointer), and `.editorconfig` carries just `ij_java_*` entries — while the web
gate is `prettier --check` (`showcase-web-ui/.prettierrc`: `printWidth: 120`, `singleQuote`, `semi`,
`trailingComma: all`; 2-space is Prettier's default — the config does not set `tabWidth`), with ESLint 10 and Vitest
(`*.test.ts(x)`, 18 files). `showcase-web-ui/tsconfig.json` maps `@/*` → `src/*`. The preceding change generalized the
setup to **merge** our `config/idea/*.xml` components into IntelliJ's files (`scripts/ensure-idea-settings.py`); this
change extends that mechanism. IntelliJ IDEA supports JavaScript/TypeScript and includes the built-in Prettier
integration — free from 2026.1 (an Ultimate subscription on the earlier unified 2025.3–2026.0) — so no plugin
installation is needed. That integration, however, applies by default only to a fixed file-type scope (`filesPattern` =
`**/*.{js,ts,jsx,tsx,cjs,cts,mjs,mts,json,vue,astro}`) that omits CSS and HTML, so reformatting those files falls back
to IDEA's built-in formatter.

## Goals / Non-Goals

**Goals:**

- An IDE that formats the web module to match `prettier --check`, so editing the UI in IDEA does not fight the gate.
- A JS/TS indentation that agrees with Prettier for the paths Prettier does not cover, sourced from `.editorconfig`.
- The web module included in the same one-command reconcile (`./scripts/setup-idea.sh`) as the JVM side.

**Non-Goals:**

- Replacing Prettier or ESLint — they remain the gates; the IDE merely matches them.
- Configuring per-user/workspace (`.idea/workspace.xml`) settings.
- Making the IDE the enforcer of the gates.

## Decisions

**D1: "Formatter-matched" for the web module means Prettier, via IDEA's built-in integration (no `installPlugins`).**
Prettier is already the module's gate and a project dependency, and IDEA bundles the JS/TS support, so the setup ships
the project's Prettier configuration rather than installing a formatter plugin. _Alternative considered:_ a Prettier IDE
plugin (rejected — IDEA's Prettier integration is built in; none is needed).

**D2: Source the JS/TS indentation from `.editorconfig`, and do not author an IDEA code-style scheme.** Prettier is the
formatter for these files, so the setup needs only IDEA's Prettier configuration plus the `2`-space indentation — and
`.editorconfig` is a read-only file IDEA honors but does not rewrite, mirroring how the Java import layout is sourced.
_Alternative considered:_ authoring `.idea/codeStyles/Project.xml` (rejected — a large, IDEA-managed file to merge into,
for a setting Prettier and `.editorconfig` already cover).

**D3: Reuse the existing merge mechanism.** The new artifacts are `config/idea/*.xml` templates merged into `.idea/` by
`scripts/ensure-idea-settings.py` (new components), so the web module gets the same reconcile-on-re-run behavior as the
JVM side — no separate, web-only script.

**D4: Resolve the parked-idea's audit items by evidence, not assumption.** Findings: the `@/` alias needs no
configuration (IDEA reads `tsconfig.json` `paths`); the Java test-tier naming inspection is class-based and does not
transfer to the web module's test _files_ (`*.test.ts(x)`), so no naming inspection is added there; the TS
wildcard-import concern does not apply (TS imports are explicit — there is no Java-style on-demand threshold); IDEA's
Prettier integration resolved and ran on the module's files in the live reformat (whether it needs an explicit Node
interpreter path when Node is the Gradle-managed one is left open — see Open Questions); IDEA's **Optimize Imports** is
gate-neutral — it removes unused and duplicate imports and preserves side-effect imports (verified live:
`import './app/styles.css'` survived), and although it reorders imports (e.g. hoisting `import type` above value
imports), no gate enforces import order (Prettier does not reorder imports and `eslint.config.js` has no import-order
rule), so it costs cosmetic churn at most; and no `installPlugins` change is needed.

**D5: Extend IDEA's Prettier file scope (`myFilesPattern`) to the module's CSS and HTML.** IDEA's bundled Prettier
`filesPattern` default (`**/*.{js,ts,jsx,tsx,cjs,cts,mjs,mts,json,vue,astro}`) omits CSS and HTML, so Reformat Code runs
IDEA's built-in formatter on them and diverges from `prettier --check` (observed live: reformatting
`showcase-web-ui/src/app/styles.css` broke the gate). The setup therefore sets `myFilesPattern` to the default plus
`css,html`, so every file type the module's gate covers is formatted by Prettier. _Alternative considered:_ leave the
default and format CSS/HTML only via the gate/CLI (rejected — the IDE would keep fighting the gate for those files, the
exact drift this change prevents).

## Open Questions

- Whether IDEA actually applies the JS/TS indentation from `.editorconfig` on the installed build — verify after a
  reload; Prettier remains the formatter regardless.
- Whether IDEA's Prettier integration needs an explicit Node interpreter path when Node is the Gradle-managed one.

## Risks / Trade-offs

- [`.idea` XML keys differ across IDEA versions] → derive each template from an IDE-written file (the
  `PrettierConfiguration` component was written by IDEA) and verify by reformatting a file and matching
  `prettier --check`.
- [Prettier integration depends on a Node interpreter] → verify; document the setting if IDEA cannot auto-detect the
  repo's Node (the web module already requires Node to build).
- [IDEA's `.editorconfig` handling may not apply every JS/TS property] → only the standard indentation is set, and
  Prettier remains the formatter; verify after a reload.
- [The web-module formatting depends on IntelliJ's Prettier integration, free only from IDEA 2026.1 (Ultimate
  subscription before that)] → documented in `README.md`/`AGENTS.md`; where it is unavailable the config is inert and
  Prettier remains the gate.
- [Overriding `filesPattern` replaces IDEA's default scope rather than extending it] → the committed `myFilesPattern` is
  the default list plus `css,html`, so no file type the default covered is lost.
