# showcase/quality/merge-governance Specification

## Purpose

Defines how changes land on `main`: the branch-protection rulesets that constrain pushes and merges (force-push,
linear history, PR approval, deletion), and the continuous-integration gates that run on pull requests and pushes.
The workflows run existing Gradle gates without introducing new application behavior.

## Requirements

### Requirement: Force pushes to main are blocked

The repository SHALL block force pushes to `main` via an active branch ruleset targeting `refs/heads/main`. The
repository owner SHALL be an always-bypass actor for this ruleset.

#### Scenario: Force push to main is rejected

- **WHEN** a contributor attempts to force-push to `main`
- **THEN** the push is rejected by the force-push-protection ruleset

#### Scenario: Repository owner can force-push to main

- **WHEN** the repository owner force-pushes to `main`
- **THEN** the push succeeds because the owner is an always-bypass actor for the ruleset

### Requirement: Main history is linear

The repository SHALL require linear history on `main` via an active branch ruleset targeting `refs/heads/main` with
NO bypass actors — merge commits SHALL NOT be pushed to `main` by anyone, including repository administrators.

#### Scenario: Non-linear push to main is rejected

- **WHEN** a commit with more than one parent is pushed to `main`
- **THEN** the push is rejected by the linear-history ruleset

#### Scenario: Repository administrators cannot bypass linear history

- **WHEN** a repository administrator attempts to push a merge commit to `main`
- **THEN** the push is rejected because the linear-history ruleset has no bypass actors

### Requirement: Merges into main require a pull request review

The repository SHALL require pull request reviews before merging into `main` via an active branch ruleset targeting
`refs/heads/main`: at least one approving review SHALL be required, and the allowed merge method SHALL be squash
merge only. The repository owner SHALL be an always-bypass actor for this ruleset.

#### Scenario: Merge without approval is blocked

- **WHEN** a contributor attempts to merge a pull request into `main` with fewer than one approving review
- **THEN** the merge is blocked by the pull-request ruleset

#### Scenario: Only squash merges are allowed

- **WHEN** a contributor merges an approved pull request into `main`
- **THEN** only the squash merge method is available and produces a single linear commit

#### Scenario: Repository owner can merge without review

- **WHEN** the repository owner merges a pull request into `main` without an approving review
- **THEN** the merge succeeds because the owner is an always-bypass actor for the ruleset

### Requirement: Main branch cannot be deleted

The repository SHALL prevent deletion of `main` via an active branch ruleset targeting `refs/heads/main` with NO
bypass actors.

#### Scenario: Branch deletion is rejected

- **WHEN** an actor attempts to delete the `main` branch via the UI or the git references API
- **THEN** the deletion is rejected by the deletion-protection ruleset

#### Scenario: Repository administrators cannot delete main

- **WHEN** a repository administrator attempts to delete the `main` branch
- **THEN** the deletion is rejected because the deletion-protection ruleset has no bypass actors

### Requirement: Pull requests run the fast quality gate

A pull request to the repository SHALL run the Docker-free quality tiers as a single CI check named `build`: the
standard `check` task with `-PskipITs` (unit tests, component tests, and all static gates — formatting, checkstyle,
SpotBugs, ErrorProne) plus an OpenSpec validation of changes and specs. The coverage gate SHALL NOT run on the
pull-request path, because it is calibrated on integration-test coverage that only the `main` gate provides. The
check SHALL run on `ubuntu-latest` with a Temurin JDK 21 and SHALL NOT require Docker.

#### Scenario: Pull request triggers the fast gate

