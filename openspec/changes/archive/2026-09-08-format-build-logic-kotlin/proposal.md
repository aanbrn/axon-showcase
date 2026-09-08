# Proposal: Format build-logic Kotlin with Spotless ktfmt

## Why

The repo formats Java (Spotless palantir), Gradle Kotlin DSL (Spotless ktfmt via `kotlinGradle`), and markdown (Spotless
Prettier) automatically, but the **build-logic Kotlin task classes are not format-gated**. The root `spotless` block's
`kotlinGradle` target covers only `*.gradle.kts` (`build-logic/src/**/*.gradle.kts`), so the compiled `.kt` classes —
`PackBuildImageTask`, `HelmUpdatesTask`, `VerifyInfraImageVersionsTask`, `AbstractHelmRepositoriesTask` — rely on manual
style (imports, no wildcards, 120-column wrapping). This is the last source tree without an automated formatting gate.

## What Changes

- Add a `kotlin` format to the **existing root Spotless** config covering `build-logic/src/**/*.kt`, using **ktfmt**
  matching the `kotlinGradle` config exactly: `kotlinlangStyle()` + `setMaxWidth(120)`.
- The four build-logic task classes are then gated by the existing `spotlessCheck` in `check`, like the rest of the
  repo.
- **One-time reflow**: the first `format` run may adjust the existing task classes to ktfmt output (imports, wrapping);
  afterwards the gate keeps them stable.
- **Scope**: build-logic Kotlin only — module `.kt` sources (e.g. `showcase-*` services) remain Java (palantir) or
  unformatted as today; this does not introduce a repo-wide Kotlin style beyond build-logic.

## Capabilities

### New Capabilities

- None (this is tooling, not a runtime/deployment behavior).

### Modified Capabilities

- `showcase/quality/code-quality` — the formatting gate grows from Java/markdown/Gradle-DSL to include build-logic
  Kotlin task classes.

## Impact

- **Build**: extend the existing root **Spotless** config with a `kotlin` format (`ktfmt().kotlinlangStyle()` with
  `setMaxWidth(120)`) targeting `build-logic/src/**/*.kt`; `check` gates it via the existing `spotlessCheck`.
- **Code**: one-time reflow of the four build-logic task classes on the first `format` run (imports/wrapping only, no
  behavioral change).
- **Docs**: `AGENTS.md` / `README.md` — minor refresh of the formatting convention (build-logic Kotlin now gated).
- **Behavior**: no runtime change; CI gates build-logic Kotlin formatting.
