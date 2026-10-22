---
name: deployment-operations
description:
  Deployment, container-image, local-stack, and environment/port guidance for this repository — Helm releases and
  values, image and buildpack pins, Docker Compose and `bootRun`, the deployed-system probes, and the environment
  variables the surfaces must agree on.
---

# Deployment operations

Source: `AGENTS.md`'s `## Docker Images`, `## Kubernetes Deployment`, `## Local Development`,
`## Key Environment Variables`, and `### Deployment, Helm & Kubernetes`, plus the Helm update-check bullet and the
DB-scripts block from `## Build & Test`.

## Docker images

Each boot service builds a Docker image:

- `aanbrn/axon-showcase-command-service:${project.version}`
- `aanbrn/axon-showcase-api-gateway:${project.version}`
- `aanbrn/axon-showcase-query-service:${project.version}`
- `aanbrn/axon-showcase-projection-service:${project.version}`
- `aanbrn/axon-showcase-web-ui:${project.version}` (static nginx serving the built frontend)

On a release, `.github/workflows/release.yml` publishes the five images to the GitHub Container Registry
(`ghcr.io/<owner>/axon-showcase-*`, tagged `<version>` and `latest`, `linux/amd64`) — see ADR-0018 — and the Helm chart
to `oci://ghcr.io/<owner>/charts/axon-showcase` — see ADR-0019 — while a `publish_tag` dispatch backfills an existing
release's `<version>` image (no `latest`, no chart), and the local and `ci` targets keep building the images into the
local daemon. Each service's `image.registry` defaults to `ghcr.io`; the local and `ci` values set it empty, and the
global registry stays empty so the metrics-exporter sidecar keeps Docker Hub.

Image names are set in each service's `bootBuildImage` task configuration. To build for a non-default platform (e.g.,
ARM64 host), pass `-PimagePlatform=linux/amd64` (or `--imagePlatform=linux/amd64`), which Gradle maps to the
`bootBuildImage`/`dockerBuildImage` task's `imagePlatform` `@Option`.

