# Spec Delta

## MODIFIED Requirements

### Requirement: Per-change quality-gate and analysis subagents are available

The repository SHALL provide locally-defined agent subagents under `.opencode/agent/` for the per-unit quality gates and
analysis workflows — the per-unit review and lesson-capture agents, and the visual and diagram agents — each described
by the scenarios below or by its own requirement in this capability. Each SHALL be invocable by the main agent, with its
purpose described in its agent definition and (where relevant) in `AGENTS.md`. A reviewer invocation SHALL be bounded
and recoverable: the pass it runs stops when what it was given has been examined, and an invocation that stops
responding is replaced from the caller's own record — so a reviewer's duration is bounded by what it was given rather
than by the repository, and a stall costs the caller a wait rather than the review.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: Quick review runs after proposal and implementation

- **WHEN** a unit's proposal (planning artifacts) or its implementation is finished
- **THEN** the `review-quick` subagent reviews it against the unit's planning artifacts, and the loop repeats until it
  reports no new observations before a manual review is requested
- **AND** the pass is a bounded screen over what it is given — the supplied diff and the explicitly supplied planning
  artifacts, and the risk focuses the caller supplies, and nothing else — so a repository-wide search is not part of it,
  and a local defect does not trigger one
- **AND** the caller supplies the screen's inputs as a list it can state and a reader can check — the artifacts to
  screen and the checks to run over them — and the pass's report names that list, so which surfaces were screened is
  verifiable from the report rather than inferred from its prose
- **AND** for a capture, the supplied list includes the existing rules each proposal claims to extend (or its statement
  that none covers it), the capture evidence (the round rows and the change record), and the `lesson-capture` definition
  where compliance with its gate is checked, so the comparison rests on materials the caller supplied rather than a
  search the screen would have to run
- **AND** the risk focuses are the caller's, stated with the invocation, and they widen which of the supplied surfaces
  the screen looks at closely rather than adding surfaces to screen — so a focus is a direction of attention within the
  bounded set, not a second scope
- **AND** its fixed scope is: coherence between the supplied proposal, design, tasks, delta specs, and diff; a completed
  task that visibly does not correspond to the supplied work; an obvious implementation-to-delta-spec mismatch; an
  explicit broken reference; an obvious convention violation in a changed file; the formatting of a changed file no
  formatter owns; and the prior-round finding instances the caller supplies
- **AND** the pass is not required to search for every instance of a finding class, to reproduce every claims-table
  source or inspect every scan candidate, to analyse tests mutation-style, or to analyse boundary conditions, error
  paths, callers, configuration, or documentation exhaustively beyond the supplied scope — that depth belongs to the
  deep review the routing decision below selects
- **AND** a risk the supplied scope cannot settle is escalated to `review-thorough` — through the routing decision below
  or by the caller raising it — rather than absorbed by widening this pass, so the quick screen stays bounded and the
  depth it cannot afford runs in the pass that has the budget for it
- **AND** its stop condition scales with what it is given rather than with the repository: the pass ends when each
  supplied artifact and each changed file has been examined once, so a larger diff is more work and a repository the
  change does not touch is none, and no second sweep of the same file is part of the screen
- **AND** it does not expand its own scope: it adds no surface beyond the supplied list and the checks its own contract
  names, and where a check cannot establish its evidence within that scope it reports the limitation rather than
  searching for more — so an unresolvable question narrows the report instead of extending the pass, and the pass
  returns its best completed report rather than treating uncertainty as a reason for unbounded investigation
- **AND** the pass's report states that scope and stop condition rather than implying its analysis was unlimited, so a
  bounded screen is not read as a thorough pass
- **AND** the governance the pass does not replace stays in force — the pre-write derivation pass, the mandatory
  quick-review gate, the tests, the manual review, and every other check
- **AND** for a unit whose diff adds or changes `AGENTS.md` rules (a capture), it also challenges each new rule's
  durability — the decision the rule governs, and whether a future change would plausibly hit it — so trivia is caught
  at review time rather than by a later consolidation audit
- **AND** it applies the capture scenario's promotion gate and dispositions to each proposal, from the supplied list
  above, so the reviewer compares the proposal with the existing obligation and its evidence rather than the gate's
  criteria being restated a second time, and a proposal the gate disposes of as `already covered`, or one governing no
  decision, is a finding against the capture
- **AND** each capture finding names the proposed text, the relevant existing obligation or supplied evidence, and the
  concrete defect
- **AND** where the supplied materials cannot establish a comparison or a causal claim, the review reports that specific
  limitation and whether the missing material is one the capture contract requires — a capture defect — or a limit of
  the bounded screen, and an uncertainty alone is not reported as a confirmed finding
- **AND** each finding is classified from the canonical list the reviewer's contract carries — each class naming what
  must change, and the calling agent extending the list in the same change when a finding fits none — and when the
  caller's request names a prior round's findings, a finding whose class a prior round already raised is reported as a
  repeat of that class, naming it — so the loop's convergence signal comes from the review's output rather than the
  caller's recollection, and the caller re-derives the root cause on that signal rather than patching the instance
