# Spec Delta

## REMOVED Requirements

### Requirement: Per-change quality-gate and analysis subagents are available

**Reason**: The requirement owned four subjects in one block — the quick review loop, the routed deep review, the
post-implementation capture, and the diagram/vision agents — and restated the caller's operational procedure clause by
clause, duplicating the review gate's procedure and `review-quick.md`'s contract in a second copy that no gate paired
with the first.

**Migration**: Every one of the twenty scenarios is carried into a replacement requirement under its exact name — the
quick loop (three), the routed deep review (nine), the capture (four), and the diagrams (four). The procedural clauses
the quick loop's first scenario restated move to their owners: the reviewer's screen, finding classes, evidence rules,
report contract, and capture challenge to `review-quick.md`; the caller's steps, pre-write record, claims table, session
preflight, ledger append, and checkpoint to the new `review-gate` skill; the ledger row's format and verification to
`showcase/quality/commit-hygiene`. No migrated scenario's behavior is added or removed except the two `review-gate`
additions the narrow retarget makes to the first capture scenario — a destination and an entry in its existing-rule
enumeration — and none is reordered; the diagram scenarios move verbatim, and the capture scenarios are otherwise
unchanged. The six scenario names the delta adds belong to its two ownership requirements.

## ADDED Requirements

### Requirement: The quick review loop runs on every unit

The repository SHALL run the bounded quick review after a unit's proposal and after its implementation, repeating until
it reports no new observations. The reviewer's screen, finding classes, evidence rules, report contract, and capture
challenge are its definition's; the caller's steps — the pre-write record, the claims table, the session preflight, the
ledger row and its checkpoint — are the review-gate skill's; the ledger row's format and verification are
`showcase/quality/commit-hygiene`'s.

#### Scenario: Quick review runs after proposal and implementation

- **WHEN** a unit's proposal (planning artifacts) or its implementation is finished
- **THEN** the `review-quick` subagent reviews it against the unit's planning artifacts, and the loop repeats until it
  reports no new observations before a manual review is requested
- **AND** the pass is a bounded screen over the inputs the caller supplies — the screen `review-quick.md` defines — so a
  repository-wide search is not part of it, and its report states that scope and stop condition rather than implying
  unlimited analysis
- **AND** the caller supplies the screen's inputs as a list its report names, and for a capture that list includes the
  existing rules each proposal claims to extend (or its statement that none covers it), the capture evidence, and the
  `lesson-capture` definition
- **AND** a risk the supplied scope cannot settle is escalated to the deep review — through the routing decision or by
  the caller raising it — rather than absorbed by widening the pass
- **AND** the screen, the finding classification, each class's evidence requirement, the report contract, and the review
  of a capture's proposals against the capture contract are `review-quick.md`'s, while the caller's steps — the
  pre-write record, the claims table, the session preflight, the ledger row, and its checkpoint — are the `review-gate`
  skill's
- **AND** a defect the applying command rewrites — `spotlessApply`, or the web module's `npmFormat` — is routed to that
  command rather than reported, so what the command leaves alone stays reportable
- **AND** for a record artifact (a retrospective, a load-test record), a later round raising only new instances of an
  already-raised class stops and escalates to the owner rather than iterating, since such an artifact has no shipped
  behavior to converge on

#### Scenario: A stalled reviewer is replaced from a recorded handover

- **WHEN** a reviewer invocation has not responded by the deadline the caller's policy supplies
- **THEN** the caller records the failure as `stopped-responding`, naming the stalled session's identifier and its
  replacement's, and creates the replacement session only after that record
- **AND** the only cases that justify a replacement are: the prior session expired, the reviewer's definition changed
  after that session began, or the reviewer stopped responding mid-round (the loop has observed a session stall with no
  reply, which no wait resolves)
- **AND** the handover it gives the replacement is the record it wrote **before** waiting — the unit's identity, its
  base and current revisions, the supplied artifacts, the scope, the prior findings with their instance-level evidence,
  the completed checks, and the known limitations — so the context a replacement needs never depends on the stalled
  invocation reporting, and cancelling it loses no progress that record does not already hold
- **AND** the deadline comes from the invocation or the caller's policy, never a universal figure stated as a measured
  fact, because no measurement of a reviewer's duration is reproducible from the repository
