# Tasks

## 1. Add the Checkstyle rule

- [x] 1.1 In `config/checkstyle/checkstyle.xml`, add the Checker-level `RegexpMultiline` module from `design.md` (the
      anchored pattern and the message) as a sibling of `TreeWalker`. Verify it parses by running
      `./gradlew :showcase-query-proto:checkstyleMain` and confirming the build configures the rule.
- [x] 1.2 Prove the rule with a known-bad / known-good control: run the affected modules' Checkstyle tasks
      (`:showcase-api-gateway:checkstyleComponentTest`, `:showcase-projection-service:checkstyleTest`,
      `:showcase-query-client:checkstyleComponentTest`, `:showcase-query-service:checkstyleComponentTest`) and read the
      Checkstyle reports — each names its glued site (the known-bad input) and none of the enum, annotation-type, or
      empty bodies. The group-2 fixes turn the same run into the known-good.

## 2. Fix the six glued sites

- [x] 2.1 In `showcase-api-gateway/src/componentTest/java/showcase/api/ShowcaseApiErrorResolverCT.java`, separate the
      first member of `Payload` (line 45) and of `Controller` (line 50) from the opening brace — a blank line or a
      Javadoc.
- [x] 2.2 In `showcase-api-gateway/src/componentTest/java/showcase/api/rest/ShowcaseRestControllerCT.java`
      (`TestTicker`, line 1574) and
      `showcase-projection-service/src/test/java/showcase/projection/ShowcaseProjectorTests.java` (line 13), separate
      the first member from the brace.
- [x] 2.3 In `showcase-query-client/src/componentTest/java/showcase/query/ShowcaseQueryClientCT.java`
      (`FakeQueryService`, line 390) and
      `showcase-query-service/src/componentTest/java/showcase/query/ShowcaseQueryTransportServiceCT.java`
      (`CapturingObserver`, line 204), separate the first member from the brace.
- [x] 2.4 Re-run the task-1.2 checkstyle tasks and confirm they pass (the known-good side of the control).

## 3. Documentation

- [x] 3.1 Add the Formatting note to `AGENTS.md`: a type body's opening brace is followed by a blank line or a comment
      (Checkstyle's `RegexpMultiline` gate; `spotlessApply` preserves a blank after `{` and removes the one before `}`,
      and will not insert the padding). Verify with `./gradlew spotlessApply` and a grep.
- [x] 3.2 In `docs/ideas.md`, replace this change's blank-line idea (the `## 2026-10-08` section's only entry) with the
      member-separation rule the exploration surfaced as a parked follow-up — measured data: `EmptyLineSeparator`
      (member tokens, generated sources suppressed) flags no hand-written methods, constructors, initializers, or nested
      types and 14 grouped fields across 8 blocks, all in test sources; the open decision is
      `allowNoEmptyLineBetweenFields`. Verify the padding entry is gone, the new entry carries its trailing parked tag,
      and the file's other sections are intact.

## 4. Verification

- [x] 4.1 Run `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (the Docker-free gate, which includes every
      module's Checkstyle tasks) and confirm it passes.
- [x] 4.2 Run `openspec validate --all` and confirm the delta validates.
- [x] 4.3 Run `./gradlew spotlessApply` after the last edit, then `git status` and `git diff` to confirm the config, the
      five files, the docs, and the `docs/ideas.md` edit are the intended set.

## 5. Lesson capture

- [x] 5.1 Run the `lesson-capture` subagent over the implementation diff, the quick-review findings, and this change
      dir; apply the durable proposals to `AGENTS.md` (or record why each is rejected) and record the applied net
      `AGENTS.md` delta on this task. Verify by re-running `./gradlew spotlessApply` and reading the applied delta.
      Applied: one merge into this change's own Formatting sub-bullet (a Checkstyle `message` is a `MessageFormat`
      pattern) — net **+1** line. The six-round proposal-review churn was a non-compliance with the existing
      repeated-class rule, not a missing one, and a loop-bounding rule was rejected (it would have shipped the real
      `MessageFormat` defect).

## Workflow follow-up

- Run the change's quick review, then request the manual review pass; commit, push, and open one PR only after approval.
- Archive the change (move the change dir, sync the main spec) as an additional commit in the same PR after CI is green.
