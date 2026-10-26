## ADDED Requirements

### Requirement: The pause-and-resume procedure is maintained in its project skill

The unit pause-and-resume procedure SHALL live in the project skill `.opencode/skills/pause-resume-unit/SKILL.md`, which
owns the `Resume checkpoint` fields and the resume's ordered steps. `AGENTS.md` SHALL carry the entry point — the
trigger, the skill's path, a pointer to what the skill owns, and the stop-and-report rule for a missing or unreadable
skill — and SHALL NOT carry the checkpoint fields or the procedure's steps. A correction to the procedure SHALL be made
in the skill.

#### Scenario: The root trigger reaches the skill

- **WHEN** an agent is about to pause a unit, or continues a unit whose work is unfinished
- **THEN** `AGENTS.md` names the skill and its path, and the agent loads it before taking the step

#### Scenario: A missing skill stops the step

- **WHEN** the skill file is missing or unreadable at the trigger
- **THEN** the agent stops and reports it rather than writing an improvised `Resume checkpoint` or reconstructing the
  procedure from the root text

#### Scenario: A correction lands in the skill

- **WHEN** a `Resume checkpoint` field or a resume step is wrong or incomplete
- **THEN** it is corrected in the skill, and no command, spec, or `AGENTS.md` entry restates the procedure's steps

### Requirement: A pause records a resume checkpoint

The agent SHALL write the unit's `Resume checkpoint` — the fields the skill names — into the unit's `tasks.md`, or into
its hand-off when the unit has no change dir, before it reports a pause, and SHALL read that record back. A pause SHALL
alter no delivery state: nothing is committed, stashed, pushed, archived, or merged for it, and the tree stays assigned
to the unit.

#### Scenario: A pause records the resume checkpoint in one place

- **WHEN** the owner pauses a unit before its completion
- **THEN** the unit's record carries one `Resume checkpoint` section, created or updated in place, and it is re-read
  before the turn ends

#### Scenario: A pause leaves the delivery state alone

- **WHEN** a unit is paused
- **THEN** no commit, stash, push, archive, or merge is made for the pause, and the review gate's approval stop and
  staged-set rules are unchanged

#### Scenario: A pause keeps the unit's tree and reviewer

- **WHEN** a paused unit is resumed in the same tree
- **THEN** the tree still belongs to that unit, and no replacement reviewer session is started because of the pause
  alone

### Requirement: A continued unfinished unit verifies its available state before the first write

A session continuing an unfinished unit SHALL verify the state available to it — the workspace records and the live tree
— before its first write, compare that state with the unit's `Resume checkpoint` when it exists, and stop rather than
repair on a mismatch; an absent checkpoint SHALL NOT be read as one. A decision the unit awaits SHALL be restored unless
the continuation request answers it, and an unknown outcome SHALL NOT count as success.

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

### Requirement: A resumed unit establishes its writer handover when a different session continues it

When a session continuing an unfinished unit is not the writer its workspace record names, it SHALL establish that the
prior writer has ended before it writes work in the tree: it SHALL obtain the owner's confirmation — carried by the
continuation request or asked for — and record itself as the writer. The state verification SHALL precede that handover,
which SHALL NOT depend on a `Resume checkpoint`. An ownership it cannot establish SHALL stop the resume.

#### Scenario: The recorded writer continues the unit

- **WHEN** the session continuing the unit is the writer its workspace record names
- **THEN** it proceeds after the state verification, with no handover step

#### Scenario: A different session hands over before writing work

- **WHEN** the session's identity differs from the writer the record names
- **THEN** it verifies the available state, obtains the owner's confirmation that the prior writer has ended, records
  itself as the writer, and only then writes work in the tree
- **AND** a matching working tree alone does not establish that the prior writer ended

#### Scenario: A confirmation the continuation request carries

- **WHEN** the owner's continuation request states that the prior writer has ended
- **THEN** that satisfies the confirmation, and the session records itself as the writer without asking again

#### Scenario: A bare resume request is not a confirmation

- **WHEN** the request asks only to continue the unit
- **THEN** the session asks for the confirmation before it writes work

#### Scenario: A unit left waiting without a pause still hands over

- **WHEN** a new session continues an unfinished unit that had no announced pause and carries no `Resume checkpoint`
- **THEN** it verifies the available state, establishes the handover, states the context no record holds, and only then
  writes work in the tree

#### Scenario: An ownership it cannot establish stops the resume

- **WHEN** the record names no writer, or a handover is required and its confirmation is not given
- **THEN** the session stops for the owner's decision and writes no work in the tree
- **AND** no build or container must be stopped for that decision
