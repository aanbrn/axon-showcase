## MODIFIED Requirements

### Requirement: The quick review loop runs on every unit

The repository SHALL run the bounded quick review after a unit's proposal and after its implementation, repeating until
it reports no new observations. The reviewer's screen, finding classes, evidence rules, report contract, and capture
challenge are its definition's; the caller's steps — the pre-write record, the claims table, the session preflight, the
ledger row and its checkpoint — are the review-gate skill's; the ledger row's format and verification are
`showcase/quality/commit-hygiene`'s.

A replacement reviewer session SHALL be started only after one of the recovery cases the review gate's rule names is
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
- **AND** the only cases that justify a replacement are: the prior session expired, the reviewer's definition changed
  after that session began, or the reviewer stopped responding mid-round (the loop has observed a session stall with no
  reply, which no wait resolves)
- **AND** the handover it gives the replacement is the record it wrote **before** waiting — the unit's identity, its
  base and current revisions, the unit's assigned working tree and branch, the supplied artifacts, the scope, the prior
  findings with their instance-level evidence, the completed checks, and the known limitations — so the context a
  replacement needs never depends on the stalled invocation reporting, and cancelling it loses no progress that record
  does not already hold
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
