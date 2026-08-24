# Design: Command-client e2e tier and the test-taxonomy definition

## Context

See proposal.md — Why. The rename commits the repo to the collaborator-realism test taxonomy already stated in
AGENTS.md ("a test's tier is decided by its collaborators — what is real vs faked") and resolves the tension between
that rule and the strict "all four service containers" e2e bullet for the client tests.

## Goals / Non-Goals

**Goals**

- The two clients place the "client against the real deployed service" test in the same suite (`e2eTest`).
- AGENTS.md's e2e definition explicitly covers the client-e2e flavor (one real service + infra) as distinct from the
  full-system gateway e2e (all four services + full pipeline).
- The command-client's real-service test keeps its build guarantees (image build, ordering, BlockHound jvmArgs).

**Non-Goals**

- Adding a WireMock integration test to the command-client — its resilience coverage already lives at the component
  tier (`ShowcaseCommandClientCT`); the tier differs from the query-client but coverage is not lost.
- Renaming `ShowcaseQueryClientE2E` to integration — the query-client's split (IT = WireMock, E2E = real service) is
  the coherent end-state; the command-client moves to match it.

## Decisions

- **e2e = a real deployed system exercised against all-real collaborators (transport-independent).** This is the
  useful distinction and AGENTS.md's stated tiebreaker. Under it: integration = the client against a *faked* remote
  (WireMock, e.g. `ShowcaseQueryClientIT`); e2e = the client against the *real* deployed service — over HTTP for the
  gateway/query-service, over the distributed command bus (JGroups) for the command-service (e.g.
  `ShowcaseCommandClientIT`, `ShowcaseQueryClientE2E`); the gateway e2e is the full-system flavor (all four services,
  full command → Kafka → projection → query pipeline).
- **Rename `ShowcaseCommandClientIT` to `ShowcaseCommandClientE2E` and move it to `src/e2eTest`.** Rationale: its
  collaborators are all real (command-service container + PostgreSQL + Kafka), and its build already
  `dependsOn(":showcase-command-service:bootBuildImage")` — it deploys the real service image, which is e2e by nature.
- **Carry the suite configuration across the rename.** The `integrationTest` suite in the command-client build becomes
  `e2eTest` with identical config (BlockHound jvmArgs, `shouldRunAfter(componentTest)`,
  `mustRunAfter(":showcase-command-service:integrationTest")`, `dependsOn(command-service bootBuildImage)`), and the
  gateway's `mustRunAfter(":showcase-command-client:integrationTest")` follows to `:showcase-command-client:e2eTest`.
- **Clarify AGENTS.md rather than weaken it.** The e2e bullet gains the collaborator-realism framing so the client e2e
  tests are sanctioned by the definition, while the gateway's all-four-services test remains the full-system e2e.

## Risks / Trade-offs

- [Renaming a suite changes task/ordering references] → Mitigation: the change updates every reference (gateway
  `mustRunAfter`, AGENTS.md ordering/`@DirtiesContext`/BlockHound notes, README) in the same change.
- [The command-client ends up with no integration-tier test] → Mitigation: accepted — its WireMock-flavored coverage
  is at the component tier; the integration tier is reserved for client-vs-faked-remote tests, which this client does
  not currently need.

## Migration Plan

1. Move and rename `ShowcaseCommandClientIT` → `ShowcaseCommandClientE2E` (`src/e2eTest`), including its Spring test
   config (`application.yml` → `src/e2eTest/resources/`), and fix the fragile `awaitUntilClusterFormed` by buffering
   the container log in a static `StringBuilder` instead of relying on `CapturedOutput` (whose capture window begins
   after the cluster forms).
2. Rename the `integrationTest` suite to `e2eTest` in `showcase-command-client/build.gradle.kts` with identical config.
3. Update the gateway `mustRunAfter` to `:showcase-command-client:e2eTest`.
4. Update AGENTS.md (e2e definition + suite references) and README.md.
5. Run `./gradlew :showcase-command-client:e2eTest` and `openspec validate`.
6. Rollback: move the class and resources back to `integrationTest` and revert the wiring.

## Open Questions

None — the taxonomy decision and rename direction are settled.