# axon-showcase

A reference application demonstrating a **microservice architecture** built with the **Axon Framework** (CQRS/Event
Sourcing), Spring Boot, and Kubernetes.

## Project Structure

```
axon-showcase/
├── platform/                       # Shared platform BOM and dependency management
├── build-logic/                    # Gradle build conventions and plugins
├── gradle/                         # Version catalog (libs.versions.toml)
├── docs/adr/                       # Architecture decision records
├── openspec/                       # Spec-driven behavior specs and changes
├── helm/                           # Helm charts for Kubernetes deployment
│   └── chart/                      # Main chart for the application
├── docker-compose.yml              # Local development with Docker Compose
├── showcase-api-gateway/           # REST API gateway (entry point)
├── showcase-command-api/           # Command-side API definitions
├── showcase-command-client/        # Command-side client library
├── showcase-command-service/       # Command service (write side)
├── showcase-projection-model/      # Shared query model definitions
├── showcase-projection-service/    # Projection service (event handlers)
├── showcase-query-api/             # Query-side API definitions
├── showcase-query-client/          # Query-side client library
├── showcase-query-proto/           # Protobuf definitions for queries
├── showcase-query-service/         # Query service (read side)
├── showcase-identifier-extension/  # KSUID identifier support
├── showcase-mapstruct-extension/   # MapStruct extensions
├── showcase-resilience4j-extension # Resilience4j integration
├── showcase-test/                  # Shared test utilities
├── load-tests/                     # Gatling-based load tests
├── helm/values/                    # Helm values for local deployment
└── db-{init,drop}.sh               # Database setup scripts
```

## Technologies

- **Java 21+**
- **Spring Boot 3.5.16** (a Spring Boot 4 migration is deferred — see `docs/adr/0004`)
- **Axon Framework** — CQRS, Event Sourcing, Command/Query Bus
- **PostgreSQL** — Event store (events)
- **Apache Kafka** — Event streaming and messaging
- **OpenSearch** — Read-side projection store (query views)
- **Grafana Tempo** — Distributed tracing
- **Helm** — Kubernetes packaging and deployment
- **Gatling** — Load testing
- **Gradle** — Build orchestration

## Architecture

The application follows a **CQRS (Command Query Responsibility Segregation)** pattern:

| Component              | Role                                                        |
|------------------------|-------------------------------------------------------------|
| **API Gateway**        | Entry point; routes requests to command or query services   |
| **Command Service**    | Handles write operations; publishes events via Kafka        |
| **Projection Service** | Consumes events from Kafka and populates OpenSearch views   |
| **Query Service**      | Handles read operations; queries OpenSearch for projections |

### Event Flow

```
Write: Client → API Gateway → Command Service → (Kafka) → Projection Service → OpenSearch

Read: Client → API Gateway → Query Service → OpenSearch
```

## Prerequisites

- **Java 21+**
- **Docker & Docker Compose** (for local development)
- **Gradle 9.x** (or use the Gradle wrapper)
- **Helm 4.x** (for Kubernetes deployment)
- **Kubernetes cluster** (for deployment)
- **Snyk CLI** (for the dependency security scan)

## Local Development

### IntelliJ IDEA Setup

Formatting is enforced by Spotless — palantir-java-format for Java, ktfmt for Gradle Kotlin DSL (`*.gradle.kts`):
`./gradlew spotlessApply` formats, `spotlessCheck` verifies, and the build never depends on an IDE. IntelliJ's built-in
formatter uses its own code style and would reformat files differently, so configure the IDE to stay in sync:

- The repo's IntelliJ config is **not versioned** — `.idea/` is git-ignored. Run the setup script once (IDE closed,
  then restart): it locates your IntelliJ, installs the **palantir-java-format** and **ktfmt** plugins, writes the
  project config from the committed templates in `config/idea/`, and ensures the test-tier naming inspection:

  ```bash
  ./scripts/setup-idea.sh
  ```

  The ktfmt template uses the plugin's **Custom** style to reproduce ktfmt's kotlinlang style at 120 columns with
  unused-import removal (the plugin's `Kotlinlang` mode hard-codes ktfmt's 100-column default and ignores the
  line-length option). Where a plugin is disabled by default (palantir only auto-enables with the
  `com.palantir.java-format` Gradle plugin, which this project does not use), enable it via **Settings → Other
  Settings → palantir-java-format Settings**. When enabled it replaces `Reformat Code` (`Ctrl+Alt+L`) with the
  palantir formatter.
- The plugin only replaces `Reformat Code`; **import order is a separate mechanism** — IntelliJ's `Optimize Imports`
  (`Ctrl+Alt+O`) is governed by `.editorconfig`, which the repo ships with the palantir layout
  (`ij_java_imports_layout = $*,|,*` — *import static all other imports*, blank line, *import all other imports*) so
  `Optimize Imports` matches `spotlessApply` automatically. If the layout is not picked up, set it manually:
  **Settings → Editor → Code Style → Java → Imports**, *Import Layout* panel → clear the rows and add: *import
  static all other imports*, blank line, *import all other imports* (single imports, no wildcards).