The web UI image is built differently: `frontend-conventions` registers a generic `dockerBuildImage` task (typed as
`PackBuildImageTask`) that runs the `pack` CLI with the **version-pinned** Paketo NGINX + Procfile buildpacks
(`paketo-buildpacks/nginx@1.3.0`, `paketo-buildpacks/procfile@5.15.4`; the versions are catalog-owned as `paketo-nginx`
and `paketo-procfile`) over `build/dist` (the `pack` CLI is a build prerequisite like Helm/Snyk). The pins are explicit
because an unversioned buildpack reference becomes ambiguous — `pack` fails with "multiple versions … must specify an
explicit version" — once the builder bundles two versions of a buildpack (the intermittent `e2e`/`helmInstallToLocal`
failure). The builder itself is also pinned (`builder-jammy-base:0.4.662`, catalog-owned as `paketo-builder-jammy-base`)
rather than floating, and the `buildpackUpdates` task / `buildpack-updates` workflow reports newer builder and buildpack
versions — no other update check covers Paketo. **The builder and the buildpacks it bundles must be bumped together:** a
`paketo-nginx` pin that is _absent_ from the builder (e.g. holding a buildpack one line behind the builder) makes `pack`
add it from the registry and resolve the added buildpackage to an **arm64** slice even though the builder is published
`linux/amd64` only — the mismatched pair fails with `exec: nginx: not found` where the matched pair serves a proper
x86-64 nginx; the full mechanics and the `address-2026-09-21-update-checks` A/B are in the buildpack-pin gotcha below.
An earlier record held `paketo-nginx` back at `1.2.0` because `1.2.1` "does not work" —
`address-2026-09-21-update-checks` showed that reading was imprecise and took both bumps together. Reported upstream as
a comment on `paketo-buildpacks/nginx#1340` (2026-09-22): whether the added buildpackage should resolve to the target
platform's slice is open upstream. Close-out: re-take a mismatched pairing only once that upstream resolution lands
(then re-test the built image), or if `pack` changes how it resolves an added buildpackage's slice — the rule above
(bump the builder and its bundled buildpacks together) stays the default either way. Unlike the builder, the run image
(`paketobuildpacks/run-jammy-base:latest`) is deliberately left floating so base-OS security — do not "complete" the pin
by freezing it. The image serves the bundle via nginx on `8080` and exposes nginx `stub_status` metrics on `9090`
(`BP_NGINX_STUB_STATUS_PORT`); the Helm deployment's gated `nginx-prometheus-exporter` sidecar (`webUi.metricsExporter`,
on by default when observability metrics export and the web UI ServiceMonitor are enabled) converts stub_status to
Prometheus `/metrics` on port `9113`, which the Service's `http-metrics` port and the ServiceMonitor scrape — the chain
is specified in the `deployment/web-ui` spec. A `PackBuildImageTask` convention defaults the image name to
`${project.name}:${project.version}`, which the web UI module overrides with the deployable
`aanbrn/axon-showcase-web-ui:${project.version}` in `showcase-web-ui/build.gradle.kts`. **A Docker 29 daemon's store is
not by itself the trigger for this build's failure — do not read the driver as the cause:** the failure
(`failed to fetch base layers: open /tmp/imgutil.local.image.*: no such file or directory`) reproduced on the GitHub
runner's Docker 29.4.2 (`ubuntu-26.04`) while **the same build succeeds on Docker 29.5.2 with the containerd `overlayfs`
snapshotter locally** (`io.containerd.snapshotter.v1`), so the driver alone does not explain it and the runner's Docker
setup is part of the trigger. The `daemon.json` `{"storage-driver":"overlay2"}` step is a documented workaround the
runner needs — the deployment smoke sets it for that reason, and that step is retired when `buildpacks/pack#2527` (the
owning venue; `spring-projects/spring-boot#49251` is the downstream thread) fixes the store. `SHOWCASE_API_BASE_URL` —
**no baked default** (compose sets `http://localhost:8080`; the chart's `webUi.apiBaseUrl` defaults to empty) — has no
ConfigMap or volume mount. The runtime contract (the `start.sh` render into `/workspace/config.js` and the fail-fast on
an unset value) is specified in the `deployment/web-ui` spec. The `dockerBuildImage` run prints two informational
warnings from the toolchain, not defects: "Exporting to docker daemon (building without --publish) and daemon uses
containerd storage" (pack exports to the local daemon's containerd store, losing the fast publish path) and "deprecated
usage of stack" (an upstream Paketo buildpack still declares the deprecated `stacks` key instead of `targets`). Neither
is actionable in the build — ignore them. (The first warning names the store the runner's Docker 29 reports, the context
the constraint above is scoped to — so it is a signal about the daemon, not a defect to chase in the repo.)

Similarly, a CNB-built image's timestamps are not host state: Cloud Native Buildpacks stamp buildpack layers with a
fixed past date — they list as `Jan 1 1980` — so builds are reproducible and layer caching stays stable, and this build
passes no `--creation-time`, so the image's creation date reads correspondingly old. Do not read those timestamps, or
the image's age, as evidence about when it was built, which host built it, or whether an image store was re-imported.

## Kubernetes deployment

```bash
# Deploy to local cluster (must be ordered)
helm install kps prometheus-community/kube-prometheus-stack --version 92.11.0 \
  --namespace monitoring --create-namespace --wait
helm install tempo grafana/tempo --version 1.24.4 --namespace monitoring --create-namespace --wait
helm install axon-showcase-db-events bitnami/postgresql --version 16.7.27 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase-kafka bitnami/kafka --version 31.5.0 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase-os-views bitnami/opensearch --version 2.0.10 \
  --namespace axon-showcase --create-namespace --wait
helm install axon-showcase helm/chart/build/helm/charts/axon-showcase \
  -f helm/values/axon-showcase/values-local.yaml \
  --namespace axon-showcase --create-namespace --wait

# Or use Gradle Helm plugin (builds images, then installs all releases to the local target)
./gradlew helmInstallToLocal
```

Per-release install/uninstall tasks follow `helmInstall<Release>To<Target>` / `helmUninstall<Release>From<Target>`, e.g.
`helmInstallKpsToLocal` and `helmUninstallKpsFromLocal` (to install/verify a single chart without building images — the
app-release install task additionally depends on the four `bootBuildImage` tasks and the web UI `dockerBuildImage`).
Uninstalling leaves `createNamespace` namespaces (`monitoring`, `axon-showcase`) behind; remove them with
`kubectl delete namespace` afterwards.

**Re-applying an infrastructure release's local values wipes its data and leaves the app unusable until the app release
is upgraded too.** Every `helm/values/axon-showcase-*/values-local.yaml` sets `persistence.enabled: false`, so the
values change restarts PostgreSQL/Kafka/OpenSearch against an empty store, and the app chart's schema-creating hooks
(`db-migration`, `index-initialization`, both `pre-install,pre-upgrade`) run only on an app-release upgrade — follow the
infra upgrades with `helmInstallAxonShowcaseToLocal`, or the gateway answers 503 against the missing schema/index.
Verify a resource sizing change under load (`./scripts/load-test-baseline.sh`: 0 failed, no `OOMKilled`/restart spikes),
since an idle install cannot OOM a limit trimmed toward the working set. captured: right-size-infra-resources

**A chart Deployment addition must extend the app release's `installDependsOn` in `build.gradle.kts`.** The list must
enumerate an image-build task for every image the chart's Deployments reference — `ship-web-ui-as-deployable` added the
web UI Deployment without adding its image build, so `helmInstallToLocal` deployed a web UI pod with an image that was
never built. Note the web UI image is built by `:showcase-web-ui:dockerBuildImage` (a pack-based task, not a
`bootBuildImage`); verify the graph with `./gradlew helmInstallToLocal --dry-run` and confirm every chart Deployment's
image has a build task in it. The local and `ci` values set the chart's image registry empty (see the Docker Images note
above), so those targets use the locally-built daemon images, and a `kind` cluster's nodes cannot see the host daemon's
images, so a runner or bare-kind install must build them and `kind load` each before `helmInstallToLocal` — load by
repository rather than pinning the image version, and pass `--name <cluster>` to `kind load`, since kind's CLI defaults
to a cluster named `kind`: a bare `kind load docker-image` against the smoke's `axon-showcase-smoke` failed its first
dispatch with `ERROR: no nodes found for cluster "kind"`. captured: add-deployment-smoke captured:
trim-deployment-smoke-install

