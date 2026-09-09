## MODIFIED Requirements

### Requirement: Vendored agent skills are available to agents

The repository SHALL vendor the AxonIQ `axoniq-migration` plugin skills (version 0.2.2) from the `AxonIQ/agent-skills`
repository under `.opencode/skills/`, so that opencode discovers them as project skills. The vendored set SHALL include
`axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`. Vendoring SHALL NOT add the skill files to
any Gradle module's classpath or to any built Docker image.

#### Scenario: Migration skills are discoverable by agents

- **WHEN** an agent lists the project's skills in `.opencode/skills/`
- **THEN** it finds `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`, each with a `SKILL.md`
  entry point and its referenced assets

#### Scenario: Vendored skills do not affect the application build

- **WHEN** the project build runs any Gradle task (compile, test, package)
- **THEN** the vendored skill files under `.opencode/skills/` are not part of any module's source set, classpath, or
  Docker image

## ADDED Requirements

### Requirement: Per-change quality-gate and analysis subagents are available

The repository SHALL provide locally-defined agent subagents under `.opencode/agent/` for the per-change quality gates
and analysis workflows: `review-quick`, `review-thorough`, `lesson-capture`, and `vision`. Each SHALL be invocable by
the main agent, with its purpose described in its agent definition and (where relevant) in `AGENTS.md`.

#### Scenario: Quick review runs after proposal and implementation

- **WHEN** a change's proposal (planning artifacts) or its implementation is finished
- **THEN** the `review-quick` subagent reviews it against the change's planning artifacts, and the loop repeats until it
  reports no new observations before a manual review is requested

#### Scenario: Thorough review is available on demand

- **WHEN** a deep review pass is wanted (drift, correctness, architecture, conventions)
- **THEN** the `review-thorough` subagent reviews the change against its delta specs, tasks, and the surrounding code

#### Scenario: Lessons are captured after implementation

- **WHEN** a change's implementation quick review is clean
- **THEN** the `lesson-capture` subagent proposes `AGENTS.md` gotchas/conventions from the change's lessons, which the
  main agent verifies and applies

#### Scenario: Screenshots are reviewed visually

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling)
- **THEN** the `vision` subagent reads the image and returns a description, so the text-only main agent can delegate
  visual review
