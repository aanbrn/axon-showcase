## 1. Add the frontend build infrastructure

- [x] 1.1 Add a pinned `node` version to `gradle/libs.versions.toml` and the node-gradle plugin coordinate.
- [x] 1.2 Create the `frontend-conventions` build-logic convention plugin applying the node-gradle plugin (pinned Node,
  `npm ci` wired to dependency setup, `npm run build` producing `dist/`).
- [x] 1.3 Create the `showcase-web-ui` module directory with `build.gradle.kts` applying `frontend-conventions`, and add
  it to `settings.gradle.kts`.

## 2. Scaffold the React application

- [x] 2.1 Add `package.json` (React + Vite + TypeScript), `package-lock.json`, `tsconfig.json`, Vite config, and
  ESLint config in `showcase-web-ui`.
- [x] 2.2 Add the app entry (`index.html`, `main.tsx`) and a minimal component tree for the showcase list, create form,
  and per-showcase timeline.
- [x] 2.3 Wire frontend `lint`, `format:check` (Prettier), and `test` (Vitest) into the module's `check` via the
  convention plugin. The production bundle (`npm run build`) is produced by `build`/`assemble`, not `check` — mirroring
  the JVM modules' `bootJar`. Frontend tasks are grouped like the Java modules (`build`/`verification`/`application`)
  rather than the node-gradle default `Npm tasks` group.

## 3. Restructure the gateway by surface

Restructure the gateway so each HTTP surface owns its package, before adding the new live-event feature on top:
`showcase.api` (app entry point + app-wide config), `showcase.api.rest` (the REST surface:
`ShowcaseRestController`/`ShowcaseRestApi`, request/response DTOs, `ShowcaseRestConfiguration` for the query caches,
`ShowcaseApiErrorResolver`), and `showcase.api.events` (the live event stream, added in the next section). Made the
cross-package DTOs and properties classes `public` as needed.

- [x] 3.1 Restructure the gateway into packages by surface: move the REST controller/API/DTOs and query caches into
  `showcase.api.rest`, keep the app entry point and app-wide config in `showcase.api`, and make the cross-package
  DTOs and properties classes `public`.
- [x] 3.2 Move the existing `ShowcaseApiConfigurer` (which handles only blocking-execution routing at this point) into
  `showcase.api.rest`, renaming it `ShowcaseBlockingExecutionConfigurer` — so the `@WebFluxTest` slice discovers it
  from its base package instead of scanning the top-level package. The app-wide CORS configurer is added separately
  in section 5.

## 4. Expose live events from the gateway

The live-event classes live in the `showcase.api.events` package established above: the SSE controller and its
OpenAPI interface, the Kafka consumer configuration, the SSE DTO, and the MapStruct event mapper.

- [x] 4.1 Add `axon-kafka` and `reactor-kafka` dependencies to the gateway (both already in the catalog), with the
  projection-style `KafkaMessageConverter` bean wired to the Axon event serializer.
- [x] 4.2 Add a Kafka consumer bean in `showcase.api.events` (a `KafkaReceiver` subscribing to `axon-showcase-events`
  with a consumer group distinct from the projection's) that decodes and exposes domain events as a reactive `Flux`
  (eagerly subscribed, replay-buffered so late SSE clients still receive events).
- [x] 4.3 Add the `GET /events` SSE endpoint in `showcase.api.events` (produces `text/event-stream`) publishing the
  consumer's event Flux, with reconnection on transient Kafka failures.
- [x] 4.4 Add tests for the SSE endpoint: a unit test verifying the controller maps events to named Server-Sent
  Event frames with their type and showcase identity preserved, a component test (`@WebFluxTest` slice) verifying
  the HTTP contract (`/events`, `text/event-stream` content type, JSON payload), an integration test
  (`ShowcaseLiveEventStreamIT`) that publishes a real domain event to a Kafka Testcontainer and verifies it is
  consumed and delivered over SSE, and an end-to-end test (`ShowcaseApiGatewayE2E`) that schedules a real showcase
  through the full pipeline and verifies the `SCHEDULED` event arrives over SSE. The component test's
  `TestConfiguration` is excluded from `ShowcaseRestControllerCT`'s component scan so the two slices do not collide.
- [x] 4.5 Add `BPE_DEFAULT_KAFKA_BOOTSTRAP_SERVERS=axon-showcase-kafka:9092` to the gateway's `bootBuildImage` env
  (surfaced by the new e2e test: the live-events consumer fell back to `localhost:9092` and never received events).

## 5. Configure CORS for the UI origin

- [x] 5.1 Add a new app-wide CORS `WebFluxConfigurer` (or `CorsWebFilter`) in `showcase.api` with an allow-list of
  origins from configuration, applying to both the REST and SSE endpoints. The container image default is empty
  (fail-closed — deployments must allow their UI origin explicitly); local dev (docker-compose, `bootRun`, and the
  Helm `values-local.yaml`) allows the Vite dev and preview origins, and the Helm chart exposes it as
  `apiGateway.cors.allowedOrigins`.
- [x] 5.2 Add a component test verifying the CORS origins default and env-var override bind correctly.

## 6. Build the UI features

- [x] 6.1 Implement the showcase list view rendering title, status, and read-model timestamps, refreshing without a full
  reload.
- [x] 6.2 Implement the create-showcase form (title, start time, duration) calling the gateway schedule endpoint and
  surfacing validation errors.
- [x] 6.3 Implement start/finish/remove actions calling the corresponding gateway endpoints.
- [x] 6.4 Implement the per-showcase history timeline from `scheduledAt`/`startedAt`/`finishedAt`.
- [x] 6.5 Implement the SSE client (`EventSource` over `/events`) appending live events to the matching timeline, with
  reconnect on disconnect.
- [x] 6.6 Add unit tests (Vitest) for the API client and the timeline/SSE merging logic.
- [x] 6.7 Add Playwright end-to-end tests for the web UI (`e2e/`, run via `./gradlew :showcase-web-ui:e2eTest`), which
  boot the full pipeline via docker-compose, serve the built UI with Vite preview, and drive the browser: create →
  appears in the list, start → STARTED, saga auto-start reflected over SSE, live events appended to the timeline, and a
  duplicate title surfacing the gateway error. Requires `@playwright/test`, `playwright.config.ts`, and a Vite preview
  `webServer`; the gateway's CORS allow-list includes the preview origin (`http://localhost:4173`) so the browser can
  call it cross-origin.

## 7. Verify and document

- [x] 7.1 Run the full local stack (infra + services + UI dev server) and manually verify: create a showcase, watch its
  lifecycle events arrive live over SSE, and confirm the history timeline matches the read model.
- [x] 7.2 Update `README.md` and `AGENTS.md` with the UI module, how to run it locally, and the new gateway `/events`
  endpoint.