- **AND** every round emits one machine-readable record line — the date, the unit, the round (continuous per unit across
  its proposal and implementation reviews), the classes raised (comma-separated; `-` when none), which repeated a prior
  class (comma-separated; `-` when none), and the count of findings that were a compliance miss of an existing rule —
  for the calling agent to append to `docs/review-rounds.tsv`, so the digest can trend the loop's cost and the pre-write
  pass can be evaluated
- **AND** the calling agent appends the round's row with the reviewer-session fields the ledger's contract declares —
  the session identifier, whether the session was created, continued, or replaced, and the replacement's reason when it
  was replaced — supplying the identifier the invocation returned rather than inventing one, so the record evidences the
  transition instead of leaving it to recollection
- **AND** with no prior-round list supplied, the review still classifies each finding and states that a repetition is
  unknown rather than inferring one, since the classification cannot be compared against what it has not seen
- **AND** a defect the applying command rewrites — `./gradlew spotlessApply`, or the web module's `npmFormat` — is
  routed to that command rather than reported as a finding; the boundary is the command's own rewrite, not a category of
  violation, so what it leaves alone — an inline code span's interior, a file type no formatter owns — stays reportable
- **AND** for a record artifact (a retrospective, a load-test record), a later round raising only new instances of an
  already-raised class stops and escalates to the owner rather than iterating — such an artifact has no shipped behavior
  to converge on, so the escalation names the loop that cannot converge rather than continuing
- **AND** every unit carries the pre-write derivation pass as a record — a task in the change's `tasks.md`, or a line in
  the pull-request checklist for a unit with no change dir — so a skipped pass is a missing box rather than looking like
  a pass that found nothing
- **AND** the pass emits a claims table — each fact the change states about its evidence, the prior artifacts it rests
  on, with its source or the deriving command — so the review reports a claim the supplied artifacts state that the
  table omits, a row whose stated source plainly does not reproduce, or a scan candidate neither resolved into a row nor
  listed as a dismissal, without re-deriving every row from scratch
