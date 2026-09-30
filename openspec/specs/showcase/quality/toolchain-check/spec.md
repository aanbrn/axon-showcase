# showcase/quality/toolchain-check Specification

## Purpose

Diagnoses whether the local machine can run and verify the project: it probes the toolchain the build, tests, and
deployment depend on, reports each prerequisite's status and the action needed, and does so without requiring the build
or Java to run first.

## Requirements

### Requirement: The local toolchain is diagnosed without the build

The repository SHALL provide a diagnostic that probes the local machine's toolchain and reports each prerequisite's
status, so a contributor can learn what is missing in one pass instead of discovering it through a failing task. The
diagnostic SHALL run with nothing but a POSIX shell — it SHALL NOT require Java, Gradle, a network, or the project's
dependencies — so it can diagnose the build's own prerequisites, including Java itself. It SHALL cover three classes of
prerequisite: **tool presence**, **minimum versions**, and **repo state** (the setup steps beyond a bare install). It
SHALL report, per prerequisite, whether it is satisfied and, for anything actionable, the install or setup command that
resolves it. The set of probes SHALL be **declared once** — one row per probe naming it and its class — and the
declaration's **shape SHALL be validated where it is owned**, so a malformed row cannot make a probe invisible. The
diagnostic SHALL report **every declared probe** on any machine, and the declared set SHALL be asserted by a check that
runs in the build and verifies both the declaration's shape and that the diagnostic actually reports every probe it
declares — so a probe silently changing class, or failing to reach the code, fails rather than passing unnoticed.

#### Scenario: A satisfied prerequisite is reported as satisfied

- **WHEN** the diagnostic runs on a machine that has a prerequisite installed at a supported version
- **THEN** that prerequisite is reported as satisfied, with its detected version where one is readable

#### Scenario: A missing prerequisite names its remedy

- **WHEN** the diagnostic runs on a machine missing a prerequisite
- **THEN** that prerequisite is reported as missing, together with the command that installs it

#### Scenario: A too-old prerequisite is distinguished from a missing one

- **WHEN** a prerequisite is installed but below its minimum supported version
- **THEN** the diagnostic reports it as too old — stating the detected version, the required minimum, and the upgrade
  command — rather than reporting it as missing or satisfied

#### Scenario: The diagnostic runs on a machine without Java

- **WHEN** the diagnostic runs on a machine where Java is absent or too old, so the Gradle build cannot start
- **THEN** it still runs to completion and reports Java's status along with the other prerequisites

#### Scenario: An unreadable version is reported as unknown, never as satisfied

- **WHEN** a prerequisite's executable is present but its version cannot be read (the probe prints nothing it can parse)
- **THEN** the diagnostic reports that prerequisite's version as unknown rather than reporting it satisfied, so a
  missing version reading is never indistinguishable from a version that meets the floor

#### Scenario: Every declared probe is reported

- **WHEN** the diagnostic runs, on any machine and whatever is installed
- **THEN** it reports one status line for every probe in its declaration, so a probe that never reaches the reporting
  code is a detectable omission rather than a silent absence

#### Scenario: A malformed declaration fails rather than hiding a probe

- **WHEN** the probe declaration is malformed — a row with the wrong number of columns, a blank or duplicated key, or a
  value outside what a column allows
- **THEN** the diagnostic fails loudly naming the row, and the check fails too, rather than a probe disappearing from
  the set the diagnostic reports

#### Scenario: A probe cannot erase its own documentation

- **WHEN** a probe is declared as not documented in the README while a Prerequisites row documents it, or declared as
  documented while its row is absent
- **THEN** the check fails on the disagreement, because the probe-to-row mapping is anchored to the README's own rows
  rather than to the declaration — so a probe cannot drop out of the documented list by declaring itself out of it

#### Scenario: The check runs in the build and asserts the declaration

- **WHEN** the build's default verification runs
- **THEN** the check asserts the declaration's shape, that the diagnostic reports every declared probe, and the set's
  agreement with the README and the spec's required set — without requiring a network, a container, or an installed
  prerequisite

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
  Compose)
- **THEN** the diagnostic reports it and exits non-zero

#### Scenario: A required tool below its floor fails the diagnosis

- **WHEN** a required prerequisite is present but below its minimum version (e.g. Java 17 where Java 21 is required)
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
  Docker with a live daemon, Compose v2) is satisfied
- **THEN** the hooks are reported as not installed and the default run exits successfully, because the hooks are not on
  the default build-and-test path

#### Scenario: An unreachable Docker daemon fails the default diagnosis

- **WHEN** the diagnostic runs on a machine whose Docker client is present but whose daemon is not reachable
- **THEN** the daemon is reported as unreachable and the default run exits non-zero, because the integration tests
  require a live daemon

### Requirement: The diagnostic's guidance is platform-appropriate

The diagnostic SHALL run on any Unix-like platform, not only the one the repository's documentation is written from: its
install guidance SHALL match the detected platform, and where it does not recognize the platform it SHALL name the
missing prerequisite without prescribing an install command that would not run there. Its documented install guidance
SHALL agree with the README's Prerequisites install column for each platform the README covers, so the two surfaces
cannot give conflicting advice.

#### Scenario: The install guidance matches the detected platform

- **WHEN** the diagnostic reports a missing or too-old prerequisite on macOS
- **THEN** it prints the macOS install command the README's Prerequisites table carries for that tool

#### Scenario: A recognized Linux platform gets its own guidance

- **WHEN** the diagnostic runs on a Debian/Ubuntu Linux host and reports a missing or too-old prerequisite
- **THEN** it prints that platform's install command rather than the macOS one — no `brew` command is offered on Linux

#### Scenario: An unrecognized platform is not prescribed a wrong command

- **WHEN** the diagnostic runs on a platform it does not recognize
- **THEN** it names the missing prerequisite without printing an install command that is only valid elsewhere