**Helm release order**: kps → tempo → db-events/kafka/os-views → axon-showcase, declared by `mustInstallAfter`/
`mustUninstallAfter` in `build.gradle.kts`. Uninstall in reverse. Because the app release also depends on the five image
builds, `helmInstallToLocal` is whole-stack — it builds all five images and installs all six of the releases a person
runs locally (the ingress controller is `ci`-only). The `ci` target is the deliberate exception for the deployment
smoke: it selects the application, infrastructure, and ingress releases (no observability), points at the runner's fixed
kube context, and the releases it installs carry the trimmed `values-ci.yaml` files the smoke needs to fit its runner
(`merge-governance`'s deployment-smoke requirement); the local path stays the one the docs give a person. Fitting a
runner is a **requests** concern — the scheduler places on requests — so a trim lowers the requests and leaves the
memory **limits** alone: a JVM's heap is a fraction of its limit, and cutting the limit toward the runner starves the
heap under load. captured: add-deployment-smoke captured: fix-the-deployment-smoke-503s

**Helm release namespaces**: declared in `build.gradle.kts` — the observability releases (kps, tempo) deploy into the
`monitoring` namespace, the ingress controller (`ingress-nginx`, selected only by `ci`) into its own `ingress-nginx`
namespace, and the application and infrastructure releases (db-events, kafka, os-views, axon-showcase) deploy into a
dedicated `axon-showcase` namespace (created on install). The local deployment does not depend on the kube context's
current namespace or a `helm.namespace` gradle property.

**Helm release target kube contexts**: each release target declares the kube context it deploys to in
`build.gradle.kts`. The `local` target resolves its context per-machine from the `helm.local.kubeContext` Gradle
property (set in `~/.gradle/gradle.properties` or via `-P`), falling back to the developer's current kube context when
unset — so macOS (colima) and Linux (kind/minikube) contributors each deploy to their own local cluster without a
hard-coded context name in the repo. The `ci` target declares the fixed `kind-axon-showcase-smoke` context that the
deployment smoke's throwaway cluster is created under — a comment on each side names the other as the coupling's other
half. Do not hard-code a machine-specific local context name (like `colima`) in the versioned build.

**Chart validation**: the chart is linted as part of packaging (`helmPackageMainChart` → `helmLintMainChart`), and
`check` depends on that packaging — the members the `helm-chart` requirement names — so a chart that cannot bundle its
declared dependency fails the pull request rather than only in the nightly deployment smoke's install. The Bitnami
`common` dependency is fetched by the plugin's `helmUpdateMainChartDependencies` task — which its own docs equate with
`helm dependency update` — and that task's `onlyIf` resolves false when the configuration cache stores its entry before
the chart sources are collected, i.e. on clean checkouts and in CI, so `helm/chart/build.gradle.kts` replaces the gate
with `setOnlyIf { true }`: without it nothing fetches on a clean checkout, the strict lint fails with "chart directory
is missing these dependencies: common", and the deployment smoke fails at the install with "found in Chart.yaml, but
missing in charts/ directory: common". Measured on one commit with only the cache toggled: the task reports SKIPPED with
the cache on and fetches with `--no-configuration-cache`. The upstream gap is parked in `docs/ideas.md`. Lint runs
strict (warnings are errors) and lints the Bitnami `common` subchart, rendering two extra value sets:

```bash
./gradlew :helm:chart:helmLintMainChartFull :helm:chart:helmLintMainChartMinimal
```

Value files live in `helm/chart/src/test/helm/` (`helm/chart/src/test/helm/helm-lint-full.yaml` enables all optional
features, `helm/chart/src/test/helm/helm-lint-minimal.yaml` disables the default-on ones).

Custom values can be placed in `helm/values/<release-name>/values-<target>.yaml` — the plugin applies a release's file
only to the target it names, so `values-local.yaml` is the local target's and `values-ci.yaml` the deployment smoke's. A
target that installs the app release must set `webUi.apiBaseUrl` in its own file: the chart defaults it to empty and the
web UI image fails fast on that (see the Docker Images note), so the UI pod crash-loops and the release's `wait = true`
turns it into an install timeout — a target inherits nothing from another target's values. captured:
trim-deployment-smoke-install

**The `*-client` pod labels belong to the targets that set `allowExternal: false` — keep them in those targets' values,
never in the chart.** They sit in `helm/values/axon-showcase/values-local.yaml` for the local target and
`helm/values/axon-showcase/values-ci.yaml` for the deployment smoke's. The labels (`axon-showcase-kafka-client`,
`axon-showcase-db-events-client`, `axon-showcase-os-views-client`) exist only because those targets set
`allowExternal: false` on the bitnami infra charts (their netpols admit pods carrying the `<release>-client` label), and
their values derive from the release names (declared in `build.gradle.kts`). Moving them into the reusable app chart
would couple it to (a) the bitnami netpol convention, (b) the release names, and (c) a netpol-strictness decision the
chart cannot observe — a deployment that leaves the bitnami netpols open would carry labels nothing consumes. A
`*-client` label with no matching infra release is dead weight (the removed `axon-showcase-redis-client` was a leftover
from an earlier design — nothing in the stack or code referenced redis). This was explored as a chart-default change and
reverted; the labels belong co-located with the netpol restrictions that require them.

