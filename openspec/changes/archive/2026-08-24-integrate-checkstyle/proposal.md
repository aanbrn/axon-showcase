# Proposal: Integrate Checkstyle as a build-enforced code quality gate

## Why

The repo's correctness gates — ErrorProne, NullAway, SpotBugs — all run inside the Gradle build and need no IDE.
Style enforcement, however, currently depends on the author's IntelliJ inspections, an IDE-bound workflow. This is
step 1 of a three-step roadmap toward a fully IDE-independent development environment: **Checkstyle → Spotless and
other quality gates → complete IDE-independence**. A build-gated style checker ensures the project's style conventions
(120-character lines, naming, import hygiene) are enforced uniformly by `./gradlew check`, with no IDE required.

## What Changes

- New capability `showcase/quality/code-quality` captured in specs.
- Checkstyle wired into the `code-check-conventions` convention plugin (where SpotBugs and ErrorProne already live), so
  every module gets the style check as part of `check`, with no IDE involvement.
- A ruleset at `config/checkstyle/checkstyle.xml` encoding the project's documented conventions, plus
  `config/checkstyle/suppressions.xml` for any legitimate legacy exclusions.
- The Checkstyle tool version pinned in the version catalog (catalog-owned, so `dependencyUpdates` reports it).

## Capabilities

### New Capabilities

- `showcase/quality/code-quality` — build-enforced code style and static quality conventions.

### Modified Capabilities

None.

## Impact

- **Code**: `build-logic/src/main/kotlin/code-check-conventions.gradle.kts` (Checkstyle wiring),
  `gradle/libs.versions.toml` (tool version), `config/checkstyle/checkstyle.xml` (new),
  `config/checkstyle/suppressions.xml` (new).
- **Docs**: note Checkstyle among the quality gates in `AGENTS.md`; README unchanged unless the build section needs the
  gate listed.
- **Build**: `check` now includes `checkstyleMain`/`checkstyleTest` across all modules.
- **Tests**: no test changes; existing suites must still pass with the style gate active.
