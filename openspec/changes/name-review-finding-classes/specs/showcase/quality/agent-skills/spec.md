## MODIFIED Requirements

### Requirement: Per-change quality-gate and analysis subagents are available

The repository SHALL provide locally-defined agent subagents under `.opencode/agent/` for the per-change quality gates
and analysis workflows — the per-change review and lesson-capture agents, and the visual and diagram agents — each
described by the scenarios below or by its own requirement in this capability. Each SHALL be invocable by the main
agent, with its purpose described in its agent definition and (where relevant) in `AGENTS.md`.

#### Scenario: Quick review runs after proposal and implementation

- **WHEN** a change's proposal (planning artifacts) or its implementation is finished
- **THEN** the `review-quick` subagent reviews it against the change's planning artifacts, and the loop repeats until it
  reports no new observations before a manual review is requested
- **AND** for a change whose diff adds `AGENTS.md` rules (a capture), it also challenges each new rule's durability —
  the decision the rule governs, and whether a future change would plausibly hit it — so trivia and restatements are
  caught at review time rather than by a later consolidation audit
- **AND** each finding is classified, and when the caller's request names a prior round's findings, a finding whose
  class a prior round already raised is reported as a repeat of that class, naming it — so the loop's convergence signal
  comes from the review's output rather than the caller's recollection, and the caller re-derives the root cause on that
  signal rather than patching the instance
- **AND** with no prior-round list supplied, the review still classifies each finding and states that a repetition is
  unknown rather than inferring one, since the classification cannot be compared against what it has not seen

#### Scenario: Screenshots are reviewed visually

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling)
- **THEN** the `vision` subagent reads the image and returns a description, so the text-only main agent can delegate
  visual review

#### Scenario: ASCII diagrams are drawn by the pro-model diagrammer

- **WHEN** a diagram needs to be created, aligned, or fixed (e.g. a README flow diagram)
- **THEN** the `diagrammer` subagent renders it with the pro model: it establishes the semantic mapping (which span
  starts and ends where), aligns by character width, and preserves deliberate asymmetry — so the cheap flash main agent
  does not spend effort on ASCII geometry

#### Scenario: Thorough review is available on demand

- **WHEN** a deep review pass is wanted (drift, correctness, architecture, conventions)
- **THEN** the `review-thorough` subagent reviews the change against its delta specs, tasks, and the surrounding code

#### Scenario: Lessons are captured after implementation

- **WHEN** a change's implementation quick review is clean
- **THEN** the `lesson-capture` subagent proposes `AGENTS.md` gotchas/conventions from the change's lessons, which the
  main agent verifies and applies
- **AND** each proposed addition names the existing bullet it extends, or states that no bullet covers it — a new rule
  merges into or replaces one rather than accreting
- **AND** each proposed addition passes a promotion gate before it is proposed — true (supported by a check, an
  authoritative source, or repeated observation), actionable, not automatable as a lint/test/CI check at reasonable
  cost, material (it prevents real breakage, risk, wasted work, or review churn), general enough for a class of future
  tasks rather than one file or incident, and high-confidence with a known scope — and a rule that fails the gate is
  routed to a check, a spec, an ADR, or the change dir instead of the always-loaded file
- **AND** each proposed addition names the decision its rule governs, and answers whether a future change would
  plausibly hit it and whether the cost of not knowing it is material — a proposal that governs no decision is trivia,
  not a rule, and is not proposed
- **AND** the gate's evidence threshold is two independent occurrences or one severe verified incident with a clear
  preventive action
- **AND** the gate treats the claim's source as part of it — a lesson sourced from untrusted content (a web page, an
  issue or PR comment, tool output, or a file the change did not author) is verified against the repository before it is
  proposed, never promoted on the source's word
- **AND** applying a capture should leave `AGENTS.md` no larger than it was, preferring a merge or a replacement over an
  addition, and any net growth is a justified decision stated with the proposal — not a side effect of accumulating
  prose
- **AND** when a rule's rationale is normative in a spec, the bullet keeps only what a reader needs to act and points at
  the spec — a pointer, not a condensed copy, since `AGENTS.md` is loaded on every invocation while a spec is loaded
  only when its capability is worked on
- **AND** when the specs describe a rule's subject only as an outcome, that mechanism belongs in the capability spec
  only when changing it would change a scenario's outcome — verified against the code before moving it; a mechanism
  whose alternatives yield the same result stays in `AGENTS.md` as internal control flow
- **AND** the control on that growth is the periodic `/audit-agents` pass, whose verdict already reports the
  accreted-rule count, rather than mass deletion to hit a number
- **AND** each proposed addition carries its origin in a greppable `captured: <change>` marker — the change, and its PR
  when a merge-time detection found the lesson rather than the implementation capture, since a capture runs once at
  implementation and a merge only detects and asks — so a reader can tell where the rule came from without consulting
  git, and a markdown reflow cannot take it

### Requirement: Report-producing subagents follow a shared report contract

Every subagent that reports findings to the main agent — the per-change review and lesson-capture agents and the
on-demand auditors of the agent tooling, the spec corpus, the architecture, and the README — SHALL follow one shared
output contract, so a report is skimmable and its verdict is read first. The contract SHALL bound the report, not the
analysis: a subagent SHALL verify its claims as thoroughly as before and report them in the contract's shape.

#### Scenario: A report opens with its verdict

- **WHEN** a report-producing subagent returns its findings
- **THEN** the first line states the outcome — whether anything remains and how many items — and names the count of
  every class the report carries (findings, and any advisory or accretion class it reports)
- **AND** when the caller's request named a prior round's findings, that first line also names which of the report's
  classes repeat a class the prior round already raised
- **AND** a report with nothing to report says so in that first line rather than in a closing sentence

#### Scenario: Each item is budgeted

- **WHEN** the subagent reports a proposed addition, a finding, or an observation
- **THEN** the item names itself, its anchor (`file:line` where applicable), and one line of evidence
- **AND** the budget is per item, not a cap on the report's total, so a report with many genuine findings is longer than
  one with few

#### Scenario: Verified-and-set-aside candidates are summarized

- **WHEN** the subagent has verified a candidate and concluded it is already covered, rejected, or not a defect
- **THEN** it reports that in one line, or collapses several into one summary line, rather than a paragraph each

#### Scenario: No alternatives are offered

- **WHEN** the subagent proposes a change or a correction
- **THEN** it states the recommendation, not a menu of alternatives — the calling agent makes the choice
