# Tasks

## 1. Add the Checkstyle rule

- [x] 1.1 In `config/checkstyle/checkstyle.xml`, add the `EmptyLineSeparator` module from `design.md` inside
      `TreeWalker` (member tokens; `allowNoEmptyLineBetweenFields=false`). Verify it parses by running
      `./gradlew :showcase-query-proto:checkstyleMain` and confirming the build configures the rule.
- [x] 1.2 Prove the rule with a known-bad / known-good control: run the affected modules' Checkstyle tasks
      (`:showcase-api-gateway:checkstyleComponentTest`, `:showcase-api-gateway:checkstyleE2eTest`,
      `:showcase-command-service:checkstyleComponentTest`, `:showcase-mapstruct-extension:checkstyleTest`,
      `:showcase-projection-model:checkstyleComponentTest`, `:showcase-resilience4j-extension:checkstyleTest`,
      `:showcase-test:checkstyleTest`) and read the reports — they name the 14 grouped fields (the known-bad input) and
      nothing in a method body or at the license-header-to-`package` gap. The group-2 fixes turn the same run into the
      known-good.

## 2. Fix the 14 grouped fields

- [x] 2.1 Insert a blank line between the grouped fields in `ShowcaseApiErrorResolverCT.java` (line 81),
      `ShowcaseApiGatewayE2E.java` (lines 155–156), and `ShowcaseDbSchedulerMetricsCT.java` (line 20).
- [x] 2.2 Insert a blank line between the grouped fields in `FluentAccessorNamingStrategyTests.java` (lines 35–36),
      `ShowcaseEntityMappingCT.java` (lines 18–19), and `Resilience4jAutoConfigurationImportFilterTests.java` (lines
      24–30).
- [x] 2.3 Insert a blank line between the grouped fields in `KafkaTestPublisherTests.java` (line 22) and
      `RandomTestUtilsTests.java` (line 16).
- [x] 2.4 Re-run the task-1.2 Checkstyle tasks and confirm they pass (the known-good side of the control).

## 3. Documentation

- [x] 3.1 Extend the `AGENTS.md` Formatting note: a blank line separates consecutive members (fields included; no blank
      line is required after an enum's constant, an annotation type's declaration, or an annotation type's member),
      gated by Checkstyle's `EmptyLineSeparator` with member tokens. Verify with `./gradlew spotlessApply` and a grep.
- [x] 3.2 Remove the `## 2026-10-08` section from `docs/ideas.md` entirely (its only entry is this change's idea; it
      rides this change's branch); verify the section is gone and the file's other sections are intact.

## 4. Verification

- [x] 4.1 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the Docker-free gate, which includes every
      module's Checkstyle tasks) and confirm it passes.
- [x] 4.2 Run `openspec validate --all` and confirm the delta validates.
- [x] 4.3 Run `./gradlew spotlessApply` after the last edit, then `git status` and `git diff` to confirm the config, the
      eight files, the docs, and the `docs/ideas.md` removal are the intended set.

## 5. Lesson capture

- [x] 5.1 Run the `lesson-capture` subagent over the implementation diff, the quick-review findings, and this change
      dir; apply the durable proposals to `AGENTS.md` (or record why each is rejected) and record the applied net
      `AGENTS.md` delta on this task. Verify by re-running `./gradlew spotlessApply` and reading the applied delta.
      Applied: one merge into this change's own Formatting sub-bullet — the exemption is directional (the module checks
      the gap after a token) — net **+1** line. The recurring `over-claim` class was the repeated-class rule's
      boundary-wording shape, and a loop-bounding rule was again rejected (the loop found a real defect at round 3).

## Workflow follow-up

- Run the change's quick review, then request the manual review pass; commit, push, and open one PR only after approval.
- Archive the change (move the change dir, sync the main spec) as an additional commit in the same PR after CI is green.
