## MODIFIED Requirements

### Requirement: Report-producing subagents follow a shared report contract

Every subagent that reports findings or a recommendation to the main agent — the per-unit review and lesson-capture
agents, the on-demand auditors of the agent tooling, the spec corpus, the architecture, and the README, and the
independent technical advisor — SHALL follow one shared output contract, so a report is skimmable and its verdict is
read first. The contract SHALL bound the report's shape; the analysis a subagent performs is bounded by that subagent's
own stated scope, not by this contract — an auditor or the deep reviewer verifies its claims as thoroughly as its scope
requires, while a bounded screen such as the quick review performs only the pass its scope defines and says so.

#### Scenario: A report opens with its verdict

- **WHEN** a report-producing subagent returns its report
- **THEN** the first line states the outcome — for a findings report, whether anything remains and how many items,
  naming the count of every class it carries (findings, and any advisory or accretion class); for the technical advisor,
  its recommendation and the counts of its observation classes
- **AND** when the report is a findings report and the caller's request named a prior round's findings, that first line
  also names which of the report's classes repeat a class the prior round already raised
- **AND** a report with nothing to report says so in that first line rather than in a closing sentence

#### Scenario: Each item is budgeted

- **WHEN** the subagent reports a proposed addition, a finding, or an observation
- **THEN** the item names itself, its anchor (`file:line` where applicable), and one line of evidence
- **AND** the budget is per item, not a cap on the report's total, so a report with many genuine findings is longer than
  one with few

#### Scenario: Verified-and-set-aside candidates are summarized

- **WHEN** the subagent has verified a candidate and concluded it is already covered, rejected, or not a defect
- **THEN** it reports that in one line, or collapses several into one summary line, rather than a paragraph each

#### Scenario: A bounded pass does not present itself as an unbounded analysis

- **WHEN** a report-producing subagent's scope bounds the analysis it performs — the quick review's fixed scope and stop
  condition, for example
- **THEN** its report states that bound rather than implying the analysis was unlimited, so a reader does not read a
  bounded screen as a thorough pass

#### Scenario: No alternatives are offered

- **WHEN** the subagent proposes a change or a correction
- **THEN** it states the recommendation, not a menu of alternatives — the calling agent makes the choice

## ADDED Requirements

### Requirement: Independent technical consultation is available on demand

The repository SHALL provide a `technical-advisor` subagent at `.opencode/agent/technical-advisor.md` and an explicit
`/consult` command, so the owner can obtain an independent recommendation on a concrete decision question without
transferring context to an external advisor. The advisor SHALL recommend a direction and SHALL NOT implement changes,
approve delivery, or gate a change. It SHALL be distinct from `review-quick`, `review-thorough`, and
`architecture-auditor`.

#### Scenario: A question is evaluated

- **WHEN** the owner runs `/consult <question>`
- **THEN** the advisor returns a recommendation on that question in the relevant work context

#### Scenario: The advisor is distinct from the reviewers

- **WHEN** a question is answered
- **THEN** the advisor evaluates the decision and direction without repeating `review-quick`, `review-thorough`, or
  `architecture-auditor`

#### Scenario: A recommendation is not authorization

- **WHEN** the advisor returns a recommendation
- **THEN** it does not implement the change, approve delivery, or gate the change, and the invoking agent presents it
  without treating it as approval

### Requirement: The consult command resolves the question

`/consult` without arguments SHALL evaluate the current objective, progress, and next step. An ambiguous target SHALL
produce one focused clarification before any consultation begins. No automatic consultation SHALL be added, and the
review gate's routing SHALL remain unchanged.

#### Scenario: The no-argument form evaluates the current work

- **WHEN** the owner runs `/consult` without arguments
- **THEN** the advisor evaluates the current objective, progress, and next step

#### Scenario: An ambiguous target produces one clarification

- **WHEN** the target or question is ambiguous
- **THEN** exactly one focused clarification is asked before any consultation begins

#### Scenario: No automatic consultation is added

- **WHEN** a change is proposed or implemented
- **THEN** the technical advisor is not invoked automatically, and the review gate's routing is unchanged

### Requirement: A consultation uses a fresh session or continues the same decision

An independent consultation SHALL run in a fresh child session. A follow-up about the same decision MAY continue the
existing advisor session. When the session identifier is unavailable, the caller SHALL start a fresh consultation and
say that it is fresh.

#### Scenario: An independent consultation uses a fresh child session

- **WHEN** a decision is consulted for the first time
- **THEN** the consultation runs in a fresh child session rather than in the parent session

#### Scenario: A follow-up may continue the same session

- **WHEN** a follow-up concerns the same decision and the session identifier is available
- **THEN** the consultation continues the existing advisor session

#### Scenario: A lost identifier starts a fresh consultation

- **WHEN** the session identifier for the decision is unavailable
- **THEN** the caller starts a fresh consultation and states that it is fresh

