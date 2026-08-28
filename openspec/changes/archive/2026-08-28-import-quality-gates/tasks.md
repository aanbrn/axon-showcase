# Tasks

## 1. Add the import steps to the Spotless build gate

- [x] 1.1 Add `importOrder()`, `removeUnusedImports()`, and `forbidWildcardImports()` to the Spotless `java` block in
      `build-logic/src/main/kotlin/code-check-conventions.gradle.kts`, in that order, before `palantirJavaFormat()`,
      and verify `./gradlew spotlessCheck` on the root passes
- [x] 1.2 Run `./gradlew spotlessApply` at the root and inspect the diff: confirm the only changes are import
      reordering/removal (no code-body churn) and that `git diff --stat` shows no unexpected files
- [x] 1.3 Confirm `./gradlew spotlessCheck` fails when a wildcard import is present: add a temporary `import x.*;` to a
      scratch Java file under a module's `src/`, run `spotlessCheck`, confirm the failure, then remove the scratch file
- [x] 1.4 Verify no module's existing sources regress: run `./gradlew spotlessCheck` and the touched modules'
      `checkstyleIntegrationTest` tasks and confirm all pass

## 2. Confirm the committed tree is clean

- [x] 2.1 Confirm no files in the committed tree carry wildcard imports: run `git grep -n "import .*\.\*;" HEAD --
      '*.java'` and verify the only matches, if any, are in generated sources (which are excluded from Spotless)
- [x] 2.2 Confirm the working tree matches HEAD on the touched IT file's imports: `git diff HEAD --`
      `.../ShowcaseProjectorIT.java` shows no import-line changes (the IDEA corruption was local-only and already
      reverted)

## 3. Commit the IDEA code-style scheme

- [x] 3.1 Confirm `.idea/codeStyles/Project.xml` contains `USE_SINGLE_CLASS_IMPORTS=true` and both
      `CLASS_COUNT_TO_USE_IMPORT_ON_DEMAND`/`NAMES_COUNT_TO_USE_IMPORT_ON_DEMAND` set to `999`, and verify the XML is
      well-formed (opens/parses in IDEA or `xmllint --noout`)
- [x] 3.2 Document the import-gate behavior in `AGENTS.md` (palantir ignores imports; the scheme's
      `USE_SINGLE_CLASS_IMPORTS` + Spotless `forbidWildcardImports` are the enforcement points; a scheme change needs
      an IDEA restart) and verify the rendered Markdown stays within the 120-character wrap

## 4. Verification

- [x] 4.1 Run `openspec validate import-quality-gates` and confirm the change is valid with all artifacts consistent
- [x] 4.2 Run the full `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (Docker-free path; the coverage flag is
      required because the jacoco gate measures unit+component+integration exec data, and with ITs skipped the
      coverage would dip below the baseline) and confirm spotless + checkstyle gates pass for all modules