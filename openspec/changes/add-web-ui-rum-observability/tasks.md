# Tasks

## 1. Gateway: client-telemetry endpoint

- [x] 1.1 Add the request DTOs under `showcase-api-gateway/src/main/java/showcase/api/telemetry/`
      (`ClientTelemetryReport` with a required `path`, a `vitals` list, and an `errors` list; nested `ClientVital` with
      a bounded name and rating plus a non-negative value; nested `ClientError` with capped string fields), carrying
      Jakarta constraints (`@Size`, `@Pattern`, `@PositiveOrZero`, `@Valid` cascade) — the vitals/errors lists use
      `@Singular(ignoreNullCollections = true)` so an omitted or `null` list becomes empty, and the vital name and
      rating are constrained strings, not Java enums, so an unknown value is a validation failure rather than a
      deserialization failure. Add the package's `package-info.java` (Javadoc + `@NullMarked`, like the sibling `api/`
      packages); give every new Java file the SPDX header (Spotless-enforced); verify
      `./gradlew :showcase-api-gateway:compileJava` passes.
- [x] 1.2 Add `TelemetryApi` (an OpenAPI-annotated interface following the sibling `ShowcaseRestApi`) and
      `TelemetryController` exposing `POST /telemetry`, returning `204 No Content`, with a
      `@ExceptionHandler(HandlerMethodValidationException.class)` delegating to `ShowcaseApiErrorResolver` so a
      malformed report yields the `400 Bad Request` problem detail with a `bodyErrors` map; add `showcase.api.telemetry`
      to `springdoc.packages-to-scan` in the gateway `application.yml`; verify `compileJava` passes and the endpoint
      appears in the gateway OpenAPI document.
- [x] 1.3 Add a `TelemetryRecorder` component recording each vital into a Core Web Vitals distribution
      (`showcase.web.vitals`) and each error into a counter (`showcase.web.errors`), tagged only with bounded labels
      (vital name, rating, error type, route); normalize the labels by dropping the route's query string, folding an
      unknown route to `other`, and folding an unrecognized error type to `Error`; log each error's message/source at a
      capped length (never as a label); verify `compileJava` passes.
- [x] 1.4 Add `TelemetryControllerCT` (a `@WebFluxTest` slice like `ShowcaseRestControllerCT`, with a static
      `@DisplayName` on the class and each case) asserting: a valid report returns `204` and records the expected
      vitals/errors; a malformed report returns `400` with `bodyErrors` and records nothing (the known-bad vs known-good
      control for the validation, parameterized over each rejection arm); the slice wires no command or query
      collaborator, so recording cannot dispatch one. Run `./gradlew :showcase-api-gateway:componentTest` and confirm
      the new cases pass.
- [x] 1.5 Add a unit test class (`@DisplayName` on the class and each case) for label normalization (a route with a
      query string loses it; an unknown route becomes `other`; an unrecognized error type becomes `Error`); run
      `./gradlew :showcase-api-gateway:test`.

## 2. Web UI: measurement and reporting

- [x] 2.1 Add `web-vitals` to `showcase-web-ui/package.json` and update the lockfile; verify the frontend dependency
      resolves and `./gradlew :showcase-web-ui:npmTest` still runs.
- [x] 2.2 Add `showcase-web-ui/src/shared/telemetry.ts` (SPDX header, JSDoc on its exports): register the Core Web
      Vitals callbacks and the `error`/`unhandledrejection` listeners, and report a bounded payload through the shared
      `request` helper (`request('/telemetry', { method: 'POST', keepalive: true, ... })`, which supplies `BASE` and the
      `traceparent`); export it through `shared/index.ts`; verify `./gradlew :showcase-web-ui:check` (lint + type-check)
      passes.
- [x] 2.3 Initialise telemetry once from `showcase-web-ui/src/main.tsx`; verify `./gradlew :showcase-web-ui:build`
      produces the bundle.
