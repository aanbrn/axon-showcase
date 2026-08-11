# axon-showcase

A reference application demonstrating a **microservice architecture** built with the **Axon Framework** (CQRS/Event
Sourcing), Spring Boot, and Kubernetes.

## Project Structure

```
axon-showcase/
├── platform/                  # Shared platform BOM and dependency management
├── build-logic/               # Gradle build conventions and plugins
├── helm/                      # Helm charts for Kubernetes deployment
│   └── chart/                 # Main chart for the application
├── docker-compose.yml         # Local development with Docker Compose
├── showcase-api-gateway/      # REST API gateway (entry point)
├── showcase-command-api/      # Command-side API definitions
├── showcase-command-client/   # Command-side client library
├── showcase-command-service/  # Command service (write side)
├── showcase-projection-model/ # Shared query model definitions
├── showcase-projection-service# Projection service (event handlers)
├── showcase-query-api/        # Query-side API definitions
├── showcase-query-client/     # Query-side client library
├── showcase-query-proto/      # Protobuf definitions for queries
├── showcase-query-service/    # Query service (read side)
├── showcase-identifier-extension/  # KSUID identifier support
├── showcase-mapstruct-extension/   # MapStruct extensions
├── showcase-resilience4j-extension # Resilience4j integration
├── showcase-test/             # Shared test utilities
├── load-tests/                # Gatling-based load tests
├── helm/values/               # Helm values for local deployment
└── db-{init,drop}.sh          # Database setup scripts
```

## Technologies

- **Java 21+**
- **Spring Boot 3.x**
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
- **Gradle 8.x** (or use the Gradle wrapper)
- **Helm 3.x** (for Kubernetes deployment)
- **Kubernetes cluster** (for deployment)

## Local Development

### Start Dependencies with Docker Compose

```bash
docker compose up -d
```

This starts:

- **PostgreSQL** — event store database
- **OpenSearch** — projection store (read model)
- **Apache Kafka** — event streaming

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

### Database Scripts

```bash
# Initialize the event store database
./db-init.sh

# Drop the event store database
./db-drop.sh
```

## Kubernetes Deployment

### Deploy to Local Cluster (Kind/minikube)

```bash
# Install monitoring stack (Prometheus + Grafana + Tempo)
helm install kps prometheus-community/kube-prometheus-stack \
  --namespace monitoring --create-namespace \
  --wait

helm install tempo grafana/tempo \
  --namespace monitoring \
  --wait

# Install infrastructure
helm install axon-showcase-db-events bitnami/postgresql \
  --wait

helm install axon-showcase-kafka bitnami/kafka \
  --wait

helm install axon-showcase-os-views bitnami/opensearch \
  --wait

# Install the application
helm install axon-showcase ./helm/chart \
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
    "duration": "PT2H"
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

## Load Testing

```bash
./gradlew :load-tests:test
```

## Tracing

Distributed tracing is available via **Grafana Tempo**. Traces can be viewed in the Grafana dashboard at
`http://localhost:3000`.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Author

**Alexey Afanasyev** — [GitHub](https://github.com/aanbrn)
