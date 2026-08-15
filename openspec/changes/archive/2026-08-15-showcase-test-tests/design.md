## Context

See proposal.md - Why. `showcase-test` is a shared test-utilities module with no tests of its own. Its `RandomTestUtils`
is pure static logic; `KafkaTestPublisher` wraps an Axon `KafkaPublisher` (via `DefaultUnitOfWork`) and tracks
per-aggregate sequence numbers. Both are consumed by many other modules' test suites.

## Goals / Non-Goals

**Goals:**

- Verify the real logic in both classes at the unit tier, following the repo's tier rules (tier = what is real vs. faked).
- Keep the build change minimal and consistent with sibling library modules.

**Non-Goals:**

- Starting any Kafka infrastructure — the integration tier already exercises `KafkaTestPublisher` against real Kafka in
  `showcase-projection-service`.
- Changing production code or behavior of `showcase-test`.

## Decisions

### 1. Test `KafkaTestPublisher` at the unit tier with a mocked `KafkaPublisher`

- **Decision**: mock `KafkaPublisher<?, ?>` with Mockito and assert on the `GenericDomainEventMessage` passed to `send`.
  The only external collaborator (Kafka itself) is faked; `DefaultUnitOfWork` and `GenericDomainEventMessage` are real
  in-process Axon classes. Per the repo tier rule that makes this a unit test (`Tests` suffix, `src/test`).
- **Alternative considered — component tier** (`AggregateTestFixture`-style, real publisher): rejected because a real
  `KafkaPublisher` requires a real Kafka producer, which is integration territory already covered by
  `ShowcaseProjectorIT`.
- **Alternative considered — no tests (rely on downstream ITs)**: rejected because downstream failures don't pinpoint
  regressions in `KafkaTestPublisher` itself.

### 2. Assert on the sent message, not on side effects of a real broker

- **Decision**: capture the `EventMessage` argument via Mockito `ArgumentCaptor` and assert payload, aggregate type,
  extracted identifier, and sequence numbers. Sequence tracking is verified by publishing two events for the same
  aggregate (0 then 1) and two events for different aggregates (each starting at 0).

### 3. Add mockito as a `testImplementation` dependency

- **Decision**: add `testImplementation(libs.mockito.core)` to `showcase-test/build.gradle.kts`, matching
  `showcase-resilience4j-extension`. The Mockito inline agent is already applied globally in `java-conventions`, so no
  other wiring is needed.

## Risks / Trade-offs

- [DefaultUnitOfWork.execute may not behave standalone outside a Spring/Axon context] → Mitigation: verify in the first
  task; if it misbehaves, the message-construction assertions can be extracted by mocking the `KafkaPublisher` and
  observing only the argument — `DefaultUnitOfWork` is only the execution wrapper, not the assertion target.

## Migration Plan

None — tests only; no deployment or rollback.

## Open Questions

None.