## 1. The interval property, on every surface its default lives on

- [x] 1.1 Add a nested `Events` section to `showcase-api-gateway/src/main/java/showcase/api/ShowcaseApiProperties.java`
      holding a `@NotNull @DurationMin(seconds = 1) Duration keepAliveInterval` defaulting to `Duration.ofSeconds(15)`,
      following the shape of the existing `Cors` section; verify with `./gradlew :showcase-api-gateway:compileJava`.
- [x] 1.2 Mirror that default in `showcase-api-gateway/src/main/resources/application.yml` as
      `showcase.api.events.keep-alive-interval: ${SHOWCASE_EVENTS_KEEP_ALIVE_INTERVAL:PT15S}`, using the same value as
      the Java field; verify with the placeholder assertion extended in 1.3.
- [x] 1.3 Extend `showcase-api-gateway/src/componentTest/java/showcase/api/ShowcaseApiPropertiesCT.java` on **every**
      surface it pins: the documented-defaults assertion, the yml-placeholder/env-override assertion, and
      `invalidEnvVars` — adding the new property's out-of-range case and the missing `Cache.expiresAfterWrite` ones (one
      per cache, mirroring the other fields, since it is the only constrained field without one); verify with
      `./gradlew :showcase-api-gateway:componentTest`.
- [x] 1.4 Add `apiGateway.events.keepAliveInterval` (with its `@param` line and the same 15-second default) to
      `helm/chart/src/main/helm/values.yaml` and render `SHOWCASE_EVENTS_KEEP_ALIVE_INTERVAL` in
      `helm/chart/src/main/helm/templates/api-gateway/deployment.yaml` beside the existing tuning environment variables;
      verify with `./gradlew :helm:chart:helmLintMainChartFull` and a `helm template` showing the rendered variable.
- [x] 1.5 Add `BPE_DEFAULT_SHOWCASE_EVENTS_KEEP_ALIVE_INTERVAL=PT15S` to the gateway's `bootBuildImage` `environment`
      map in `showcase-api-gateway/build.gradle.kts`, beside the existing `BPE_DEFAULT_*` entries and using the same
      variable name and value as the yml placeholder — the image ships the default, and a deployment's real environment
      variable still overrides it (`paketo-buildpacks/environment-variables`); verify by building the image
      (`./gradlew :showcase-api-gateway:bootBuildImage`) and confirming the variable is present in the built image's
      launch environment.

## 2. The keep-alive on the stream

- [x] 2.1 Merge a comment-only frame (`ServerSentEvent.builder().comment(...)`) at the configured interval into the
      mapped stream in `showcase-api-gateway/src/main/java/showcase/api/events/ShowcaseEventStreamController.java`,
      leaving the shared `Flux<ShowcaseEventDto>` bean untouched; verify with 2.3.
- [x] 2.2 Register `ShowcaseApiProperties` in the `ShowcaseEventStreamControllerCT` slice with
      `@EnableConfigurationProperties(ShowcaseApiProperties.class)` — the pattern `ShowcaseRestControllerCT` already
      uses — because the slice does not load the application's own property registration and the controller now needs
      the interval; verify the slice starts with `./gradlew :showcase-api-gateway:componentTest`.
- [x] 2.3 Add a unit test (`ShowcaseEventStreamControllerTests`) asserting an idle stream emits a keep-alive: construct
      the controller with a non-emitting source (`Flux.never()`) and a short interval, and assert the first frame
      carries the comment and no event data; verify with `./gradlew :showcase-api-gateway:test`. Testing at the unit
      tier rather than through the `@WebFluxTest` slice was a deliberate correction: an SSE exchange never completes, so
      the slice version left it open and broke 15 unrelated `ShowcaseRestControllerCT` cases (componentTest 15/76
      failing with it, 108/108 without). The existing `GET /events` assertion needs no change —
      `ServerSentEventHttpMessageReader` drops comment-only frames for a non-`ServerSentEvent` target, so a comment
      never reaches its `String` body.

## 3. Deployment: document and verify, do not default

- [x] 3.1 Record the durable fact in `AGENTS.md`'s Kubernetes Deployment section: the live stream's continuity depends
      on the keep-alive interval staying below the ingress's read timeout; the annotation is controller-specific
      (Traefik on colima, ingress-nginx on kind/minikube), the chart sets no default, and the existing
      `apiGateway.annotations` value carries one when needed; verify with `./gradlew spotlessCheck`.
- [x] 3.2 Run the live check the parked idea asked for: `./gradlew helmInstallToLocal`, then hold an SSE connection open
      through the ingress longer than a quiet period and confirm it receives keep-alives and is not closed — answering
      "does an idle stream survive the deployed proxy?". Ran the full stack (all 8 releases) with the Gradle helm flow
      pointed at a loopback copy of the kubeconfig, because the host's Warp-spawned shell cannot reach the VM's IP (see
      the `AGENTS.md` macOS gotcha). Against the deployed gateway (`SHOWCASE_EVENTS_KEEP_ALIVE_INTERVAL=PT15S` in the
      running Deployment), an idle SSE client through the `axon-showcase-api` ingress received three `:keep-alive`
      frames across consecutive intervals over an otherwise silent stream, the connection only ending when the client
      cut it — so the stream survives the ingress and no read-timeout annotation was needed.
- [x] 3.3 Extend the configuration-defaults gotcha in `AGENTS.md` — the bullet requiring a default change to name every
      assertion that pins the value, "on each surface it is declared" — with the image surface: a service's
      `bootBuildImage` `BPE_DEFAULT_*` map sets that variable's launch-environment default, as
      `SHOWCASE_CORS_ALLOWED_ORIGINS` does, and ADR-0002's enumeration did not mention it until task 3.4 completed it.
      Not every property is carried there — the query caches are not — so the clause should say a variable present in
      that map has a fourth default to keep in step; verify with `./gradlew spotlessCheck`.
- [x] 3.4 Correct `docs/adr/0002-configuration-property-defaults-owned-by-java.md`: its enumeration of where a default
      is declared names the Java field, the yml placeholder and the chart values, but a variable present in a service's
      `bootBuildImage` `BPE_DEFAULT_*` map has a fourth — the image's launch-environment default — so the Context
      listing and the Consequences sentence about keeping surfaces in step both need it; leave the Decision and Status
      unchanged, recording it in the change's report as completing the enumeration rather than superseding it; verify
      with `./gradlew spotlessCheck` and by re-reading both passages.

## 4. Housekeeping and gates

- [x] 4.1 Check whether `README.md` needs a change — a keep-alive is not user-visible, so the expected answer is no —
      and record the answer in the change's report rather than leaving it unexamined.
- [x] 4.2 Remove this change's idea from `docs/ideas.md` (the 2026-09-16 idle-stream entry) so it rides this change's
      branch; verify the file no longer carries that entry.
- [x] 4.3 Run the gates: `./gradlew :showcase-api-gateway:check -PskipITs -Pcoverage.gate.enabled=false`,
      `./gradlew :helm:chart:helmLintMainChartFull`, and `openspec validate --all`; verify each is green before
      reporting the change done.
