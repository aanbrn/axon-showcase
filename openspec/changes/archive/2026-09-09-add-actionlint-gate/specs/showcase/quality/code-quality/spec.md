## ADDED Requirements

### Requirement: GitHub workflows are linted by the build

The build SHALL lint every GitHub Actions workflow (`.github/workflows/*.yml`) with actionlint as part of the standard
`check` task, so workflow syntax and `run:` script errors surface locally instead of failing remotely after push. If
`actionlint` is not installed, the gate SHALL fail with a clear message naming the tool.

#### Scenario: Workflow lint runs in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it lints every `.github/workflows/*.yml` file with actionlint

#### Scenario: Workflow syntax error fails the build

- **WHEN** a workflow file has a syntax error (e.g. malformed `on` or job definition)
- **THEN** the lint check fails and reports the offending workflow and line

#### Scenario: Broken run script fails the build

- **WHEN** a `run:` script in a workflow contains a shell error that shellcheck reports
- **THEN** the lint check fails and reports the offending script

#### Scenario: Missing actionlint is reported clearly

- **WHEN** the lint check runs on a machine without `actionlint` installed
- **THEN** it fails with a message naming `actionlint` as the required tool
