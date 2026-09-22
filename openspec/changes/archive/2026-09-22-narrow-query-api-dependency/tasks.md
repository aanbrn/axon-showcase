# Tasks

## 1. Narrow the dependency

- [x] 1.1 In `showcase-query-api/build.gradle.kts`, replace `api(project(":showcase-command-api"))` with
      `api(project(":showcase-identifier-extension"))`
- [x] 1.2 In `showcase-query-client/build.gradle.kts`, add `implementation(libs.hibernate.validator)` — the
      `org.hibernate.validator.constraints.URL` it imports in `ShowcaseQueryClientProperties`

## 2. Update the records

- [x] 2.1 Amend `docs/adr/0010-enforce-module-dependency-graph.md`: record that the deferred narrowing has landed in all
      three of its sites — Context, Decision, and Consequences (keep the deferral's account; add the resolution)
- [x] 2.2 Update the `AGENTS.md` `project(...)` gotcha — its worked example is this edge; keep the rule, restate the
      example in its resolved form
- [x] 2.3 Remove the implemented idea from `docs/ideas.md` **and** sweep the "Make the next phase a product phase"
      entry, whose candidate list still names this narrowing

## 3. Verify

- [x] 3.1 `./gradlew spotlessApply` then `spotlessCheck`
- [x] 3.2 `./gradlew compileJava compileTestFixturesJava compileTestJava` across all modules succeeds (the experiment's
      breakage set is now fixed)
- [x] 3.3 `./gradlew verifyModuleDependencies` passes — the new `query-api → identifier-extension` edge is not forbidden
      (contract → `-extension` is unconstrained, and `showcase-command-api` already declares the same edge)
- [x] 3.4 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes
- [x] 3.5 Confirm no consumer still resolves `axon.modelling` / `hibernate.validator` / `jackson2.databind` through the
      removed re-export (`./gradlew :showcase-query-api:dependencies --configuration compileClasspath` no longer lists
      `showcase-command-api`)
- [x] 3.6 `openspec validate --all` passes
