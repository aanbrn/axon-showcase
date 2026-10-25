## MODIFIED Requirements

### Requirement: Pull requests run the fast quality gate

A pull request to the repository SHALL run the Docker-free quality tiers as a single CI check named `build`: the
standard `check` task with `-PskipITs` (unit tests, component tests, and all static gates — formatting, checkstyle,
SpotBugs, ErrorProne), an OpenSpec validation of changes and specs, a verification that the OpenSpec configuration's
declared list surfaces — its per-artifact rules and its operation guidance — are readable by the CLI, so a list the CLI
would silently ignore fails the check, a verification that the OpenCode configuration (`.opencode/opencode.json*`) and
the project's agent, command, and skill definitions are loadable by the consumer the `opencode` GitHub action installs
with each definition's metadata intact, so a V2-only setting the action's V1 binary rejects, or a definition the loader
silently degrades, fails the check instead of silently killing the cloud workflows, and a source-secret scan with
gitleaks over the pull request's files, so a committed secret fails the check. The coverage gate SHALL NOT run on the
pull-request path, because it is calibrated on integration-test coverage that only the `main` gate provides. The check
SHALL run on `ubuntu-latest` with a Temurin JDK 21 and SHALL NOT require Docker. Before running `check`, the gate SHALL
provision the Python interpreter at the minimum the toolchain diagnosis declares, so the build's Python tooling runs
under the declared floor rather than whichever interpreter the runner image supplies.

#### Scenario: Pull request triggers the fast gate

- **WHEN** a pull request is opened or updated
- **THEN** the `build` check runs `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (without the coverage gate)
  and `openspec validate --all` against the pull request head, followed by the OpenSpec configuration check and — when
  the pull request changes any `.opencode/` path or `.github/workflows/ci.yml` — the OpenCode configuration and
  definition-inventory check, and the source-secret scan

#### Scenario: The gate runs under the declared Python minimum

- **WHEN** the `build` check runs on its runner
- **THEN** the check provisions the Python minimum the toolchain diagnosis declares and asserts the interpreter it
  resolved reports at least that version, so the Python tooling is verified against the floor rather than against an
  interpreter the runner image happens to supply

#### Scenario: Fast gate failure blocks merging

- **WHEN** a pull request's `build` check fails (style, unit, component, OpenSpec validation, an OpenCode configuration
  or definition the action's consumer cannot load intact, or a committed secret)
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

#### Scenario: An OpenCode config the action's consumer cannot load fails the fast gate

- **WHEN** a pull request changes `.opencode/opencode.json*` to a shape the consumer the `opencode` action installs (the
  V1 binary) rejects — for example a V2 `permissions` array
- **THEN** the OpenCode configuration check fails and reports the defect, instead of the V1 consumer exiting at startup
  and silently killing the cloud workflows

#### Scenario: A definition the loader silently degrades fails the fast gate

- **WHEN** a pull request changes an `.opencode/` agent, command, or skill definition to a shape the consumer the
  `opencode` action installs resolves without the definition's metadata — for example an unquoted `: ` in a multi-line
  `description`, which drops an agent's or command's fields or removes a skill from the inventory while the load still
  exits 0; or a definition the loader drops from the resolved inventory entirely
- **THEN** the OpenCode definition-inventory check fails and reports the definition, instead of the loader silently
  degrading or dropping it

#### Scenario: A committed secret fails the fast gate

- **WHEN** a pull request's files contain a secret gitleaks detects — for example a hardcoded API key or password
- **THEN** the source-secret scan fails the `build` check and reports the finding, so the secret is removed before it is
  merged rather than after it is in `main`'s history

#### Scenario: A secret in generated or dependency output is not reported

- **WHEN** gitleaks matches a string that resembles a secret inside a generated or downloaded path (`build/`,
  `.gradle/`, or `node_modules/`) rather than source
- **THEN** the committed `gitleaks.toml` allowlists that path, so the scan reports no finding and stays scoped to source

### Requirement: Pushes to main run the full quality gate

A push to `main` SHALL run the full quality gate as the same `build` check: the complete `check` task including the
integration tier (Testcontainers — PostgreSQL, Kafka, OpenSearch) and the JaCoCo coverage gate, plus an OpenSpec
validation and the same verification that the OpenSpec configuration's declared list surfaces — its per-artifact rules
and its operation guidance — are readable by the CLI. The full gate SHALL require Docker. It SHALL provision the Python
minimum the toolchain diagnosis declares, as the pull-request gate does, so the integration path runs the Python tooling
under the same floor.

#### Scenario: Push to main triggers the full gate

- **WHEN** a commit is pushed to `main`
- **THEN** the `build` check runs `./gradlew check` (with integration tests and coverage), `openspec validate --all`,
  and the OpenSpec configuration check

#### Scenario: Integration-tier failure fails the main gate

- **WHEN** the `build` check on `main` fails in the integration tier
- **THEN** the check is reported as failed and the commit does not pass the quality gate
