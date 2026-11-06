# Spec Delta — showcase/quality/agent-skills

## ADDED Requirements

### Requirement: Every unit follows the published lifecycle

Every unit intended to ship SHALL follow the lifecycle `AGENTS.md` publishes, whether or not it carries an OpenSpec
change directory.

#### Scenario: A unit without a change follows the same lifecycle

- **WHEN** a unit carries no change directory
- **THEN** it passes the same stages and no archive step applies to it

#### Scenario: A change without deltas is still archived

- **WHEN** a change validly declares `skip_specs`
- **THEN** its change directory is archived in Deliver like any other change

### Requirement: The published lifecycle names its stages, transitions, and owners

The published lifecycle SHALL name its stages, permitted transitions, approval boundaries, exceptional routes, terminal
exits, and each entry's owning procedure.

#### Scenario: A material scope change returns through the approval boundary

- **WHEN** an operation exposes a material scope change
- **THEN** the affected planning and approval boundary is revisited rather than the change being treated as a correction

#### Scenario: Rejection settles without delivery

- **WHEN** the owner rejects or abandons a unit
- **THEN** it is settled with that outcome and no delivery stage runs

#### Scenario: An approved unit waits for the delivery request

- **WHEN** the owner has approved the implementation but requested no delivery
- **THEN** the unit stays at its implementation boundary and delivery does not begin

#### Scenario: Settlement follows the merge

- **WHEN** a unit's pull request has merged
- **THEN** the workspace remains assigned until settlement establishes that release is safe

### Requirement: A stage transition rests on its evidence and authority

A stage transition SHALL rest on the evidence and the authority it requires, and SHALL NOT be inferred from a command's
completion alone.

#### Scenario: A completed command does not advance the stage

- **WHEN** a stage's command has completed but the decision the next stage requires is unanswered
- **THEN** the unit stays at its current stage and the unanswered decision is reported

### Requirement: An OpenSpec operation runs inside its owning unit stage

An OpenSpec operation SHALL occur within the unit stage that owns it and SHALL NOT substitute for the unit's
authorization, review, delivery, or settlement obligations.

#### Scenario: A change's planning and implementation run inside the unit stages

- **WHEN** a unit carries an OpenSpec change
- **THEN** the change's planning runs in Plan, its apply in Implement, and its archive in Deliver after green CI

#### Scenario: A lifecycle entry does not authorize its operation

- **WHEN** a generated entry point serves a stage
- **THEN** the unit's authorization and the stage's boundary still apply to it

### Requirement: The Begin procedure is maintained in its project skill

The unit workspace-establishment procedure SHALL live in the project skill `.opencode/skills/begin-unit/SKILL.md`, which
owns its ordered steps, and `AGENTS.md` SHALL carry the entry point and the stop-and-report rule rather than the steps.
The `/begin-unit` command SHALL load that skill and SHALL NOT reproduce it.

#### Scenario: An unreadable skill stops Begin

- **WHEN** the Begin skill is missing or unreadable
- **THEN** Begin stops and reports it rather than reconstructing the procedure

#### Scenario: The tree mechanism stays in its script

- **WHEN** a tree must be created and prepared
- **THEN** the skill delegates to the existing script rather than reimplementing its creation, preparation, or refusal
  behaviour

### Requirement: Begin establishes the workspace before the first content write

Begin SHALL establish the unit's workspace before its first content write, applying the workspace rule
`Project-authored command definitions that start a mutating unit carry the lifecycle preflight` states.

#### Scenario: A new unit on the primary checkout

- **WHEN** the primary checkout holds no other unit and the owner has authorized a unit
- **THEN** Begin establishes the unit record and the assignment in that tree before the first content write

#### Scenario: A new unit on a sibling tree

- **WHEN** the primary checkout belongs to another unit
- **THEN** Begin creates and prepares a sibling tree through the script and reports its readiness before any write in it

#### Scenario: A continued unit keeps its branch

- **WHEN** Begin runs for a unit continuing an existing branch
- **THEN** it takes a tree on that branch rather than creating one from `origin/main`

### Requirement: A project-authored command declares the lifecycle entry it serves

Every project-authored command SHALL declare the lifecycle stage it serves, or `cross-cutting` for one that spans stages
within a unit, or `outside-unit` for one that operates with no unit; its invocation as owner-invoked, procedure-invoked,
or scheduled; its effect as report-only, host operation, unit start, or an operation within an existing unit; and the
procedure that owns it.

A command whose branches differ in stage or authority SHALL declare each branch.

#### Scenario: A command declares its entry

