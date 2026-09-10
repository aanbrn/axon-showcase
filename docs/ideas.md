# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file is
a scratchpad, not a backlog of planned work. An idea is removed from the list once implemented (captured by a change);
only open, not-yet-implemented ideas remain.

Changes to this file are committed as their own docs PR (like `AGENTS.md`/`README.md` refresh PRs) — never bundled with
an OpenSpec change or its branch. When an idea graduates into a concrete candidate for work, it may be promoted to a
GitHub issue that links to the eventual OpenSpec change. Ideas are grouped into `## YYYY-MM-DD` sections ordered
newest-first; each idea goes under a section dated when it was added (start a new section for a new day rather than
appending to the most recent one).

## 2026-09-10

- Rethink or rewrite the load tests — parked; no change yet. The current Gatling setup (`load-tests/src/gatling/java`,
  `ShowcaseSimulation`) is a single probabilistic scenario exercising the API gateway (list, then schedule/start/
  finish/remove with decreasing probability) with per-profile pass assertions — it predates the distributed command bus,
  the web UI, and the current architecture and has not kept pace with the system it tests. Revisit the scenario mix
  (include the query service / Protobuf paths, the SSE stream, the web UI), the injection profiles and pass assertions,
  whether load tests should run against the compose stack or the Helm deployment, and how results feed the
  requests/limits baselines (see the resource-sizing idea below).

- Grafana ingress + hostname instead of port-forward — parked; no change yet. The README documents observability access
  via `kubectl port-forward -n monitoring svc/kps-grafana 3000:80`, which is inconvenient. Add an ingress for the
  Grafana service (e.g. hostname `axon-showcase-grafana`) plus a `setup-hosts.sh` entry, mirroring the app's
  `axon-showcase-api`/`axon-showcase-ui` hostnames, so Grafana — and the Tempo data source inside it — is reachable by
  hostname with no port-forward. Check whether the kube-prometheus-stack chart exposes `grafana.ingress` to enable, and
  whether `setup-hosts.sh`'s LoadBalancer-address detection needs a monitoring-namespace case.

## 2026-09-09

- Format YAML files with Prettier (via the root Spotless step, like markdown) — explored, **decided against**; no
  change. Rationale: the Helm chart templates (56 files) are half Go-template logic — a YAML formatter that re-indents
  or reflows `{{ }}` lines can silently change the rendered manifest, and they are already gated by `helm lint`; the
  safe populations (GitHub workflows, `application*.yml`, `docker-compose.yml`, `openspec/config.yaml`) are small,
  hand-consistent, and rarely churn; the `.playwright-mcp/*.yml` snapshots are machine-generated and would need
  excluding. The risk outweighs the payoff for a surface that is not a real pain today — unlike markdown, whose manual
  120-char wrapping drove the Prettier gate. Revisit only if YAML drift actually becomes a pain.

## 2026-09-08

- Make the api-gateway's kafka-client NetworkPolicy label a chart default — parked; no change yet. The gateway consumes
  Kafka for the live-events SSE stream (`showcase-live-events` consumer group), so its pod needs the
  `axon-showcase-kafka-client: "true"` label whenever the kafka NetworkPolicy is active (the bitnami kafka chart allows
  ingress from pods carrying that label). Today the label is only set in `values-local.yaml` per service
  (command/projection and now api-gateway), so a deployment that activates the kafka netpol without that label silently
  loses the live-events stream — the gateway's Kafka consumer can't connect, the SSE stream stays empty, and the web UI
  only reconciles on window focus. Fix: default `apiGateway.podLabels` (and the other kafka consumers) in the chart to
  include `axon-showcase-kafka-client: "true"`, or make the kafka netpol match on the services' stable component labels
  instead of a bespoke per-client label.

## 2026-09-07

- Measure code coverage for the web UI — parked; no change yet. The JVM modules have a JaCoCo coverage gate
  (`jacocoTestCoverageVerification`, baseline in `config/jacoco/coverage-baseline.properties`), but `showcase-web-ui`
  (Vitest) has no coverage measurement. Explore wiring Vitest's built-in `--coverage` (via `@vitest/coverage-v8`) into
  the frontend `check`, and whether a coverage gate (threshold) makes sense for the UI or just a reporting step.

- Client-side (RUM) observability for the web UI — parked; no change yet. The deployable-UI change adds only server-side
  nginx metrics (stub_status + ServiceMonitor); the UI's user-facing experience is still unobserved. A separate UI
  change would add: (1) web-vitals + JS-error reporting (e.g. Grafana Faro or a push-to-gateway metrics endpoint), and
  (2) W3C Trace Context propagation (`traceparent` header) on API calls so the browser's requests join the existing
  Tempo traces from the gateway onward — the highest-leverage piece, since the pipeline already traces gateway →
  command/query. Consider whether the gateway CORS needs to allow the trace header.

