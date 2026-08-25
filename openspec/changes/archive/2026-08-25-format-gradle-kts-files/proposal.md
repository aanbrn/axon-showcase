# Proposal: Format Gradle Kotlin DSL files with ktfmt

## Why

Java sources are formatted by Spotless (palantir); the 34 `.gradle.kts` files (build-logic convention plugins, module
build files, root and build-logic settings) are only hand-wrapped at 120. The deferred "second bite" from the Spotless
adoption. A spike compared ktlint and ktfmt: ktlint mangles the Gradle DSL (explodes method chains and restructures
`register<JvmTestSuite> { }` blocks), while ktfmt's `kotlinlang` style matches the repo's existing IntelliJ formatting
on the conforming files and produces consistent, non-destructive output on the rest. ktfmt also has an official
IntelliJ plugin (marketplace id 14912, by the ktfmt maintainer), giving the same build/IDE parity as palantir for Java.

## What Changes

- Add a Spotless `kotlinGradle` step using `ktfmt()` (kotlinlang style, `max_line_length = 120`, unused-import
  removal) so `.gradle.kts` formatting is build-gated: `spotlessApply` formats, `spotlessCheck` verifies in `check`.
- Wire it to cover modules (via `code-check-conventions`), the root `build.gradle.kts`, and the `build-logic`
  convention files.
- One-time `spotlessApply` reformat of all `.gradle.kts` files (the spike showed `java-conventions`,
  `build.gradle.kts`, and `settings.gradle.kts` are already conforming; the complex DSL files get the ktfmt restyle).
- IDE parity: commit the ktfmt IntelliJ plugin's project-level config (`.idea/ktfmt.xml`) — its **Custom** style set
  to reproduce kotlinlang at 120, since the plugin's `Kotlinlang` mode hard-codes ktfmt's 100-column default — and
  extend `scripts/setup-idea.sh` to install the ktfmt plugin (marketplace id 14912).
- Docs: `AGENTS.md` Formatting convention extends to `.gradle.kts`; README as needed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality` — MODIFIED "Source formatting is enforced by the build": the build now formats
  both Java and Kotlin DSL (`.gradle.kts`) sources.

## Impact

- **Code**: `build-logic/src/main/kotlin/code-check-conventions.gradle.kts` (or a new spotless wiring), root
  `build.gradle.kts`, `build-logic/build.gradle.kts`, all `.gradle.kts` files (one-time reformat),
  `.idea/` ktfmt plugin flag, `scripts/setup-idea.sh`.
- **Docs**: `AGENTS.md`, `README.md`.
- **Build**: `check` now includes `spotlessCheck` for `.gradle.kts`.
- **Tests**: no test changes.