- **WHEN** a project-authored command is read
- **THEN** its declaration names its stage, its invocation, its effect, and its owning procedure

#### Scenario: A branching command declares each branch

- **WHEN** a command's branches differ in the lifecycle stage they serve or in the authority they exercise
- **THEN** each branch carries its own declaration rather than one classification covering the file

### Requirement: A declaration grants no authority

A declaration SHALL NOT grant authority: an invocation that starts a unit SHALL satisfy the lifecycle's authorization
rule, and one that performs an operation SHALL satisfy that operation's boundary.

#### Scenario: A declaration grants no authority

- **WHEN** a command declares a unit-starting effect and is invoked by a procedure
- **THEN** the unit's authorization and approval boundaries still apply as the lifecycle states them

### Requirement: A generated entry is mapped, not edited

A generated or vendored definition SHALL NOT be edited; its repository-specific entry SHALL be mapped in the lifecycle
section that publishes the model.

#### Scenario: Generated entries are mapped, not edited

- **WHEN** a generated command serves a lifecycle stage
- **THEN** its mapping lives in the lifecycle section and its generated definition is left unedited

## MODIFIED Requirements

### Requirement: A unit's record carries its state

A unit's record SHALL carry exactly one state: `active`, `paused`, or `closed`.

A record with no state SHALL be read as unknown.

The state SHALL change only at the transition it names:

- `active` is written when the unit's workspace is established at Begin, and when a resume continues the work, after its
  state verification and the writer handover.
- `paused` is written when a pause records its checkpoint.
- `closed` is written when the unit enters delivery, or is rejected or abandoned.

For a unit that ships, `closed` SHALL be written in the commit before its merge — the archive commit for a change unit,
and the unit's final commit otherwise — because a write after the merge cannot reach the branch it merges.

For a unit that does not ship, `closed` SHALL be written in its own tree before its branch is released, and that write
is local by nature, since no merge carries it.

A `closed` record SHALL NOT be read as proof that the unit merged, that its delivery completed, or that its workspace
was released.

#### Scenario: A pause writes pausing

- **WHEN** a unit is paused
- **THEN** its record's state becomes `paused`

#### Scenario: A resume writes continuing

- **WHEN** a resume continues the unit's work
- **THEN** its record's state becomes `active`, and a resume that stops before the work continues leaves it `paused`

#### Scenario: The delivering commit closes the unit

- **WHEN** a unit that ships is about to merge
- **THEN** the commit before its merge carries the record with the state `closed`

#### Scenario: A rejected unit closes in its own tree

- **WHEN** a unit is rejected or abandoned without shipping
- **THEN** its record is set to `closed` in its tree before the branch is released, and no merge carries that write

#### Scenario: A record with no state is unknown

- **WHEN** a record carries no state
- **THEN** it is read as unknown and is never treated as `paused` or `active`

#### Scenario: Begin initializes the record

- **WHEN** a unit's workspace is established
- **THEN** its record carries `active` before the unit's first content write

#### Scenario: A closed record does not prove delivery

- **WHEN** a unit's record reads `closed` while its pull request is unmerged
- **THEN** its delivery and settlement are still outstanding and neither is inferred

### Requirement: Project-authored command definitions that start a mutating unit carry the lifecycle preflight

A project-authored command that starts a mutating unit that ships SHALL state the lifecycle its unit owes: its workspace
— the working tree assigned to the unit, the primary checkout when no other unit holds it and a sibling
`../axon-showcase-<unit>` worktree otherwise, established before its first write, with an ownership it cannot establish
treated as occupied; its own branch from a freshly fetched `origin/main` for a new unit, or the existing branch for a
unit continuing one; unrelated dirty changes preserved; the owner's authorization for the unit's start and their
approval of its proposal before any implementation edit; work uncommitted until the implementation's manual approval
pass; and no commit, push, pull request, archive, or merge without that approval, apart from a commit the owner
explicitly requests and the proposal-stage draft pull request the lifecycle's procedure documents. It SHALL point at the
workflow owning the lifecycle and the review gate, SHALL carry the declaration the requirement
`A project-authored command declares the lifecycle entry it serves` states, and every `review-quick` invocation SHALL
carry the session-continuity preflight.

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
  or merge happens before it apart from the exceptions this requirement's description names

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

#### Scenario: The narrow publication exceptions are stated

- **WHEN** a unit-starting command a project author wrote states the prohibition on publishing before the
  implementation's approval
- **THEN** it names the exceptions the procedure allows rather than stating the prohibition unconditionally