- Rethink reconciliation in the web UI — parked; no change yet. `ShowcasesPage` reconciles local writes and
  saga-triggered events against the eventually-consistent read model by waiting on the projected state per event
  (`waitForEvent`/`waitForReadModel`, with a connect-time filter). This works but couples the page to polling; a
  redesign could subscribe the read model itself to the event stream (server-side projection push) or refetch on event
  with a single debounced invalidation instead of one wait per event.

- Rethink resource requests/limits in the Helm chart and the local target — parked; no change yet. The chart's default
  resources are uneven and largely unvalidated: the JVM services default to `requests: 1.0 CPU / 0.5Gi` with
  `limits: 3.0 CPU / 1Gi` (generous, speculative), while the web UI uses `requests: 100m / 64Mi` with
  `limits: memory 128Mi` (no CPU limit), and the local target (`values-local.yaml`) overrides no resources at all.
  Revisit with measured baselines (e.g. from the Gatling load tests / kube-state-metrics) so requests/limits reflect
  real usage, decide on CPU limits (the JVM services set them; the web UI does not), and align defaults across services
  for consistency.

- Fix the per-service compose tasks' "no such service" failure — parked; no change yet. The `docker-conventions`
  per-service `compose*` tasks pass `project.name` (e.g. `showcase-api-gateway`) as the compose service argument, but
  the `docker-compose.yml` services are named differently (`api-gateway`, `command-service`, `query-service`,
  `projection-service`, `web-ui`, `db-events`, ...), so `./gradlew :showcase-api-gateway:composeStop` fails with
  `no such service`. Fix: map each module to its compose service name (or drop the service argument and rely on the
  compose project scope).

- Migrate off the deprecated OpenSearch low-level REST client — parked; no change yet.
  `org.opensearch.client.RestClientBuilder` (and the `RestClient` it builds) is `@Deprecated`, to be removed in future
  releases in favor of the official OpenSearch Java Client. The projection service's
  `openSearchRestClientBuilderCustomizer` bean (`ShowcaseProjectionApplication`) surfaces a `[deprecation]` compile
  warning because Spring Data OpenSearch's `RestClientBuilderCustomizer` contract forces touching the deprecated type to
  configure connection pooling / idle eviction. When Spring Data OpenSearch updates its customizer to the newer client
  (or we migrate the projection/query services to the OpenSearch Java Client transport directly), the warning resolves;
  track so it does not become a hard break when the low-level client is removed.

## 2026-09-04

- Dependency updates for the web UI — parked; explore whether it's possible to check frontend dependency updates the way
  the JVM modules do (`dependencyUpdates`), and whether we can also scan the web UI for dependency vulnerabilities. The
  `dependency-updates` machinery currently only covers catalog-owned Gradle coordinates; the web UI's npm dependencies
  (`package.json` / `package-lock.json`) are outside it. To explore: `npm outdated` / `npm audit` (and
  `npm audit --omit=dev`) as analogues of `dependencyUpdates` / `dependencySecurityCheck`, wired as Gradle tasks or npm
  scripts and reported like the existing update/security issues; whether the observability chart has no `*-image-tag`
  for the UI (it is now a dedicated nginx image serving the built frontend), so a UI image-tag bump would be a manual
  coordinate; and whether Snyk can also scan `package-lock.json` (the existing `dependencySecurityCheck` uses the Snyk
  CLI with the root `.snyk` policy).

