## ADDED Requirements

### Requirement: No tracked file exceeds the configured size limit

The standard `check` task SHALL verify that no tracked file exceeds a maximum size declared in
`config/commit-hygiene/large-files.properties`, so a large blob — a binary, a vendored archive, or an accidentally
committed build output — fails the CI `build` gate before it enters history. The verification SHALL name each oversized
file with its size and the configured limit.

#### Scenario: The standard check verifies file sizes

- **WHEN** the standard `check` task runs
- **THEN** it includes the tracked-file size verification

#### Scenario: A tracked file over the limit fails the build

- **WHEN** a tracked file's size exceeds the configured maximum
- **THEN** the size verification fails and names the file, its size, and the limit

#### Scenario: Files at or under the limit pass

- **WHEN** every tracked file is at or under the configured maximum
- **THEN** the size verification passes
