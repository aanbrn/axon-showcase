## MODIFIED Requirements

### Requirement: Infrastructure image references are single-sourced

The version catalog SHALL declare, for each infrastructure component used across deployment, local dev, and tests —
PostgreSQL, Kafka, and OpenSearch — the concrete official Docker Hub image tag (`*-image-tag`) used by docker-compose
and Testcontainers, and the Bitnami Helm chart version (`bitnami-*`) used by the Helm charts. No surface SHALL
hard-code an independent version. Every Helm chart coordinate in the version catalog — including the observability
charts (`prometheus-community-stack`, `grafana-tempo`) and the application chart's `common` subchart dependency —
SHALL be a concrete version (e.g. `77.14.0`), never a floating major-line pin such as `77.x.x`, so the Helm deployment
is reproducible at a reviewable version.

#### Scenario: All surfaces resolve from the catalog

- **WHEN** a maintainer looks up the PostgreSQL, Kafka, or OpenSearch image used by the Helm chart, docker-compose, or
  Testcontainers
- **THEN** the test surfaces resolve to the corresponding `*-image-tag` coordinate, and the Helm charts resolve to the
  pinned `bitnami-*` chart version (whose preconfigured `image.tag` the charts deploy)

#### Scenario: Bumping a version updates its surface

- **WHEN** a `*-image-tag` coordinate for a component is bumped
- **THEN** docker-compose and Testcontainers reference the new tag
- **WHEN** a `bitnami-*` chart version for a component is bumped
- **THEN** the Helm chart resolves to the new chart version, which carries its own preconfigured `image.tag`

#### Scenario: Every Helm chart coordinate is a concrete version

- **WHEN** a maintainer looks up any Helm chart version coordinate in the catalog (infra, observability, or the
  `common` subchart dependency)
- **THEN** the coordinate declares a concrete version such as `77.14.0`, not a floating major-line pin such as `77.x.x`

#### Scenario: A floating chart pin is not used

- **WHEN** the project adds or updates a Helm chart version coordinate in the catalog
- **THEN** the coordinate is concrete, so a change from a floating pin to a concrete version is never reverted