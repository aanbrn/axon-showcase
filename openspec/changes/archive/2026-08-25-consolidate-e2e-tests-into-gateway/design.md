# Design: Consolidate the client e2e tests into the API gateway e2e

## Context

See proposal.md — Why. The gateway e2e boots the full pipeline and is the only consumer of both client libraries;
the command-client e2e duplicates its command/error coverage, and the query-client e2e's filtering coverage can move
to the gateway's already-exposed `GET /showcases` filter params. The command-client e2e is the only *isolated*
verification of the distributed-bus wire contract, which a serializer round-trip test replaces.

## Goals / Non-Goals

**Goals:**
- Reduce to a single e2e suite (the gateway) that covers the command lifecycle, error round-trips, and — newly —
  list filtering on real pipeline data.
- Preserve BlockHound coverage of the real reactive path and the isolated wire-contract check.
- Remove the client e2e suites, their deps, and the `mustRunAfter` ordering constraints.

**Non-Goals:**
- Changing production code, the component/unit tiers, or the services' own IT/e2e tests.
- Adding HTTP contract-testing frameworks (Pact/SCC) — the wire contract is covered by the serializer test.
- Keeping the "client library in isolation" e2e tier (the gateway is the only consumer; regression debugging relies on
  the component tests + a failing gateway e2e).

## Decisions

- **The gateway e2e becomes the sole e2e suite.** Its existing schedule/start/finish/remove + problem-mapping tests
  already exercise both clients through the real transports; the client e2e added only isolation, which is not worth
  two container-booting suites for a single-consumer repo.
- **Filtering coverage is already in the gateway e2e.** `FetchingListTests` seeds showcases across all statuses and
  asserts `GET /showcases` with `title`/`status`/multi-status filters, `size`, and `afterId` on real pipeline data; the
  query service orders by showcase ID descending, which the pagination test already relies on. The change therefore
  only adds BlockHound to the gateway e2e; it does not add new filtering scenarios.
- **BlockHound moves to the gateway e2e.** Install it in `@BeforeAll` and add the
  `-XX:+AllowRedefinitionToAddDeleteMethods` / `-XX:+EnableDynamicAgentLoading` jvmArgs to the gateway `e2eTest` task;
  this checks the whole reactive stack, not just one client.
- **The serializer round-trip test lives in `showcase-command-api`.** It round-trips the four commands and
  `ShowcaseCommandErrorDetails` through `JacksonSerializer` (what `axon.serializer.messages: jackson` produces),
  asserting field preservation — the same wire contract the command-client e2e verified over the JGroups bus, without
  the transport. The JGroups transport itself is now exercised only transitively by the gateway e2e; accepted as a
  trade-off.
- **Remove the ordering constraints.** The gateway e2e drops `mustRunAfter(":showcase-command-client:e2eTest")` and
  `mustRunAfter(":showcase-query-client:e2eTest")`; the client build files drop their `e2eTest` suites entirely
  (including now-unused e2e-only dependencies).

## Risks / Trade-offs

- [Loss of failure isolation: a client-only regression surfaces as a gateway HTTP symptom] → Mitigation: the
  component tests pin the client libraries' behavior (fake bus / WireMock), and the serializer test pins the wire
  contract, so a client regression is still localizable at the component tier.
- [Filtering e2e tests could be flaky on projection timing] → Mitigation: reuse the existing `awaitShowcase*`
  helpers so every assertion runs only after the projected state is visible.
- [The gateway e2e grows and slows] → Mitigation: it replaces two other container-booting suites, so net CI time
  drops; the added filtering scenarios reuse existing helpers.

## Migration Plan

1. Delete the command-client and query-client `src/e2eTest` trees; remove their `e2eTest` suite blocks and
   e2e-only dependencies from the two `build.gradle.kts` files.
2. In `showcase-api-gateway/build.gradle.kts`, drop the two `mustRunAfter` client-e2e references and add the BlockHound
   jvmArgs to the `e2eTest` task.
3. Add the `reactor-blockhound` dependency and `BlockHound.install()` to the gateway e2e (its `FetchingListTests`
   already covers the filtering scenarios).
4. Add the serializer round-trip test in `showcase-command-api/src/test/java`.
5. Update AGENTS.md (test-tier, ordering, BlockHound gotcha) and README.
6. Verify `:showcase-command-api:test`, the gateway `e2eTest`, and `./gradlew build -x e2eTest -PskipITs`.
7. Rollback: `git checkout` the removed suites and revert the build/docs edits.

## Open Questions

None.