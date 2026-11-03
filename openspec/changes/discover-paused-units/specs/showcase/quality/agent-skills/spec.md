## MODIFIED Requirements

### Requirement: A pause records a resume checkpoint

The agent SHALL write the unit's `Resume checkpoint` — the fields the skill names — into the unit's record at
`docs/units/<unit>/unit.md` before it reports a pause, and SHALL read that record back.

A pause SHALL alter no delivery state: nothing is committed, stashed, pushed, archived, or merged for it, and the tree
stays assigned to the unit.

The pause report SHALL name the saved `Resume checkpoint`, the unit, and the tree, and SHALL tell the owner to continue
in a new session with `/resume-unit <unit>`.

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
- **THEN** the report names the saved `Resume checkpoint`, the unit, and the tree, and tells the owner to continue in a
  new session with `/resume-unit <unit>`

#### Scenario: An unmeasured limitation is reported as an assessment

- **WHEN** the agent pauses because it decided it could not reliably continue, and it holds no measured limit from the
  harness, the provider, or the account
- **THEN** it describes the limitation as its own assessment rather than as an exhausted context window or provider
  quota

### Requirement: A continued unfinished unit verifies its available state before the first write

A session continuing an unfinished unit SHALL verify the state available to it — the workspace records and the live tree
— before its first write, and SHALL compare that state with the unit's `Resume checkpoint` when one exists.

It SHALL stop rather than repair on a mismatch, and an absent checkpoint SHALL NOT be read as one.

The unit's membership SHALL be established from its record's `path` and `branch`, and its claim SHALL be consulted only
to contradict them. Membership SHALL NOT be taken from the `Resume checkpoint`'s own copy of that state.

A disagreement among those records SHALL stop the resume.

Once the work continues, the `Resume checkpoint` SHALL be frozen as a snapshot: it SHALL NOT be updated as the unit
proceeds. A later pause replaces that snapshot with a new one.

A decision the unit awaits SHALL be restored unless the continuation request answers it.

An unknown outcome SHALL NOT count as success.

#### Scenario: The available state is verified

- **WHEN** a session continues an unfinished unit
- **THEN** it verifies the workspace records and the live tree before its first write
- **AND** it compares them with the `Resume checkpoint` when the unit carries one

#### Scenario: A mismatch stops the resume

- **WHEN** the live state disagrees with the `Resume checkpoint`
- **THEN** the session stops and reports the disagreement instead of repairing the tree or reconstructing the earlier
  context
- **AND** an absent checkpoint is not a mismatch

#### Scenario: An absent resume checkpoint leaves context unrecovered

- **WHEN** the unit carries no `Resume checkpoint`
- **THEN** the session states that the context no record holds is unrecovered and derives its next step from the
  artifacts it can read
- **AND** the absence neither proves the interruption went wrong nor permits inventing that context

#### Scenario: A restored request stays open when the request does not answer it

- **WHEN** the unit was waiting on a decision from the owner when the work stopped, and the continuation request does
  not answer it
- **THEN** the session restores what is awaited and does not treat it as answered, so the resume may end by asking again
- **AND** a bare request answers nothing

#### Scenario: An answer the continuation request carries is applied

- **WHEN** the continuation request answers the decision the unit awaited
- **THEN** the session applies that answer and does not ask the decision again

#### Scenario: An unknown outcome is not a success

- **WHEN** an operation's outcome is unknown, whether a record carries it or the live state shows it
- **THEN** the session establishes that outcome before relying on the operation or re-running it

#### Scenario: A disagreement among the unit's records stops the resume

- **WHEN** the unit's record, its branch, or its claim disagree about which unit the tree holds, including a
  `Resume checkpoint` whose own copy of that state differs from the record it cites
- **THEN** the session stops and reports the disagreement, and takes no value from the copy
- **AND** it writes no work in the tree

#### Scenario: A resumed checkpoint is frozen

- **WHEN** the resume continues the unit's work
- **THEN** the checkpoint stays as the snapshot the pause wrote, and the unit's later progress is not recorded into it

## ADDED Requirements

### Requirement: A unit's record carries its state

A unit's record SHALL carry exactly one state: `active`, `paused`, or `closed`.

