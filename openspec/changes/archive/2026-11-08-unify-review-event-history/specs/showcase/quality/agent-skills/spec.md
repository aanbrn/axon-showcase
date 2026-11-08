# showcase/quality/agent-skills Specification

## MODIFIED Requirements

### Requirement: A unit's deep-review and consultation sessions are recorded and reused

A unit's registered invocations — the deep review's, and a consultation the unit binds to itself — SHALL each resolve a
session from the unit's agent-session record, continue it by its recorded identifier while the main session that
recorded it runs, and replace it only on an evidenced reason. The registered agents are `review-thorough` and
`technical-advisor` for a unit whose first round predates `2026-11-09`; for a unit whose first round is dated on or
after it, those agents and `review-quick`. A unit whose first round predates it SHALL NOT record the quick reviewer
here: its continuity is that record's.

A forced replacement — an expired session, an unresponsive one, or a main session that cannot continue the child — SHALL
be recorded with its reason and reported to the owner. A discretionary one — a definition that changed after the session
began — SHALL have the owner's approval before the invocation, because the change makes the session's instructions
stale. An independent screen is not a replacement cause, as the quick-loop requirement states. A stateless subagent
SHALL NOT be registered: a fresh session is its correct behaviour, not a replacement.

Each row SHALL be appended when its invocation's outcome is known, never before: the hand-over section is written before
the invocation, the row after it.

#### Scenario: A deep pass records its session on the first invocation

- **WHEN** the routing decision selects a deep review for a unit with no recorded deep-review session
- **THEN** the pass runs in a new session and its invocation is recorded with the `created` transition

#### Scenario: A follow-up pass continues the recorded session

- **WHEN** a later deep-review pass runs in the main session that recorded the earlier one
- **THEN** it continues that session by its identifier, and the invocation is recorded with `continued`

#### Scenario: A forced replacement is recorded and reported

- **WHEN** a recorded session expired, stopped responding, or belongs to a main session that cannot continue it
- **THEN** the replacement is recorded with that reason and reported to the owner, and it starts from the recorded
  hand-over

#### Scenario: A discretionary replacement needs the owner's approval

- **WHEN** a replacement is wanted because the agent's definition changed after the session began
- **THEN** the owner's approval is obtained before the replacement runs and recorded in the replacement's hand-over
  section

#### Scenario: A stateless subagent is not registered

- **WHEN** an invocation runs a subagent whose calls carry no unit state — a diagram, a visual review, an exploration,
  or an audit
- **THEN** no agent-session row is written for it, because a fresh session is its intended behaviour

### Requirement: A registered invocation's hand-over is written before it is awaited

A registered invocation SHALL write its hand-over section at `docs/units/<unit>/review-handover.md` before the
invocation is awaited, so a stall or a replacement costs a wait rather than the work the section holds. A quick row's
section is the round's `## Round <N>` heading, which the hand-over check already requires before the delegation. The
section SHALL carry the fields the invocation's own command declares for its hand-over, plus the agent, the scope, the
opinion, the expected session identifier, and — for a discretionary replacement — the owner's approval, and SHALL NOT
restate those fields as a second list. A non-quick row's heading SHALL begin with the slug the agent-session row
records, so the row and the section resolve to each other; a quick row's slug is `round-<round>` and resolves to the
round's `Round <N>` heading.

A registered invocation that ran before this requirement existed in the unit whose change introduced the mechanism SHALL
have its section written from its reports, and the section SHALL state that it was written after the invocation. An
earlier unit's invocations SHALL NOT be reconstructed: their context stays as unavailable as it is.

#### Scenario: The section precedes the invocation

- **WHEN** a registered invocation is about to run
- **THEN** its hand-over section is written before it is awaited, and the row that records the invocation names that
  section's slug

#### Scenario: The section carries its command's fields

- **WHEN** a deep-review pass or a consultation writes its hand-over
- **THEN** the section carries the fields that command's own hand-over contract declares, so the fields are stated once
  at their owner

#### Scenario: An invocation that predates the mechanism is recorded

- **WHEN** a registered invocation ran before this requirement existed in the unit whose change introduced the mechanism
- **THEN** its section is written from its reports, and it states that it was written after the invocation
- **AND** an earlier unit's invocations are not reconstructed

#### Scenario: A replacement reads the section, not the transcript

- **WHEN** a registered session is replaced
- **THEN** the replacement's context is the recorded hand-over section, and the current state is reviewed independently

### Requirement: The quick review loop runs on every unit

The repository SHALL run the bounded quick review after a unit's proposal and after its implementation, and at the
manual-pass boundary after a refresh that changed a file the closing round reviewed, repeating until it reports no new
observations before the manual review is requested again. The reviewer's screen, finding classes, evidence rules, report
contract, and capture challenge are its definition's; the caller's steps — the pre-write record, the claims table, the
session preflight, the round's row and its checkpoint, and the register's quick row for a unit whose rounds carry six
fields — are the review-gate skill's; both records' formats and verifications are `showcase/quality/commit-hygiene`'s.

