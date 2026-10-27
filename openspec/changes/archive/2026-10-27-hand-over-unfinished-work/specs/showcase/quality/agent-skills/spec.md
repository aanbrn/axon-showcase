# Spec Delta

## MODIFIED Requirements

### Requirement: The pause-and-resume procedure is maintained in its project skill

The unit pause-and-resume procedure SHALL live in the project skill `.opencode/skills/pause-resume-unit/SKILL.md`, which
owns the `Resume checkpoint` fields and the resume's ordered steps. `AGENTS.md` SHALL carry the entry point — the
trigger, the skill's path, a pointer to what the skill owns, and the stop-and-report rule for a missing or unreadable
skill — and SHALL NOT carry the checkpoint fields or the procedure's steps. The trigger SHALL include the agent's own
decision that it cannot reliably continue an unfinished unit in the current session, and the agent SHALL then follow the
pause procedure rather than continuing the unit through requests to confirm each next work step. A correction to the
procedure SHALL be made in the skill.

#### Scenario: The root trigger reaches the skill

- **WHEN** an agent is about to pause a unit, or continues a unit whose work is unfinished
- **THEN** `AGENTS.md` names the skill and its path, and the agent loads it before taking the step

#### Scenario: A handover the agent decides on reaches the skill

- **WHEN** the agent decides it cannot reliably continue an unfinished unit in the current session
- **THEN** `AGENTS.md` names the skill and its path, and the agent loads it and follows the pause procedure
- **AND** it does not continue the unit by requesting confirmation for each successive work step instead

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
to the unit. The pause report SHALL name the saved `Resume checkpoint` and tell the owner to continue in a new session
with `/resume-unit`. A session limitation the agent could not measure SHALL be described as an assessment rather than as
an exhausted context window or provider quota.

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

#### Scenario: The pause report names the way to continue

- **WHEN** an agent reports a pause it recorded
- **THEN** the report names the saved `Resume checkpoint` and tells the owner to continue in a new session with
  `/resume-unit`

#### Scenario: An unmeasured limitation is reported as an assessment

- **WHEN** the agent pauses because it decided it could not reliably continue, and it holds no measured limit from the
  harness, the provider, or the account
- **THEN** it describes the limitation as its own assessment rather than as an exhausted context window or provider
  quota