A record with no state SHALL be read as unknown.

The state SHALL change only at the transition it names:

- `paused` is written when a pause records its checkpoint.
- `active` is written when a resume continues the work, after its state verification and the writer handover.
- `closed` is written when the unit is delivered, rejected, or abandoned.

For a unit that ships, `closed` SHALL be written in the commit before its merge — the archive commit for a change unit,
and the unit's final commit otherwise — because a write after the merge cannot reach the branch it merges.

For a unit that does not ship, `closed` SHALL be written in its own tree before its branch is released, and that write
is local by nature, since no merge carries it.

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

### Requirement: A continuation establishes which unit it continues

Before it verifies the state, a session continuing an unfinished unit SHALL establish which unit it continues.

A unit the request names SHALL be resolved among the repository's registered working trees and its primary checkout.

Otherwise, the units whose record carries the state `paused` SHALL be discovered.

A single discovered unit SHALL be continued. Several SHALL be presented to the owner for a choice, and none SHALL be
continued until that choice arrives. IF no unit's record carries `paused`, THEN the session SHALL report that none was
found and SHALL stop.

The current tree's unit SHALL NOT be preferred.

A record whose state is absent SHALL be reported as unknown and SHALL NOT be selected automatically, and an incomplete
discovery SHALL select nothing.

An assignment whose state is absent or cannot be read SHALL block automatic selection of every other unit, because the
set of paused units is not established while one assignment is unresolved.

A session that must move to the unit's tree SHALL do so only after that resolution, and SHALL re-verify the path and
branch there.

The name SHALL establish the unit to continue, and SHALL NOT establish the writer handover or answer a decision the unit
awaits.

#### Scenario: A named unit resolves to its tree

- **WHEN** the continuation request names a unit that one registered working tree or the primary checkout assigns
- **THEN** the session resolves it there, verifies its membership from the owning records, and continues it
- **AND** it moves the session first when the session is in another tree, and re-verifies the path and branch afterwards

#### Scenario: A unit without a change directory resolves like any other

- **WHEN** the named or discovered unit has no change directory
- **THEN** it is resolved from its record, whose `path` and `branch` name the tree that holds it, and its claim is
  consulted only to contradict that, never to establish the name

#### Scenario: An unknown name stops

- **WHEN** the named unit has no assignment in any registered tree
- **THEN** the session reports that, and continues nothing

#### Scenario: Several assignments of one name stop

- **WHEN** the named unit is assigned by more than one registered tree
- **THEN** the session reports the conflict with its trees, and continues nothing

#### Scenario: A closed record with no live assignment stops

- **WHEN** the named unit's record is `closed` and no tree assigns it
- **THEN** the session reports that no such unit is resumable, and does not reopen it

#### Scenario: The only paused unit continues

- **WHEN** a continuation request names no unit, and exactly one unit's record carries the state `paused`
- **THEN** the session reports that unit and its tree and continues it

#### Scenario: Several paused units are offered to the owner

- **WHEN** a continuation request names no unit, and more than one unit's record carries the state `paused`
- **THEN** the session presents each unit with its tree and state and ends the turn for the owner's choice
- **AND** it continues none of them until that choice is answered

#### Scenario: No paused unit is found

- **WHEN** a continuation request names no unit, no record carries the state `paused`, and the discovery is complete
- **THEN** the session reports that none was found and stops, continuing no other unfinished work in the current tree

#### Scenario: A legacy record is unknown rather than paused

- **WHEN** an assignment's record carries no state, or its checkpoint still sits in the change's `tasks.md`
- **THEN** the discovery reports it as unknown, does not select it automatically, and blocks the automatic choice of
  another unit

#### Scenario: An unknown assignment blocks the automatic choice

- **WHEN** the discovery finds one paused unit and also an assignment whose state it could not read
- **THEN** the session continues neither, reports the unresolved assignment, and ends the turn

#### Scenario: An incomplete discovery selects nothing

- **WHEN** a registered tree or a record cannot be read, or an assignment is contradictory
- **THEN** the session reports the discovery as incomplete and continues nothing, rather than treating the readable
  candidates as the whole set
