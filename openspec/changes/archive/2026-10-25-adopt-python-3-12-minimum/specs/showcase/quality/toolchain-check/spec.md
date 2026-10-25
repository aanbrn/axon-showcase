## MODIFIED Requirements

### Requirement: The diagnostic distinguishes required from optional prerequisites

The diagnostic SHALL distinguish prerequisites required to run and verify the application from those needed only for
optional paths — deployment, security scanning, and the agentic workflow — so a machine that can build and test is not
reported as unfit because an optional tool is absent. It SHALL exit non-zero when a prerequisite for the default
build-and-test path is unsatisfied, so it can serve as a check, and SHALL still report optional prerequisites without
failing on them by default.

#### Scenario: A missing optional tool does not fail the diagnosis

- **WHEN** the diagnostic runs on a machine missing a deployment-, scanning-, or agent-workflow-only tool (Helm, the
  Snyk or Kubernetes CLI, or the agent CLIs) while every required prerequisite is satisfied
- **THEN** the tool is reported as missing, and the diagnostic exits successfully because the default build-and-test
  path does not require it

#### Scenario: A missing required tool fails the diagnosis

- **WHEN** the diagnostic runs on a machine missing a tool the default build-and-test path requires (Java, Docker with
  Compose, or the Python 3.12+ interpreter the build's Python tooling runs under)
- **THEN** the diagnostic reports it and exits non-zero

#### Scenario: A required tool below its floor fails the diagnosis

- **WHEN** a required prerequisite is present but below its minimum version (e.g. Java 17 where Java 21 is required, or
  Python 3.11 where the build's Python tooling requires 3.12)
- **THEN** the diagnostic reports it as too old and exits non-zero, so a present-but-too-old required tool is a failure
  rather than a satisfied prerequisite

### Requirement: The diagnostic reports repo-state prerequisites

The diagnostic SHALL check the repository's setup state beyond bare tool installation — at least whether the Git hooks
are installed and whether the Docker daemon is reachable — and report each with the command that applies it, since these
are prerequisites a tool-presence check alone would miss. The repo-state checks SHALL NOT by themselves fail the default
run: a clone that has not installed the Git hooks SHALL be reported as such and the default run SHALL still exit
successfully, because the hooks guard commits rather than the default build-and-test path. The Docker daemon is the
exception — the integration tests require a live daemon, so an unreachable daemon SHALL fail the default run.

#### Scenario: Uninstalled Git hooks are reported

- **WHEN** the repository's Git hooks have not been installed in the current clone
- **THEN** the diagnostic reports them as not installed and names the command that installs them

#### Scenario: An unreachable Docker daemon is reported

- **WHEN** a Docker client is installed but the daemon is not reachable
- **THEN** the diagnostic reports the daemon as unreachable, distinct from Docker being absent

#### Scenario: Missing Git hooks do not fail the default diagnosis

- **WHEN** the diagnostic runs on a clone whose Git hooks are not installed while every required prerequisite (Java,
  Docker with a live daemon, Compose v2, Python 3.12+) is satisfied
- **THEN** the hooks are reported as not installed and the default run exits successfully, because the hooks are not on
  the default build-and-test path

#### Scenario: An unreachable Docker daemon fails the default diagnosis

- **WHEN** the diagnostic runs on a machine whose Docker client is present but whose daemon is not reachable
- **THEN** the daemon is reported as unreachable and the default run exits non-zero, because the integration tests
  require a live daemon