### Requirement: A consultation receives a focused handoff

The invoking agent SHALL supply only what the decision needs: selected relevant excerpts, source references, and prior
findings with their provenance and disposition are permitted; the full conversation and an undifferentiated tool-output
dump are not. The advisor SHALL assess supplied claims independently and reread the relevant sources where available.
Direct delegation SHALL use the same handoff, and the report SHALL be presented without converting a recommendation into
authorization.

#### Scenario: The handoff carries only the decision's inputs

- **WHEN** the invoking agent prepares a consultation
- **THEN** it supplies the owner's objective and material constraints, the question and proposed next action, the change
  identity and stage, artifact references and the diff base, the relevant diff hunks with their changed paths and
  revision context, any pre-change or history excerpts, the relevant findings and disagreements, the applicable subject
  packs, the report's audience, the requested language, any explicit effort override, and whether the consultation is
  fresh or a follow-up
- **AND** it does not forward the full conversation or an undifferentiated tool-output dump, while selected relevant
  excerpts remain permitted

#### Scenario: Selected excerpts carry provenance

- **WHEN** the handoff includes an excerpt or a prior finding
- **THEN** it names the excerpt's source and, for a prior finding, its disposition

#### Scenario: The advisor verifies independently

- **WHEN** the handoff summarizes a claim that affects the decision
- **THEN** the advisor assesses it independently and rereads the referenced source where available rather than accepting
  the summary as proof

#### Scenario: Direct delegation follows the same contract

- **WHEN** another agent delegates directly to the technical advisor
- **THEN** it supplies the same focused handoff and presents the report without converting the recommendation into
  authorization

### Requirement: A consultation is bounded by the decision

The advisor SHALL examine the evidence the question needs and SHALL NOT run a repository-wide audit unless the owner
explicitly requests one. It SHALL load every applicable subject pack before making a governed judgement in that subject.
It SHALL return one report once the evidence supports a recommendation, and SHALL state an explicit limitation when the
available evidence cannot settle the question rather than continuing to eliminate every uncertainty.

#### Scenario: The pass stops when the decision is supported

- **WHEN** the evidence needed for the question has been examined
- **THEN** the advisor returns the recommendation instead of widening the investigation

#### Scenario: A repository-wide audit is not run unrequested

- **WHEN** a repository-wide audit is not explicitly requested
- **THEN** the advisor does not perform one

#### Scenario: Applicable subject packs are loaded first

- **WHEN** the question turns on a subject `AGENTS.md` assigns to a subject pack
- **THEN** the advisor loads that pack before making the judgement

#### Scenario: Insufficient evidence is stated as a limitation

- **WHEN** the available evidence cannot settle the question
- **THEN** the report states that limitation rather than continuing the investigation

#### Scenario: The report states its bound and its packs

- **WHEN** the report closes
- **THEN** it states the subject packs it loaded and that the pass was bounded by the question rather than a repository
  audit

### Requirement: The technical advisor is read-only

The advisor SHALL NOT implement, edit files, publish, send messages to external services, delegate recursively, or
obtain an automatic second opinion. It SHALL NOT run a build, test suite, benchmark, or behavioral experiment; when one
would settle the question it SHALL propose the experiment with the decision it resolves and its expected cost, and the
invoking agent SHALL run it only after the owner authorizes it. It SHALL rely on its read tools and the handoff for
history and diff excerpts.

#### Scenario: The advisor edits and publishes nothing

- **WHEN** the advisor needs a fact from the repository
- **THEN** it reads or inspects the source and edits nothing, publishes nothing, and contacts no external service

#### Scenario: No recursive delegation or automatic second opinion

- **WHEN** the advisor is uncertain
- **THEN** it does not delegate to another agent or obtain an automatic second opinion

#### Scenario: A resource-intensive experiment is proposed, not run

- **WHEN** a build, test suite, benchmark, or behavioral experiment would settle the question
- **THEN** the advisor proposes it with the decision it resolves and its expected cost, and the invoking agent runs it
  only after the owner authorizes it

#### Scenario: The handoff supplies history and diff context

- **WHEN** the decision needs a change's diff or history
- **THEN** the invoking agent supplies the relevant hunks, changed paths, and revision context in the handoff, because
  the advisor runs no shell commands

### Requirement: A consultation report leads with its recommendation

The report SHALL start with the advisor's recommendation and the counts of its observation classes — observations,
confirmed problems, hypotheses, and open questions — then report only the material observations, each with its evidence
and its effect on the decision. It SHALL distinguish confirmed problems, hypotheses, and unresolved questions, and SHALL
recommend one concrete next action — including retaining or stopping when that is justified — rather than a menu of
alternatives.

#### Scenario: The report starts with the recommendation

- **WHEN** the advisor returns its report
- **THEN** the first line states its recommendation and the counts of observations, confirmed problems, hypotheses, and
  open questions

