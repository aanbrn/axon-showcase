# Spec Delta

## MODIFIED Requirements

### Requirement: Web UI dependency vulnerability scan

The build SHALL provide an `npmAudit` task that scans the web UI's npm dependencies with `npm audit`, covering both
production and development dependencies, and failing when a high-severity (or greater) vulnerability is present. The
task SHALL use the project's pinned Node and SHALL NOT be part of the `check` lifecycle. The task SHALL consult a
suppression list (`showcase-web-ui/npm-audit-ignores.json`) that names npm advisories with no available fix, each pinned
to its advisory id with a stated reason and an expiry date, so that an unfixable advisory does not leave the check
permanently red; a suppressed advisory's transitive dependents SHALL also be treated as suppressed. The task SHALL fail
when any unsuppressed high-severity (or greater) vulnerability is present, SHALL fail on a malformed suppression entry
or one whose advisory id matches no finding in the report (so a stale or mistyped entry cannot sit silently), and SHALL
report each suppressed advisory and its expiry.

#### Scenario: Developer runs the web UI vulnerability scan

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmAudit` and a high-severity vulnerability is present
- **THEN** the task fails and reports the vulnerable npm package and its advisory

#### Scenario: A development-dependency advisory is reported

- **WHEN** a high-severity vulnerability is present in a development dependency
- **THEN** the task fails, because the scan covers development as well as production dependencies

#### Scenario: A suppressed advisory passes with its expiry reported

- **WHEN** the only high-severity findings trace to an advisory listed in the suppression list with an unexpired
  `expires` date
- **THEN** the task succeeds and reports the suppressed advisory and its expiry

#### Scenario: An expired suppression fails again

- **WHEN** the only high-severity findings trace to a suppressed advisory whose `expires` date is in the past
- **THEN** the task fails, so the suppression re-surfaces for re-assessment rather than hiding the finding indefinitely

#### Scenario: A stale or mistyped suppression entry fails

- **WHEN** the suppression list holds an entry whose advisory id matches no finding in the `npm audit` report
- **THEN** the task fails, naming that entry, so a suppression for an already-fixed advisory (or a typo) cannot sit
  silently in the list

#### Scenario: A malformed suppression entry fails

- **WHEN** a suppression entry is missing its `id`, `reason`, or `expires` field
- **THEN** the task fails, naming the entry

#### Scenario: A clean web UI audit passes

- **WHEN** a developer runs `./gradlew :showcase-web-ui:npmAudit` and no high-severity vulnerability is present
- **THEN** the task completes successfully

#### Scenario: Normal build does not run the web UI vulnerability scan

- **WHEN** a developer runs `./gradlew check` or any build task other than `npmAudit`
- **THEN** the web UI vulnerability scan does not run