Each round's finding instances SHALL be written to the unit's store (`docs/units/<unit>/findings/round-<N>.md`) when its
report arrives, so a round's classes and counts in the record have the instances beside them rather than only in the
reviewer's report or a pause checkpoint. A round recorded before the store existed SHALL fall back to the checkpoint or
the hand-over, and a unit holding neither SHALL be read as unrecovered context rather than as a round that raised
nothing.

A replacement reviewer session SHALL be started only after one of the recovery cases this requirement names is
evidenced; an independent screen is not a recovery case, and a session change without an evidenced case is recorded as a
continuity violation rather than as a permitted replacement.

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
  pre-write record, the claims table, the session preflight, the round's row, its checkpoint, and the register's quick
  row for a post-retirement unit — are the `review-gate` skill's
- **AND** a defect the applying command rewrites — `spotlessApply`, or the web module's `npmFormat` — is routed to that
  command rather than reported, so what the command leaves alone stays reportable
- **AND** for a record artifact (a retrospective, a load-test record, or a process-defect record), a later round raising
  only new instances of an already-raised class stops and escalates to the owner rather than iterating, since such an
  artifact has no shipped behavior to converge on

#### Scenario: A stalled reviewer is replaced from a recorded handover

- **WHEN** a reviewer invocation has not responded by the deadline the caller's policy supplies
- **THEN** for a unit whose first round is dated on or after `2026-11-09` the caller records each invocation in the
  register — the stalled attempt's row when the deadline passes, before the replacement starts, and the replacement's
  row when its own report arrives with the reason `stopped-responding` — and for a pre-retirement unit the attempt
  evidence stays in the round's hand-over and the ledger keeps its single row for the completed round, carrying that
  round's final session and the replacement's reason; repeated stalled attempts are recorded the same way, and every
  attempt's context remains in the hand-over
- **AND** the only cases that justify a replacement are: a session that expired, a reviewer's definition that changed
  after the session began, a reviewer that stopped responding mid-round (the loop has observed a session stall with no
  reply, which no wait resolves), or a reviewer session the unit's new main session cannot continue; a recorded
  `caller-restarted` is a violation, never a justification
- **AND** the handover it gives the replacement is the record it wrote **before** waiting, at
  `docs/units/<unit>/review-handover.md` in the section for that invocation — the unit's identity, its base and current
  revisions, the unit's assigned working tree and branch, the supplied artifacts, the scope, the prior findings with
  their instance-level evidence, the completed checks, and the known limitations — so the context a replacement needs
  never depends on the stalled invocation reporting, and cancelling it loses no progress that record does not already
  hold
- **AND** a hand-over recorded before the per-unit location existed is read where it was recorded, in the change
  directory or its archive
- **AND** the unit's missing file is not evidence that no earlier invocation happened
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
- **AND** a unit with no change dir drafts its pull-request body from the template before the pass runs, so the
  checklist line the table and the pass's record go on exists when they are written
- **AND** for a unit whose claims-table home is the body, the pass's seed scans the saved body's text beside its
  ordinary diff scan, so a deletion-only diff does not hide the claims the body states
- **AND** the scan reports the candidates its inputs contain
- **AND** an empty list means no recognized candidate rather than a complete table

#### Scenario: An escalated record unit is not cleared by the stop

- **WHEN** a record artifact's later round raises only new instances of an already-raised class and the loop escalates
  to the owner
- **THEN** the finding-bearing ledger row and the finding instances are preserved, and the owner's decision is recorded
  on the unit's review task or pull-request review record
- **AND** the loop resumes per that decision without a manufactured clean round, and the stop is not clearance: the unit
  does not proceed to delivery while its highest recorded round still carries a finding class

#### Scenario: A boundary refresh that changed a reviewed file re-opens the loop

- **WHEN** the review gate's manual-pass boundary refreshes a unit's branch, and the refresh changed a file the closing
  quick round reviewed — a conflict resolved by hand, or a unit file the base advanced
- **THEN** the gate re-runs its stages over the result before the manual pass is requested again — the pre-write pass's
  claims table when a planning artifact changed, then the quick loop, then the routing decision reassessed on the
  changed tree and any deep review the routing selects — rather than treating the closing round as covering the
  rewritten tree

#### Scenario: A round's finding instances are persisted beside its record

- **WHEN** a quick-review round's report arrives
- **THEN** the caller writes its finding instances to `docs/units/<unit>/findings/round-<N>.md`, one entry per finding
  with its class, its location, and its claim, in the reviewer's order