#### Scenario: Observations carry their evidence and effect

- **WHEN** the report states a material observation
- **THEN** it names the evidence and the observation's effect on the decision

#### Scenario: Certainty is separated

- **WHEN** the report distinguishes its conclusions
- **THEN** confirmed problems, hypotheses, and unresolved questions are labeled separately

#### Scenario: One next action is recommended

- **WHEN** the report recommends a next action
- **THEN** it states one action, including retaining or stopping when justified, rather than a menu of alternatives

#### Scenario: An existing review finding is referenced, not reproduced

- **WHEN** an existing review finding affects the direction
- **THEN** the report references it without reproducing the review

### Requirement: A consultation report follows the language rules

An agent-facing report and the inter-agent handoff SHALL be English. An owner-facing report SHALL use the language of
the owner's current request unless they explicitly request another. Identifiers and exact quotations SHALL remain
unchanged.

#### Scenario: An owner-facing report uses the owner's language

- **WHEN** the report is owner-facing
- **THEN** it uses the language of the owner's current request unless another language was explicitly requested

#### Scenario: An agent-facing report is English

- **WHEN** the report is agent-facing
- **THEN** it is written in English and keeps identifiers and exact quotations unchanged

### Requirement: The technical advisor's model and effort are pinned

The advisor's model SHALL be exactly `openai/gpt-6.1-sol`, and its default reasoning effort SHALL be `high`. `xhigh`
SHALL be used only when the owner explicitly requests it, and the effort SHALL never be raised automatically. When the
model or effort is unavailable, the invoking agent SHALL report the limitation with a concrete alternative rather than
substituting one silently.

#### Scenario: The model and default effort are pinned

- **WHEN** the advisor is invoked without an explicit override
- **THEN** it runs on `openai/gpt-6.1-sol` at the `high` reasoning effort

#### Scenario: Extra-high effort requires an explicit request

- **WHEN** the owner explicitly requests `xhigh` effort
- **THEN** the invocation overrides the effort to `xhigh`; otherwise the effort is never raised automatically

#### Scenario: An unavailable model or effort is reported

- **WHEN** the configured model or effort is unavailable
- **THEN** the limitation and a concrete alternative are reported for the owner's decision, and no substitution is made

### Requirement: The technical advisor uses the existing provider configuration

The advisor SHALL use the existing OpenAI provider configuration, SHALL NOT introduce a separate API-key credential in
place of the owner's configured sign-in, and SHALL NOT display, copy, or persist credentials.

#### Scenario: The existing provider configuration is used

- **WHEN** the advisor authenticates
- **THEN** it uses the existing OpenAI provider configuration without introducing an API-key credential

#### Scenario: Credentials are not exposed

- **WHEN** the advisor runs or reports
- **THEN** no credential is displayed, copied, or persisted

### Requirement: The technical advisor's access is enforced by permissions

Under the installed OpenCode V2, the advisor's tool access SHALL be restricted with its `permissions` ruleset: edit,
write, and patch tools, shell, MCP tools, web tools, recursive delegation, and Code Mode SHALL be denied; and `read`
access to `.env` files SHALL be denied. This is a tool-policy restriction, not OS-level isolation. The `.env` deny
covers `read`; `grep` is not path-scoped by the permission model, so `.env` content is not protected through it.

#### Scenario: Configured restrictions deny write and network tools

- **WHEN** the advisor attempts an edit, a shell command, an MCP or web tool, a recursive delegation, or Code Mode
- **THEN** the installed OpenCode denies it

#### Scenario: The .env deny covers the read tool only

- **WHEN** the advisor attempts to read a `.env` file
- **THEN** the installed OpenCode denies the `read` call, while a `grep` search is not path-scoped by the permission
  model and remains a residual channel

### Requirement: The technical advisor is a V2-local agent

A V1 consumer does not enforce the `permissions` ruleset, so the advisor SHALL be treated as a V2-local agent there. The
project instructions OpenCode supplies automatically SHALL NOT be bypassed to reduce context.

#### Scenario: A V1 consumer does not enforce the ruleset

- **WHEN** the advisor definition is loaded by a V1 consumer
- **THEN** the `permissions` key is not a boundary there, and the advisor is treated as a V2-local agent

#### Scenario: Project instructions are not bypassed

- **WHEN** a consultation runs
- **THEN** the project instructions OpenCode supplies remain in force, and the handoff does not suppress them

### Requirement: Technical-advisor isolation is stated precisely

A consultation's isolation SHALL be described as a separate child session with a focused handoff, not as a claim that no
shared project instructions, configuration, skills, or provider context exist.

#### Scenario: Isolation is stated precisely

- **WHEN** the separate child session is described
- **THEN** it is described as a fresh session with a focused handoff, not as a guarantee that no project instructions or
  provider context are shared
