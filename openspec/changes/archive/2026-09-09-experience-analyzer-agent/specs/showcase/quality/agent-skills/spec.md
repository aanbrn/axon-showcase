## MODIFIED Requirements

### Requirement: Vendored agent skills are available to agents

The repository SHALL vendor the AxonIQ `axoniq-migration` plugin skills (version 0.2.2) from the `AxonIQ/agent-skills`
repository under `.opencode/skills/`, so that opencode discovers them as project skills. The vendored set SHALL include
`axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`. Vendoring SHALL NOT add the skill files to
any Gradle module's classpath or to any built Docker image. In addition to the vendored skills, the repository SHALL
provide locally-defined agent subagents under `.opencode/agent/` (e.g. `review-quick`, `review-thorough`,
`lesson-capture`, `vision`) for the quality gates and analysis workflows documented in `AGENTS.md`.

#### Scenario: Migration skills are discoverable by agents

- **WHEN** an agent lists the project's skills in `.opencode/skills/`
- **THEN** it finds `axon4to5-migrate-code`, `axon4to5-openrewrite`, and `axon4to5-isolatedtest`, each with a `SKILL.md`
  entry point and its referenced assets

#### Scenario: Vendored skills do not affect the application build

- **WHEN** the project build runs any Gradle task (compile, test, package)
- **THEN** the vendored skill files under `.opencode/skills/` are not part of any module's source set, classpath, or
  Docker image

## ADDED Requirements

### Requirement: Experience analysis is available to agents

The repository SHALL provide an `experience-analyzer` agent subagent that, given a gathered digest of recent experience
(merged pull requests, archived changes, `AGENTS.md` gotchas, and `docs/ideas.md`), produces a retrospective and
forward-looking improvement suggestions. The retrospective SHALL group shipped PRs by theme, summarize lessons learned,
and record went-well/went-wrong observations. Each improvement suggestion SHALL be classified as a system suggestion
(addressed via `docs/ideas.md` or an OpenSpec proposal) or a process suggestion (addressed via `AGENTS.md`). The agent
SHALL propose the retrospective and suggestions without editing files; the main agent verifies and applies them.

#### Scenario: A retrospective is produced from gathered experience

- **WHEN** the main agent gathers a digest of recent experience and invokes the `experience-analyzer` subagent
- **THEN** it returns a retrospective grouping shipped PRs by theme, summarizing lessons, and recording went-well and
  went-wrong observations

#### Scenario: Improvement suggestions are classified

- **WHEN** the `experience-analyzer` subagent proposes improvements
- **THEN** each suggestion is labeled as a system change (→ `docs/ideas.md` or a proposal) or a process change (→
  `AGENTS.md`), so the main agent can route it

#### Scenario: The agent does not edit files itself

- **WHEN** the `experience-analyzer` subagent runs
- **THEN** it returns proposed text and suggestions without modifying files; the main agent verifies and applies them
