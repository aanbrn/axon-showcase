# showcase/quality/merge-governance — Delta Spec

## ADDED Requirements

### Requirement: Dependency security scan runs on a schedule and on demand

The dependency security scan SHALL run automatically on a schedule and be manually triggerable, as the same `snyk`
job in a dedicated workflow separate from the merge gate. It SHALL install the Snyk CLI, SHALL run the existing
`:dependencySecurityCheck` task (Snyk `test --all-sub-projects --policy-path=.snyk`), SHALL authenticate with the
`SNYK_TOKEN` secret, SHALL run on `ubuntu-latest`, and SHALL NOT be part of the merge-gate `build` check or a
required check for merging into `main`.

#### Scenario: Scheduled trigger runs the dependency security scan

- **WHEN** the scheduled trigger fires
- **THEN** the `snyk` job runs `./gradlew dependencySecurityCheck`, scanning all sub-projects with the root `.snyk`
  policy applied

#### Scenario: Manual trigger runs the dependency security scan

- **WHEN** a maintainer dispatches the snyk workflow manually
- **THEN** the `snyk` job runs the same scan against the current `main`

#### Scenario: The dependency security scan is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the snyk run is not required, because it is not part of the merge-gate `build` check and no ruleset
  requires it