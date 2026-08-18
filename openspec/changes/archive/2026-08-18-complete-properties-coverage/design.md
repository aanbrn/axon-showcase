## Context

See proposal.md - Why. The write side and read side expose `@ConfigurationProperties` classes under the
`showcase.*` prefixes, bound from `application.yml` env-var placeholders and (for command-service) the Helm chart.
The prior change introduced `ApplicationContextRunner`-based binding component tests for `ShowcaseCommandProperties`
and `ShowcaseQueryProperties`, but only for the `validationEnabled` property. The properties classes carry bean
validation constraints (`@Min`, `@Max`, `@NotNull`, `@DurationMin`, `@DurationMax`, `@NotEmpty`, `@URL`) and are
annotated `@Validated`; none of those constraints is currently exercised by a test.

## Goals / Non-Goals

**Goals:**
- One component test class per properties class covering (a) unset-property defaults, (b) env-var-form binding,
  (c) bean-validation constraint violations that fail the Spring context, and (d) the real `application.yml`
  placeholder wiring — `${ENV:default}` defaults and env-var override through the placeholder.
- Wire every property of every properties class into its service `application.yml` and, where the Helm chart manages
  env vars, into the chart values and command-service deployment.
- Add a `componentTest` suite to the projection-service build so its properties class is tested at the same tier as
  the other services.

**Non-Goals:**
- Not testing dotted-form (`withPropertyValues`) binding — the existing pattern and production wiring use env-var
  placeholders, so env-var-form binding is the behavior that matters.
- Not testing `@NotNull` failures (an unset nested default always satisfies it) or `@NotBlank` map keys.
- No behavior change to the properties classes themselves; defaults stay as declared in Java.

## Decisions

**D1: Extend the existing `ApplicationContextRunner` pattern rather than introduce a new test approach.**
Each test class keeps the nested `PropertiesConfig` with `@EnableConfigurationProperties`, uses
`SystemEnvironmentPropertySource` for env-var-form values, and asserts defaults on the bean. For constraint
violations, bind an out-of-range value and assert `context.getStartupFailure()` is non-null (or use
`assertThat(context).hasFailed()`). Alternatives considered: `@SpringBootTest` (too heavy, boots full context) and
`Binder` unit tests (bypasses `@ConfigurationProperties` validation wiring, loses the `@Validated` behavior under
test). Rejected.

**D2: Env-var names for the new command-service properties follow the existing `SHOWCASE_CACHE_*` convention.**
`saga-cache` → `SAGA_CACHE_*`, `saga-associations-cache` → `SAGA_ASSOCIATIONS_CACHE_*`, and
`showcase-snapshot-trigger` → `SHOWCASE_SNAPSHOT_TRIGGER_LOAD_TIME_THRESHOLD`. The yml default values mirror the Java
field defaults so behavior is unchanged.

**D3: Constraint tests assert context failure, not specific messages.**
Asserting `context.getStartupFailure() != null` (via AssertJ `hasFailed()`) is robust against validation message
wording across hibernate-validator versions. The binding tests already pin the accepted values, so a failed context
plus a successful binding test for the same property gives full coverage.

**D4: Projection-service `componentTest` suite mirrors the query-service suite.**
Register the suite in `testing { suites { } }`, add the shared test dependencies (self, `showcase-test`,
command-api testFixtures), and set `shouldRunAfter(test)` and the required jvmArgs
(`-XX:+AllowRedefinitionToAddDeleteMethods`, `-XX:+EnableDynamicAgentLoading`) to match sibling modules.

**D5: `ShowcaseQueryClientPropertiesCT` lives in `showcase-query-client`, not the gateway.**
The properties class is owned by the library module, which already has a `componentTest` suite. Testing it there
keeps the class's contract with its owning module and requires no new gateway build changes.

**D6: Load the real `application.yml` inside the `ApplicationContextRunner` to verify placeholder wiring.**
`ConfigDataApplicationContextInitializer` processes the classpath `application.yml` exactly as Spring Boot does; each
properties CT adds it to the runner and then asserts (a) the `${ENV:default}` placeholders bind the yml-declared
defaults with no env vars set, and (b) a `SystemEnvironmentPropertySource` layered at the front overrides the yml
default through the real placeholder — reproducing Spring Boot's env-over-yml precedence. This closes the
yml-placeholder gap at the component tier: no full context, no containers, no integration tests. A
`@SpringBootTest`-based override test was rejected because in-process overrides (`@DynamicPropertySource` /
`TestPropertyValues`) do not exercise the env-var path, and OS env vars cannot be set at runtime in the test JVM.
`ShowcaseQueryClientPropertiesCT` gets no yml test of its own: the query-client module has no `application.yml` — its
`SHOWCASE_QUERY_SERVICE_URL` placeholder lives in the api-gateway yml, so the api-gateway CT's yml test covers it.

## Risks / Trade-offs

- **Validation only fires when a JSR-303 validator is on the classpath** → every module already carries
  hibernate-validator (command/query via `spring-boot-starter-validation`, gateway directly, projection transitively
  via `showcase-command-api`, query-client via `showcase-query-api`). The constraint tests double as a guard that the
  validator stays on the classpath.
- **`ApplicationContextRunner` now loads the real `application.yml`** → the yml-placeholder gap is closed by D6 at the
  component tier; no integration tests are needed. The yml-wiring tests pin the yml's current defaults (which may
  differ from the Java field defaults, e.g. the gateway caches), so a later yml or values edit that diverges from the
  pinned defaults fails the CT. What remains untested at this tier is the Helm chart env wiring, which is covered by
  `helmLint` (values structure) and E2E (runtime).
- **Map binding replaces defaults** → the gateway defaults test asserts both cache entries exist with their defaults,
  pinning the two-entry default map and documenting that binding one entry replaces the whole map.
- **New yml blocks change what a deployment can tune** → defaults mirror the current Java values, so existing
  deployments see identical behavior until they opt in via env vars.

## Migration Plan

No rollout steps beyond deploying the updated services and chart; the new env vars default to the current behavior.
Rollback is a revert of the yml and values additions.

## Open Questions

None.