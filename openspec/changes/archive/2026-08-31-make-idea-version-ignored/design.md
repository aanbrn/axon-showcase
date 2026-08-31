# Design: make .idea/ version-ignored

## Context

The repo currently tracks five `.idea/` files (palantir/ktfmt enablement, the code-style scheme with the palantir
import layout, and the test-naming inspection profile) so IntelliJ matches `spotlessApply`. The IDE owns these files
and rewrites them as runtime state — it has already silently dropped `USE_SINGLE_CLASS_IMPORTS` from
`codeStyles/Project.xml`. The fix is to stop versioning IDE-owned files: the IDE should *read* config, not *own* it.
See `proposal.md` — Why.

## Goals / Non-Goals

- **Goal**: `.idea/` is fully git-ignored; no versioned file is ever rewritten by the IDE.
- **Goal**: a fresh clone gets a formatter-matched IDE after one `setup-idea.sh` run (plugins were already a
  prerequisite).
- **Goal**: the load-bearing settings (import layout, test-naming inspection) cannot be silently dropped by the IDE.
- **Goal**: preserve each developer's own inspection tweaks — the profile is patched, not regenerated.
- **Non-Goal**: changing the formatters, the spotless config, or the `spotlessCheck` build gate (still authoritative).
- **Non-Goal**: a setup script that runs automatically — running it remains an explicit developer step.

## Decisions

### `.idea/` is fully ignored; the five files are untracked

Remove the `!` exceptions in `.gitignore` so `.idea/*` matches everything, and `git rm -r --cached .idea/` to untrack
the five files (they stay on disk). The IDE then owns `.idea/` completely, and git never sees churn.

- *Alternatives considered*: (1) `git update-index --skip-worktree` on the churning files — rejected: it hides *real*
  changes too and must be repeated per developer; (2) keeping the files tracked and re-committing after IDE touches —
  rejected: perpetual noise and the IDE keeps clobbering the canonical config.

### The import layout moves to `.editorconfig`

The scheme's load-bearing content (`USE_SINGLE_CLASS_IMPORTS`, the two on-demand counts, the palantir import layout)
moves to a new `.editorconfig` using the `ij_java_*` keys. `.editorconfig` is the one channel IntelliJ deliberately
treats as read-only input, so the import layout can no longer be silently dropped. `codeStyles/Project.xml` and
`codeStyleConfig.xml` no longer carry anything we depend on and are left to the IDE.

- *Alternatives considered*: (1) patching `Project.xml` back in place — rejected: the IDE re-serializes the scheme and
  drops the setting again, a tug-of-war; (2) keeping the layout in a tracked template and regenerating `Project.xml` —
  rejected: reintroduces a second source for a setting `.editorconfig` expresses natively.
- **Validation spike (required before implementation)**: confirm the exact `ij_java_layout_imports` encoding that
  reproduces the palantir layout (static group, blank line, all non-static) and that IntelliJ honors the `ij_java_*`
  keys with no `Project.xml` scheme present. If the keys need a scheme to layer onto, a minimal committed
  `codeStyleConfig.xml`/`Project.xml` pair remains a fallback — record the spike result in `design.md`.
- **Spike result (2026-08-31)**: IntelliJ's `.editorconfig` uses `ij_java_imports_layout` (not `ij_java_layout_imports`)
  with `$` = static, `|` = blank line, `*` = any package. The palantir layout (static-all, blank, non-static-all)
  encodes as `$*,|,*`. Single-class imports are `ij_java_use_single_class_imports=true` with the two on-demand counts
  at `999`. These `ij_java_*` keys apply on top of the current code style and are honored even with the default scheme
  (`.editorconfig` overrides the scheme per the JetBrains docs), so no `Project.xml`/`codeStyleConfig.xml` pair is
  needed. The design uses `ij_java_imports_layout = $*,|,*`; `ij_java_layout_imports` is a deprecated/absent key.

### The plugin-toggle files are generated from committed templates

`palantir-java-format.xml` and `ktfmt.xml` are separate, plugin-owned files containing only the toggle/settings we
need (palantir `enabled=true`; ktfmt `enableKtfmt`, Custom style, 120 cols, unused-import removal). They have no user
state worth preserving, so `setup-idea.sh` writes them from committed templates (under `config/idea/`).

- *Alternatives considered*: patching them in place — rejected: there is nothing else in these files to preserve, so a
  template is simpler and equally safe.

### The inspection profile is patched in place, not regenerated

The test-naming regex (`NewClassNamingConvention` + `JUnitTestClassNamingConvention`) has no `.editorconfig` home and
is the only enforcement of the `Tests`/`CT`/`IT`/`E2E` suffixes. The profile is IDE-managed and accumulates the user's
own inspection settings, so `setup-idea.sh` performs an idempotent upsert: locate the `inspection_tool` block by its
`class` attribute, replace just that block (or insert it before `</profile>` if absent) with the canonical block, and
leave every other inspection untouched.

- *Alternatives considered*: (1) regenerating the whole profile from a template — rejected: clobbers the developer's
  own inspection settings; (2) IntelliJ's "Import Profile" — rejected: it *replaces* the active profile rather than
  merging.
- **Mechanism**: a small Python helper (stdlib only — no `xmlstarlet`/`lxml` on the machine) doing a targeted block
  replace keyed on the `class` attribute, preserving the rest of the file byte-for-byte. This makes **Python 3 a new
  developer prerequisite** for running `setup-idea.sh` (macOS ships `python3` via Command Line Tools; other platforms
  may need it installed).

### `setup-idea.sh` grows from "install plugins" to "install + ensure config"

The script keeps installing the two plugins, then ensures the config files exist with the right settings: writes the
two toggle templates and `codeStyleConfig.xml` if absent, and runs the inspection-profile upsert. Idempotent — running
it twice changes nothing.

- *Alternatives considered*: a separate `apply-idea-config.sh` — rejected: one setup entry point is simpler for a
  fresh clone; the script's single responsibility ("get this IDE to match the formatter") is unchanged, only its scope.

## Risks / Trade-offs

- [A fresh clone has no IDE config until `setup-idea.sh` runs] → Mitigation: plugins already required the script; the
  change extends the same documented step, and `AGENTS.md`/`README.md` state it explicitly.
- [IntelliJ honors `ij_java_*` only with a scheme present] → Mitigation: validation spike before implementation; if
  needed, keep a minimal committed scheme pair as a fallback (recorded in `design.md`).
- [The inspection upsert mangles the profile XML] → Mitigation: targeted block replace preserves everything outside
  the block; a committed canonical block string is the single source of truth; the script is idempotent.
- [A developer already has their own inspection profile] → Mitigation: the upsert only adds/updates the naming block;
  their other inspections are preserved.

## Migration Plan

1. Update `.gitignore` (drop the `!` exceptions) and `git rm -r --cached .idea/`; verify the files remain on disk.
2. Spike the `.editorconfig` import-layout encoding and scheme-independence; record the result in `design.md`.
3. Add `.editorconfig` with the `ij_java_*` keys; commit the plugin-toggle templates under `config/idea/`.
4. Extend `setup-idea.sh` (write templates if absent + inspection upsert via a stdlib Python helper); make it
   idempotent and verify on a clean clone.
5. Update `AGENTS.md` and `README.md`.

Rollback: restore the `.gitignore` exceptions and `git rm --cached` reverts (`git add .idea/...`) — the files are
still on disk and versioned in history.

## Open Questions

None — the `.editorconfig` spike is a required pre-implementation validation step recorded in the migration plan, not
a deferred decision.