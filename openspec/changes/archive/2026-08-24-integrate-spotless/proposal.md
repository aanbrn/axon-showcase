# Proposal: Integrate Spotless for build-enforced formatting

## Why

Formatting is the last quality gate that depends on the author's IntelliJ IDE. This is step 2 of the roadmap toward a
fully IDE-independent development environment: **Checkstyle → Spotless and other quality gates → complete
IDE-independence**. Integrating Spotless with the palantir-java-format engine makes formatting a build-gated,
IDE-independent step: `spotlessCheck` verifies in `./gradlew check` with no IDE involved, and `spotlessApply` fixes.
It also adds per-file license headers matching the repo's existing MIT LICENSE, and retires the checkstyle
`CustomImportOrder` rule — the formatter becomes the single owner of import order, as anticipated when that rule was
introduced.

## What Changes

- Add the Spotless Gradle plugin to build-logic and apply it in `code-check-conventions.gradle.kts` so every module
  gets `spotlessCheck` (wired into `check`) and `spotlessApply`.
- Configure Spotless `java` target: `palantirJavaFormat()` (fixed 120 columns, matching the project's `LineLength`
  convention) and `licenseHeader("// SPDX-License-Identifier: MIT")` (one-line SPDX header matching the existing MIT
  LICENSE; no copyright line or year in the header — the copyright stays in the LICENSE file).
- Remove the `CustomImportOrder` module from `config/checkstyle/checkstyle.xml`; checkstyle keeps `UnusedImports` and
  the naming rules, and line-length enforcement moves to the formatter (palantir wraps to 120) — the 120 convention
  and import order are both owned by the formatter.
- One-time `spotlessApply` reformat of all Java sources (palantir style + headers). No ratchet: after the full
  adoption reformat, `spotlessCheck` verifies the whole tree on every build.
- Docs refresh: `AGENTS.md` formatting convention moves from the IntelliJ formatter to `./gradlew spotlessApply`;
  `README.md` check pipeline note gains spotless.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality` — ADDED requirements for build-enforced formatting and license headers.

## Impact

- **Code**: `build-logic/build.gradle.kts` (Spotless plugin dependency),
  `build-logic/src/main/kotlin/code-check-conventions.gradle.kts`
  (apply + configure), `gradle/libs.versions.toml` (plugin version), `config/checkstyle/checkstyle.xml` (remove
  `CustomImportOrder`), all Java sources (one-time reformat + header).
- **Docs**: `AGENTS.md`, `README.md` updated as part of this change.
- **Build**: `check` now includes `spotlessCheck` across all modules; formatting violations fail the build.
- **Tests**: no test changes; existing suites must pass with the formatted sources.