The local values expose the API gateway, the web UI, and Grafana via ingress at the hostnames `axon-showcase-api`,
`axon-showcase-ui`, and `axon-showcase-grafana`. To reach them by hostname (instead of a `Host:`-header curl
workaround), run `./setup-hosts.sh setup`, which detects the local cluster's ingress-controller LoadBalancer address
generically (against the current kube context, so it works on colima + Traefik, kind/minikube + ingress-nginx, etc.) and
manages the `/etc/hosts` entries (`./setup-hosts.sh remove` to clean up; re-run `setup` if the address changes on
cluster restart).

**The live event stream's continuity depends on the ingress read timeout.** The gateway emits an SSE keep-alive at
`apiGateway.events.keepAliveInterval` (default `PT15S`), so a quiet period still carries bytes; keep that interval below
the ingress controller's read timeout. The chart sets no read-timeout annotation, because the annotation name is
controller-specific (Traefik on colima, ingress-nginx on kind/minikube) — a deployment that needs one sets it through
the existing `apiGateway.annotations` value.

## Local development

```bash
# Infrastructure
docker compose up -d

# Run services individually (each on separate port)
./gradlew :showcase-api-gateway:bootRun        # :8080
./gradlew :showcase-command-service:bootRun     # :8081
./gradlew :showcase-query-service:bootRun       # :8083 (gRPC :9090)
./gradlew :showcase-projection-service:bootRun  # :8082

# Web UI (Vite dev server, proxies /showcases, /events, and /telemetry to :8080)
./gradlew :showcase-web-ui:viteDev              # :5173
```

