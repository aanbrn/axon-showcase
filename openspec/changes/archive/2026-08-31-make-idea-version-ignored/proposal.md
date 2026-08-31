# Make .idea/ version-ignored

## Why

The repo tracks five `.idea/` files to make IntelliJ's formatting match `spotlessApply` (palantir/ktfmt enablement,
import layout, test-naming inspection). But the IDE *owns* those files and rewrites them as runtime state: it silently
dropped `USE_SINGLE_CLASS_IMPORTS` from `codeStyles/Project.xml`, so the versioned config can drift from what the IDE
actually uses, and every IDE touch produces diff noise. Versioning IDE-managed files is the wrong model — the IDE
should read the config, not own it.

## What Changes

- **Ignore `.idea/` entirely**: remove the `!` exceptions from `.gitignore` (`.idea/*` with no unignores) and
  `git rm -r --cached .idea/` — the five tracked files leave git but stay on disk.
- **Move the import layout to `.editorconfig`**: the scheme's `ij_java_layout_imports`,
  `ij_java_use_single_class_imports`, and the two on-demand-count settings move to a new `.editorconfig`, which the IDE
  reads but never rewrites — so the load-bearing import layout can no longer be silently dropped.
- **Generate the plugin-toggle files from templates**: `palantir-java-format.xml` and `ktfmt.xml` are separate
  plugin-owned files with nothing else in them, so `setup-idea.sh` (or a sibling script) writes them from committed
  templates rather than the IDE accumulating changes in them.
- **Patch the inspection profile in place**: the test-naming regex has no `.editorconfig` home and the profile is the
  only enforcement of the `Tests`/`CT`/`IT`/`E2E` suffixes, so `setup-idea.sh` performs an idempotent "ensure this
  inspection exists" upsert on `inspectionProfiles/Project_Default.xml`, adding/updating only the
  `NewClassNamingConvention` + `JUnitTestClassNamingConvention` block and leaving the rest of the user's profile
  untouched.
- **Update `setup-idea.sh`**: from "install plugins" to "install plugins + ensure the four config files exist with the
  right settings" (templates for the toggles, in-place upsert for the inspection profile), so a fresh clone gets a
  formatter-matched IDE after one run.
- **Update docs**: `AGENTS.md` and `README.md` reflect that `.idea/` is generated/ignored and the canonical config
  lives in `.editorconfig` + the setup script.

## Capabilities

### New Capabilities

- `showcase/quality/ide-config`: the repository keeps its IntelliJ configuration out of version control, sourcing the
  formatter/import/test-naming settings that the IDE needs from `.editorconfig` and a committed setup script, so the
  IDE never rewrites a versioned file and a fresh clone can be configured to match the build formatter.

### Modified Capabilities

(none)

## Impact

- **`.gitignore`**: `.idea/*` becomes fully ignored (the five `!` exceptions removed).
- **Git**: the five `.idea/` files are untracked (remain on disk); no new files land in `.idea/` going forward.
- **New `.editorconfig`**: carries the palantir import layout (`ij_java_layout_imports`, single-class imports, on-demand
  counts), read by IntelliJ but never rewritten.
- **`scripts/setup-idea.sh`**: grows to install plugins and ensure the four config files exist (templates for the two
  plugin toggles + `codeStyleConfig.xml` pointer, idempotent in-place patch for the inspection profile).
- **Prerequisites**: **Python 3** becomes a new developer requirement for `setup-idea.sh` (the inspection upsert uses a
  stdlib-only Python helper); documented in `AGENTS.md` prerequisites.
- **Templates**: committed templates for the plugin-toggle files (likely under `config/idea/`).
- **Behavior**: a fresh clone requires one `setup-idea.sh` run for IDE parity (plugins already required it); the
  formatter gate (`spotlessCheck`) remains the authoritative enforcement and still needs no IDE.