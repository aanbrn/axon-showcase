# Spec Delta

## MODIFIED Requirements

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
  Docker with a live daemon, Compose v2) is satisfied
- **THEN** the hooks are reported as not installed and the default run exits successfully, because the hooks are not on
  the default build-and-test path

#### Scenario: An unreachable Docker daemon fails the default diagnosis

- **WHEN** the diagnostic runs on a machine whose Docker client is present but whose daemon is not reachable
- **THEN** the daemon is reported as unreachable and the default run exits non-zero, because the integration tests
  require a live daemon