Docker Compose (`docker-compose.yml`) starts all infrastructure **and** the Java services (using pre-built Docker images
`aanbrn/axon-showcase-*:${PROJECT_VERSION}`). Build the images first (`./gradlew bootBuildImage` for the JVM services,
`./gradlew :showcase-web-ui:dockerBuildImage` for the UI); `PROJECT_VERSION` resolves the image tags and must equal the
version the images were built with — the Gradle compose tasks set it automatically, a raw `docker compose up -d` needs
it set explicitly. To run services from source instead, use `bootRun` as shown above. The compose stack also runs the
deployed web UI at `http://localhost:8084` (its image is the nginx-built
`aanbrn/axon-showcase-web-ui:${PROJECT_VERSION}`); the Vite dev server (`viteDev`) remains the hot-reload alternative
for UI development.

**Ports:** the HTTP ports (`server.port` in each service's `application.yml`) are the API Gateway `8080`, Command
Service `8081`, Query Service `8083`, Projection Service `8082`. The query service **additionally** serves the generic
gRPC query transport on `9090` (`grpc.server.port`); its `server.port` (`8083`) now serves only the actuator, since the
query service has no HTTP API. In `docker-compose.yml`, the published `8000`–`8003` mappings are **JVM debug ports**
(`BPL_DEBUG_PORT`), not the services' HTTP ports — only the API Gateway publishes its HTTP port (`8080`); the other
services' HTTP ports are reachable only via the Docker network or `bootRun`. The web UI is published on `8084` (its
container nginx port is `8080`; `stub_status` metrics on `9090`).

