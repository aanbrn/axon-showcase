# Design: Reclassify the query-client WireMock integration test as a component test

## Context

See proposal.md — Why. `ShowcaseQueryClientIT` is a `@SpringBootTest` + `@EnableWireMock` suite whose collaborators
are in-process (WireMock, real serializers, resilience registries) with no external infrastructure — the repo's
definition of a component test. The query-client `componentTest` suite already declares WireMock/BlockHound/resilience4j
dependencies, so it was prepared to host this test; only `showcase-query-proto` and the BlockHound jvmArgs are missing.

## Goals / Non-Goals

**Goals:**
- Reclassify the WireMock suite to `src/componentTest` with the `CT` suffix and matching `@DisplayName`.
- Make the query-client suite wiring consistent with the taxonomy and with `ShowcaseCommandClientCT`.
- Update the AGENTS.md references so documentation matches reality.

**Non-Goals:**
- Changing any production code, test behavior, or scenario coverage (the tests move as-is).
- Touching other modules' suites (`integrationTest` remains where real Testcontainers ITs exist).
- Introducing a real query-client integration test (its only real collaborator is the query-service HTTP endpoint,
  exercised by the e2e).

## Decisions

- **Reclassify, don't redefine the taxonomy.** The AGENTS.md tier rule ("a Spring context with WireMock" = component)
  is the authoritative convention and is already followed by the command-client; align the query-client to it rather
  than redefining integration to include WireMock.
- **Remove the emptied `integrationTest` suite.** After the move it holds no tests; a leftover suite named
  `integrationTest` would invite future mislabeling. The `java-conventions` `check` wiring simply stops matching a
  non-existent suite, and no other module references `:showcase-query-client:integrationTest`.
- **`e2eTest` re-orders to `shouldRunAfter(componentTest)`**, preserving the `test → componentTest → e2eTest` chain.
- **Move the profile resources with the test.** The four `application*.yml` files are consumed by the
  `@ActiveProfiles`-annotated nested classes and must sit on the `componentTest` classpath; the `componentTest` suite
  currently has no resources, so this is a clean transfer with no merge conflicts.

## Risks / Trade-offs

- [The componentTest suite grows from one `ApplicationContextRunner` test to a full BlockHound suite] → Mitigation:
  the AGENTS.md jvmArgs rule says to add the BlockHound flags exactly when a suite calls `BlockHound.install()`, which
  the moved test does; the gotcha is updated to name the `componentTest` suite.
- [Removing the `integrationTest` suite changes `-PskipITs` semantics for query-client] → Mitigation: that is the
  intent — there is no Docker-backed IT to skip anymore; the e2e remains opt-in.

## Migration Plan

1. `git mv` `ShowcaseQueryClientIT.java` → `src/componentTest/java/showcase/query/ShowcaseQueryClientCT.java` and the
   four resources → `src/componentTest/resources/`; rename the class and `@DisplayName`.
2. Rewire `showcase-query-client/build.gradle.kts`: add `showcase-query-proto` + BlockHound jvmArgs to
   `componentTest`; delete the `integrationTest` suite; point `e2eTest.shouldRunAfter` at `componentTest`.
3. Update AGENTS.md (BlockHound gotcha; component-tier example).
4. Verify `./gradlew :showcase-query-client:componentTest :showcase-query-client:check` and the whole
   `./gradlew build -x e2eTest -PskipITs`.
5. Rollback: `git checkout` the moved files and revert the build/docs edits.

## Open Questions

None.