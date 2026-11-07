# showcase/quality/agent-skills Specification

## MODIFIED Requirements

### Requirement: A consultation uses a fresh session or continues the same decision

An independent consultation SHALL run in a fresh child session. A follow-up about the same decision MAY continue the
existing advisor session. When the session identifier is unavailable, the caller SHALL start a fresh consultation and
say that it is fresh. A consultation bound to a unit SHALL resolve its session from the unit's agent-session record,
keyed by the decision and the opinion, and SHALL record each invocation there as the recording requirement states. A
second opinion the owner requests SHALL be its own conversation, not a replacement of the first.

#### Scenario: An independent consultation uses a fresh child session

- **WHEN** a decision is consulted for the first time
- **THEN** the consultation runs in a fresh child session rather than in the parent session

#### Scenario: A follow-up may continue the same session

- **WHEN** a follow-up concerns the same decision and the session identifier is available
- **THEN** the consultation continues the existing advisor session

#### Scenario: A second opinion is its own conversation

- **WHEN** the owner asks for an independent second opinion on the same decision
- **THEN** the opinion runs in a new conversation, recorded as `created` with its own opinion, and the first opinion's
  session is not replaced

#### Scenario: A unit-bound consultation resolves its recorded session

- **WHEN** a consultation belongs to a unit and the unit's record carries a session for that decision and opinion
- **THEN** the consultation continues that session, and its invocation is recorded in the unit's agent-session record

#### Scenario: A lost identifier starts a fresh consultation

- **WHEN** the session identifier for the decision is unavailable
- **THEN** the caller starts a fresh consultation and states that it is fresh

### Requirement: The quick review loop runs on every unit

The repository SHALL run the bounded quick review after a unit's proposal and after its implementation, and at the
manual-pass boundary after a refresh that changed a file the closing round reviewed, repeating until it reports no new
observations before the manual review is requested again. The reviewer's screen, finding classes, evidence rules, report
contract, and capture challenge are its definition's; the caller's steps — the pre-write record, the claims table, the
session preflight, the ledger row and its checkpoint — are the review-gate skill's; the ledger row's format and
verification are `showcase/quality/commit-hygiene`'s.

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
  pre-write record, the claims table, the session preflight, the ledger row, and its checkpoint — are the `review-gate`
  skill's
- **AND** a defect the applying command rewrites — `spotlessApply`, or the web module's `npmFormat` — is routed to that
  command rather than reported, so what the command leaves alone stays reportable
- **AND** for a record artifact (a retrospective, a load-test record, or a process-defect record), a later round raising
  only new instances of an already-raised class stops and escalates to the owner rather than iterating, since such an
  artifact has no shipped behavior to converge on

#### Scenario: A stalled reviewer is replaced from a recorded handover

- **WHEN** a reviewer invocation has not responded by the deadline the caller's policy supplies
- **THEN** the caller records the failure as `stopped-responding`, naming the stalled session's identifier and its
  replacement's, and creates the replacement session only after that record
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
- **AND** the round's `rounds.tsv` row keeps its fixed fields, so the instances live beside the record rather than
  inside it, and the record format stays as `showcase/quality/commit-hygiene` states it
- **AND** the review gate's hand-over and a resumed unit's checkpoint read that store, so an instance is not carried
  only in a checkpoint field
- **AND** a round recorded before the store existed falls back to the checkpoint or the hand-over, and a unit holding
  neither reads as unrecovered context rather than as a clean round

### Requirement: A pause records a resume checkpoint

The agent SHALL write the unit's `Resume checkpoint` — the fields the skill names — into the unit's record at
`docs/units/<unit>/unit.md` before it reports a pause, and SHALL read that record back.

A pause SHALL alter no delivery state: nothing is committed, stashed, pushed, archived, or merged for it, and the tree
stays assigned to the unit.

The pause report SHALL name the saved `Resume checkpoint`, the unit, and the tree, and SHALL state that continuing the
main session by its identifier keeps the unit's recorded agent sessions, while continuing in a new session records each
one's `parent-session-changed` replacement and restarts it from the recorded hand-over. It SHALL name the
`/resume-unit <unit>` entry beside that choice.

A session limitation the agent could not measure SHALL be described as an assessment rather than as an exhausted context
window or provider quota.