- Document the IDE-tooling MCP setup in the README — parked; recommended resolution for the MCP-config question. The
  repo uses several MCPs but only Playwright is in the project config (`.opencode/opencode.json`); the rest live in the
  user's global `opencode.jsonc` and are mostly undocumented: `github` (PRs, CI check status, `gh`), `steroid`
  (IntelliJ/IDEA tooling — `steroid_execute_code` / `runInspectionsDirectly`, used by the `codefmt` flow), and
  `amplicode` (Spring/Amplicode plugin tools). Contributors can't discover these exist or how to enable them. The split
  is correct (auth/IDE-bound tooling belongs in global config — copying it into the project would not enable it without
  the contributor's own setup), so the fix is documentation, not config duplication: a README note covering (1) the
  global GitHub MCP setup (`gh auth login` + the `gh mcp` entry), (2) Steroid/Amplicode for IDEA-based work and which of
  them help with repo-specific problems (Steroid for the Spotless/`codefmt` inspection flow, Amplicode for Spring/Axon
  work), and (3) that Playwright is project-configured for the web-UI e2e.

- Enforce web UI conventions with tooling — parked; do as its own change after `add-web-ui` is merged. Prettier is a
  formatter, not a style linter: it gates formatting (width, quotes, semicolons) but not _conventions_. ESLint
  (correctness) and tsc (types) gate their slices, but two convention areas are currently human-review/AGENTS.md-only:
  (1) **FSD import-direction rules** (slices import only downward; `app/` → `pages/` → `widgets/` → `features/` →
  `entities/` → `shared/`) — enforce with `eslint-plugin-boundaries`, with public-API boundaries per slice; and (2)
  **naming conventions** (e.g. `use*` hooks, `*.test.ts(x)`/`*.spec.ts` suffixes) — via ESLint rules or
  `eslint-plugin-import` naming. Keep it out of the current change to keep the review focused; verify existing code
  conforms (it was built cleanly) and let CI gate it from then on.

## 2026-09-03

- Extend `scripts/setup-idea.sh` for the web module and audit what's stale there. Today the script only covers the
  Java/Gradle side: it installs the `palantir-java-format` + `ktfmt` plugins, copies `config/idea/*.xml` (Java code
  style, ktfmt, codeStyleConfig), and upserts the test-tier naming inspection. It is now incomplete for
  `showcase-web-ui` and should be extended (and its assumptions re-checked): (1) explore whether there is a Prettier
  plugin for IDEA (and if so, install it) so `Reformat Code` matches the web module's `prettier --check` gate — note
  IDEA ships built-in Prettier support that may need enabling rather than a separate plugin; add it into the setup
  script's plugin installs and add a 2-space JS/TS code-style scheme — IntelliJ's default is 4 spaces, which disagrees
  with the web module's `prettier --check` gate (`.prettierrc` uses `tabWidth: 2`), the same "IDE vs build gate" drift
  the script already fixes for Spotless; (2) the JS/TS code style must not collapse to wildcard imports (the Java
  palantir layout sets `ij_java_imports_layout`; the TS side needs the equivalent single-import + on-demand-count
  preference so Prettier never has to expand a wildcard by hand); (3) the inspection-profile upsert is Java-only — the
  web module's Vitest naming (`.test.ts(x)`) has no IDEA inspection yet; (4) audit the script for other stale bits, e.g.
  whether it should also configure the Node plugin / `@/` alias awareness (tsconfig paths are picked up automatically,
  but the npm tasks and 2-space scheme are not), and whether the "IDE must be closed" abort + installPlugins flow still
  holds on current IDEA builds. **Also investigate a durability problem: on reimport of the Gradle project, IDEA
  regenerates the `.idea` directory completely and overwrites the files `setup-idea.sh` added (the committed
  `config/idea/*.xml` copies and the inspection-profile upsert), so the setup is not idempotent across Gradle reimports.
  Find a way to make it survive — e.g. store the config so IDEA's reimport preserves it (shared workspace XML / `*.idea`
  git-tracked scheme files, a `.idea/codeStyles` scheme referenced by name rather than an inline copy, or a re-import
  hook / Gradle task that re-applies the setup), and verify the flow after a fresh Gradle reimport. **Also fix the
  running-IDE ordering: the script aborts before `ensure_project_config` when the IDE is running, so a running IDE
  blocks the config-file install too — even though copying `config/idea/*.xml` and upserting the inspection profile do
  not need the IDE closed (only `installPlugins` does). Split the flow so `ensure_project_config` runs regardless, and
  only the plugin install requires the closed IDE (e.g. warn-and-skip plugin install when running, or re-order to apply
  config first then install plugins).

## 2026-09-02

- Remove the gateway's blocking-execution routing — parked; keep for now. `ShowcaseBlockingExecutionConfigurer`
  (`configureBlockingExecution(__ -> true)`) routes every controller method to a blocking scheduler. It was added in
  `fadc7bc` ("fixed blocking issues using reactor blockhound") as a global workaround, but the controller is fully
  reactive (the only blocking call, `IdentifierFactory.generateIdentifier()`, is already offloaded via
  `subscribeOn(boundedElastic)`). It's a coarse band-aid, not the right fix — the real work is rooting out whatever
  still trips BlockHound in the error/validation path and offloading it surgically, then dropping the global routing
  (and the `@WebFluxTest` configurer-discovery complexity it forces). Split off CORS into `ShowcaseApiConfigurer`.

## 2026-09-01

- Managed-k8s staging for free or cheaply — explored, parked (no change yet). Goal: a managed Kubernetes staging env for
  the chart, ideally free, otherwise as cheap as reasonable. Findings: (1) the two real free-control-plane paths are AKS
  Free (control plane free, but you pay for nodes — not
  $0 ongoing) and OKE (control plane free + 2 Always Free ARM
  nodes = 12 GB, genuinely $0); (2) Oracle halved the
  Always Free Ampere A1 to 2 OCPU / 12 GB in June 2026, so the full stack (~11-12 GiB with the `kps` + `tempo`
  observability stack, 5 services, Kafka, OpenSearch, Postgres) does **not** fit OKE's free budget with observability
  enabled — it fits only if staging trims observability/single-replica/smaller OpenSearch, and images must be ARM
  (`-PimagePlatform=linux/arm64` already supported); (3) there is no free managed k8s that runs the full chart as-is,
  always-on, at $0 — every free path needs either trimming (OKE), paying for nodes (AKS,
  ~$40-80/mo), or accepting
  ephemerality. Reference: `nce/oci-free-cloud-k8s` runs OKE free but on a leaner stack. Open decision: how faithful
  staging must be to the observability stack (the real decider between OKE-free and AKS-paid), and whether always-on vs
  on-demand node-pool stop/start changes the budget.
