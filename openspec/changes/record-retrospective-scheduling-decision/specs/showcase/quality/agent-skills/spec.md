## MODIFIED Requirements

### Requirement: The on-demand audits are also available on a schedule

The repository's audits of its agent tooling, its spec corpus, and its architecture SHALL be runnable unattended on a
schedule, in addition to their on-demand `/audit-*` triggers, so the reconciliation they perform does not depend on a
human asking. The scheduled run SHALL perform the audits through the OpenCode GitHub action's scheduled path — which
requires a `prompt` input, authenticates by OIDC (`id-token: write`), and produces a branch or pull request rather than
an issue — and SHALL batch them into one run whose findings land in a single committed report the action carries into a
pull request, so the pro-model cost is one run per period rather than one per audit. Its cadence SHALL serve the
reconciliation the audits exist for (the consolidation that counters the accretion the capture loop produces) rather
than being a bare reminder: a run with nothing to report SHALL commit nothing and so produce no artifact. The
`readme-auditor` SHALL remain on-demand only rather than joining the scheduled set: it verifies the human-facing
`README.md` against the repository, an accuracy-and-coverage check distinct from the reconciliation of the agent's own
machinery the schedule exists for. The `experience-analyzer` SHALL likewise remain on-demand only: its retrospective is
a narrative judgment about a period rather than a reconciliation verifiable against the repository, and its richest
input — what went wrong that no diff captures — is available only in the session that lived it, so a scheduled run could
not supply it. A scheduled run that _produces_ the retrospective SHALL NOT be used in its place, because it automates
the decision rather than the trigger and still lacks the session-only context; a scheduled check that only _reports_
what has accumulated since the newest retrospective, and otherwise stays silent, is not such a run — it surfaces the
trigger while leaving the decision and the analysis to the on-demand invocation.

#### Scenario: The audits run unattended on their schedule

- **WHEN** the scheduled audit workflow fires
- **THEN** it runs the agents-auditor, specs-auditor, and architecture-auditor audits in one unattended agent run and
  reports their findings together

#### Scenario: The scheduled audits do not replace the on-demand triggers

- **WHEN** a maintainer invokes an audit on demand (e.g. `/audit-agents`)
- **THEN** the on-demand trigger still runs that single audit, and the scheduled workflow's existence does not change it

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
- **AND** the control on that growth is the periodic `/audit-agents` pass, whose verdict already reports the
  accreted-rule count, rather than mass deletion to hit a number
- **AND** each proposed addition carries its origin in a greppable `captured: <change>` marker — the change, and its PR
  when a merge-time detection found the lesson rather than the implementation capture, since a capture runs once at
  implementation and a merge only detects and asks — so a reader can tell where the rule came from without consulting
  git, and a markdown reflow cannot take it

#### Scenario: Screenshots are reviewed visually

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling)
- **THEN** the `vision` subagent reads the image and returns a description, so the text-only main agent can delegate
  visual review

#### Scenario: ASCII diagrams are drawn by the pro-model diagrammer

- **WHEN** a diagram needs to be created, aligned, or fixed (e.g. a README flow diagram)
- **THEN** the `diagrammer` subagent renders it with the pro model: it establishes the semantic mapping (which span
  starts and ends where), aligns by character width, and preserves deliberate asymmetry — so the cheap flash main agent
  does not spend effort on ASCII geometry