- With the plugins enabled and the `.editorconfig` import layout in effect, IntelliJ's `Reformat Code` and
  `Optimize Imports` produce spotless-compatible output, so the automatic reformat triggers are safe to keep on:
  **Actions on Save** → *Reformat code* / *Optimize imports*, and **Auto Import** →
  *Optimize imports on the fly*. If
  the plugin is not active on a machine, disable those triggers instead to avoid drift; `spotlessCheck` in `check` is
  the backstop either way.
- When in doubt, format with `./gradlew spotlessApply` — it is the single source of truth.

### Start Dependencies with Docker Compose

```bash
docker compose up -d
```

This starts all infrastructure and application services:

- **PostgreSQL** — event store database
- **OpenSearch** — projection store (read model)
- **Apache Kafka** — event streaming
- **Kafka Init** — creates the `axon-showcase-events` topic
- **API Gateway** — REST entry point (port 8080, debug 8000)
- **Command Service** — write side (debug 8001)
- **Query Service** — read side (debug 8002)
- **Projection Service** — event handlers (debug 8003)

Application images must be built first (`./gradlew bootBuildImage`) or set `PROJECT_VERSION` accordingly.

### Build the Project

```bash
./gradlew build
```

### Run Locally

Each service can be run individually or via `docker compose`:

```bash
./gradlew :showcase-api-gateway:bootRun
./gradlew :showcase-command-service:bootRun
./gradlew :showcase-query-service:bootRun
./gradlew :showcase-projection-service:bootRun
```

Each service runs on its own HTTP port: API gateway `8080`, command service `8081`, projection service `8082`, query
service `8083`.

### Database Scripts

```bash
# Initialize the event store database (idempotent)
./db.sh init

# Drop the event store database
./db.sh drop

# Drop and recreate the event store database
./db.sh reset
```

## Spec-Driven Development

This repository is built spec-first. Behavior is captured as OpenSpec specs under `openspec/specs/` and changes are
planned under `openspec/changes/` using the propose → apply → archive workflow (via the `opsx-*` opencode commands /
`openspec-*` skills). `AGENTS.md` is the behavioral source of truth — read it before contributing.

Cross-cutting architecture decisions and their rationale are recorded as Architecture Decision Records under
`docs/adr/`. OpenSpec captures what the system does and how a change is planned; ADRs capture why the system is shaped
the way it is.

## Continuous Integration

`.github/workflows/ci.yml` gates every pull request and push to `main` with a single `build` check. Pull requests run
the Docker-free fast gate (`check -PskipITs` with the coverage gate disabled), while pushes to `main` run the full
gate (`check` with integration tests and coverage); both run `openspec validate --all`. The Gradle cache is restored
across runs via `gradle/actions/setup-gradle`. The `main-required-checks` ruleset requires the `build` check for all
merges into `main`, with no bypass actors.

`.github/workflows/e2e.yml` runs the end-to-end suite (`:showcase-api-gateway:e2eTest`, which builds all four service
images and boots the full pipeline) on a nightly schedule and via `workflow_dispatch` — observational, never a merge
gate, no secrets.

`.github/workflows/snyk.yml` runs the dependency security scan (`dependencySecurityCheck`, all sub-projects with the
root `.snyk` policy) on a weekly schedule and via `workflow_dispatch`, authenticated with the `SNYK_TOKEN` secret —
observational, never a merge gate.

`.github/workflows/dependency-updates.yml` runs the Gradle dependency update report (`dependencyUpdates`) on a weekly
schedule and via `workflow_dispatch`, opening or updating the "Dependency updates" issue with only the actionable
sections of the report (stable catalog updates + Gradle wrapper status) using the `GITHUB_TOKEN` (`issues: write`).
When there are actionable updates it posts a comment mentioning the repository owner (so they are notified); runs
with no updates update the issue silently — observational, never a merge gate.

## Testing

Tests are organized into four tiers, run in order:

| Tier             | Command                                            | Notes                         |
|------------------|----------------------------------------------------|-------------------------------|
| Unit             | `./gradlew :<module>:test`                         | isolated, no Spring context   |
| Component        | `./gradlew :<module>:componentTest`                | real in-process collaborators |
| Integration      | `./gradlew :<module>:integrationTest`              | Testcontainers (needs Docker) |
| End-to-end       | `./gradlew :<module>:e2eTest`                      | real deployed service + infra |

Run the full check for a module — compile, spotless, checkstyle, spotbugs, errorprone, test, componentTest,
integrationTest — with `./gradlew :<module>:check` (add `-PskipITs` to drop integration for a Docker-free check;
`e2eTest` is a separate opt-in task). All quality gates run in the Gradle build, so no IDE is required to verify a
change. An IDE (e.g. IntelliJ IDEA) is an optional convenience for interactive editing, debugging, and inspection.

## Dependency Security

```bash
./gradlew dependencySecurityCheck
```

Runs the Snyk dependency scan (`snyk test --all-sub-projects`) across all sub-projects. Requires the Snyk CLI on `PATH`
and is intentionally not part of `./gradlew check`.

