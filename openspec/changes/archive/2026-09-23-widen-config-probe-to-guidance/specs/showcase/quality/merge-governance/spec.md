## MODIFIED Requirements

### Requirement: Pull requests run the fast quality gate

A pull request to the repository SHALL run the Docker-free quality tiers as a single CI check named `build`: the
standard `check` task with `-PskipITs` (unit tests, component tests, and all static gates — formatting, checkstyle,
SpotBugs, ErrorProne), an OpenSpec validation of changes and specs, and a verification that the OpenSpec configuration's
declared list surfaces — its per-artifact rules and its operation guidance — are readable by the CLI, so a list the CLI
would silently ignore fails the check. The coverage gate SHALL NOT run on the pull-request path, because it is
calibrated on integration-test coverage that only the `main` gate provides. The check SHALL run on `ubuntu-latest` with
a Temurin JDK 21 and SHALL NOT require Docker.

#### Scenario: Pull request triggers the fast gate

- **WHEN** a pull request is opened or updated
- **THEN** the `build` check runs `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (without the coverage gate)
  and `openspec validate --all` against the pull request head, followed by the OpenSpec configuration check

#### Scenario: Fast gate failure blocks merging

- **WHEN** a pull request's `build` check fails (style, unit, component, or OpenSpec validation)
- **THEN** the pull request is not mergeable until the check passes

#### Scenario: A rule set the CLI cannot read fails the fast gate

- **WHEN** `openspec/config.yaml` declares a per-artifact rule in a shape the CLI cannot read (for example an unquoted
  scalar containing `: `, which YAML parses as a mapping rather than a string)
- **THEN** the OpenSpec configuration check fails and reports the defect, instead of the CLI silently ignoring that
  artifact's rules

#### Scenario: An operation guidance set the CLI cannot read fails the fast gate

- **WHEN** `openspec/config.yaml` declares an operation guidance item in a shape the CLI cannot read (for example an
  unquoted scalar containing `: `)
- **THEN** the OpenSpec configuration check fails and reports the defect, instead of the CLI silently ignoring that
  operation's guidance

### Requirement: Pushes to main run the full quality gate

A push to `main` SHALL run the full quality gate as the same `build` check: the complete `check` task including the
integration tier (Testcontainers — PostgreSQL, Kafka, OpenSearch) and the JaCoCo coverage gate, plus an OpenSpec
validation and the same verification that the OpenSpec configuration's declared list surfaces — its per-artifact rules
and its operation guidance — are readable by the CLI. The full gate SHALL require Docker.

#### Scenario: Push to main triggers the full gate

- **WHEN** a commit is pushed to `main`
- **THEN** the `build` check runs `./gradlew check` (with integration tests and coverage), `openspec validate --all`,
  and the OpenSpec configuration check

#### Scenario: Integration-tier failure fails the main gate

- **WHEN** the `build` check on `main` fails in the integration tier
- **THEN** the check is reported as failed and the commit does not pass the quality gate