- [x] 2.4 Add `showcase-web-ui/src/shared/telemetry.test.ts` (SPDX header, mock `fetch`) asserting a measured vital
      triggers a `POST` to `/telemetry`, a thrown error/unhandled rejection triggers a report, the request carries a
      `traceparent`, and a rejected `fetch` does not throw; run `./gradlew :showcase-web-ui:npmTest`.
- [x] 2.5 Confirm the web-UI coverage gate still passes with the new module: `./gradlew :showcase-web-ui:check`.
- [x] 2.6 Add `/telemetry` to the Vite proxy in `showcase-web-ui/vite.config.ts` (so `viteDev` and the `vite preview`
      server the e2e drives both forward it to the gateway on `:8080`) and update the proxy JSDoc in
      `showcase-web-ui/playwright.config.ts`; verify `./gradlew :showcase-web-ui:check` passes and a report reaches the
      gateway under `viteDev`.

## 3. Grafana dashboard

- [x] 3.1 Add a "Web UI experience" row and panels (Core Web Vitals percentiles by rating, JS-error rate by type) to
      `helm/chart/src/main/helm/files/grafana-dashboards/axon-showcase.json`, querying the gateway's new metrics; verify
      `./gradlew verifyDashboardJson` passes and
      `./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal` still lints clean.

## 4. Documentation

- [x] 4.1 Add the client-side experience metrics to the README's observability description (the Grafana dashboard now
      shows browser-side Core Web Vitals and JS errors alongside the nginx server metrics); re-derive and update the
      dashboard's panel/section count it states (`README.md` currently says "35 panels across 6 sections"); verify the
      section reads consistently with the existing observability access path.
- [x] 4.2 Remove the client-side (RUM) observability idea from `docs/ideas.md`, since this change implements it.
- [ ] 4.3 Refresh the `showcase/clients/web-ui` capability `## Purpose` in
      `openspec/specs/showcase/clients/web-ui/spec.md` to mention client-side experience measurement — a delta cannot
      carry a Purpose, so the edit lands in the archive commit; record the deferral in the change report.
- [x] 4.4 Widen every live enumeration of the gateway's surfaces for the new `/telemetry` endpoint, deriving the set by
      grepping the repository for the existing endpoint strings (`/showcases`, `/events`) across `AGENTS.md`,
      `README.md`, `openspec/config.yaml`, and the web-UI config — do not rely on this list. It covers at least the
      `AGENTS.md` Architecture bullet and its Local Development dev-server proxy comment, and the README project-tree
      comment, component-table row, and dev-server proxy sentence; decide explicitly whether `openspec/config.yaml`'s
      coarse service description changes.
- [x] 4.5 Run `./gradlew spotlessApply` and `./gradlew spotlessCheck` after the final edit to any formatter-owned file.

## 5. Integration verification

- [x] 5.1 Run the gateway checks end to end:
      `./gradlew :showcase-api-gateway:check -PskipITs -Pcoverage.gate.enabled=false` (Docker-free), then the full
      `./gradlew :showcase-api-gateway:check` against Docker if available.
- [x] 5.2 Run `./gradlew :showcase-web-ui:e2eTest` and confirm the built UI drives a real `POST …/telemetry` to the
      gateway during a page load (assert it via request interception with a tolerant timeout), alongside the existing
      e2e cases.
- [x] 5.3 Run the `lesson-capture` subagent over this unit's diff, review findings, and change dir; apply the durable
      proposals to `AGENTS.md` and record the applied net `AGENTS.md` delta on this task. Applied four rules (the
      `@Builder.Default`-vs-explicit-`null` and manual-120-check rules merged into the Lombok and Formatting bullets;
      the controller-local-`@ExceptionHandler`-activation and gateway-surface-registration rules added as Gotchas, both
      new subjects), plus the count-free replacement of the stale "Fourteen" tally. Net `AGENTS.md` delta: **+19 lines**
      (32 insertions, 13 deletions).

## Workflow follow-up

- Owner-run live check on a cluster: `./gradlew helmInstallToLocal`, browse the deployed UI, and confirm the Grafana
  "Web UI experience" panels populate from the gateway metrics (the deployment-only signal the local gates cannot show).
- Archive the change only after the implementation is approved and CI is green, then sync the main specs.