The `docker-conventions` plugin adds `compose*` Gradle tasks that wrap Docker Compose and set `PROJECT_VERSION` + image
versions automatically (also `composeBuildAndUp`, `composeBuildAndRestart`): `./gradlew composeUp`,
`./gradlew composeDown` address the whole project, and the four service modules carry the same tasks scoped to their own
compose service — `./gradlew :showcase-api-gateway:composeUp` brings up `api-gateway` with its Compose dependency graph,
and `./gradlew :showcase-api-gateway:composeDown` stops and removes just that container. A compose task runs only when
it is explicitly requested on the command line (standalone `./gradlew composeUp` — a leading `:` from the IDE, e.g.
`:composeUp`, is tolerated) or when a scheduled task needs it as a dependency or finalizer — the web UI `e2eTest` boots
the stack via `composeBuildAndUp` and tears it down via `composeDown`. Broad builds that do not schedule a compose task
never start/stop containers as a side effect. `composeBuildAndUp` starts the stack with `docker compose up -d` (no
`--wait` — a one-shot `kafka-init` container would trip `--wait`'s health check), so the web UI e2e suite polls the
gateway's health endpoint itself to avoid racing gateway startup.

**Infra image versions are single-sourced** in `gradle/libs.versions.toml`: `*-image-tag` coordinates
(`postgres-image-tag`, `kafka-image-tag`, `opensearch-image-tag`) for the official Docker Hub images used by
docker-compose and the Testcontainers IT/e2e suites (`postgres`, `apache/kafka`, `opensearchproject/opensearch`;
postgres omits a trailing `.0`), and pinned `bitnami-*` chart versions (`bitnami-postgresql`, `bitnami-kafka`,
`bitnami-opensearch`) for the Helm deployment. Each chart ships its own preconfigured `image.tag`, which the Helm charts
deploy as-is — no `image.tag` override in build logic or values files. To bump an infra component, update its
`*-image-tag` and/or its `bitnami-*` chart version together. The `verifyInstallCommands` task (also part of `check`)
verifies the manual `helm install … --version` commands in this file's Kubernetes Deployment section against the catalog
chart pins, so a chart bump cannot leave the documented command stale. The `verifyDocumentedPins` task (also part of
`check`) verifies the catalog's Paketo pins — the buildpack builder and the buildpacks — wherever `AGENTS.md`,
`README.md`, or this pack states them, so a buildpack bump cannot leave a documented pin stale. The
`verifyInfraImageVersions` task (part of `check`) derives its checks from the actual `helm.releases` container, resolves
each pinned chart's preconfigured `image.tag` via the Helm CLI (`helm show values bitnami/<chart> --version <pinned>`,
using the plugin-managed client; the task adds and updates the declared chart repositories itself), fails the build if
its app version drifts from the `*-image-tag` after truncating the chart app version to the official tag's numeric
segment count (so `17.6` matches a chart app version `17.6.0` at minor granularity, while `3.9.0` requires an exact
chart app version match), rejects any official tag with fewer than two numeric segments as a floating reference (e.g.
`17`, which Docker Hub re-points to the latest 17.x), and fails if any infra values file (`helm/values/*/values*.yaml`)
pins `image.tag` — so the repo cannot reintroduce a separate override. The task is build-cacheable on its inputs (the
pinned coordinates and values files): since a pinned chart version's preconfigured `image.tag` is immutable, unchanged
inputs restore the verification from the Gradle build cache and skip the Helm resolution entirely. Deriving the checks
from the configured releases means renaming an infra release retargets its check and removing one drops it. External
`image.tag` overrides at deploy time (e.g. `--set` in a release pipeline) are outside this in-repo gate.

**Every Helm chart coordinate in the version catalog is a concrete version** — never a floating major-line pin such as
`77.x.x`. This covers the observability charts (`prometheus-community-stack`, `grafana-tempo`) and the `common` subchart
dependency as well as the `bitnami-*` infra charts, so the Helm deployment is reproducible at a reviewable version. Bump
a chart by updating its concrete coordinate (e.g. `77.14.0` → `77.15.0`), and verify the bump with a live install +
smoke test (`helmInstall<Release>ToLocal`: pods ready, Prometheus targets up, Grafana datasources wired) —
`verifyInfraImageVersions` gates only the bitnami image-tag drift, not that the bumped chart deploys correctly. The
smoke can run on a throwaway `kind` cluster in a GitHub runner — `bump-kps-chart-91` did exactly that
(`helmInstallKpsToLocal` against a runner-hosted `kind`, cluster deleted after) — so a bump is verifiable end to end in
CI without a local cluster. The Kafka image-tag bump's `KAFKA_LISTENERS` constraint is in `jvm-testing`'s Kafka 3.9.0
note.

## Key environment variables

- `SHOWCASE_CORS_ALLOWED_ORIGINS` — comma-separated browser origins allowed cross-origin access to the gateway. The
  container image defaults to empty (fail-closed — deployments must allow their UI origin explicitly); docker-compose
  sets it to the local dev + preview origins, and the Helm chart exposes it as `apiGateway.cors.allowedOrigins`.
- `SHOWCASE_CORS_ALLOWED_HEADERS` — comma-separated request headers the UI may send cross-origin. Defaults to the
  headers the UI sends (`Content-Type`, `Idempotency-Key`, `traceparent`) on every surface — the Java field, the yml
  placeholder, the image's `BPE_DEFAULT_*` map, and the chart value (`apiGateway.cors.allowedHeaders`) — so it behaves
  the same in a deployment and under `bootRun`.
- `DB_PASSWORD=showcase` — PostgreSQL password for command-service
- `BPL_DEBUG_ENABLED=true` / `BPL_DEBUG_PORT=8000-8003` — JVM debug (JDWP) agent ports (`8000`–`8003` are the published
  debug ports in `docker-compose.yml`; see Local Development)
