# Design: Spotless integration

## Context

See proposal.md — Why. Step 1 (Checkstyle) is done; formatting is now the only IDE-bound gate. This change makes
formatting build-enforced via Spotless + palantir-java-format, adds SPDX license headers, and retires the checkstyle
`CustomImportOrder` rule.

## Goals / Non-Goals

**Goals**

- `spotlessCheck` runs in `check` across all modules; `spotlessApply` fixes, no IDE required.
- Canonical style is palantir-java-format (fixed 120 columns — matches the project's `LineLength` convention).
- Every Java source carries `// SPDX-License-Identifier: MIT` matching the existing LICENSE.
- Import order is owned by the formatter; checkstyle drops `CustomImportOrder` but keeps `UnusedImports` and naming.
- Line-length enforcement is owned by the formatter: palantir wraps to 120 columns, and checkstyle's `LineLength` rule
  is removed — it only ever fired on lines the formatter cannot wrap, so it was friction with no unique value.
- Formatting is a mechanical `./gradlew spotlessApply` step, not a hand-formatting or IDE-formatting step.

**Non-Goals**

- Full parity with IntelliJ inspection set (step 3 of the roadmap).
- Kotlin DSL (`.kts`) formatting of the build-logic — deferred; Java sources only.
- Google-java-format or eclipse-jdt styles — rejected below.
- Ratchet-based gradual formatting — the whole tree is reformatted in the adoption commit.

## Decisions

- **Use Spotless, not the `com.palantir.java-format` Gradle plugin.** Rationale: Spotless is the standard
  orchestrator — it wires `spotlessCheck`/`spotlessApply` into Gradle, supports the license-header step, and can later
  cover the build-logic `.kts` via ktlint. The dedicated palantir plugin is Java-only and lacks the header step.
  Alternative considered: invoking `palantir-java-format` via a hand-rolled Exec task — rejected (re-invents
  `spotlessCheck`/`spotlessApply`, no license step).
- **palantir-java-format engine, not google-java-format.** Rationale: palantir is fixed at 120 columns, exactly the
  project's `LineLength` convention; google-java-format is locked at 100 columns (deliberate, non-configurable) and
  would force a convention change. Eclipse JDT is configurable but text-based and verbose — a weaker engine for
  modern Java (lambdas, records, chains).
- **License header is a single SPDX line, no copyright/year.** Rationale: the MIT LICENSE already carries the
  copyright ("Copyright (c) 2025 Alexey Afanasyev"); a one-line SPDX identifier is machine-detectable, low-maintenance
  (no `$YEAR` churn), and respects the "no comments in source" convention in spirit. Full license blocks per file are
  rejected as bloat.
- **No ratchet.** Rationale: the adoption commit reformats the entire tree once, so `spotlessCheck` verifies the whole
  codebase on every build from then on. Ratchet only helps gradual adoption over a legacy unformatted baseline, which
  this is not.
- **Wire Spotless in `code-check-conventions.gradle.kts`**, alongside Checkstyle/SpotBugs/ErrorProne, so every module
  inherits it uniformly. The `java` target covers all source sets (main, test, testFixtures, componentTest,
  integrationTest, e2eTest, gatling) since they are all `src` trees.
- **Formatting workflow is `./gradlew spotlessApply`, not the IDE.** The AGENTS.md formatting convention flips from
  "format edited files with the IntelliJ formatter (Steroid MCP)" to "run `./gradlew spotlessApply`" — the developer
  (human or agent) applies the deterministic formatter rather than hand- or IDE-formatting. The `codefmt` skill's
  Spotless CLI fallback becomes the primary path; the IDE remains an optional convenience for interactive editing, and
  `spotlessCheck` in `check` is the enforcement backstop for anything the workflow misses. Rationale: this is the
  mechanism that makes formatting IDE-independent and closes the loop on generated/edited code.
- **Scope the `java` target explicitly to `src/**/*.java`.** Spotless's default `java` target is *all* source-set
  sources regardless of location — which would pull in protobuf and MapStruct output under `build/generated/`
  (non-palantir style, long lines) and make `spotlessCheck` fail on generated code. All handwritten sources live under
  `src/`, so an explicit `src/**/*.java` target formats exactly the committed tree and nothing in `build/` — the same
  boundary the checkstyle `suppressions.xml` already draws.
- **Import-order ownership moves to the formatter.** palantir reorders imports into its canonical layout (static →
  java/javax → third-party). The checkstyle `CustomImportOrder` rule is removed; checkstyle retains `UnusedImports`
  and naming rules. This is the "stopgap retirement" anticipated when the rule was introduced.
- **Version the IntelliJ code style so the IDE import layout matches the formatter.** The palantir IntelliJ plugin does
  not touch imports — `Optimize Imports` uses the code style's *Import Layout*. The palantir layout (static first,
  blank line, then all other imports) is captured in `.idea/codeStyles/Project.xml` with `codeStyleConfig.xml`
  preferring the `Project` scheme, and `.idea/codeStyles/` is un-ignored so the IDE config is repo-versioned and
  contributors inherit it on open (no manual setup). The running IDE adopts it on project reopen.
- **Line-length enforcement moves to the formatter too.** After palantir owns wrapping, checkstyle's `LineLength` only
  fires on lines the formatter cannot break (string-literal continuations, DSL chains) — so `spotlessCheck` is the
  effective 120-column gate for every wrapable line and `LineLength` becomes pure friction with no unique value. The
  rule is removed; the `code-quality` spec's line-length requirement is satisfied by the formatter. Rationale: avoids
  perpetual suppressions for unbreakable lines (surfaced during implementation in the gatling simulation and one
  integration-test file).

## Risks / Trade-offs

- [One-time whole-tree reformat produces a large mechanical diff] → Mitigation: it is the adoption commit; the ratchet
  decision means no further churn, and `spotlessApply` output is deterministic.
- [palantir import order differs from the previous grouped layout] → Mitigation: the formatter owns ordering; the
  canonical order (static top, java, third-party) is standard. `CustomImportOrder` removal prevents conflict.
- [License-header step touches every file] → Mitigation: single-line SPDX header, applied in the same adoption commit;
  no year maintenance.
- [Spotless version drift / engine native-image slowness] → Mitigation: pin the plugin version in the catalog
  (catalog-owned, surfaced by `dependencyUpdates`); prefer the default Java-based palantir execution.

## Migration Plan

1. Add the Spotless plugin version to the catalog and `implementation(libs.spotless.plugin)` to build-logic.
2. Apply `id("com.diffplug.spotless")` in `code-check-conventions.gradle.kts` and configure the `java` target
   (`palantirJavaFormat()` + `licenseHeader`).
3. Remove `CustomImportOrder` from `config/checkstyle/checkstyle.xml`.
4. Run `./gradlew spotlessApply` once to reformat all Java sources and add headers.
5. Verify `./gradlew check` (or `spotlessCheck` + checkstyle) passes.
6. Rollback: remove the plugin, restore checkstyle config, `git checkout` the formatted files.

## Open Questions

- Whether the build-logic `.kts` sources should be formatted in a later step (ktlint via Spotless) — deferrable, does
  not change this design.
- Exact Spotless plugin version — resolved at implementation time to the current stable compatible with Gradle 9.7.1.