- **AND** the replacement reviews the current state independently, treating the handover as context rather than as proof
  that a result is correct, and re-derives what it reports from the state it inspects
- **AND** the record keeps only facts the caller can verify — the replacement's reason, both session identifiers, the
  handover, and the round's outcome — and never a duration, a token count, or a session count asserted as a measurement,
  so the ledger trends what a gate can re-derive rather than a figure it cannot

#### Scenario: A skipped pre-write pass is a missing record

- **WHEN** a unit's planning artifacts are finished and the review loop is about to start
- **THEN** the unit's record names the pre-write derivation pass it ran — the change's `tasks.md` task, or the
  pull-request checklist line for a unit with no change dir — so a pass that was skipped is reported as that record
  missing or unticked rather than reconstructed from recollection

### Requirement: The deep review is routed and bounded

The repository SHALL make one routing decision per unit before any deep-review session is created, resolving to
`REQUIRED`, `RECOMMENDED`, or `NOT_NEEDED`, and SHALL run the `review-thorough` subagent on an outcome that selects it.
The decision's deterministic floor is `scripts/review-routing.py`'s; the pass's procedure and hand-over are
`/review-thorough`'s; the reviewer's dimensions, evidence, and grouping are its definition's; the routed pass's status
is recorded on the change's review task.

#### Scenario: Thorough review is available on demand

- **WHEN** a deep review pass is wanted (drift, correctness, architecture, conventions) — a manual `/review-thorough`
  invocation, or an outcome the deep-review routing decision below selects
- **THEN** the `review-thorough` subagent reviews the change against its delta specs, tasks, and the surrounding code,
  proportionately to the risk the decision's outcome names
- **AND** it inspects the dimensions `review-thorough.md` defines, at the depth that risk names
- **AND** every finding it raises identifies a concrete defect or material risk, cites repository evidence and a precise
  location, explains the consequence, gives a corrective direction, and distinguishes a confirmed defect from an
  uncertainty — never a preference, a speculation, or a second instance of a root cause it already reported
- **AND** a manual `/review-thorough` invocation is an explicit run: it is never a substitute for a `REQUIRED` outcome,
  and it does not route

#### Scenario: Deep review is routed by one three-outcome decision

- **WHEN** a unit's implementation is complete and its quick-review loop has run
- **THEN** one routing decision is made before any deep-review session is created, from the completed implementation,
  the diff, the applicable planning artifacts, and the existing review evidence, and it resolves to exactly one of
  `REQUIRED` (a deep review must run before completion), `RECOMMENDED` (elevated risk or uncertainty requires an
  explicit recorded resolution), or `NOT_NEEDED` (the change is clearly low risk and may bypass deep review)
- **AND** `REQUIRED` covers a material change to a public contract, a cross-component boundary, an Axon
  command/event/aggregate/saga/projection, persistence or a migration, a transaction, a retry, idempotency, concurrency,
  ordering, reactive behavior, security, infrastructure, deployment, or an operational failure mode, a major
  specification deviation, and a unit whose recorded rounds show a material defect an earlier review missed
- **AND** `RECOMMENDED` covers credible but non-mandatory risk — a complex invariant, an ambiguous material requirement,
  a broad interaction, substantial design uncertainty, or a plausible defect quick review and the tests are unlikely to
  find — and it is resolved explicitly from evidence rather than escalated or skipped by default
- **AND** `NOT_NEEDED` applies only where the change is localized, low risk, and adequately verified, and never on a
  criterion of line count, changed-file count, or the build agent's own confidence alone — a documentation-only,
  formatting-only, or mechanical change is an example of the class, not an unconditional exemption
- **AND** the decision has a deterministic floor derived from the change's own facts — its changed paths and the
  declarations in its diff, its delta spec's requirement-block kinds, and its recorded rounds — so the decision is
  explainable and reproducible rather than an unaided judgment, and the agent's decision is at or above that floor and
  records the signals it relied on
- **AND** the decision is reassessed only when material new evidence changes the risk

#### Scenario: A missing or failed routing decision cannot skip mandatory review

- **WHEN** the routing decision is absent, malformed, or its derivation fails
- **THEN** the outcome is `REQUIRED` rather than a silent bypass, so an unreadable or failed decision can only make
  review run, never skip it

#### Scenario: A low-risk change bypasses the deep-review lifecycle

