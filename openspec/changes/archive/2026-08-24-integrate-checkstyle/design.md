# Design: Checkstyle integration

## Context

See proposal.md — Why. The repo already runs ErrorProne, NullAway, and SpotBugs from
`build-logic/src/main/kotlin/code-check-conventions.gradle.kts`; Checkstyle joins them in the same convention plugin.
Style rules are minimal on purpose: the goal is a build-gated style gate that encodes the project's *documented*
conventions, not full parity with the author's IntelliJ inspections (which is explicitly a later step in the
IDE-independence roadmap).

## Goals / Non-Goals

**Goals**

- Checkstyle runs in `check` across all modules, no IDE required.
- Rules encode documented conventions: 120-character lines, naming (including the `Tests`/`CT`/`IT`/`E2E` test-tier
  suffixes), import hygiene.
- Version is catalog-owned so `dependencyUpdates` reports it.

**Non-Goals**

- Full parity with the IntelliJ inspection set (step 3 of the roadmap).
- Formatting/format enforcement — Spotless is a separate follow-up (step 2).
- Style rewrites beyond what the chosen minimal ruleset requires.

## Decisions

- **Use Gradle's built-in `checkstyle` plugin** (`id("checkstyle")`), not a standalone CLI or a third-party Gradle
  plugin. Rationale: it is a core plugin, wires per-source-set tasks (`checkstyleMain`, `checkstyleTest`) into `check`
  automatically, and matches how SpotBugs/ErrorProne already integrate. Alternatives considered: invoking the
  Checkstyle CLI via an Exec task — rejected (per-module wiring and `check` integration would be hand-rolled); a
  third-party plugin — unnecessary given the core plugin.
- **Wire it in `code-check-conventions.gradle.kts`** so every module inherits it uniformly, alongside SpotBugs and
  ErrorProne. Rationale: single point of configuration, consistent with the existing static-analysis setup.
- **Minimal ruleset to start**: `LineLength` (120), naming rules (`TypeName`, `MethodName`, `ConstantName`, with the
  test-tier suffixes allowed on types), `UnusedImports` and import ordering, plus `JavadocType`-style presence only
  where the project already documents (Javadoc requirements). Rationale: keep the day-one violation baseline small;
  expand the ruleset in later iterations once the gate is green.
- **Test sources are checked too** (`checkstyleTest`), honoring the test-tier suffix naming. Rationale: the spec
  requires the suffix acceptance to be exercised; skipping test sources would silently miss the convention.
- **Violation strategy**: fix existing violations during implementation where few; use
  `config/checkstyle/suppressions.xml` only for legitimate exclusions, tracked and minimized — never suppress a whole
  module or disable a rule globally.
- **Version pinning**: catalog version in `gradle/libs.versions.toml`, referenced via `toolVersion`. Rationale:
  catalog-owned coordinates surface in `dependencyUpdates` (project convention) and keep the version visible.

## Risks / Trade-offs

- [Large day-one violation baseline forces a noisy diff] → Mitigation: minimal ruleset, fix-in-apply, suppressions
  only for legitimate exclusions.
- [Checkstyle and the IntelliJ formatter disagree on style] → Mitigation: ruleset encodes only documented conventions
  (line length, naming, imports); the canonical-formatter convergence happens in the Spotless step.
- [Checkstyle version incompatible with Java 21] → Mitigation: pin a current 10.x release known to run on Java 21 and
  verify in the build before committing.

## Migration Plan

1. Add the Checkstyle version to the catalog.
2. Apply `id("checkstyle")` in `code-check-conventions.gradle.kts` with `toolVersion` from the catalog.
3. Create `config/checkstyle/checkstyle.xml` (and `suppressions.xml` as needed) at the repo root, mirroring the
   existing `spotbugs-include.xml`/`spotbugs-exclude.xml` pattern.
4. Run `./gradlew check` across modules, fix violations, and confirm the gate is green.
5. Rollback: remove the plugin id and the ruleset files; `check` returns to its prior behavior.

## Open Questions

- Exact rule-list tuning (which naming/import rules beyond the core) is safe to settle during implementation once the
  first run shows the violation surface.
- The specific suppression baseline is determined by the first run; it should trend to zero over time.