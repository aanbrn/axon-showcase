## ADDED Requirements

### Requirement: Archived change names are unique

The standard `check` task SHALL verify that no two directories under `openspec/changes/archive/` carry the same change
name, its own name with one leading `YYYY-MM-DD-` segment removed, so a reused archived name fails the CI `build` gate.
It SHALL name the shared name and every directory carrying it. One historical collision SHALL be exempt only as the
exact directory set the verification records beside its reason; that exemption SHALL NOT cover the name generally.

#### Scenario: The standard check verifies the archived change names

- **WHEN** the standard `check` task runs
- **THEN** it includes the archived-change-name verification

#### Scenario: Two archived directories carrying one name fail the build

- **WHEN** two directories under `openspec/changes/archive/` carry the same change name after their date prefixes are
  removed
- **THEN** the verification fails and names the shared name and both directories

#### Scenario: A name shared across two archive dates fails the build

- **WHEN** two archived directories were archived on different dates and carry the same change name
- **THEN** the verification fails, because the date prefix is stripped rather than compared

#### Scenario: Distinct archived names pass

- **WHEN** no two archived directories carry the same change name after their date prefixes are removed
- **THEN** the verification passes

#### Scenario: An archive entry without a date prefix is not judged

- **WHEN** an `openspec/changes/archive/` entry's name carries no `YYYY-MM-DD-` prefix
- **THEN** the verification does not judge it and it joins no name group

#### Scenario: The acknowledged historical pair passes

- **WHEN** the only directories carrying a name are the exact pair the verification records as acknowledged
- **THEN** the verification passes

#### Scenario: A third directory carrying the acknowledged name fails

- **WHEN** a third directory carries a name whose recorded pair is acknowledged
- **THEN** the verification fails and names that directory

#### Scenario: A missing archive directory fails

- **WHEN** `openspec/changes/archive/` is absent
- **THEN** the verification fails and names the missing directory rather than reporting an empty archive clean
