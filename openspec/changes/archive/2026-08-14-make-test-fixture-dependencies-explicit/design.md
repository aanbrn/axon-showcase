## Context

See proposal.md - Why. The build's testFixtures graph has hidden transitive edges: `showcase-query-api` re-exports
`showcase-command-api` fixtures via `testFixturesApi`, `showcase-projection-model` declares a `testFixtures` bridge with
no fixture sources, and several consumers depend on `showcase-query-proto` testFixtures that ships nothing. All test
suites use the `jvm-test-suite` plugin wired in `build-logic/java-conventions.gradle.kts`, which applies
`java-test-fixtures` to every module, so every module *can* declare a testFixtures source set — whether it has one or
not.

## Goals / Non-Goals

**Goals:**

- Every consumer declares each testFixtures dependency it actually uses, directly and single-hop.
- A reader of any `build.gradle.kts` can trace an imported test utility to its declaring module.
- Remove all dead testFixtures edges (phantom bridge in projection-model, empty query-proto fixtures).

**Non-Goals:**

- Adding testFixtures sources to `showcase-projection-model` or `showcase-query-proto` — the projection-model tests and
  a `RandomShowcaseEntityTestUtils` are a separate, postponed change.
- Changing `java-test-fixtures` application or any convention-plugin wiring.
- Altering production dependency scopes (`api` vs `implementation`) of any module.

## Decisions

- **Demote `showcase-query-api`'s fixture dependency from `testFixturesApi` to `testFixturesImplementation`.** Its own
  `RandomQueryTestUtils` references `RandomCommandTestUtils` internally, so `testFixturesImplementation` is sufficient
  for compiling the fixture source set; nothing about query-api's public fixture API exposes command-api types. The
  `testFixturesApi` visibility was the *cause* of the hidden transitive reach. The demotion additionally requires a
  direct `implementation(testFixtures(project(":showcase-command-api")))` in query-api's own `test` suite, which
  imports `RandomCommandTestUtils` and previously received it through the `testFixturesApi` re-export. Alternative
  considered and rejected: keeping `testFixturesApi` and only adding direct deps at consumers — it leaves the
  misleading re-export in place, and future consumers would silently pick up command-api fixtures again.
- **Add direct `testFixtures(project(":showcase-command-api"))` to `showcase-query-service` and
  `showcase-query-client`.** These two import `RandomCommandTestUtils` and currently reach it through query-api's
  `testFixturesApi`. With the demotion, the direct dependency becomes mandatory, not optional.
- **Remove the projection-model testFixtures block entirely** (`testFixturesApi` + `testFixturesImplementation` lines).
  The module has no `src/testFixtures`; the jar it produces contains only a manifest. The removed edges were also
  silently carrying projection-model's *main* classes (`ShowcaseEntity`, `ShowcaseStatus`) and its exported
  spring-data-opensearch API into the integration suites of `showcase-query-service`, `showcase-query-client`, and
  `showcase-projection-service`; each of those `integrationTest` suites now declares
  `implementation(project(":showcase-projection-model"))` directly. Alternative considered and rejected: leaving the
  block as a placeholder for the postponed testFixtures work — an empty source set that exists only as a dependency
  carrier is exactly the confusion this change removes, and the postponed change can re-add the block when it adds real
  sources.
- **Remove `testFixtures(project(":showcase-query-proto"))` from `showcase-query-service` and
  `showcase-query-client`.** query-proto ships no testFixtures sources, and neither consumer imports anything from that
  source set. The dependency is a no-op edge. The removed edge also carried query-proto's *main*
  `QueryMessageRequestMapper` into `showcase-query-service`'s `integrationTest`, which now declares
  `implementation(project(":showcase-query-proto"))` directly.
- **Change `showcase-query-proto`'s `testFixtures(project(":showcase-test"))` to
  `implementation(project(":showcase-test"))`.** Its component test uses `showcase.test.RandomTestUtils`, which lives in
  showcase-test's main source set. The testFixtures variant exists but is empty, so this only works by accident today.
  Alternative considered: switching showcase-test's whole convention — out of scope and unnecessary.

## Risks / Trade-offs

- [A consumer missed in the audit still relies on the transitive `testFixturesApi` reach and fails to compile] →
  Mitigation: the audit is complete (proposal lists every consumer of `RandomCommandTestUtils`/`RandomQueryTestUtils`);
  a full `./gradlew build` catches any straggler, and the fix is a one-line direct dependency.
- [Demoting to
  `testFixturesImplementation` breaks a consumer that legitimately needs command-api types through query-api fixtures] →
  Mitigation: no consumer does today; the proposal's compile-affecting change is paired with the direct-dependency
  additions in the same change, so the tree always compiles at each commit.
- [The phantom projection-model test-fixtures jar removal surprises tooling or CI caching] → Mitigation: the jar is
  referenced by no consumer's classpath once the dead edges are removed; Gradle simply stops producing it.
