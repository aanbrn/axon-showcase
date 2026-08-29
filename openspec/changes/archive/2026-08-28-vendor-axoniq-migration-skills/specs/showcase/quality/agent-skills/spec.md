## Purpose

Provides curated agent skills to the repository by vendoring upstream skill sets under `.opencode/skills/`, so agents
can run the AxonIQ Axon 4→5 migration recipes against this codebase.

## ADDED Requirements

### Requirement: Vendored agent skills are available to agents

The repository SHALL vendor the AxonIQ `axoniq-migration` plugin skills (version 0.2.2) from the
`AxonIQ/agent-skills` repository under `.opencode/skills/`, so that opencode discovers them as project skills. The
vendored set SHALL include `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`. Vendoring
SHALL NOT add the skill files to any Gradle module's classpath or to any built Docker image.

#### Scenario: Migration skills are discoverable by agents

- **WHEN** an agent lists the project's skills in `.opencode/skills/`
- **THEN** it finds `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`, each with a
  `SKILL.md` entry point and its referenced assets

#### Scenario: Vendored skills do not affect the application build

- **WHEN** the project build runs any Gradle task (compile, test, package)
- **THEN** the vendored skill files under `.opencode/skills/` are not part of any module's source set, classpath,
  or Docker image

### Requirement: Vendored skills carry version provenance

The vendored skills SHALL record their upstream source repository and the exact plugin version they were copied
from, so that an upstream update is a deliberate, reviewable change rather than silent drift.

#### Scenario: Provenance is recorded

- **WHEN** a maintainer inspects the vendored skills or the `agent-skills` capability spec
- **THEN** the upstream source (`AxonIQ/agent-skills`), plugin (`axoniq-migration`), and version (0.2.2) are
  documented

#### Scenario: Updating to a new plugin version is an explicit change

- **WHEN** a new `axoniq-migration` plugin version is published upstream
- **THEN** refreshing the vendored skills is performed as a reviewed change that updates the recorded version,
  rather than applied silently