- **AND** the pass seeds that table mechanically: a scan of the authored set's added lines (`scripts/claim-scan.py`, run
  through `/seed-claims`) lists each claim shape it finds — a count, an absolute quantifier, a cross-artifact reference,
  a backticked path — with its location, and the author resolves each candidate into a row or records its dismissal
  beside the table (grouped by kind where the dismissal is uniform, and a row's source states the reason by class rather
  than a list of a command's output lines), with the reason, so a claim the author did not notice stating is listed
  rather than waiting for a review round to find it, and the scan's output is accounted for; the table is re-derived
  after the unit's last append to its own record, which adds a candidate to the scan; the scan seeds the table and never
  replaces the derivation, since it enumerates shapes, not facts
- **AND** the caller passes each prior finding's instance (`file:line` and the claim), not only its class, so a repeat
  is reported as an unfixed prior instance or as a new instance of an exhausted class
- **AND** the caller runs a reviewer-session preflight immediately before each invocation: it creates the reviewer's
  session on the unit's first round with a fresh invocation and retains the identifier that invocation returns; on every
  later round it invokes the reviewer by passing that saved identifier as the invocation's session-continuation
  parameter, never a fresh invocation labelled as a continuation; and it starts a replacement only after the invocation
  it replaces — a continuation attempt, or the unit's first round's fresh invocation — has produced one of the recovery
  cases below, recording the concrete reason and both identifiers — the session that failed and its replacement's —
  before it replaces
- **AND** after a continuation invocation the caller compares the identifier the invocation returns with the one it
  saved, and a difference stops the round and is reported as a session-continuity failure rather than recorded as a
  continuation — so a loop that restarted its reviewer each round is distinguishable from one that continued it, rather
  than resting on the caller's recollection
- **AND** the only cases that justify a replacement are: the prior session expired, the reviewer's definition changed
  after that session began, or the reviewer stopped responding mid-round (the loop has observed a session stall with no
  reply, which no wait resolves)
- **AND** the reuse never substitutes for the per-round instance hand-off above, which the caller supplies on every
  round regardless of the reviewer's session
- **AND** the review checks, in one line each and within the supplied scope, the proposal's coherence and whether the
  required behavior is represented and its tasks cover the tests and documentation; the implementation's obvious
  boundary conditions and changed semantics (defaults, failure handling, retries, ordering, concurrency), a test in the
  diff that would still pass with the behavior broken, and the changed files' effect on callers, configuration, and
  documentation; and the conventions visible in the changed files (naming, dead code, missing mandated Javadoc,
  documentation still describing superseded behavior, and broken cross-references) — so the pass names what to look at
  without becoming a thorough review
- **AND** a finding carries the evidence its class needs — an enumeration against the corpus it summarizes, a
  contradiction against both statements, a missing scenario against the behavior and the delta that omits it, a
  `test-control` against the expected behavior and the failure it should detect, an `impact-omission` against the
  affected site — so a suspicion the evidence cannot establish is not reported as a confirmed defect
- **AND** the report is ordered by practical importance — incorrect behavior and broken contracts; missing required
  behavior, scenarios, or records; contradictions between the implementation, the plan, and the evidence; unsupported
  claims and invalid premises; documentation and rule violations; formatting last — the order guiding presentation, not
  what is reported
- **AND** each finding names its class, its location, the issue, one line of evidence, and the required correction
- **AND** a defect a prior round's fix only partially corrected, moved, or rewrote is still a recurrence of the same
  problem, reported as such

#### Scenario: A stalled reviewer is replaced from a recorded handover

- **WHEN** a reviewer invocation has not responded by the deadline the caller's policy supplies
- **THEN** the caller records the failure as `stopped-responding`, naming the stalled session's identifier and its
  replacement's, and creates the replacement session only after that record
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

#### Scenario: Thorough review is available on demand

- **WHEN** a deep review pass is wanted (drift, correctness, architecture, conventions) — a manual `/review-thorough`
  invocation, or an outcome the deep-review routing decision below selects
- **THEN** the `review-thorough` subagent reviews the change against its delta specs, tasks, and the surrounding code,
  proportionately to the risk the decision's outcome names
- **AND** the dimensions it inspects are specification compliance and the delta-spec scenarios; correctness, state
  transitions, failure paths, retries, timeouts, and idempotency; architecture, dependency boundaries, and deviations
  from the approved design; concurrency, reactive behavior, ordering, transaction boundaries, persistence, and
  distributed failure modes; the tests and the verification a concrete risk is missing; security, trust boundaries,
  configuration, deployment, observability, and operational risk; and the applicable repository conventions and
  structured-writing rules
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
  review state are preserved, and it is handed only the task-relevant context — the change identity, the current diff,
  the applicable OpenSpec artifacts, the prior findings and their statuses, the current pass, the known limitations, and
  whether the pass is an initial review or a fix verification
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
- **THEN** its default budget is two full passes — an initial deep review, and an independent verification after the
  valid findings are fixed — and a further full pass requires an explicit recorded decision resting on the material
  residual risk and the expected value of the pass
- **AND** a targeted re-review may verify a localized fix and its impact radius, and it does not conceal an unresolved
  material finding
- **AND** at the budget limit an unresolved blocking finding prevents normal completion, an unresolved should-fix
  finding requires an explicit resolution or a recorded acceptance, incomplete verification and uncertainty stay
  visible, and the limit never accepts residual material risk by itself

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

#### Scenario: Lessons are captured after implementation

- **WHEN** a unit's implementation quick review is clean
- **THEN** the main agent decides from the unit's own evidence whether it shows a candidate lesson, and invokes the
  `lesson-capture` subagent only when it does — otherwise the capture is skipped and the skip recorded with its reason,
  so a skip is a recorded decision rather than a run that found nothing
- **AND** the `lesson-capture` subagent proposes each lesson as an addition to the artifact that owns it — an
  `AGENTS.md` gotcha or convention, a check/test authoring mode in the `check-and-test-authoring` skill, or a
  shipping-procedure correction in the `ship-change` skill — or a pair of additions when the lesson has both halves —
  which the main agent verifies and applies
- **AND** each proposed addition names the existing rule it extends — an `AGENTS.md` bullet, a check/test authoring mode
  already in the skill, or a `ship-change` procedure — or states that none covers it, so a new rule merges into or
  replaces one rather than accreting
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
- **AND** the capture's verdict states the proposed net `AGENTS.md` delta in lines (additions minus retirements) and its
  authoring-system recommendation count, and the applying agent records the applied delta, so net growth is stated when
  it is decided, alongside the periodic audit's accreted-rule count
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
- **AND** the unit leaves a record the merge-time read can check — the capture's applied net `AGENTS.md` delta, or its
  recorded skip and reason — so neither an unrun capture nor an unrecorded skip reads as a run that found nothing

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

### Requirement: Project-owned agent tooling is audited for consistency and conciseness

The repository SHALL provide an `agents-auditor` agent subagent that audits the project-owned agent tooling —
`AGENTS.md`, the project-authored files under `.opencode/agent/`, `.opencode/commands/`, and `.opencode/skills/`, and
the guidance evidence records under `docs/guidance-evidence/` — for **consistency** and **conciseness**, and reports its
findings without editing files. The audit SHALL exclude files the repository does not author: the generator-owned
OpenSpec instruction files (the commands and skills written by `openspec update`) and the vendored `axon4to5-*` skills
(copied verbatim from the upstream AxonIQ repository); a project-authored file that shares a generated file's name
prefix (e.g. the `opsx-tool-update` command) SHALL remain in scope. The audit SHALL cover, at least: contradictions
between entries or files, stale claims and enumerations the repository has outgrown, dead cross-references, and drift
from the code/workflows/specs the prose describes (consistency); and duplicated or near-duplicate entries, one-off
trivia, over-long or over-specific entries, and misplaced entries (conciseness). Within conciseness the audit SHALL
report these analyses as standing findings rather than only under a scoped run: **merge candidates**, each naming the
overlapping or complementary entries and the merged text that preserves every anchor and piece of evidence the originals
carried — and never blending two distinct lessons into one — and where the merged text would only restate a rule the
file already carries, the candidate is a deletion of the duplicate rather than a merge, reported as a `remove` so the
verdict's count includes it, and the audit SHALL say which it is; **removal candidates**, each naming a rule that
governs no decision (trivia, not a rule — the same test the capture's filter applies), what would be lost, and whether
git preserves it; and **route candidates**, each naming a rule whose subject a deterministic mechanism already enforces
— a build or CI gate, a lint or test, a CLI validation, or a deterministic workflow step such as the archive-time spec
sync, but never a review, whether a human's or a review subagent's — and proposing to reduce the rule to a pointer at
that mechanism, or move its content to the spec or ADR that owns it, naming the mechanism, what would be lost, and
confirming the mechanism exists and covers the rule's subject; and **extraction candidates**, each a verified proposal
to partition one `AGENTS.md` unit into a minimal retained root entry point, an authoritative destination that loads or
is discoverable when its trigger occurs, and an explicit retrieval path from that trigger to that destination, as the
conditions below define. All are candidates for the owner, not actions. Each finding SHALL be verified against the
repository rather than inferred from the prose alone, and SHALL be reported with its location and a concrete suggested
rewrite. A reduction candidate — a merge, removal, route, or extraction — SHALL carry seven fields: its location; the
decision its rule changes; the task or condition that requires it; the unique content the change would otherwise lose
(every imperative, warning, exception, identifier, rationale, provenance marker, and verification); its current loading
cost; its disposition; and the repository evidence that the surviving guidance preserves the same obligation. A merge
candidate whose merged text delegates content to a target — a spec or an ADR it points the reader to — SHALL have that
delegation verified against the target: its behavior **and** the identifiers, declarations, and gate conditions the
delegated content names, not the behavior alone. A delegated item the target does not carry SHALL stay in the merged
text, and the candidate SHALL say which items it kept for that reason. The subagent SHALL propose its findings without
modifying files; the main agent verifies and applies those the user approves.

The audit SHALL additionally report an **instruction-budget ledger** for `AGENTS.md`, measuring the always-loaded
instruction surface rather than judging it: the file's total size in bytes and lines; its size by top-level section; the
effective bound on that surface together with the source establishing it, stating whether the loading mechanism applies
a limit of its own; the first rule that falls beyond a derived effective threshold, expressed as an estimate with its
derivation rather than as a position at which content is dropped; the growth since the newest prior report under
`docs/audits/`; and the sections and `captured:`-marked rule units responsible for that growth. The ledger SHALL
estimate the bytes safely removable from the always-loaded surface, SHALL list the rules that must remain loaded with
the risk their unavailability would carry, SHALL list the rules safe to load on demand with their trigger and retained
pointer, SHALL tabulate the extraction candidates with their destination type and retrieval-path result, SHALL carry a
separate **extraction opportunities** subsection after the ready candidates — excluded from the verdict counts, stating
that its items require the owner's selection and a dedicated design or implementation change, distinguishing an existing
destination that failed retrieval from a destination that does not yet exist, and collapsing "no opportunity" to one
line — and SHALL state the post-change verification: re-measurement, pointer-target verification, retrieval-path tests,
a search for lost unique terms, and the review gate. The ledger SHALL NOT invent a limit the loading mechanism does not
apply, and a bound it reports as configured SHALL cite the configuration that sets it.

The audit SHALL keep root `AGENTS.md` guidance scoped to what an agent must see before safely starting work: a rule a
narrower task scope governs, and whose destination is durable, project-authored, owned, and loaded or deterministically
discoverable when that scope's trigger occurs, is a candidate to move out of the always-loaded surface. The audit SHALL
NOT propose a move that would leave the reader to infer a warning, an imperative, an exception, or a target-less fact
the retained root text does not state, and SHALL NOT report a rule as a removal candidate merely for being old,
uncommon, or lengthy — the accretion class's rule for a meta rule governs that case. Where the passage's intended
structure or meaning cannot be recovered confidently, the audit SHALL report the ambiguity rather than invent a rewrite.

The audit SHALL propose an **extraction candidate** only where every one of these holds: the candidate governs a
narrower task scope than root `AGENTS.md`; its destination is durable, project-authored, and owned; that destination is
loaded or deterministically discoverable when the trigger occurs; the retained root text lets an agent identify the
trigger and the required next action without reconstructing them; the move preserves every unique imperative, warning,
exception, identifier, verification, and provenance marker; the destination already carries, or the proposed change to
it will carry, the complete transferred contract; the move reduces the always-loaded surface by a material, measured net
amount — the removed span less the retained root text and any always-loaded destination metadata, with the destination's
on-demand load and the frequency of its trigger stated, and the total maintained content before and after; and it does
not split a procedure across files such that a reader must guess an order, condition, or failure path. The audit SHALL
derive the preferred destination type from the content's own class before selecting one: a global safety, branch,
review, or repository-wide workflow **invariant** — its authorization boundary, its entry trigger, any ordering that
binds across the stage, and the missing-destination handling — stays at root `AGENTS.md`, while that workflow's
stage-specific execution mechanics (its ordered steps, commands, decision branches, and recovery handling) MAY move to a
project-owned destination only where the retained root entry point requires reading that destination completely before
the governed decision. This permission is limited to a stage whose trigger is that decision: it does not extend to
guidance needed across more than one stage or before a stage can be identified, to a mechanism whose change would change
a capability's specified behavior, or to a warning, imperative, or exception the retained root text leaves to be
inferred; guidance needed only while working below one unambiguous directory goes to a nested `AGENTS.md`; behavior that
changes a capability outcome to an existing or proposed capability spec; a durable architecture decision, rationale, or
trade-off to an existing or proposed ADR; a procedure invoked through a named task entry point to an existing command or
skill; incident history, diagnostic evidence, or a large reference inventory used by several future tasks to an existing
durable reference document; and a subject a deterministic mechanism enforces to that mechanism's authoritative
configuration or test, with a concise root pointer. The audit SHALL NOT propose a generic "documentation" destination
and SHALL NOT create a catch-all extracted-guidance directory, and a proposed new destination SHALL have a clear owner,
a stable scope, and evidence that more than one future task can discover and use it. Internal control flow whose
alternatives yield the same result SHALL stay in `AGENTS.md`, except where it is a workflow's stage-specific execution
mechanics that satisfy the permission above, in which case it MAY move to that workflow's project-owned procedure
destination; it SHALL NOT move into a capability spec even where the spec describes the same outcome, and only a
mechanism whose change would change the capability's specified behavior is a candidate for a capability-spec
destination. Before reporting a candidate the audit SHALL test the retrieval path — starting from the root instruction
set, stating the task trigger, following only the retained root text and normal project discovery, and confirming the
destination is reached before the governed decision must be made and carries every transferred requirement and
verification step. A failed retrieval path to an **existing** destination remains a reason to retain the source content,
and is not an extraction opportunity. For an opportunity the audit SHALL NOT claim that a destination which does not
exist passed the test: instead it SHALL verify whether the proposed retained root entry point could name the trigger and
the destination deterministically, record the missing target or discovery mechanism as the blocking precondition, and
require the dedicated change to create the target, add the retained root pointer, and run the full test before the
extraction becomes applicable.

The audit SHALL additionally report, as an **extraction opportunity** kept separate from its findings, a source unit
that is likely too narrow or too costly for root `AGENTS.md` but whose destination does not yet exist — and which
therefore cannot yet pass the retrieval-path test. An opportunity is not a finding: it SHALL NOT be counted in the
verdict line, SHALL NOT be reported as a removal or a route candidate, and SHALL NOT be applied by the standing
reduction unit — it is a design recommendation for the owner, who selects it and carries it out in a dedicated change.
The audit SHALL report an opportunity only where the source is materially costly in root guidance and the destination
type can be justified from the content's class, and it SHALL NOT report one merely to reduce the file's size: where the
source is global or cross-cutting — guidance needed across more than one stage, or before a stage can be identified — or
cannot be retrieved before its decision is needed, the audit SHALL retain it at root. Each opportunity SHALL state that
its proposed destination is a design hypothesis rather than an existing source of truth, and SHALL carry the source unit
and its bounded content, the decision it governs and when it is needed, its current loading cost and why that cost is
material, its content classification, its recommended destination type, the exact existing target or a proposed path
with its owning artifact type, the missing precondition that keeps it from being a candidate, the content that must
remain at root until a destination is created and verified, the intended root-to-destination retrieval path once a
future change creates the target, and one recommended next action — retain, create the destination in a dedicated
change, or reject the extraction.

The audit SHALL additionally report, as an **advisory** class kept separate from its fix findings and reported without
severity, third-party inconsistency: a file the audit excludes (a generated `openspec-*` instruction file, or a vendored
`axon4to5-*` skill) that contradicts how this repository uses it. The class SHALL be bounded by a harm test — reported
only where the contradiction would mislead a workflow driven by the file, or instruct a pattern the repository's code or
conventions contradict, never a mere textual difference from the repository's own prose. Each item SHALL name the harm
and the decision it invites (report it upstream, re-vendor at a newer version, or change the repository's usage) and
SHALL NOT propose a local edit to the excluded file, which the exclusion rule forbids.

The audit SHALL additionally report, as an **accretion** class kept separate from both its fix findings and its advisory
class, the in-scope rules that are meta rather than product — a rule about the agent, its tooling, the per-change
workflow, or the documentation, as opposed to a fact about the product. Each item SHALL name the rule, the origin that
introduced it (established from the in-prose `captured:` marker where present, and from `git blame` / `git log -S`
otherwise), and the source used to establish it. The class SHALL be reported without severity and is not a defect: an
accreted rule may be correct and load-bearing, so the audit SHALL NOT propose removing or merging it for being meta — a
meta rule that governs no decision is a removal candidate under the conciseness analysis, not a victim of its class —
that is the user's decision on the report. The class's count SHALL use one fixed metric, so successive reports are
comparable: the number of distinct meta rule units carrying at least one `captured:` marker, each counted once however
many markers it carries — never the raw marker count, and never the number of items the report lists — and a rule
written before the marker convention carries none and falls outside the metric. The audit SHALL also state how many of
those rule units are new since the newest prior report under `docs/audits/`, reporting `none recorded` when no prior
report exists and that the figure is unavailable when it cannot be determined.

The audit SHALL additionally report, as an **authoring-system gap** class kept separate from its fix findings and
reported without severity, a recurring defect whose cause is a mechanism that produces or maintains the artifact rather
than the artifact's content. Each item SHALL name the mechanism, the gap in how that mechanism communicates or enforces
the applicable standard, the smallest correction at the mechanism, and how that correction would prevent or reduce the
recurrence. The class SHALL be bounded by evidence: a defect reported from a single artifact with no established link to
the mechanism that produced it is a content defect under the consistency axis, not an authoring-system gap, and the
class SHALL NOT become a route for speculative rules. The correction's handling SHALL follow the mechanism's scope: for
an in-scope mechanism (an agent definition, command, or skill under `.opencode/`) the audit proposes the correction
directly; for an editable project-owned mechanism outside the audit's scope (the `openspec/config.yaml` per-artifact
rules) it reports the gap with the correction routed to the change workflow, never applied as a local edit; and for an
excluded mechanism (a generated OpenSpec instruction file, or a vendored skill) it reports the item under the advisory
class and never proposes a local edit to it. A correction SHALL reference the authoritative standard rather than copy it
into the mechanism.

Every item the report carries SHALL also carry the shared report contract's per-item budget, which that requirement
owns, not this one.

#### Scenario: An agent-tooling audit is produced

- **WHEN** the main agent invokes the `agents-auditor` subagent (e.g. via the `/audit-agents` command)
- **THEN** it returns findings grouped by severity, covering consistency (contradictions, stale claims, dead
  cross-references, drift) and conciseness (duplication, trivia, length, placement) across the audited project-owned
  tooling — `AGENTS.md`, the project-authored `.opencode/` files, and the `docs/guidance-evidence/` records, each with a
  location and a suggested rewrite — plus, separately, any advisory third-party inconsistency, the accreted meta rules
  with their origins, and any authoring-system gap, with the merge, removal, and route candidate counts named in the
  verdict line

#### Scenario: Generated and vendored files are excluded

- **WHEN** the `agents-auditor` subagent selects what to audit
- **THEN** it excludes the OpenSpec instruction files written by `openspec update` and the vendored `axon4to5-*` skills,
  whose correct state is defined by their generator or upstream rather than by this repository — while a
  project-authored file with a similar name (the `opsx-tool-update` command) stays in scope

#### Scenario: Findings are verified against the repository

- **WHEN** the `agents-auditor` subagent flags a claim, enumeration, or cross-reference
- **THEN** it checks the claim against the repository (the referenced files, workflows, or specs) before reporting it,
  so a deliberate or still-true entry is not reported as stale

#### Scenario: The auditor does not edit files itself

- **WHEN** the `agents-auditor` subagent runs
- **THEN** it returns proposed findings and rewrites without modifying files; the main agent verifies and applies those
  the user approves

#### Scenario: An excluded file inconsistent with our usage is reported as advisory

- **WHEN** an excluded file (a generated instruction file or a vendored skill) contradicts how this repository uses it —
  for example a vendored migration skill instructing a pattern the codebase no longer follows
- **THEN** the audit reports it in a separate advisory class, naming the harm and the decision it invites (report
  upstream, re-vendor, or change our usage), and never proposes a local edit to that file

#### Scenario: A mere textual difference is not reported

- **WHEN** an excluded file differs from the repository's own prose but would not mislead a workflow or instruct a
  contradicted pattern
- **THEN** the audit does not report it, so the advisory class stays limited to contradictions that would change what a
  reader does

#### Scenario: Accreted meta rules are reported with their origin

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports the in-scope rules that are meta rather than product as a separate accretion class, each with the
  origin that introduced it (the `captured:` marker where present, `git blame` / `git log -S` otherwise) and the source
  used, without proposing that the rule be removed or merged for being meta

#### Scenario: The accretion count uses one fixed metric

- **WHEN** the `agents-auditor` subagent reports its accretion class
- **THEN** its verdict states the count as the number of distinct meta rule units carrying at least one `captured:`
  marker, each counted once regardless of how many markers it carries, together with the number of those rule units new
  since the newest prior report under `docs/audits/` (`none recorded` when no prior report exists), so the count is
  comparable across successive reports

#### Scenario: Merge and removal candidates are standing findings

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports merge candidates (with a merged text that preserves every anchor and piece of evidence the
  originals carried, and never blending two distinct lessons — or, where that text would only restate a rule the file
  already carries, a deletion of the duplicate, still counted as a `remove`, saying which) and removal candidates (rules
  that govern no decision — trivia, not a rule — with what would be lost and whether git preserves it) — as findings for
  the owner, not actions

#### Scenario: A merge candidate's pointer target is verified

- **WHEN** a merge candidate's merged text points at a spec or an ADR for content it removes
- **THEN** the audit verifies the target carries the delegated content — its behavior and the identifiers, declarations,
  and gate conditions the delegated text names — and keeps in the merged text any delegated item the target does not
  carry, saying which it kept for that reason

#### Scenario: A rule enforced by a deterministic mechanism is a route candidate

- **WHEN** the `agents-auditor` subagent finds a rule whose subject an in-place deterministic mechanism already enforces
  (a build or CI gate, a lint or test, a CLI validation, or a deterministic workflow step such as the archive-time spec
  sync)
- **THEN** it reports a route candidate naming the mechanism and proposing to reduce the rule to a pointer at it, or to
  move the rule's content to the spec or ADR that owns it, with what would be lost — as a candidate for the owner, not
  an action

#### Scenario: A rule detectable only by review is not a candidate

- **WHEN** the only detector of a rule's violation is a review — a human's or a review subagent's — and no deterministic
  mechanism enforces it
- **THEN** the audit does not report the rule as a removal or route candidate, because review is not a gate and the rule
  may itself be what makes the violation visible to the reviewer

#### Scenario: A recurring defect is traced to the authoring mechanism

- **WHEN** the `agents-auditor` subagent finds a defect that recurs across the artifacts one mechanism produces — a
  skill that keeps emitting dense procedural prose, or a command that omits a step the workflow needs
- **THEN** it reports an authoring-system gap naming the mechanism, the gap in how that mechanism communicates or
  enforces the standard, the smallest correction at the mechanism, and how the correction would prevent or reduce the
  recurrence — separate from its fix findings and without severity

#### Scenario: A one-off defect is not an authoring-system gap

- **WHEN** a defect appears in a single artifact and no link to the mechanism that produced it is established
- **THEN** it is reported as a content defect under the consistency axis, not as an authoring-system gap

#### Scenario: An authoring-system gap in an excluded mechanism is advisory

- **WHEN** the mechanism responsible for a recurring defect is excluded (a generated OpenSpec instruction file, or a
  vendored skill)
- **THEN** the audit reports the gap under the advisory class, naming the harm and the decision it invites, and never
  proposes a local edit to the excluded mechanism

#### Scenario: An instruction-budget ledger is reported

- **WHEN** the `agents-auditor` subagent audits `AGENTS.md`
- **THEN** it reports an instruction-budget ledger naming the file's total size, its size by top-level section, the
  effective bound on the always-loaded surface and its source, the first rule beyond the derived effective threshold,
  the growth since the newest prior report under `docs/audits/`, and the sections and `captured:`-marked rule units
  responsible for that growth

#### Scenario: The ledger states the bound's source and invents no limit

- **WHEN** the loading mechanism applies no size limit of its own
- **THEN** the ledger says so and reports the effective bound with the source that establishes it, rather than reporting
  a configured limit that does not exist

#### Scenario: A reduction candidate carries its decision and its cost

- **WHEN** the auditor reports a merge, removal, route, or extraction candidate
- **THEN** the candidate names its location, the decision its rule changes, the task or condition that requires it, the
  unique content the change would otherwise lose, its current loading cost, its disposition, and the evidence that the
  surviving guidance preserves the same obligation

#### Scenario: An extraction candidate meets every condition

- **WHEN** the auditor proposes an extraction candidate
- **THEN** every condition the extraction analysis states holds, and the candidate names its destination type, the
  retained root text, and the authoritative destination

#### Scenario: A failed retrieval path is a reason to retain the content

- **WHEN** the retrieval-path test cannot reach the destination before the governed decision must be made, or the
  destination omits a transferred requirement or verification step
- **THEN** the audit reports the failure and retains the content rather than proposing the extraction

#### Scenario: Internal control flow is not extracted

- **WHEN** a spec describes the same outcome but changing the rule's mechanism would not change the capability's
  specified behavior
- **THEN** the audit keeps that control flow out of the capability spec; it stays in `AGENTS.md` unless it is
  stage-specific execution mechanics that satisfy the complete-read permission

#### Scenario: A rule is not a removal candidate for being lengthy

- **WHEN** a rule is old, uncommon, or lengthy but governs a decision
- **THEN** it is not reported as a removal candidate, and the reason it stays is stated

#### Scenario: Guidance is not moved where the reader would infer it

- **WHEN** the retained root text would leave a warning, an imperative, an exception, or a target-less fact to be
  inferred
- **THEN** the audit keeps the rule at root rather than proposing the move

#### Scenario: An extraction opportunity is reported where no destination exists

- **WHEN** a source unit is materially costly in root guidance but no existing project-authored destination owns its
  subject
- **THEN** the audit reports an extraction opportunity naming its content classification, its recommended destination
  type, the proposed destination with its owning artifact type, and the missing precondition, and states that the
  proposed destination is a design hypothesis rather than an existing source of truth

#### Scenario: An extraction opportunity is not a finding and is never applied

- **WHEN** the audit reports an extraction opportunity
- **THEN** it is excluded from the verdict line, is not reported as a removal or a route candidate, and the standing
  reduction unit never applies it

#### Scenario: A global rule is retained rather than extracted

- **WHEN** the source is needed across more than one stage, before a stage can be identified, or cannot be retrieved
  before its decision is needed
- **THEN** the audit retains it at root and reports no extraction opportunity for it

#### Scenario: A failed path to an existing destination is not an opportunity

- **WHEN** a destination exists but the retrieval path to it fails
- **THEN** the audit reports the failure as a reason to retain the source content, not as an extraction opportunity

#### Scenario: A proposed new destination needs an owner and a scope

- **WHEN** the audit proposes a destination that does not exist yet
- **THEN** it names the owning artifact type, a stable scope, and evidence that more than one future task can discover
  and use it, and it proposes no generic documentation destination and no catch-all extracted-guidance directory

#### Scenario: The ledger carries the opportunities subsection

- **WHEN** the audit reports its instruction-budget ledger
- **THEN** a separate extraction-opportunities subsection follows the ready candidates, is excluded from the verdict
  counts, states that its items need the owner's selection and a dedicated design or implementation change,
  distinguishes an existing destination that failed retrieval from a destination that does not yet exist, and collapses
  "no opportunity" to one line

#### Scenario: An evidence section whose rule is gone is a pairing finding

- **WHEN** a section of a `docs/guidance-evidence/` record carries a `Rule:` paragraph that matches no live `AGENTS.md`
  rule
- **THEN** the audit reports it as a pairing finding, naming the record's path and the section's heading

#### Scenario: A converted rule whose evidence section is missing is a pairing finding

- **WHEN** a rule carries an `evidence:` token naming a record that holds no section whose `Rule:` paragraph matches
  that rule
- **THEN** the audit reports it as a pairing finding, naming the rule and the record

#### Scenario: A section matching a rule that carries no token is a pairing finding

- **WHEN** a record section's `Rule:` paragraph matches a live `AGENTS.md` rule that carries no `evidence:` token
- **THEN** the audit reports it as a pairing finding, naming the section and the rule

#### Scenario: A stage procedure's mechanics move behind a named entry point

- **WHEN** the auditor weighs an extraction whose source is a workflow stage triggered by a request
- **THEN** the candidate keeps the stage trigger, the authorization boundary, the cross-stage ordering, and the
  missing-destination handling at root, names the project-owned destination the entry point requires reading completely
  before the governed decision, and reports the removed span, the retained root text, the added always-loaded metadata,
  the on-demand load, the trigger's frequency, and the total maintained content

## ADDED Requirements

### Requirement: The shipping procedures are maintained in their project skill

The procedures for shipping a local change and for completing a cloud agent's pull request SHALL live in the project
skill `.opencode/skills/ship-change/SKILL.md`, not as procedure bodies in the always-loaded `AGENTS.md`. The skill SHALL
carry each procedure's ordered steps, decision branches, failure handling, completion criteria, and the identifiers
those steps name.

#### Scenario: The skill carries the full procedure

- **WHEN** an agent follows the root entry point for either stage
- **THEN** the skill carries the ordered steps, decision branches, failure handling, completion criteria, and
  identifiers the procedure needs

#### Scenario: The root does not carry the procedure body

- **WHEN** a reader inspects either procedure's steps, branches, or failure handling
- **THEN** they are in the skill body, and `AGENTS.md` carries only the stage entry point that names it

### Requirement: The root carries the shipping stage entry point

`AGENTS.md` SHALL carry the retained root entry point the extraction conditions define, together with the instruction to
load the skill before the governed decision and the stop-and-report rule for a missing or unreadable skill. It SHALL
name the skill and its path.

#### Scenario: The root carries the stage entry point

- **WHEN** an agent is about to push, open a pull request, archive a change, or merge a pull request
- **THEN** `AGENTS.md` names the `ship-change` skill and its path and instructs the agent to load it before that action

#### Scenario: A missing destination stops the governed action

- **WHEN** the skill file is missing or unreadable at a stage entry
- **THEN** the agent stops and reports it rather than shipping from the root text

#### Scenario: Loading the skill grants no permission

- **WHEN** an agent has loaded the skill but the owner's approval is not established
- **THEN** no governed action follows, because the authorization boundary stays in `AGENTS.md` and the skill adds none

### Requirement: A shipped procedure names its source and takes its corrections in the skill

Each procedure in the skill SHALL carry its own `Source:` line quoting the `AGENTS.md` heading it replaced, so a missing
destination, a renamed source, or an orphaned section is detectable. A correction to either procedure SHALL be made in
the skill, not recreated in `AGENTS.md`.

#### Scenario: The source is identifiable in the destination

- **WHEN** a reader or the agent-tooling audit looks for a procedure's source
- **THEN** the skill's `Source:` line quotes the replaced `AGENTS.md` heading verbatim, so a renamed source or an
  orphaned section fails to pair

#### Scenario: A correction lands in the skill

- **WHEN** a procedure step, branch, or failure rule is wrong or incomplete
- **THEN** it is corrected in the skill rather than recreated in `AGENTS.md`
