## ADDED Requirements

### Requirement: A dated ideas section carries an entry

The standard `check` task SHALL verify that every dated section (`## YYYY-MM-DD`) in `docs/ideas.md` carries at least
one idea entry, so removing a section's last entry fails the build rather than leaving an empty heading in the file. The
verification SHALL name each offending section's line and heading. A file that carries no dated section SHALL pass: an
emptied ideas file is a legitimate state, not a defect. A missing `docs/ideas.md` SHALL fail.

#### Scenario: The standard check verifies the ideas sections

- **WHEN** the standard `check` task runs
- **THEN** it includes the ideas-section verification

#### Scenario: An emptied section fails the build

- **WHEN** a dated section in `docs/ideas.md` carries no `- ` entry
- **THEN** the verification fails and names the section's line and heading

#### Scenario: A section carrying entries passes

- **WHEN** every dated section in `docs/ideas.md` carries at least one `- ` entry
- **THEN** the verification passes

#### Scenario: A file with no dated section passes

- **WHEN** `docs/ideas.md` carries no dated section
- **THEN** the verification passes

#### Scenario: A missing ideas file fails

- **WHEN** `docs/ideas.md` is absent
- **THEN** the verification fails and names the missing file
