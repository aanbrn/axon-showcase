## MODIFIED Requirements

### Requirement: Project-authored command definitions that start a mutating unit carry the lifecycle preflight

A project-authored command that starts a mutating unit that ships SHALL state the lifecycle its unit owes: its workspace
— the working tree assigned to the unit, the primary checkout when no other unit holds it and a sibling
`../axon-showcase-<unit>` worktree otherwise, established before its first write, with an ownership it cannot establish
treated as occupied; its own branch from a freshly fetched `origin/main` for a new unit, or the existing branch for a
unit continuing one; unrelated dirty changes preserved; work uncommitted until the manual approval pass; and no commit,
push, pull request, archive, or merge without that approval. It SHALL point at the workflow owning the lifecycle and the
review gate, and every `review-quick` invocation SHALL carry the session-continuity preflight.

#### Scenario: A mutating command states the branch rule

- **WHEN** a project-authored command starts a mutating unit that ships
- **THEN** it states that a new unit starts on its own branch from a freshly fetched `origin/main`, and that a unit
  continuing an existing branch or pull request works on that branch

#### Scenario: A mutating command preserves unrelated work

- **WHEN** a unit the command starts finds unrelated dirty-worktree changes
- **THEN** the command preserves them rather than reverting or committing them

#### Scenario: Work stays uncommitted until approval

- **WHEN** a mutating command's unit has finished its edits
- **THEN** the work stays uncommitted until the owner's explicit approval, and no commit, push, pull request, archive,
  or merge happens before it

#### Scenario: A review invocation carries the session preflight

- **WHEN** a mutating command invokes `review-quick`
- **THEN** the invocation carries the session-continuity preflight the review gate's procedure owns

#### Scenario: A mutating command states the workspace preflight

- **WHEN** a project-authored command starts a mutating unit that ships
- **THEN** it requires the unit's working tree to be established before its first write, naming a sibling worktree when
  the primary checkout already belongs to another unit, and treats an ownership it cannot establish as occupied

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