- **WHEN** a pull request is opened or updated
- **THEN** the `build` check runs `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (without the coverage
  gate) and `openspec validate --all` against the pull request head

#### Scenario: Fast gate failure blocks merging

- **WHEN** a pull request's `build` check fails (style, unit, component, or OpenSpec validation)
- **THEN** the pull request is not mergeable until the check passes

### Requirement: Pushes to main run the full quality gate

A push to `main` SHALL run the full quality gate as the same `build` check: the complete `check` task including the
integration tier (Testcontainers — PostgreSQL, Kafka, OpenSearch) and the JaCoCo coverage gate, plus an OpenSpec
validation. The full gate SHALL require Docker.

#### Scenario: Push to main triggers the full gate

- **WHEN** a commit is pushed to `main`
- **THEN** the `build` check runs `./gradlew check` (with integration tests and coverage) and `openspec validate --all`

#### Scenario: Integration-tier failure fails the main gate

- **WHEN** the `build` check on `main` fails in the integration tier
- **THEN** the check is reported as failed and the commit does not pass the quality gate

### Requirement: Merge protection requires the CI check for everyone

The repository SHALL require the `build` status check before any commit merges into `main`, enforced by a branch
ruleset targeting `refs/heads/main` with NO bypass actors — the check SHALL bind repository administrators as well as
ordinary contributors.

#### Scenario: Contributors cannot merge without a passing check

- **WHEN** a contributor attempts to merge a pull request into `main` whose `build` check has not passed
- **THEN** the merge is blocked by the required-check ruleset

#### Scenario: Repository administrators cannot bypass the check

- **WHEN** a repository administrator attempts to merge or push to `main` without a passing `build` check
- **THEN** the merge or push is blocked because the required-check ruleset has no bypass actors

### Requirement: Fast and full gates use the same check name

Both the pull-request fast gate and the `main` full gate SHALL report the CI result under the same status check name
`build`, so a single required check in the ruleset covers both paths.

#### Scenario: Both gates share the check name

- **WHEN** the pull-request fast gate and the `main` full gate each complete
- **THEN** both report a status check named `build`, distinguished by commit rather than by name

### Requirement: End-to-end tests run on a schedule and on demand

The end-to-end test suite SHALL run automatically on a nightly schedule and be manually triggerable, as the same
`e2e` job in a dedicated workflow separate from the merge gate. It SHALL build all four service images and boot the
full pipeline (PostgreSQL, Kafka, OpenSearch, and the four services) via the existing
`:showcase-api-gateway:e2eTest` task, SHALL run on `ubuntu-latest` with a Temurin JDK 21, and SHALL NOT be part of
the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Nightly schedule triggers the e2e suite

- **WHEN** the scheduled nightly trigger fires
- **THEN** the `e2e` job runs `./gradlew :showcase-api-gateway:e2eTest`, building all four service images and booting
  the full pipeline

#### Scenario: Manual trigger runs the e2e suite

- **WHEN** a maintainer dispatches the e2e workflow manually
- **THEN** the `e2e` job runs the same full end-to-end suite against the current `main`

#### Scenario: The e2e suite is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the e2e run is not required, because it is not part of the merge-gate `build` check and no ruleset requires
  it

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

### Requirement: Dependency update report runs on a schedule and on demand

The dependency update report SHALL run automatically on a schedule and be manually triggerable, as the same
`dependency-updates` job in a dedicated workflow separate from the merge gate. It SHALL run the existing
`dependencyUpdates` Gradle task (catalog-owned coordinates, majors for deferred groups suppressed) and SHALL surface
the result by opening or updating a GitHub issue, SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN` granted
`issues: write`, and SHALL NOT be part of the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the dependency update report

- **WHEN** the scheduled trigger fires
- **THEN** the `dependency-updates` job runs `./gradlew dependencyUpdates` and opens or updates the "Dependency
  updates" issue with the available catalog updates and the Gradle wrapper section, mentioning the repository owner
  so they are notified

#### Scenario: Manual trigger runs the dependency update report

- **WHEN** a maintainer dispatches the dependency-updates workflow manually
- **THEN** the `dependency-updates` job runs the same report against the current `main` and updates the issue

#### Scenario: The dependency update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the dependency-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it