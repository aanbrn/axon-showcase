## MODIFIED Requirements

### Requirement: Experience analysis is available to agents

The repository SHALL provide an `experience-analyzer` agent subagent that, given a gathered digest of recent experience
(merged pull requests, archived changes, `AGENTS.md` gotchas, and `docs/ideas.md`), produces a retrospective and
forward-looking improvement suggestions. The retrospective SHALL group shipped PRs by theme, summarize lessons learned,
and record went-well/went-wrong observations. Each improvement suggestion SHALL be classified as a system suggestion
(addressed via `docs/ideas.md` or an OpenSpec proposal) or a process suggestion (addressed via `AGENTS.md` or a subagent
definition). The agent SHALL propose the retrospective and suggestions without editing files; the main agent verifies
and applies them.

#### Scenario: A retrospective is produced from gathered experience

- **WHEN** the main agent gathers a digest of recent experience and invokes the `experience-analyzer` subagent
- **THEN** it returns a retrospective grouping shipped PRs by theme, summarizing lessons, and recording went-well and
  went-wrong observations

#### Scenario: Improvement suggestions are classified

- **WHEN** the `experience-analyzer` subagent proposes improvements
- **THEN** each suggestion is labeled as a system change (→ `docs/ideas.md` or a proposal) or a process change (→
  `AGENTS.md` or a subagent definition), so the main agent can route it

#### Scenario: The agent does not edit files itself

- **WHEN** the `experience-analyzer` subagent runs
- **THEN** it returns proposed text and suggestions without modifying files; the main agent verifies and applies them

### Requirement: Per-change quality-gate and analysis subagents are available

The repository SHALL provide locally-defined agent subagents under `.opencode/agent/` for the per-change quality gates
and analysis workflows — the per-change review and lesson-capture agents, and the visual and diagram agents — each
described by the scenarios below or by its own requirement in this capability. Each SHALL be invocable by the main
agent, with its purpose described in its agent definition and (where relevant) in `AGENTS.md`.

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
- **AND** each proposed addition names the existing bullet it extends, or states that no bullet covers it — a new rule
  merges into or replaces one rather than accreting

#### Scenario: Screenshots are reviewed visually

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling)
- **THEN** the `vision` subagent reads the image and returns a description, so the text-only main agent can delegate
  visual review

#### Scenario: ASCII diagrams are drawn by the pro-model diagrammer

- **WHEN** a diagram needs to be created, aligned, or fixed (e.g. a README flow diagram)
- **THEN** the `diagrammer` subagent renders it with the pro model: it establishes the semantic mapping (which span
  starts and ends where), aligns by character width, and preserves deliberate asymmetry — so the cheap flash main agent
  does not spend effort on ASCII geometry