- **WHEN** the routing decision resolves to `NOT_NEEDED`
- **THEN** no deep-review session is created, no hand-over is prepared, and no expensive-model invocation is made, while
  the mandatory quick review, the tests, and the manual approval still run

#### Scenario: A routed deep review runs in its own session with a task-relevant hand-over

- **WHEN** the routing decision requires or recommends a deep review and the owner resolves to run it
- **THEN** one dedicated `review-thorough` session is created for that change, its supported session identifier and
  review state are preserved, and it is handed only the task-relevant context `/review-thorough`'s procedure names
- **AND** the build agent, the quick reviewer, and the deep reviewer stay in separate sessions
- **AND** a follow-up pass on that change reuses the same deep-reviewer session, while a different change starts a fresh
  one
- **AND** the deep reviewer inspects the current state independently on every follow-up, because session continuity is
  not proof that a fix is correct
- **AND** its scope is the change's artifacts and the checks its own contract names rather than the repository at large,
  and a question its evidence cannot settle within that scope is reported as a limitation rather than pursued
  indefinitely — so a deep pass returns its best completed report rather than treating uncertainty as a reason for
  unbounded investigation

#### Scenario: A failed deep-review hand-over is detected, not reported as a review

- **WHEN** creating the session, handing over, continuing it, parsing its output, or validating it fails
- **THEN** the failure is detected and reported rather than treated as a successful review, reusing the loop's existing
  recovery where it applies
- **AND** where the session must be replaced, the current findings, their statuses, the diff, and the objective are
  transferred to the replacement, the lost continuity is recorded, and the current state is reviewed independently
- **AND** where no safe recovery is available, the unit stops and reports the blocker

#### Scenario: Deep review is bounded by a two-pass budget

- **WHEN** a routed deep review runs
- **THEN** its pass budget is the one `/review-thorough` defines
- **AND** a targeted re-review may verify a localized fix and its impact radius, and it does not conceal an unresolved
  material finding

#### Scenario: A deep-review finding is tracked to a resolved status

- **WHEN** a deep-review pass raises a finding
- **THEN** it is tracked by a stable identifier where the reviewer supports one, or otherwise by a stable root cause,
  description, and location mapping, and its status is distinguished as `OPEN`, `FIXED_AND_VERIFIED`, `REJECTED` with
  the evidence for the rejection, or `ACCEPTED` only by an explicit owner decision recorded with the reason and what it
  accepts
- **AND** the status has one authoritative record rather than a copy in several artifacts, and the quick loop's round
  ledger stays its own record, so a deep pass does not change the population the loop's trend is read from

#### Scenario: Deep review exhausts a class it raises

- **WHEN** a deep-review pass raises a finding that falls in a class
- **THEN** it searches the change's artifacts for every instance of that class's shape and reports them as one clustered
  finding, so a class surfaces whole in the deep pass rather than one instance per quick-review round

### Requirement: Lessons are captured after implementation

The repository SHALL capture a unit's durable lessons into the artifact that owns each one after the implementation's
quick review is clean, and SHALL record either the applied capture or the skip and its reason on the unit's record. The
owning destinations and their routing are `AGENTS.md`'s capture workflow; the scenarios below are carried unchanged
except as the Migration records, and the capture stage's own consolidation is a later unit.

#### Scenario: Lessons are captured after implementation

- **WHEN** a unit's implementation quick review is clean
- **THEN** the main agent decides from the unit's own evidence whether it shows a candidate lesson, and invokes the
  `lesson-capture` subagent only when it does — otherwise the capture is skipped and the skip recorded with its reason,
  so a skip is a recorded decision rather than a run that found nothing
- **AND** the `lesson-capture` subagent proposes each lesson as an addition to the artifact that owns it — an
  `AGENTS.md` gotcha or convention, a check/test authoring mode in the `check-and-test-authoring` skill, a
  shipping-procedure correction in the `ship-change` skill, a stage-procedure correction in the `review-gate` skill, or
  a section of the subject pack (`jvm-development`, `jvm-testing`, `gradle-build`, or `deployment-operations`) whose
  subject owns the lesson's decision — or a pair of additions when the lesson has both halves — which the main agent
  verifies and applies
