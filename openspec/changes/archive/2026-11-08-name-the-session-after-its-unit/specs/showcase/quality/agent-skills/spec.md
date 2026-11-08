# showcase/quality/agent-skills Specification

## ADDED Requirements

### Requirement: A session's title names the unit it works on

A session that works on a unit SHALL carry that unit's name as its title once the unit is established.

The name SHALL be the unit's record directory name, alone, with no prefix, stage, or status marker.

The title SHALL be synchronized when a unit becomes the session's current unit: at Begin once the unit's workspace and
record exist and its applicable claim step has run — a change-named branch may omit the claim — and at Resume once the
unit is verified and any writer handover or migration is done.

The title SHALL be updated when a later unit becomes the session's current unit, including when the session worked on a
different unit before.

An attachment that does not resolve SHALL leave the session's title unchanged.

A session that is not working on a unit SHALL NOT be renamed: tangential discussion, a consultation, a review, and a
delegate's session keep their titles.

A pause or a settlement SHALL keep the last title.

A rename that fails SHALL be reported and SHALL NOT block the unit's work.

An explicit instruction from the owner to keep a particular title SHALL override this requirement.

`AGENTS.md`'s Workspace rules point at this requirement, and the `begin-unit` and `pause-resume-unit` skills run the
steps.

#### Scenario: Begin names a fresh session after the unit

- **WHEN** a unit is begun in a session, its workspace and record are established, and its applicable claim step has run
- **THEN** the session's title is the unit's record directory name

#### Scenario: Begin renames a session that worked on another unit

- **WHEN** a session that previously worked on another unit begins a new unit
- **THEN** the session's title becomes the new unit's name

#### Scenario: Resume names the session after the resumed unit

- **WHEN** an unfinished unit is resumed, verified, and any writer handover or migration is done
- **THEN** the session's title is the resumed unit's name

#### Scenario: An unresolved attachment leaves the title alone

- **WHEN** a resume stops at selection, verification, or a required handover
- **THEN** the session's title is unchanged

#### Scenario: A session outside a unit is not renamed

- **WHEN** the session handles tangential discussion, a consultation, a review, or a delegate's task
- **THEN** its title is unchanged

#### Scenario: A pause and a settlement keep the last title

- **WHEN** a unit is paused or settled
- **THEN** the session keeps the title of the unit it last established

#### Scenario: A failed rename is reported and does not block

- **WHEN** the rename cannot be performed or its readback does not match
- **THEN** the failure is reported and the unit's work continues

#### Scenario: The owner's preference overrides the convention

- **WHEN** the owner asks for a particular title to be kept
- **THEN** the automatic convention does not replace it
