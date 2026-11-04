# Delta — require owner authorization for a unit's start and its implementation

## ADDED Requirements

### Requirement: A unit starts on the owner's authorization and its proposal is approved before implementation

No unit SHALL begin work — a change, a docs unit, an audit finding's application, or a dependency bump — except on the
owner's explicit instruction to begin it, on the owner's approval of a proposal the agent presented, or on the owner
naming the unit to continue. An agent that believes it has understood what the owner wants SHALL state the proposal and
wait. A discussion of what should be done, a question the owner asks, an idea they explore, or the agent's own
conclusion that the task is clear SHALL NOT be read as that authorization.

A unit's proposal SHALL pass the owner's manual review before any implementation edit, run by the review gate's manual
pass at each boundary the unit has. A unit SHALL take a single pass instead only where the owner's authorization itself
names each direct edit the unit may make; presenting the work as a proposed edit SHALL NOT remove the proposal's pass. A
two-pass unit's writable scope SHALL begin with the proposal's review, and a single-pass unit's with the owner's
authorization that names each direct edit the unit may make; until then an agent SHALL write only the unit's planning
artifacts and its records under `docs/units/<unit>/`, and SHALL NOT change an implementation target or a live rule file.

#### Scenario: A unit waits for the owner's instruction

- **WHEN** an agent has concluded what a task needs while the owner is still discussing it
- **THEN** it states the proposal and waits, and begins no unit

#### Scenario: A proposal passes manual review before implementation

- **WHEN** a unit's proposal artifacts are finished and its quick-review loop is clean
- **THEN** the agent asks for the manual review of the proposal and ends its turn, and makes no implementation edit
  until the owner approves it

#### Scenario: An authorized direct edit takes the single pass

- **WHEN** the owner's authorization itself names each direct edit the unit may make
- **THEN** the unit makes those edits and passes only the delivery-stage manual review, and no other edit is covered by
  that authorization

#### Scenario: A proposed-edit label does not skip the pass

- **WHEN** a unit's first artifacts are described as a proposed edit rather than a proposal, and the owner's
  authorization names no direct edits
- **THEN** the unit's proposal passes the owner's manual review before any implementation edit

#### Scenario: The unit's own records stay writable

- **WHEN** a unit is still gathering its proposal
- **THEN** it may write its planning artifacts and the records under `docs/units/<unit>/`, and changes no implementation
  target or live rule file

#### Scenario: Discussion is not authorization

- **WHEN** the owner discusses a problem or asks what could be done
- **THEN** the agent treats the exchange as discussion rather than permission to start the work

## MODIFIED Requirements

### Requirement: Project-authored command definitions that start a mutating unit carry the lifecycle preflight

A project-authored command that starts a mutating unit that ships SHALL state the lifecycle its unit owes: its workspace
— the working tree assigned to the unit, the primary checkout when no other unit holds it and a sibling
`../axon-showcase-<unit>` worktree otherwise, established before its first write, with an ownership it cannot establish
treated as occupied; its own branch from a freshly fetched `origin/main` for a new unit, or the existing branch for a
unit continuing one; unrelated dirty changes preserved; the owner's authorization for the unit's start and their
approval of its proposal before any implementation edit; work uncommitted until the implementation's manual approval
pass; and no commit, push, pull request, archive, or merge without that approval. It SHALL point at the workflow owning
the lifecycle and the review gate, and every `review-quick` invocation SHALL carry the session-continuity preflight.

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

#### Scenario: A mutating command states the start and proposal stops

- **WHEN** a project-authored command starts a mutating unit that ships
- **THEN** it states that the unit begins only on the owner's authorization and that its proposal passes the owner's
  manual review before any implementation edit