#### Scenario: A pause records the resume checkpoint in one place

- **WHEN** the owner pauses a unit before its completion
- **THEN** the unit's record at `docs/units/<unit>/unit.md` carries one `Resume checkpoint` section, created or updated
  in place, and it is re-read before the turn ends

#### Scenario: A pause leaves the delivery state alone

- **WHEN** a unit is paused
- **THEN** no commit, stash, push, archive, or merge is made for the pause, and the review gate's approval stop and
  staged-set rules are unchanged

#### Scenario: A pause keeps the unit's tree and reviewer

- **WHEN** a paused unit is resumed in the same tree
- **THEN** the tree still belongs to that unit, and no replacement reviewer session is started because of the pause
  alone

#### Scenario: The pause report names the way to continue

- **WHEN** an agent reports a pause it recorded
- **THEN** the report names the saved `Resume checkpoint`, the unit, and the tree, and states the choice between
  continuing the same main session and continuing in a new one, naming `/resume-unit <unit>`

#### Scenario: The pause report states the session consequence

- **WHEN** the paused unit holds recorded agent sessions
- **THEN** the report states that continuing the main session by its identifier keeps them, and that a new session
  records each one's `parent-session-changed` replacement

#### Scenario: An unmeasured limitation is reported as an assessment

- **WHEN** the agent pauses because it decided it could not reliably continue, and it holds no measured limit from the
  harness, the provider, or the account
- **THEN** it describes the limitation as its own assessment rather than as an exhausted context window or provider
  quota

## ADDED Requirements

### Requirement: A unit's deep-review and consultation sessions are recorded and reused

A unit's registered invocations — the deep review's, and a consultation the unit binds to itself — SHALL each resolve a
session from the unit's agent-session record, continue it by its recorded identifier while the main session that
recorded it runs, and replace it only on an evidenced reason. The registered agents are `review-thorough` and
`technical-advisor`. The quick reviewer's session SHALL NOT be registered here: its continuity and its replacement
reasons are the review-round record's.

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
invocation is awaited, so a stall or a replacement costs a wait rather than the work the section holds. The section
SHALL carry the fields the invocation's own command declares for its hand-over, plus the agent, the scope, the opinion,
the expected session identifier, and — for a discretionary replacement — the owner's approval, and SHALL NOT restate
those fields as a second list. Its heading SHALL begin with the slug the agent-session row records, so the row and the
section resolve to each other.

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

### Requirement: A resumed unit continues its recorded sessions when its main session survives

A unit resumed in the main session a recorded invocation names SHALL continue that session by its identifier, and SHALL
verify the identifier the invocation returns against the recorded one. A unit resumed in another main session SHALL NOT
attempt the continuation — the harness refuses a child of another session — and SHALL replace each session the resumed
stage needs with `parent-session-changed`, starting the replacement from the recorded hand-over and recording its row
when the outcome is known.

The resume SHALL restore the session references and establish any unresolved invocation's outcome before it invokes an
agent, and SHALL invoke one only when the stage's next step warrants it. A consultation SHALL still require the owner's
authorization.

#### Scenario: The same main session continues its sessions

- **WHEN** the owner resumes the main session that recorded a unit's sessions
- **THEN** the resume continues them by their identifiers, and a returned identifier that differs from the recorded one
  is a continuity failure rather than a continuation

#### Scenario: A different main session records the forced replacement

- **WHEN** the owner resumes the unit in a main session other than the one a recorded invocation names
- **THEN** the resume starts each needed session's replacement from the recorded hand-over without attempting the
  refused continuation, and records its `parent-session-changed` row when the outcome is known

#### Scenario: An unresolved invocation is established before it is relied on

- **WHEN** the register carries a `stalled` or `failed` row for a needed session
- **THEN** its outcome is established before anything relies on it, and it resolves no session

#### Scenario: A resume invokes nothing speculatively

- **WHEN** a unit is resumed
- **THEN** no registered agent is invoked merely because its session is recorded, and an invocation happens only when
  the stage's next step needs it

#### Scenario: A consultation still needs its authorization

- **WHEN** the resumed stage would consult the technical advisor
- **THEN** the consultation still requires the owner's authorization, and the recorded session does not supply it