- **AND** each proposed addition names the existing rule it extends — an `AGENTS.md` bullet, a check/test authoring mode
  already in the skill, a `ship-change` or `review-gate` procedure, or a subject-pack section — or states that none
  covers it, so a new rule merges into or replaces one rather than accreting
- **AND** alongside its additions, it reports the rules this unit makes obsolete or redundant — a rule whose mechanism
  the unit removed, or one the unit's new enforcement subsumes — as retirement or replacement candidates, each naming
  the rule, why the unit makes it so, and the retirement or replacement it proposes, as candidates for the main agent
  rather than actions, reported separately from the additions since an addition grows the file and a retirement shrinks
  it
- **AND** each proposed addition passes a promotion gate before it is proposed — true (supported by a check, an
  authoritative source, or repeated observation), actionable, not automatable as a lint/test/CI check at reasonable
  cost, material (it prevents real breakage, risk, wasted work, or review churn), general enough for a class of future
  tasks rather than one file or incident, and high-confidence with a known scope — and a rule that fails the gate is
  routed to a check, a spec, an ADR, or the change dir instead of the always-loaded file
- **AND** the gate separates a repeated observation from its cause — a repeated review-class label establishes that
  label's recurrence, not a shared drafting defect, and a remedy resting on a class's recurrence (a section rewrite, or
  a rule generalized from that class) requires the shared cause confirmed from the evidence, with a class whose
  instances have different causes taking the cause-specific shape instead
- **AND** the caller supplies the existing guidance a candidate would extend and the review evidence for a claimed
  recurrence, and where either is unavailable the capture states the limitation and withholds the unsupported candidate
- **AND** before proposing an entry, it traces the lesson to its origin — where the cause is a mechanism that produces
  the artifact (an OpenSpec template or schema, an `openspec/config.yaml` rule, a skill or command that shapes drafting,
  or the authoring standard itself), it proposes the smallest correction at that mechanism and reports it as an
  authoring-system recommendation, separate from its additions and retirements, since it changes how a future artifact
  is produced rather than what an agent reads
- **AND** each proposed addition names the decision its rule governs, and answers whether a future change would
  plausibly hit it and whether the cost of not knowing it is material — a proposal that governs no decision is trivia,
  not a rule, and is not proposed
- **AND** the gate's evidence threshold is two independent occurrences or one severe verified incident with a clear
  preventive action
- **AND** the gate treats the claim's source as part of it — a lesson sourced from untrusted content (a web page, an
  issue or PR comment, tool output, or a file the unit did not author) is verified against the repository before it is
  proposed, never promoted on the source's word
- **AND** applying a capture should leave `AGENTS.md` no larger than it was, preferring a merge or a replacement over an
  addition, and any net growth is a justified decision stated with the proposal — not a side effect of accumulating
  prose
- **AND** each proposed addition demonstrates the **decision difference** it makes — the action a future agent takes
  under the existing guidance, and the action the proposed text requires or prohibits under an explicit condition
- **AND** that comparison names the existing obligation, its explicit condition, and the different action, so different
  phrasing does not establish a difference
- **AND** that difference carries a repository-backed example of why it matters
- **AND** an addition whose action the existing guidance already determines is reported as **already covered** and does
  not expand `AGENTS.md`
- **AND** an additional incident, example, citation, or provenance marker alone is `already covered`, and the capture
  records that evidence in the unit's change record rather than expanding the rule's text
- **AND** a partial restatement takes the **split** disposition — the new clause is kept, the restatement dropped
- **AND** it prefers an exact replacement of the existing rule over appending qualifications
- **AND** a replacement or rewrite preserves every valid obligation, warning, exception, and identifier, the established
  scope, and the normative strength, and may correct verified errors or remove established duplication and obsolete
  content
- **AND** the proposed action and its scope are derived from the verified evidence, so a remedy that worked in one place
  is not stated as a universal instruction
- **AND** the incident evidence a rule rests on stays recoverable behind its `captured:` marker rather than in the
  rule's text
- **AND** the capture's verdict states the proposed net guidance delta in lines (`AGENTS.md` and the packs; additions
  minus retirements) and its authoring-system recommendation count, and the applying agent records the applied delta, so
  net growth is stated when it is decided, alongside the periodic audit's accreted-rule count
- **AND** when a rule's rationale is normative in a spec, the bullet keeps only what a reader needs to act and points at
  the spec — a pointer, not a condensed copy, since `AGENTS.md` is loaded on every invocation while a spec is loaded
  only when its capability is worked on
