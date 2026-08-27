# showcase/quality/merge-governance — Delta Spec

## Purpose
Defines how changes land on `main`: the branch-protection rulesets that constrain pushes and merges (force-push,
linear history, PR approval, deletion), and the continuous-integration gates that run on pull requests and pushes.
The workflows run existing Gradle gates without introducing new application behavior.

## ADDED Requirements

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
SpotBugs, ErrorProne, JaCoCo coverage) plus an OpenSpec validation of changes and specs. The check SHALL run on
`ubuntu-latest` with a Temurin JDK 21 and SHALL NOT require Docker.

#### Scenario: Pull request triggers the fast gate

- **WHEN** a pull request is opened or updated
- **THEN** the `build` check runs `./gradlew check -PskipITs` and `openspec validate` against the pull request head

#### Scenario: Fast gate failure blocks merging

- **WHEN** a pull request's `build` check fails (style, unit, component, coverage, or OpenSpec validation)
- **THEN** the pull request is not mergeable until the check passes

### Requirement: Pushes to main run the full quality gate

A push to `main` SHALL run the full quality gate as the same `build` check: the complete `check` task including the
integration tier (Testcontainers — PostgreSQL, Kafka, OpenSearch) and the JaCoCo coverage gate, plus an OpenSpec
validation. The full gate SHALL require Docker.

#### Scenario: Push to main triggers the full gate

- **WHEN** a commit is pushed to `main`
- **THEN** the `build` check runs `./gradlew check` (with integration tests and coverage) and `openspec validate`

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