- `THC_PATH=/actuator/health` — health check path
- `JAVA_OPTS=-XX:MaxDirectMemorySize=128m -XX:MaxGCPauseMillis=20` — container JVM tuning

## Deployment, Helm & Kubernetes

- **WHEN diagnosing a deployment issue, read the Helm chart and values files, not the live cluster alone.** The deployed
  resources are generated output of the chart (`helm/chart/src/main/helm/`; values `helm/values/*/values-*.yaml`), so a
  wrong live resource usually means a wrong source (or a stale apply), not a standalone "cluster problem." The chart
  also introduces k8s-specific concerns (namespaces, NetworkPolicies, KUBE_PING discovery) that do not exist under
  `bootRun`/`docker-compose`, so a bug can be invisible locally and only surface once deployed. Render what the source
  produces before (or alongside) inspecting live resources: `helm template` against the **packaged** chart at
  `helm/chart/build/helm/charts/axon-showcase` — the `helm/chart` module dir has no `Chart.yaml`, and the source at
  `helm/chart/src/main/helm` carries Gradle-filtered placeholders that `helm template` rejects. Two recent deployment
  bugs were chart bugs, not code bugs: the `kubernetes` EndpointSlice `lookup` namespace in the network policies
  (`fix-kube-ping-api-egress`) and the missing release-namespace declarations (`declare-axon-showcase-namespace`). **A
  deployment-only behavior is verified against the deployed signal, not the config** — a green `check` and a correct
  chart value prove the configuration, not the behavior. Observability is Kubernetes-only here, so prove a trace by
  reading what the deployed system emitted: the `traceparent` the browser really sent (`browser_network_request` with
  `part: request-headers`) and the exported trace from Tempo's API (`kubectl port-forward -n monitoring svc/tempo 3200`,
  `GET /api/traces/<trace-id>`, with the root server span parented at the browser's id). **`./setup-hosts.sh setup`
  cannot complete under an agent shell** (no TTY to answer `sudo`), so it stops at the `/etc/hosts` write. It prints the
  ingress LoadBalancer address before that: verify a hostname with a `Host:`-header curl against that address
  (`curl -H "Host: axon-showcase-grafana" http://<address>/api/health`) and hand the hosts write to the owner; record
  which half ran rather than ticking a task on the script's assumed completion. captured: propagate-ui-trace-context
  (#362) captured: expose-grafana-by-hostname

- **A containerized image build that fails in varying ways is host state until proven otherwise — check the container
  runtime's amd64 emulation before touching a pin.** Paketo `dockerBuildImage` failures with shifting signatures (a
  missing `io.buildpacks.buildpackage.metadata` label, an analyzer `panic: could not parse '0.12' as version`,
  `flate: closed writer`, a Go GC fault) point at the host, not the change. A pin decides which image `pack` pulls, so a
  host failure mimics a pin defect exactly; establish both before changing a version:
  1. Does the unmodified baseline fail the same way? Reproduce with the pre-bump pins (or on `main`); if it does, the
     change is not the cause.
  2. Is linux/amd64 emulation working? The build runs the amd64 buildpack lifecycle, which crashes under QEMU. Check the
     VM type and architecture (`colima status`, `colima list`) and the profile's Rosetta flag
     (`grep '^rosetta:' ~/.colima/default/colima.yaml`), then confirm it actually functions — a config can be stale —
     with `docker run --rm --platform linux/amd64 alpine uname -m` (must print `x86_64`). Only when the baseline passes
     and amd64 emulation works is the pin implicated. The worked case: colima's template enabled Rosetta, a macOS
     upgrade removed the Rosetta runtime, colima silently fell back to QEMU, and the amd64 lifecycle crashed; with
     Rosetta reinstalled the same pins built (the identical nginx digest that reported "could not find label" is the
     digest the successful build used). A surface that fails one run and passes the next supports no causal claim in
     either direction, so name the failures that are host state and leave a pin's fault an open question.
     `hold-back-nginx-buildpack` held `paketo-nginx` back at `1.2.0` because that episode's arm64 architecture symptom
     survived every host reset and is tracked as `paketo-buildpacks/nginx#1340`.

