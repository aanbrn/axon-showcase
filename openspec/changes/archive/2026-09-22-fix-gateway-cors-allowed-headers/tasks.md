# Tasks

## 1. Fix the CORS registration

- [x] 1.1 Add `allowedHeaders` to `ShowcaseApiProperties.Cors` — a `@NotNull List<@NotBlank String>` defaulting to
      `Content-Type`, `Idempotency-Key` (the headers the UI sends), with a Javadoc line
- [x] 1.2 Add the `allowed-headers` placeholder to `showcase-api-gateway/src/main/resources/application.yml`
      (`${SHOWCASE_CORS_ALLOWED_HEADERS:Content-Type,Idempotency-Key}`), mirroring `allowed-origins`
- [x] 1.3 Register `.allowedHeaders(apiProperties.getCors().getAllowedHeaders()...)` in `ShowcaseApiConfigurer`
- [x] 1.4 Expose the list through `helm/chart`: `apiGateway.cors.allowedHeaders` in `values.yaml` (with a `@param` doc
      comment, defaulting to `Content-Type`, `Idempotency-Key`) and the `SHOWCASE_CORS_ALLOWED_HEADERS` env var in
      `templates/api-gateway/deployment.yaml`, mirroring `allowedOrigins`
- [x] 1.5 Add `BPE_DEFAULT_SHOWCASE_CORS_ALLOWED_HEADERS` to the gateway's `bootBuildImage` environment map, mirroring
      the Java default (as `BPE_DEFAULT_SHOWCASE_EVENTS_KEEP_ALIVE_INTERVAL` does) — not empty, which is the origins'
      fail-closed override and would ship the bug this change fixes
- [x] 1.6 Document `SHOWCASE_CORS_ALLOWED_HEADERS` in `AGENTS.md`'s Key Environment Variables, beside
      `SHOWCASE_CORS_ALLOWED_ORIGINS`, naming all four surfaces

## 2. Guard it with tests

- [x] 2.1 Extend `ShowcaseApiPropertiesCT` with the header default, an env-var override, and a blank-value validation
      case in the invalid-env-var parameterized test, mirroring the CORS-origin coverage
- [x] 2.2 Add a preflight regression test to `ShowcaseApiApplicationIT` (`@SpringBootTest(RANDOM_PORT)`): an `OPTIONS`
      preflight from an allowed origin for `POST` with `Content-Type` and `Idempotency-Key` is granted, listing those
      headers in `Access-Control-Allow-Headers`. This fails against the current registration (the 403 in the design), so
      it is a real guard — confirm it fails on the pre-fix code before the fix lands

## 3. Spec delta

- [x] 3.1 Add the `MODIFIED` delta for `showcase/gateway/rest-api`'s `CORS allows the standalone UI origin` requirement
      — the header clause plus a scenario, carrying all three existing scenarios verbatim

## 4. Verify

- [x] 4.1 `./gradlew spotlessApply` then `spotlessCheck`
- [x] 4.2 `./gradlew :showcase-api-gateway:componentTest` and `:showcase-api-gateway:integrationTest` pass
- [x] 4.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` passes
- [x] 4.4 Live confirmation: boot the gateway and repeat the design's `curl` preflight matrix — the `POST` +
      `content-type,idempotency-key` case now returns 200 with `Access-Control-Allow-Headers` listing them
- [x] 4.5 `openspec validate --all` passes
- [x] 4.6 Render the chart and confirm the env var is present: `helm template helm/chart` shows
      `SHOWCASE_CORS_ALLOWED_HEADERS` with the default value, and the chart still lints
      (`./gradlew :helm:chart:helmLintMainChartFull` and `...Minimal`)

## 5. Docs

- [x] 5.1 Check `AGENTS.md` / `README.md` / `docs/adr/` for a CORS or "the UI can create" claim that this changes, and
      the parked "RUM observability" idea (whose trace half now names this list) — update in the same change only what
      this change makes stale. ADR-0008's CORS line is scoped to the origin (still fail-closed), so no ADR edit is owed
