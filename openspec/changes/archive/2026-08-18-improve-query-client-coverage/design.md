## Context

`showcase-query-client` sits below the 0.80 coverage gate baseline, so its gate is disabled. The gap is in the reactive
client's error paths and resilience behavior. Test tiers are defined by collaborators (what is real vs. faked) per
AGENTS.md, and the module's suites drifted from that contract. See proposal.md - Why for motivation.

## Goals / Non-Goals

**Goals:**

- Raise `showcase-query-client` coverage above 0.80 (gate re-enabled) and keep it there.
- Align each test suite with its tier: component (`ApplicationContextRunner`, no Spring context), integration
  (`@SpringBootTest` + WireMock), e2e (real query-service container).

**Non-Goals:**

- No production-code or spec-level behavior changes.
- No new coverage infra (the JaCoCo gate already exists).

## Decisions

- **Relabel suites to their real tiers.** `ShowcaseQueryClientCT` is a `@SpringBootTest` + WireMock test -> integration;
  the former `ShowcaseQueryClientIT` boots a real query-service container -> e2e. *Alternative considered:* keeping the
  names and living with the mismatch — rejected because AGENTS.md defines tiers by collaborators, and the labels would
  mislead about what each suite verifies.
- **Register a dedicated `integrationTest` suite.** Renaming the old `integrationTest` suite to `e2eTest` consumed the
  suite; a fresh `integrationTest` suite re-declares the `implementation`-only deps it needs (axon springBoot starter,
  OpenSearch/Elasticsearch clients, WireMock, resilience4j, reactor-test/blockhound) because JvmTestSuite
  configurations do not inherit the project's `implementation`-only dependencies.
- **Resources live with the tier that boots a Spring context.** The `application*.yml` files move to
  `src/integrationTest/resources`; `componentTest` keeps none since `ShowcaseQueryClientPropertiesCT` only uses an
  `ApplicationContextRunner`. The circuit-breaker scenario gets its own `application-circuitbreaker.yml` profile
  (following the existing `retry`/`timelimiter` profiles).
- **Cover error branches once, at the shared seam.** `handleError` is shared by `fetchList` and `fetchById`, so error
  mapping is exercised via `fetchById` only; `fetchList` covers the happy path, metadata propagation, and the resilience
  scenarios. *Alternative considered:* duplicating every error test for `fetchList` — rejected as noise with no coverage
  gain.
- **Unit-test the retry filter directly.** `ShowcaseQueryRetryFilter` is a pure predicate that the IT only reaches
  through the retryable-status path; a `Tests`-suffixed unit test covers the remaining branches (non-retryable statuses,
  `TimeoutException`, `WebClientRequestException`).
- **Dynamic-agent jvmArgs only where BlockHound runs.** `-XX:+AllowRedefinitionToAddDeleteMethods` and
  `-XX:+EnableDynamicAgentLoading` are set on `integrationTest` and `e2eTest`; the component suite needs neither.

## Risks / Trade-offs

- [WireMock fakes the query service in the IT, so wire-level mismatches could go unnoticed] → the relabeled e2e test
  boots the real query-service container over HTTP, covering the seam the IT cannot.
- [Circuit-breaker state transitions are timing-sensitive] → the profile sets `minimumNumberOfCalls: 2` and the OPEN
  state is asserted synchronously after the error signals; the follow-up call fails fast with
  `CallNotPermittedException`.
- [fetchList error paths are never directly stubbed] → acceptable because the branches are covered via the shared
  `handleError`; a future status-specific divergence would still surface in the gate report.
