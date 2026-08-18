# Proposal: Properties binding and constraint tests

## Why

The previous change made command and query validation configurable and introduced `ApplicationContextRunner`-based
component tests that verify property binding. That coverage is partial: only `validationEnabled` is tested in the
command and query services, the api-gateway, projection-service, and query-client properties classes have no binding
tests at all, and no test verifies that the bean-validation constraints declared on the properties classes actually
fire. Three command-service property groups (`saga-cache`, `saga-associations-cache`, `showcase-snapshot-trigger`)
are also absent from `application.yml` and the Helm chart, so they silently run on Java defaults and cannot be
tuned in any deployment.

## What Changes

- Add the missing `saga-cache`, `saga-associations-cache`, and `showcase-snapshot-trigger` blocks to the
  command-service `application.yml`, with env-var defaults mirroring the Java field defaults.
- Add the corresponding `sagaCache`, `sagaAssociationsCache`, and `showcaseSnapshotTrigger` sections to the Helm
  chart values and env vars to the command-service deployment, following the existing `showcaseCache` pattern.
- Rework `ShowcaseCommandPropertiesCT` and `ShowcaseQueryPropertiesCT` to cover every property: defaults, env-var-form
  binding, constraint violations (where constraints exist), and the real `application.yml` placeholder wiring (defaults
  and env-var override via `ConfigDataApplicationContextInitializer`).
- Add `ShowcaseApiPropertiesCT`, `ShowcaseProjectorPropertiesCT`, and `ShowcaseQueryClientPropertiesCT` component
  tests covering defaults, env-var-form binding, constraint violations, and (where a module `application.yml` exists)
  placeholder wiring.
- Add a `componentTest` test suite to the projection-service build (the module currently has none).
- Verify that bean-validation constraints on each properties class (for example `@Min`, `@Max`, `@DurationMin`,
  `@DurationMax`, `@NotEmpty`, `@URL`) reject out-of-range values by failing the Spring context.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/write-side/command-service`: the command service SHALL expose the saga caches and the showcase snapshot
  trigger as configurable properties, defaulting to the current Java field defaults.
- `showcase/deployment/helm-chart`: the chart SHALL wire the command-service saga cache, saga associations cache, and
  showcase snapshot trigger settings through values and environment, mirroring the existing `showcaseCache` wiring.

## Impact

- `showcase-command-service/src/main/resources/application.yml` — three new property blocks with env-var defaults.
- `showcase-command-service` component tests — `ShowcaseCommandPropertiesCT` reworked.
- `showcase-query-service` component tests — `ShowcaseQueryPropertiesCT` reworked.
- `showcase-api-gateway` component tests — new `ShowcaseApiPropertiesCT`.
- `showcase-projection-service` — new `componentTest` suite in `build.gradle.kts` and new `ShowcaseProjectorPropertiesCT`.
- `showcase-query-client` component tests — new `ShowcaseQueryClientPropertiesCT`.
- `helm/chart/src/main/helm/values.yaml` and `templates/command-service/deployment.yaml` — saga and snapshot settings.
- No dependency or API changes.
