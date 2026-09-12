# Ideas

Short notes to remember emerging development ideas. An idea becomes an OpenSpec change only when acted on — this file is
a scratchpad, not a backlog of planned work. An idea is removed from the list once implemented (captured by a change) or
once explored and decided against (the durable lesson is captured in `AGENTS.md`/an ADR instead); only open,
not-yet-implemented ideas remain.

Changes to this file travel with the change that owns them: an idea's **removal** ships in the implementing change's PR
— committed on its branch, and not left in the uncommitted review diff (a rebase that stashes an `ideas.md` edit can
conflict with a `main` that also edited the file) — while a **newly parked** idea that is not yet a change is committed
as its own docs PR (like `AGENTS.md`/`README.md` refresh PRs). When an idea graduates into a concrete candidate for
work, it may be promoted to a GitHub issue that links to the eventual OpenSpec change. Ideas are grouped into
`## YYYY-MM-DD` sections ordered newest-first; each idea goes under a section dated when it was added (start a new
section for a new day rather than appending to the most recent one).

## 2026-09-12

- Scheduled AGENTS.md audit — parked; no change yet. The `agents-auditor` subagent audits `AGENTS.md` on demand; a
  zero-touch periodic variant would run it unattended. The OpenCode GitHub action supports `on: schedule`, which —
  unlike a comment trigger — has no comment to read, so it requires a `prompt` input (see its docs' "Schedule Example").
  A weekly `.github/workflows/agents-audit.yml` (`schedule:` + `workflow_dispatch:`) could run
  `anomalyco/opencode/github@latest` with the existing `OPENCODE_API_KEY` secret and a prompt to audit `AGENTS.md` and
  open or update an issue with the findings — mirroring `dependency-updates.yml`. It needs `id-token: write` (the action
  authenticates to the OpenCode GitHub App via OIDC, as `opencode.yml` does), `contents: read` for the checkout, and
  `issues: write` to post the findings; per the docs, a scheduled run has no user context to permission-check, so every
  write it performs must be granted explicitly. Confirm the scheduled `prompt` path works before relying on it.

## 2026-09-11

- Snyk CLI update check — parked; no change yet. The `snyk-version` pin in `.github/workflows/snyk.yml` is outside every
  update-check workflow (`dependencyUpdates` covers Gradle coordinates, `helmUpdates` covers the Helm CLI and charts,
  and Dependabot manages action refs but not the `snyk-version` input), so it goes stale silently and can only be
  confirmed by hand against `gh api repos/snyk/cli/releases/latest`. Mirror the `helmUpdates` pattern: a task/workflow
  that queries `snyk/cli` releases and opens or updates an issue when the pin lags. A local bump cannot be fully
  verified anyway (actionlint only lints the YAML; the credentialed weekly run is the first real execution), so the
  check is worth automating rather than relying on manual audits.

- OpenSpec CLI update check — parked; no change yet. The CLI is pinned in CI as
  `npm install --global @fission-ai/openspec@1.11.0` (`.github/workflows/ci.yml`), so releases go stale silently (1.13.0
  is already out) because the pin sits outside every update check (`dependencyUpdates` covers Gradle catalog
  coordinates, `helmUpdates` the Helm CLI and charts, `buildpackUpdates` the Paketo builder and buildpacks, and
  Dependabot only action refs). Add an `openspecUpdates` Gradle task mirroring `buildpackUpdates` — query the npm
  registry for `@fission-ai/openspec`'s latest version, compare with the pinned one, report — plus a weekly
  `openspec-updates.yml` workflow opening or updating an issue. It pairs with the existing `/opsx-tool-update` command,
  which can act on a release but does not detect one; decide where the pin is single-sourced (it lives in the workflow
  today — the `snyk-version` case above has the same shape).

- Tooling-currency checks: unify the mechanism and cover the `pack` pin — parked; no change yet. The repo has three
  update checks of near-identical shape (`dependencyUpdates` via the gradle-versions plugin; `helmUpdates` and
  `buildpackUpdates` as `build-logic` tasks feeding a workflow that opens or updates an issue), two parked ideas for two
  more (the Snyk and OpenSpec CLI pins above, each proposing the same task-plus-workflow shape), and one more uncovered
  pin with no idea yet — `pack-version` in `.github/workflows/e2e.yml` (`buildpackUpdates` covers the Paketo builder and
  buildpacks, not the `pack` CLI). Rather than adding a fourth and fifth near-duplicate task, consider one parameterized
  mechanism: a declared list of pinned tools with their current-version source (Gradle catalog, Helm CLI/charts, Docker
  Hub, npm, GitHub releases) driving a single report and update workflow — shrinking the AGENTS.md manual-pin audit list
  to the tools it cannot reach.

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

- Enforce web UI conventions with tooling — parked; do as its own change after `add-web-ui` is merged. Prettier is a
  formatter, not a style linter: it gates formatting (width, quotes, semicolons) but not _conventions_. ESLint
  (correctness) and tsc (types) gate their slices, but two convention areas are currently human-review/AGENTS.md-only:
  (1) **FSD import-direction rules** (slices import only downward; `app/` → `pages/` → `widgets/` → `features/` →
  `entities/` → `shared/`) — enforce with `eslint-plugin-boundaries`, with public-API boundaries per slice; and (2)
  **naming conventions** (e.g. `use*` hooks, `*.test.ts(x)`/`*.spec.ts` suffixes) — via ESLint rules or
  `eslint-plugin-import` naming. Keep it out of the current change to keep the review focused; verify existing code
  conforms (it was built cleanly) and let CI gate it from then on.

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
  Free (control plane free, but you pay for nodes — not \$0 ongoing) and OKE (control plane free + 2 Always Free ARM
  nodes = 12 GB, genuinely \$0); (2) Oracle halved the Always Free Ampere A1 to 2 OCPU / 12 GB in June 2026, so the full
  stack (~11-12 GiB with the `kps` + `tempo` observability stack, 5 services, Kafka, OpenSearch, Postgres) does **not**
  fit OKE's free budget with observability enabled — it fits only if staging trims observability/single-replica/smaller
  OpenSearch, and images must be ARM (`-PimagePlatform=linux/arm64` already supported); (3) there is no free managed k8s
  that runs the full chart as-is, always-on, at \$0 — every free path needs either trimming (OKE), paying for nodes
  (AKS, ~\$40-80/mo), or accepting ephemerality. Reference: `nce/oci-free-cloud-k8s` runs OKE free but on a leaner
  stack. Open decision: how faithful staging must be to the observability stack (the real decider between OKE-free and
  AKS-paid), and whether always-on vs on-demand node-pool stop/start changes the budget.
