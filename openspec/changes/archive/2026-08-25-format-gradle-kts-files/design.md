# Design: Format Gradle Kotlin DSL files with ktfmt

## Context

See proposal.md — Why. Java is formatted via Spotless/palantir; the `.gradle.kts` files are the last
unformatted-by-build source. The spike determined ktfmt is the viable engine.

## Goals / Non-Goals

**Goals**

- `spotlessApply`/`spotlessCheck` cover `**/*.gradle.kts` across modules, the root build, and build-logic.
- The ktfmt style is `kotlinlang` (4-space, the official Kotlin style) with `max_line_length = 120` and unused-import
  removal.
- IntelliJ's `Reformat Code` matches `spotlessApply` via the official ktfmt plugin.

**Non-Goals**

- Formatting non-Gradle `.kts` scripts (e.g. `.connekt.kts`) — those belong to other tools.
- Enforcing a different style than ktfmt's `kotlinlang` — it matches the repo's existing IntelliJ formatting closely
  (three of the six spike sample files were already 0-diff).

## Decisions

- **ktfmt, not ktlint.** The spike: ktlint explodes method chains (`libs.versions.x.get()`) and restructures
  `register<JvmTestSuite> { }` blocks into verbose nested forms (195 diff lines on the gateway build). ktfmt joins
  chains, honors 4-space `kotlinlang` indent, and only diverges via break-after-`=` on assignments with a lambda RHS —
  a consistent, defensible style. ktfmt is also the only one with an official IntelliJ plugin (marketplace id 14912),
  giving build/IDE parity like palantir.
- **`kotlinlang` style + `max_line_length = 120`.** Matches the repo's 4-space convention and the 120-char wrap. The
  Spotless `ktfmt()` step must be configured for the kotlinlang style and the editorconfig line length; the spike
  confirmed ktfmt honors `max_line_length` only with editorconfig enabled.
- **Wiring spans three projects.** `code-check-conventions` (via `java-conventions`) covers each module's
  `build.gradle.kts`; the root `build.gradle.kts` and the `build-logic` included build need their own `kotlinGradle`
  step since they do not apply the convention. The target is `**/*.gradle.kts` only.
- **Unused-import removal is part of the canonical format.** The Spotless `ktfmt()` step sets
  `removeUnusedImports(true)` inside the `configure { }` lambda (Spotless exposes no DSL method for it), so
  `spotlessCheck` fails on any `.gradle.kts` with an unused import. The IDE plugin's `customRemoveUnusedImports` also
  defaults to `true`, keeping the two in lockstep.
- **IDE parity via the official plugin.** Commit the ktfmt plugin's project-level config (the file the plugin writes,
  `.idea/ktfmt.xml`, like `.idea/palantir-java-format.xml`) and extend `scripts/setup-idea.sh` to install `ktfmt`
  (marketplace id 14912) alongside `palantir-java-format`. `spotlessCheck` remains the backstop.
- **The IDE plugin uses Custom style, not Kotlinlang.** The plugin's `Kotlinlang` style mode hard-codes ktfmt's
  100-column default and ignores the line-length option (its `customMaxLineLength` applies only in Custom mode), which
  would break `spotlessCheck` on any IDE reformat. `.idea/ktfmt.xml` therefore configures the plugin's **Custom** style
  to reproduce ktfmt's `kotlinlang` style at `maxWidth = 120` (block/continuation indent 4, `ONLY_ADD` trailing commas,
  unused-import removal, no lambda-break preservation); verified byte-identical to the Spotless `ktfmt()` output.
- **One-time reformat.** The spike showed `java-conventions`, `build.gradle.kts`, and `settings.gradle.kts` are
  already conforming (0-diff); the complex DSL files (`docker-conventions`, gateway, code-coverage) get the ktfmt
  break-after-`=` restyle in the adoption commit.

## Risks / Trade-offs

- [The break-after-`=` restyle re-indents nested DSL blocks] → Mitigation: it is a consistent Kotlin style, applied
  once; `spotlessApply` output is deterministic and the ktfmt plugin keeps the IDE aligned.
- [The ktfmt IntelliJ plugin's `Kotlinlang` style mode ignores the line-length option (hard-codes 100 columns)] →
  Mitigation: configure the plugin's Custom style to reproduce kotlinlang at 120; verified the IDE output is
  byte-identical to the Spotless `ktfmt()` output.
- [The ktfmt IntelliJ plugin's config filename is plugin-specific] → Mitigation: resolved at implementation — the
  plugin writes `.idea/ktfmt.xml`; committed like `.idea/palantir-java-format.xml`.

## Migration Plan

1. Add the `kotlinGradle { ktfmt() }` step to the convention and the root/build-logic projects.
2. Run `./gradlew spotlessApply` to reformat all `.gradle.kts` files.
3. Verify `spotlessCheck` passes; enable the ktfmt plugin in the IDE, commit its enabled flag, extend `setup-idea.sh`.
4. Update `AGENTS.md` (Formatting convention covers `.gradle.kts`) and `README.md`.
5. Rollback: remove the `kotlinGradle` step and `git checkout` the reformatted files.

## Open Questions

None — the ktfmt plugin writes `.idea/ktfmt.xml`; the Custom-style config reproduces the Spotless `ktfmt()` output.