## Dependency Updates

```bash
./gradlew dependencyUpdates
```

Reports newer versions of dependencies whose version is declared with an exact `version.ref` in the version catalog
(`gradle/libs.versions.toml`); BOM-inherited versions are not reported. Major updates can be suppressed per coordinate
or group prefix in `config/dependency-updates/major-disabled.properties` — minor and patch updates for those
coordinates are still reported. The suppression rationale for each coordinate is recorded in the
`showcase/quality/dependency-management` spec. See ADR-0004 for the deferred Spring Boot 4 migration context.

For calendar-versioned coordinates (leading segment is a 4-digit year, e.g. Spring `YYYY.MINOR.MICRO` such as
`reactor-bom 2025.0.7`), a change in the `YYYY.TRAIN` pair (the first two version segments) is treated as a major
update — matching Spring's release-train definition where `2025.0` and `2025.1` are distinct trains — while a change
only in the service-release (third) segment within the same train is a minor/patch update. Semver coordinates keep the
leading-integer major comparison.

The `/dependency-updates` opencode command runs this report and summarizes the available updates; the
`/gradle-update` command updates the Gradle wrapper to the latest stable version when one is available, and the
`/opsx-tool-update` command regenerates the OpenSpec command/skill instruction files after a new `openspec` CLI
release.

Note that the report can also surface spurious rows caused by build-environment constraints: build tooling such as
SpotBugs publishes module constraints that `checkBuildEnvironmentConstraints` reads and reports as the "current
version". For example, a `log4j-core [2.17.1 -> 2.26.1]` row appears even though `log4j-core` resolves to `2.26.1`
everywhere — `2.17.1` is the floor of an external Log4Shell guard published by `spotbugs-annotations`. These rows are a
known `gradle-versions-plugin` limitation, not real updates (see upstream ben-manes/gradle-versions-plugin#755); see
ADR-0007 for the evidence trail.

## Kubernetes Deployment

### Deploy to Local Cluster (Kind/minikube)

```bash
# Install monitoring stack (Prometheus + Grafana + Tempo)
helm install kps prometheus-community/kube-prometheus-stack \
  --version 77.14.0 \
  --namespace monitoring --create-namespace \
  --wait

helm install tempo grafana/tempo \
  --version 1.24.4 \
  --namespace monitoring --create-namespace \
  --wait

# Install infrastructure
helm install axon-showcase-db-events bitnami/postgresql \
  --version 16.7.27 \
  --namespace axon-showcase --create-namespace \
  --wait

helm install axon-showcase-kafka bitnami/kafka \
  --version 31.5.0 \
  --namespace axon-showcase --create-namespace \
  --wait

helm install axon-showcase-os-views bitnami/opensearch \
  --version 2.0.10 \
  --namespace axon-showcase --create-namespace \
  --wait

# Install the application
helm install axon-showcase ./helm/chart \
  --namespace axon-showcase --create-namespace \
  --wait
```

Or use the bundled Helm release:

```bash
./gradlew helmInstallToLocal
```

### Helm Values

Custom values can be placed in `helm/values/axon-showcase/values-local.yaml`.

## API Usage

The API is exposed at `http://localhost:8080/showcases`.

### Schedule a Showcase

```bash
curl -X POST http://localhost:8080/showcases \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Showcase",
    "startTime": "2026-08-01T10:00:00Z",
    "duration": "PT5M30S"
  }'
```

### Start a Showcase

```bash
curl -X PUT http://localhost:8080/showcases/{showcaseId}/start
```

### Finish a Showcase

```bash
curl -X PUT http://localhost:8080/showcases/{showcaseId}/finish
```

### Remove a Showcase

```bash
curl -X DELETE http://localhost:8080/showcases/{showcaseId}
```

### List Showcases

```bash
curl "http://localhost:8080/showcases?title=My&status=SCHEDULED&size=10"
```

### Get Showcase by ID

```bash
curl http://localhost:8080/showcases/{showcaseId}
```

### Query (Protobuf)

Dispatches an Axon query and returns the first response. Used internally by the
query-client for inter-service communication (`application/protobuf` body).

```bash
curl -X POST http://localhost:8083/query \
  -H "Content-Type: application/x-protobuf" \
  -d '<serialized QueryRequest>'
```

### Streaming Query (Protobuf)

Dispatches an Axon query and returns the full response stream. Used internally by the
query-client for inter-service communication (`application/protobuf` body).

```bash
curl -X POST http://localhost:8083/streaming-query \
  -H "Content-Type: application/x-protobuf" \
  -d '<serialized QueryRequest>'
```

## Load Testing

```bash
./gradlew :load-tests:test
```

## Tracing

Distributed tracing is available via **Grafana Tempo**. Traces can be viewed in the Grafana dashboard at
`http://localhost:3000`.

## License

This project is a reference application licensed under the [MIT License](LICENSE) — free to use, copy, and adapt for
learning and as a starting point for derived projects.

## Author

**Alexey Afanasyev** — [GitHub](https://github.com/aanbrn)