- **AND** the round's row keeps the loop's outcome fields, and a pre-retirement unit's keeps its session fields too, so
  the instances live beside the record rather than inside it, and each record's format stays as
  `showcase/quality/commit-hygiene` states it
- **AND** the review gate's hand-over and a resumed unit's checkpoint read that store, so an instance is not carried
  only in a checkpoint field
- **AND** a round recorded before the store existed falls back to the checkpoint or the hand-over, and a unit holding
  neither reads as unrecovered context rather than as a clean round

### Requirement: The deep review is routed and bounded

The repository SHALL make one routing decision per unit before any deep-review session is created, resolving to
`REQUIRED`, `RECOMMENDED`, or `NOT_NEEDED`, and SHALL run the `review-thorough` subagent on an outcome that selects it.
The decision's deterministic floor is `scripts/review-routing.py`'s; the pass's procedure and hand-over are
`/review-thorough`'s; the reviewer's dimensions, evidence, and grouping are its definition's; the routing decision and
the routed pass's status are recorded on the unit's deep-review record (`docs/units/<unit>/deep-review.md`), whose
format `/review-thorough` owns, while the change's review task carries the planned obligation and a pointer to it.

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
- **AND** a diff whose changed-path set is non-empty and holds only Markdown record artifacts under
  `docs/retrospectives/` or `docs/load-tests/`, only a unit's `docs/units/<unit>/process-defects.md`, or the ledger path
  the invocation reads, SHALL NOT have its floor raised by the repeated-class signal alone when its derivation succeeded
- **AND** the classification SHALL be conservative: another file type or location keeps normal routing, and a rename is
  judged by both its source and its destination
- **AND** the suppressed signal SHALL be reported wherever the exemption applies, and a mandatory signal, a failed
  derivation, or the agent's own evidence still decides
- **AND** the rationale is that such a unit ships no behavior to converge on, so a repeated class is not by itself
  evidence of an unreviewed defect
- **AND** the decision is reassessed only when material new evidence changes the risk
- **AND** the decision and the signals it relied on are recorded in the unit's deep-review record, including a
  `NOT_NEEDED` outcome

#### Scenario: A missing or failed routing decision cannot skip mandatory review

- **WHEN** the routing decision is absent, malformed, or its derivation fails
- **THEN** the outcome is `REQUIRED` rather than a silent bypass, so an unreadable or failed decision can only make
  review run, never skip it

#### Scenario: A low-risk change bypasses the deep-review lifecycle

- **WHEN** the routing decision resolves to `NOT_NEEDED`
- **THEN** no deep-review session is created, no hand-over is prepared, and no expensive-model invocation is made, while
  the mandatory quick review, the tests, and the manual approval still run, and the unit's deep-review record states the
  `NOT_NEEDED` outcome and its signals

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
- **AND** the status has one authoritative record, the unit's deep-review record, rather than a copy in several
  artifacts, and the quick loop's round ledger stays its own record, so a deep pass does not change the population the
  loop's trend is read from

#### Scenario: The deep-review record is the unit's, with or without a change directory

- **WHEN** a unit needs a routing decision or a deep-review pass
- **THEN** the decision and each pass's findings and statuses are recorded at `docs/units/<unit>/deep-review.md`,
  whether or not the unit carries a change directory, and the change's review task or the pull-request checklist carries
  the planned obligation and a pointer to it rather than the statuses themselves

#### Scenario: Full passes, targeted verifications, and failed attempts are distinguished

- **WHEN** a deep-review invocation is recorded
- **THEN** the record states whether it was a full pass, a targeted verification, or a failed attempt, and the pass
  budget counts full passes only, never the record's rows

#### Scenario: An earlier unit's deep-review record stays where it was recorded

- **WHEN** a unit carried a deep pass before this record's activation
- **THEN** its change-task record stands as recorded, and a unit without a prior pass records in its unit file even when
  its quick rounds began earlier; an earlier routing-only record is linked from the new record rather than copied, and a
  missing historical record is unrecovered context rather than proof that no pass occurred

#### Scenario: Deep review exhausts a class it raises

- **WHEN** a deep-review pass raises a finding that falls in a class
- **THEN** it searches the change's artifacts for every instance of that class's shape and reports them as one clustered
  finding, so a class surfaces whole in the deep pass rather than one instance per quick-review round

#### Scenario: A record-only diff is not lifted by repeated classes

- **WHEN** a unit's changed-path set is non-empty and holds only Markdown record artifacts or the review-round ledger,
  its derivation succeeded, and its recorded rounds repeat a class
- **THEN** the routing floor is `NOT_NEEDED`, with the repeated-class signal and the record-only exemption both reported
- **AND** the agent may still decide above the floor on other evidence, while a mandatory path, a requirement block, a
  declaration, or a failed derivation still resolves to `REQUIRED` before the exemption is considered
