# Tasks

## 1. Propagate trace context from the web UI

- [x] 1.1 Add `showcase-web-ui/src/shared/tracing.ts`: a `traceparent()` helper returning `00-<32 hex>-<16 hex>-01`,
      with the trace id generated once per page load and the parent id per call, using `crypto.getRandomValues`
- [x] 1.2 Inject the header in `showcase-web-ui/src/shared/api.ts`'s `request()` — merge `traceparent` into
      `init.headers` on every call, building a header set when the caller passes `RequestInit` without one
- [x] 1.3 Add Vitest unit tests in `showcase-web-ui/src/shared/tracing.test.ts` (shape: `00-`, 32 hex, 16 hex, `-01`; a
      stable trace id across calls with distinct parent ids) and extend `showcase-web-ui/src/shared/api.test.ts` to
      assert `request()` sends the header, and update every exact `fetch`-argument assertion the added header breaks —
      in `shared/api.test.ts`, `entities/showcase/api.test.ts`, `features/showcase-actions/api.test.ts`, and
      `features/create-showcase/api.test.ts`

## 2. Admit the header in the gateway CORS allow-list

- [x] 2.1 Add `traceparent` to the `Cors.allowedHeaders` default in
      `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiProperties.java`, and to the field's Javadoc, which
      enumerates the headers the UI sends
- [x] 2.2 Add `traceparent` to the `showcase.api.cors.allowed-headers` placeholder in
      `showcase-api-gateway/src/main/resources/application.yml`
- [x] 2.3 Add `traceparent` to `BPE_DEFAULT_SHOWCASE_CORS_ALLOWED_HEADERS` in `showcase-api-gateway/build.gradle.kts`
- [x] 2.4 Add `traceparent` to the `apiGateway.cors.allowedHeaders` default and its `@param` doc comment in
      `helm/chart/src/main/helm/values.yaml` (the gateway template already joins the value — no template edit)
- [x] 2.5 Update `AGENTS.md`'s `SHOWCASE_CORS_ALLOWED_HEADERS` line to name the third header

## 3. Guard it with tests

- [x] 3.1 Extend `showcase-api-gateway/src/componentTest/java/showcase/api/ShowcaseApiPropertiesCT.java`: the
      CORS-header default test asserts the three-header list (the override test keeps its single `X-Custom` value)
- [x] 3.2 Extend the preflight test in
      `showcase-api-gateway/src/integrationTest/java/showcase/api/ShowcaseApiApplicationIT.java` to request
      `traceparent` alongside `content-type` and `idempotency-key`, and assert it is granted — confirm it fails against
      a registration without the header

## 4. Spec delta

- [x] 4.1 Keep the `ADDED` requirement on `showcase/clients/web-ui` and the `MODIFIED`
      `CORS allows the standalone UI origin` on `showcase/gateway/rest-api` coherent with the implementation (the
      MODIFIED block carries all four existing scenarios)

## 5. Verify

- [x] 5.1 `./gradlew :showcase-web-ui:check` (lint, format-check, Vitest)
- [x] 5.2 `./gradlew :showcase-api-gateway:componentTest :showcase-api-gateway:integrationTest`
- [x] 5.3 `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`
- [x] 5.4 `openspec validate --all`
- [x] 5.5 Render the resolved chart (`helm/chart/build/helm/charts/axon-showcase`) and confirm
      `SHOWCASE_CORS_ALLOWED_HEADERS` lists all three headers;
      `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal`
- [x] 5.6 Live deployment check (needs the owner's go-ahead — observability is deployment-only):
      `./gradlew helmInstallToLocal` with OTLP tracing export enabled, drive the UI, and query Tempo for a trace whose
      root is the browser's `traceparent` and which spans the gateway → command/query services; record the evidence. If
      the gateway roots a new trace instead, revisit design decision 5 before reporting the change done. **Evidence**:
      all pods Running (helmInstallToLocal); a Playwright browser on `http://axon-showcase-ui` sent
      `traceparent: 00-8afd6d0b4669e5ec05422a79b832a94d-ad783795cea7b313-01` on its page-load GET and
      `...-4dcb20cccd2e59c7-01` on its create POST (one trace id per page load, distinct parent ids, flags `01`);
      Tempo's trace `8afd6d0b4669e5ec05422a79b832a94d` holds 89 spans across axon-showcase-api-gateway (48) →
      axon-showcase-command-service (13) → axon-showcase-query-service (28), with both gateway server spans parented at
      those browser parent ids (design decision 5 holds); the cross-origin create returned 201, and an ingress preflight
      for `content-type,idempotency-key,traceparent` returned 200 listing all three

## 6. Docs

- [x] 6.1 Update `docs/ideas.md`: trim the parked RUM idea — remove the trace-propagation piece (implemented here),
      including its trailing sentence about the CORS `allowedHeaders` list, leaving the web-vitals/JS-error piece — and
      fix the product-phase entry, which still names web-UI trace propagation as the highest-value parked candidate
- [ ] 6.2 Refresh the `showcase/clients/web-ui` Purpose **in the archive commit** to name the trace propagation (a delta
      cannot carry a Purpose, so that commit is the only one that can hold it; the PR body states the deferral). The
      `showcase/gateway/rest-api` Purpose is unchanged — it never mentions CORS
- [x] 6.3 Check `README.md` for a human-visible capability this adds or changes (the observability section) and update
      it in the same change
