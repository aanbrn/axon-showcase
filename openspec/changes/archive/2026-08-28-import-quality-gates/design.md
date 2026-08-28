# Enforce Import Quality in Build Gates — Design

## Context

See proposal.md — Why. Palantir-java-format (2.80.0) ignores imports entirely (verified by running its CLI on a
wildcard-import file: the wildcard is left untouched). So `spotlessCheck` accepts wildcard/misordered/unused imports,
and only checkstyle `AvoidStarImport` catches the worst case after the fact. IDEA's native Optimize Imports is the sole
wildcard source, and its collapse thresholds (5 classes / 3 static names) are on by default unless the project scheme
overrides them. The `ShowcaseProjectorIT` incident demonstrated the failure mode locally (corruption was not committed;
HEAD is clean).

## Goals / Non-Goals

**Goals:**
- Make `spotlessApply` self-healing for imports: ordering, unused removal, and a fail-fast wildcard gate.
- Commit the IDEA scheme settings that stop native import collapse, so developers and CI agree.
- Keep the committed tree clean: confirm no wildcard imports exist in HEAD (the incident corrupted only a local working
  tree, already reverted).

**Non-Goals:**
- **Not** adopting `expandWildcardImports()` (the auto-expansion step) in the committed config — see D1.
- **Not** changing the formatter itself (`palantirJavaFormat` stays the Java formatter).
- **Not** rewriting the checkstyle `AvoidStarImport` rule out (kept as a redundant safety net).

## Decisions

### D1: `forbidWildcardImports()` over `expandWildcardImports()`

Spotless 8.10.0 ships both (verified via `javap` on the resolved jar):
- `forbidWildcardImports()` — cheap step that fails the build on any `import x.*;`. CI-safe, no classpath resolution.
- `expandWildcardImports()` — rewrites wildcards to single-class imports using JavaParser and **full classpath
  resolution per file**. Resource-intensive; Spotless' own docs recommend it only as a transitional step for cleaning
  an existing codebase ("you may want to change to `forbidWildcardImports` when your codebase is cleaned and stable").

Alternatives considered: `expandWildcardImports` (rejected — slow, and it appends new imports after existing ones, so
it must run before `importOrder()`; overkill for a clean reference repo whose only wildcard source is a misbehaving
IDE). `forbidWildcardImports` (chosen — fail-fast, near-zero cost, and the gate the ecosystem recommends for stable
codebases).

### D2: Step order in the Spotless `java` block

Steps run in declaration order. The import steps must precede `palantirJavaFormat()` so the formatter sees the final
import set:

```kotlin
importOrder()
removeUnusedImports()
forbidWildcardImports()
palantirJavaFormat()
licenseHeader("// SPDX-License-Identifier: MIT\n")
```

`importOrder()` uses the default Eclipse-style layout, which groups static imports first, a blank line, then the rest,
matching both palantir's output and the committed IDEA `IMPORT_LAYOUT_TABLE`. If the default groups ever diverge from
palantir's ordering, the explicit form (`importOrder('', '\\#')` with groups) is the fallback; verify with
`spotlessCheck` before and after.

### D3: Committed IDEA scheme settings

`.idea/codeStyles/Project.xml` gains (already applied, verified working after an IDEA restart):

```xml
<option name="USE_SINGLE_CLASS_IMPORTS" value="true" />
<option name="CLASS_COUNT_TO_USE_IMPORT_ON_DEMAND" value="999" />
<option name="NAMES_COUNT_TO_USE_IMPORT_ON_DEMAND" value="999" />
```

`USE_SINGLE_CLASS_IMPORTS=true` unconditionally forbids wildcard generation (JetBrains' documented pairing with the
counts). The palantir IntelliJ plugin does **not** help here: since 2.47.0 it only replaces Reformat Code and delegates
import handling to IDEA's native optimizer (upstream issue #1352, still open), so the scheme is the only lever.

## Risks / Trade-offs

- **`importOrder()` default layout may not exactly match palantir's ordering** → verify with `spotlessApply` +
  `spotlessCheck`; switch to explicit groups if a divergence shows up (D2).
- **`forbidWildcardImports` is redundant with checkstyle `AvoidStarImport`** → acceptable; the checkstyle rule stays
  as a belt-and-braces net, and the Spotless step surfaces the failure earlier in the apply/check flow.
- **IDEA scheme change requires a restart/reload to take effect** → documented; stale-scheme behavior is the exact
  failure mode that produced the wildcard corruption.
- **`removeUnusedImports()` may remove imports on files that currently compile** → it only strips truly unused
  imports; run `spotlessApply` and the full `check` to confirm no behavioral change.

## Migration Plan

1. Apply the three Spotless import steps to `code-check-conventions.gradle.kts` (order per D2).
2. Run `./gradlew spotlessApply` (root) and inspect the diff — expect only import reordering/removal and no code
   churn.
3. Confirm the committed tree has no wildcard imports (`git grep` on HEAD) and the working tree's IT imports match
   HEAD.
4. Run `./gradlew spotlessCheck` + each touched module's `checkstyleIntegrationTest` to confirm all gates pass.
5. Commit the `.idea/codeStyles/Project.xml` scheme (already applied) alongside the build change.
6. Rollback: revert the Spotless block and the `Project.xml` lines; no data or schema impact.

## Open Questions

None.