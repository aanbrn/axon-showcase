# Proposal: Align gateway cache defaults

## Why

The API gateway's query-cache defaults diverge between the two places they are declared: `ShowcaseApiProperties`
(Java) defaults each cache to `1000` entries with `PT10M`/`PT5M` expiry, while `application.yml` overrides them to
`100000`/`1000000` entries with `PT24H`/`PT12H` expiry. Since the yml is loaded in every environment, the Java
defaults are effectively dead and the "documented" default never runs. The two sources should agree.

## What Changes

- Make `ShowcaseApiProperties` the authoritative contract and align `application.yml` to it.
- Increase the Java default `maximumSize` for `FetchShowcaseListQueryCache` from `1000` to `10000`, and for
  `FetchShowcaseByIdQueryCache` from `1000` to `100000`, keeping the existing `PT10M`/`PT5M` expiries.
- Update the `application.yml` defaults for both cache entries to match the Java values: `FetchShowcaseListQuery`
  `maximum-size` `100000` → `10000`, `FetchShowcaseByIdQuery` `maximum-size` `1000000` → `100000`, and both
  `expires-after-access` `PT24H` → `PT10M` and `expires-after-write` `PT12H` → `PT5M`.
- Align the Helm chart `values.yaml` gateway cache defaults (`apiGateway.caches.FetchShowcaseListQuery` and
  `FetchShowcaseByIdQuery`, which the chart renders into the cache env vars) to the same reconciled values:
  `maxSize` `10000` / `100000` and `expiresAfterAccess`/`expiresAfterWrite` `PT10M` / `PT5M`.
- Update `ShowcaseApiPropertiesCT` so both the defaults test (Java values) and the yml-wiring test (yml values) pin the
  reconciled numbers: list `10000`/`PT10M`/`PT5M`, byId `100000`/`PT10M`/`PT5M`.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

(none — the gateway `Cache fallback` requirement does not pin cache sizes, so this is a configuration-default
alignment with no spec-level behavior change; `skip_specs: true` is set in `.openspec.yaml`)

## Impact

- `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiProperties.java` — Java defaults for the two cache
  entries (`maximumSize` `1000` → `10000` / `100000`).
- `showcase-api-gateway/src/main/resources/application.yml` — `showcase.api.caches.*` placeholder defaults aligned to
  the Java values.
- `helm/chart/src/main/helm/values.yaml` — `apiGateway.caches.*` defaults aligned to the reconciled values (the chart
  renders these into the cache env vars, so deployments get the same defaults as the app yml).
- `showcase-api-gateway` component tests — `ShowcaseApiPropertiesCT` defaults and yml-wiring assertions updated.
- Runtime behavior: by default the gateway's list/by-ID query caches hold `10000`/`100000` entries and evict after
  `PT10M`/`PT5M` (previously `100000`/`1000000` and `PT24H`/`PT12H`), both via the app yml and via a Helm
  deployment. Env-var overrides are unaffected.
- No dependency or API changes.
