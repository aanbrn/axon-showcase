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
  updates" issue with the available stable catalog updates and the Gradle wrapper section (the actionable sections
  of the report only), and posts a new comment mentioning the repository owner so they are notified

#### Scenario: Manual trigger runs the dependency update report

- **WHEN** a maintainer dispatches the dependency-updates workflow manually
- **THEN** the `dependency-updates` job runs the same report against the current `main` and updates the issue

#### Scenario: The dependency update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the dependency-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

#### Scenario: No stable updates are available

- **WHEN** the report contains no stable catalog updates (no "dependencies have newer versions" section)
- **THEN** the issue states that no stable catalog updates are available, without listing the non-actionable
  milestone sections, and no notification comment is posted

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds actionable updates (stable dependency updates or a
  newer Gradle wrapper)
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the
  previous bot-authored comment, keeping at most one bot comment on the issue

### Requirement: Helm update report runs on a schedule and on demand

The Helm update report SHALL run automatically on a schedule and be manually triggerable, as the same `helm-updates`
job in a dedicated workflow separate from the merge gate. It SHALL run an existing Gradle check (e.g.
`helmUpdates`) that reports, for the pinned Helm CLI version and each pinned Helm chart coordinate in the version
catalog, the latest available version from the Helm CLI's release channel and the charts' repositories, and SHALL
surface the result by opening or updating a GitHub issue, SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN` granted
`issues: write`, and SHALL NOT be part of the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the Helm update report

- **WHEN** the scheduled trigger fires
- **THEN** the `helm-updates` job runs the Helm update check and opens or updates the "Helm updates" issue listing each
  pinned coordinate with an available newer version (Helm CLI and charts), and posts a new comment mentioning the
  repository owner so they are notified

#### Scenario: Manual trigger runs the Helm update report

- **WHEN** a maintainer dispatches the helm-updates workflow manually
- **THEN** the `helm-updates` job runs the same check against the current `main` and updates the issue

#### Scenario: The Helm update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the helm-updates run is not required, because it is not part of the merge-gate `build` check and no ruleset
  requires it

#### Scenario: No Helm updates are available

- **WHEN** every pinned Helm CLI and chart coordinate is current
- **THEN** the issue states that no Helm updates are available, and no notification comment is posted

#### Scenario: Major chart updates are suppressed for deferred charts

- **WHEN** a pinned chart is listed in the helm major-disabled configuration and only a major-jump chart version is
  available
- **THEN** the issue does not list that chart, because the major bump would carry a new preconfigured image tag that
  diverges from the test-surface `*-image-tag` pins; a same-major (minor or patch) chart update SHALL still be
  reported

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds available Helm updates
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the
  previous bot-authored comment, keeping at most one bot comment on the issue

### Requirement: Helm release namespaces are declared in the build

The Helm releases for the local deployment target SHALL declare their namespaces explicitly in `build.gradle.kts`: the
observability releases (kps, tempo) SHALL use the `monitoring` namespace, and the application and infrastructure
releases (db-events, kafka, os-views, axon-showcase) SHALL use a dedicated `axon-showcase` namespace created on
install. The local deployment SHALL NOT depend on the user's kube-context current namespace or a `helm.namespace`
gradle property for the release namespaces.

#### Scenario: All releases declare their namespaces explicitly

- **WHEN** a maintainer reads the Helm release configuration in `build.gradle.kts`
- **THEN** every release sets its `namespace` (kps and tempo in `monitoring`; db-events, kafka, os-views, and
  axon-showcase in `axon-showcase`), and the four app/infra releases set `createNamespace = true`

#### Scenario: The app and infrastructure releases share one namespace

- **WHEN** the four app/infra releases are installed
- **THEN** they are created in the `axon-showcase` namespace, so their short service DNS names resolve within it, and
  the namespace is created if absent

### Requirement: Each Helm release target declares its kube context

Every Helm release target SHALL declare the kube context it deploys to in the build's `releaseTargets` configuration.
The `local` target SHALL resolve its kube context per-machine, from a `helm.local.kubeContext` Gradle property, falling
back to the developer's current kube context when unset. Any remote target (e.g. staging) SHALL declare a shared, fixed
kube context in the build, since the same remote cluster serves every contributor. The repo SHALL NOT hard-code a
machine-specific local context name (such as a macOS-only colima context) in the versioned build.

#### Scenario: The local target resolves the developer's local cluster

- **WHEN** a developer runs a Helm install with the `local` release target active
- **THEN** the target uses the `helm.local.kubeContext` property if set, or the developer's current kube context
  otherwise, so macOS (colima) and Linux (kind/minikube) contributors each deploy to their own local cluster

#### Scenario: A remote target uses a shared fixed context

- **WHEN** a release target other than `local` (e.g. staging) is active
- **THEN** the target deploys to the kube context declared for it in the build, which is the same for every contributor

#### Scenario: No machine-specific context name in the repo

- **WHEN** a maintainer reads the `releaseTargets` configuration in `build.gradle.kts`
- **THEN** the `local` target does not hard-code a context name that only exists on one OS (such as `colima`), and any
  per-machine context value is supplied via the `helm.local.kubeContext` property