- **AND** when the specs describe a rule's subject only as an outcome, that mechanism belongs in the capability spec
  only when changing it would change a scenario's outcome — verified against the code before moving it; a mechanism
  whose alternatives yield the same result stays in `AGENTS.md` as internal control flow
- **AND** the control on that growth is the periodic `/audit-agents` pass, whose verdict already reports the
  accreted-rule count, rather than mass deletion to hit a number
- **AND** each proposed addition carries its origin in a greppable `captured: <unit>` marker — the unit, and its PR when
  a merge-time detection found the lesson rather than the implementation capture, since a capture runs once at
  implementation and a merge only detects and asks — so a reader can tell where the rule came from without consulting
  git, and a markdown reflow cannot take it
- **AND** the unit leaves a record the merge-time read can check — the capture's applied net guidance delta (`AGENTS.md`
  and the packs), or its recorded skip and reason — so neither an unrun capture nor an unrecorded skip reads as a run
  that found nothing

#### Scenario: A change that obsoletes a rule yields a retirement candidate

- **WHEN** a unit's implementation makes an existing `AGENTS.md` rule obsolete (the mechanism it describes is removed)
  or redundant (the unit's new enforcement subsumes it)
- **THEN** the `lesson-capture` subagent reports a retirement or replacement candidate naming the rule, why the unit
  makes it so, and the retirement or replacement it proposes — a candidate for the main agent, not an action
- **AND** the capture's verdict names the retirement count alongside its durable-proposal count

#### Scenario: A skipped capture is a missing record at the merge

- **WHEN** a unit is prepared for its merge
- **THEN** the merge-time read reports an implementation capture that neither ran nor recorded a skip — the unit's
  record missing or unticked — as unrun rather than reconstructing it from recollection
- **AND** it re-verifies a recorded skip against the merge's own evidence, running the capture there when the skip's
  condition no longer holds

#### Scenario: A lesson whose cause is an authoring mechanism is routed to it

- **WHEN** a unit's lessons trace to the mechanism that produces a technical artifact rather than to the artifact's
  content — a template or schema that encourages the wrong structure, a `config.yaml` rule that is silently dropped, a
  skill that prompts a shape the standard forbids
- **THEN** the `lesson-capture` subagent proposes the smallest correction at that mechanism and reports it as an
  authoring-system recommendation, naming the mechanism, the gap, the proposed change, and why an `AGENTS.md` entry
  would not suffice — separate from its additions and retirements, since it changes how a future artifact is produced
  rather than what an agent reads

### Requirement: Diagrams and screenshots are produced by their specialist subagents

The repository SHALL produce and inspect diagrams and screenshots through the specialist subagents — `vision` for visual
review and `diagrammer` for diagram geometry — rather than through the text-only main agent. The scenarios below are
carried unchanged from the requirement this one replaces; the diagram stage's own consolidation is a later unit.

#### Scenario: Screenshots are reviewed visually

- **WHEN** the main agent needs to inspect a screenshot, image, or visual UI state (e.g. web-UI styling, or a rendered
  diagram)
- **THEN** the `vision` subagent reads the image and returns a **structured visual review**, so the text-only main agent
  can delegate visual review
- **AND** the review states the artifact it inspected and the scope of the review
- **AND** it reports in three separated groups rather than one description: the **observed facts** (what is visibly
  present), the **visual risks or suspected defects**, and **what the image cannot settle**
- **AND** for a diagram render it checks every visible node and whether its shape matches its intended role, each edge's
  direction and arrowhead, which label belongs to which edge, duplicate, missing, or ambiguous labels, collisions,
  overlap, clipping, cropping, unreadably small content, and the whitespace left near the right edge and near any host
  control overlay
- **AND** each item names the visible element it concerns and its approximate position in the image
- **AND** a visual claim that needs a measurement to settle is reported as a **hypothesis** naming the method that would
  settle it (e.g. `getComputedTextLength()` for SVG text against its container, or a label's `scrollWidth` against its
  container's width) — an apparent clipping, overflow, or overlap is never reported as a confirmed fact
- **AND** it stays read-only and concise: it reports what it sees and never edits a file

#### Scenario: ASCII diagrams are drawn by the pro-model diagrammer

- **WHEN** an ASCII diagram needs to be created, aligned, or fixed (e.g. the README project tree or a diagram's
  plain-text fallback)
- **THEN** the `diagrammer` subagent renders it with the pro model: it establishes the semantic mapping (which span
  starts and ends where), aligns by character width, and preserves deliberate asymmetry — so the cheap flash main agent
  does not spend effort on ASCII geometry

#### Scenario: Mermaid diagrams are drawn by the pro-model diagrammer

- **WHEN** a Mermaid diagram needs to be created, fixed, or reworked (e.g. a README flow diagram)
- **THEN** the `diagrammer` subagent produces it with the pro model: it chooses the diagram type and constructs and
  keeps the content clear of the host renderer's controls
- **AND** it returns the diagram **source**, so the cheap flash main agent does not spend its own effort on Mermaid's
  rendering pitfalls
- **AND** it returns a **visual-review handoff** beside the source: the nodes, edges, directions, labels, and spans the
  render is expected to show, the rendered artifact that must be inspected, and the visual risks a source-level check
  cannot establish
- **AND** its own `mermaid-cli` render is reported as what it proves — that the source parses and lays out — never as
  proof of the host renderer's toolbar clearance or of a browser's layout

#### Scenario: A Mermaid render is visually reviewed before the diagram ships

- **WHEN** the `diagrammer` subagent has returned Mermaid source with its visual-review handoff
- **THEN** the calling agent makes the render `diagrammer` produced inspectable — serving it, since a browser cannot
  open a `file://` path — and delegates it to the `vision` subagent with the handoff, so the render is inspected rather
  than only parsed
- **AND** where the target is GitHub-rendered Markdown and Playwright is available, it also inspects a live GitHub
  render — the host `viewscreen` toolbar collision and browser layout a local render cannot reveal
- **AND** it reports the local render check and the visual review separately
- **AND** a claim the review raises as needing a measurement is settled by one before the diagram ships, since an
  observation is not a measurement
- **AND** an unavailable live GitHub render is stated as a limitation, never treated as equivalent to one that was
  inspected

### Requirement: The review gate's caller procedure is maintained in its project skill

The calling agent's review-gate procedure SHALL live in the project skill `.opencode/skills/review-gate/SKILL.md`, not
as a procedure body in the always-loaded `AGENTS.md`. The skill SHALL carry the stages' ordered steps, decision points,
failure handling, completion criteria, the identifiers its steps name, and a `Source:` line quoting the `AGENTS.md`
heading it replaced. A correction to a step SHALL be made in the skill.

#### Scenario: The skill carries the full procedure

- **WHEN** an agent follows the root entry point for the review gate
- **THEN** the skill carries the ordered steps, decision points, failure handling, completion criteria, and identifiers
  the procedure needs

#### Scenario: A correction lands in the skill

- **WHEN** a procedure step, decision point, or failure rule is wrong or incomplete
- **THEN** it is corrected in the skill rather than recreated in `AGENTS.md`

#### Scenario: Another artifact names the skill as the owner

- **WHEN** a command definition, a subagent definition, a spec, or `AGENTS.md` refers to the review gate's steps
- **THEN** it names the skill rather than restating the steps or naming `AGENTS.md` as their owner

### Requirement: The root carries the review-gate stage entry point

`AGENTS.md` SHALL carry the review gate's entry point — its trigger, its stages with the owner each delegates to, the
authorization stops that must be loaded before the gate runs, the instruction to load the skill before the first stage,
and the stop-and-report rule for a missing or unreadable skill. It SHALL NOT carry the procedure's steps.

#### Scenario: The root carries the stage entry point

- **WHEN** an agent has finished a unit's proposal or implementation and is about to commit, push, open a pull request,
  archive, or merge
- **THEN** `AGENTS.md` names the `review-gate` skill and its path and instructs the agent to load it before the first
  stage

#### Scenario: A missing destination stops the governed action

- **WHEN** the skill file is missing or unreadable at the review gate's entry point
- **THEN** the agent stops and reports it rather than running the gate from the root text

#### Scenario: Loading the skill grants no permission

- **WHEN** an agent has loaded the skill but the owner's approval is not established
- **THEN** no governed action follows, because the authorization boundary stays in `AGENTS.md` and the skill adds none
