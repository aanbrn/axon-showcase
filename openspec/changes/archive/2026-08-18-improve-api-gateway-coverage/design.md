## Context

`showcase-api-gateway` sits at 53% coverage, below the 0.80 gate baseline, so its gate is disabled. The gaps are the
`ShowcaseApiApplication` bean wiring (0%) and the controller's validation-error mapping. Test tiers are defined by
collaborators per AGENTS.md. See proposal.md - Why for motivation.

## Goals / Non-Goals

**Goals:**

- Raise `showcase-api-gateway` coverage above 0.80 (gate re-enabled) and keep it there.
- Verify the application's bean wiring at the integration tier via a real context boot.
- Cover the validation-error mapping, including the visitor methods unreachable through the controller's HTTP surface.

**Non-Goals:**

- No spec-level or runtime behavior changes (one behavior-preserving extraction aside).
- No coverage infra changes (the JaCoCo gate already exists).

## Decisions

- **Full-context integration test for the application beans.** `ShowcaseApiApplicationIT` boots the whole gateway
  (`@SpringBootTest`) with JGroups isolated to a dedicated port/cluster, and asserts the JGroups connector, primary
  distributed command bus, cache-manager custom caches, security chain, and caches. *Alternative considered:* unit-test
  the config bean methods directly — rejected because AGENTS.md verifies bean wiring at the integration tier.
- **Extract the validation visitor into `ShowcaseApiErrorResolver`.** The `HandlerMethodValidationException` visitor's
  cookie/matrix/model/part/other methods are unreachable via HTTP (no endpoint declares those parameter kinds), so
  testing the mapping requires a standalone component. A `@Component` holding the visitor + the `WebExchangeBindException`
  mapping makes all branches directly unit-testable. *Alternatives considered:* reflectively invoking the private
  handlers (rejected — brittle), excluding the code from coverage (rejected — dishonest).
- **Real in-process Caffeine caches in the controller CT.** The `AsyncCache`s are app-owned beans, so the CT now wires
  real Caffeine caches (cleared per test) and asserts cache contents instead of mocking and verifying interactions —
  aligning with "don't mock the app's own collaborators".
- **Coverage measured on the reachable surface.** The visitor's defensive `orElseThrow` branches (blank name + no
  discoverable parameter name), the controller's generic `handleException` switch (wrapped-known-exception cases), and
  `ShowcaseApiApplication.main()` remain uncovered as edge-case/entry-point code.

## Risks / Trade-offs

- [JGroups connector boot in tests can be slow/flaky] → the IT isolates it on a dedicated bind port and cluster name.
- [`@Nested` test groups break the `@WebFluxTest` slice] → nested classes load the full app context and fail on JGroups,
  so `ShowcaseApiControllerCT` stays flat.
- [Defensive error-mapping branches stay untested] → accepted; they are edge-case code not reachable through the API.
