## MODIFIED Requirements

### Requirement: GitHub workflows are linted by the build

The build SHALL lint every GitHub Actions workflow (`.github/workflows/*.yml`) with actionlint as part of the standard
`check` task, so workflow structure and expression errors surface locally instead of failing remotely after push. If
`actionlint` is not installed, the gate SHALL fail with a clear message naming the tool. Where the merge gate installs
that tool in CI, the install SHALL be resilient to a transient download failure — retried before the step fails — so an
upstream hiccup does not fail a gate whose subject is the repository's own workflows.

#### Scenario: Workflow lint runs in the standard check

- **WHEN** the standard `check` task runs
- **THEN** it lints every `.github/workflows/*.yml` file with actionlint

#### Scenario: Workflow syntax error fails the build

- **WHEN** a workflow file has a syntax error (e.g. malformed `on` or job definition)
- **THEN** the lint check fails and reports the offending workflow and line

#### Scenario: Broken run script fails the build

- **WHEN** a workflow carries an error actionlint catches natively — an invalid step in the `run:` step's own definition
  (a `syntax-check` error), or an expression error such as an undefined context variable — rather than one only
  shellcheck would report
- **THEN** the lint check fails and reports the offending workflow and location

#### Scenario: Missing actionlint is reported clearly

- **WHEN** the lint check runs on a machine without `actionlint` installed
- **THEN** it fails with a message naming `actionlint` as the required tool

#### Scenario: A transient install failure is retried before the step fails

- **WHEN** the merge gate's install of the lint tool fails transiently (a download error that clears on a later attempt)
- **THEN** the step retries the install, so the gate fails only once the retries are exhausted — not on a single
  upstream hiccup whose subject is unrelated to the repository's workflows