- **A buildpack pin is verified by running the image, not by a green build or a metadata label — a release can install a
  binary of the wrong architecture and still build cleanly.** `bump-paketo-builder-and-buildpacks` ticked its test plan
  on `dockerBuildImage` succeeding and on reading the built image's `io.buildpacks.build.metadata` label back (which
  named `paketo-buildpacks/nginx 1.2.1`), and the pin it shipped exits 127 on the development machine: the image is
  amd64 but its `nginx` is AArch64, while the build log says `Installing Nginx Server 1.31.5` on both architectures. Run
  the image after a buildpack or builder bump — `docker run`, `GET /`, and read the binary's architecture **from the
  buildpack's layer** (`/layers/paketo-buildpacks_nginx/nginx/sbin/nginx`), not the runtime `PATH` (a
  `file $(which nginx)` probe returns not-found there), decoding the ELF `e_machine` (`3e 00` = x86-64, `b7 00` =
  AArch64) — and be explicit about the platform when reasoning about a multi-arch artifact: the buildpack images are
  multi-arch, so a bare `docker pull` on an arm64 host resolves the host's own slice and cannot evidence a claim about
  the amd64 one (the image this pipeline produces is amd64-only, so a bare run of it is not the risk). The metadata
  label is doubly misleading: it names the _buildpack version_ the build used (correctly the pinned version) while the
  wrong-architecture binary comes from the resolved **buildpackage slice**, so a label reading `1.2.0` does not clear a
  `1.2.0` pin — the `address-2026-09-21-update-checks` A/B found the pin build cleanly and exit 127 when `pack` added it
  from the registry as an arm64 slice (`sha256:0d6fedc4…`), because the builder bundled a _different_ nginx version,
  while the matched pair (pin = the builder's bundled version) ran. **Establish that pair before choosing the pin, not
  after:** on a builder or buildpack bump, inspect the new builder's bundled versions
  (`pack builder inspect paketobuildpacks/builder-jammy-base:<version>`) and pick the buildpack pins from what it lists
  — a builder that no longer bundles the pinned version makes `pack` add it from the registry as an arm64 slice (the
  mismatch above). captured: bump-buildpacks-2026-10-05 captured: bump-buildpacks-2026-10-12

- **A buildpack's CNB id is not its Docker Hub repository — a registry lookup must target the repository, not the id.**
  The buildpacks are passed to `pack` as `paketo-buildpacks/nginx` (hyphen), but their Docker Hub repositories are
  `paketobuildpacks/nginx` (no hyphen); querying the tags API with the CNB id 404s, so `BuildpackUpdatesTask`'s check
  model carries `repository` separately from the display `name`. When adding a buildpack to the check, use its Docker
  Hub repository. Verify a new entry as in _A derivation whose failure maps onto the benign outcome reads as a clean
  result_. The same repositories also publish alias tags (`1.3`, `5.15`) alongside the full semver (`1.3.0`), which is
  why the buildpack check has two version operations rather than one: `Versions.isNewer` — the shared comparator all
  three update checks use — compares numerically with zero padding, so the two spellings of one release are equal and
  neither reports the other as an update, while `Versions.highest`, which only this check needs, keeps a longer-spelling
  tiebreak so the _reported_ tag is the canonical form rather than the alias.

## Helm update checks

The Helm update check has its own suppression file, `config/helm-updates/major-disabled.txt`: major bumps of the bitnami
infra charts (postgres, kafka, opensearch) are suppressed there because a major chart ships a new preconfigured
`image.tag` that diverges from the test-surface `*-image-tag` pins (docker-compose/Testcontainers) — a coordinated
migration, not an automatic update. The observability charts (kps, tempo) carry no `*-image-tag`, so their major bumps
surface as actionable; verify them with a live install + smoke test (pods ready, Prometheus targets up, Grafana
datasources wired), since `verifyInfraImageVersions` covers only the bitnami infra charts.

## DB scripts

**DB scripts** — before running the command-service standalone (outside Docker), ensure the PostgreSQL event store is
initialized:

```bash
./db.sh init   # creates user `showcase` and database `showcase-events` if absent
./db.sh reset  # drops the database and recreates it
```
