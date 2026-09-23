# showcase/quality/merge-governance Specification

## Purpose

Defines how changes land on `main`: the branch-protection rulesets that constrain pushes and merges (force-push, linear
history, PR approval, deletion), and the continuous-integration gates that run on pull requests and pushes, together
with the observational scheduled workflows (the update checks, the upstream-reference report, and the repository audits)
that report without gating. The workflows run existing Gradle gates without introducing new application behavior.

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

The repository SHALL require linear history on `main` via an active branch ruleset targeting `refs/heads/main` with NO
bypass actors — merge commits SHALL NOT be pushed to `main` by anyone, including repository administrators.

#### Scenario: Non-linear push to main is rejected

- **WHEN** a commit with more than one parent is pushed to `main`
- **THEN** the push is rejected by the linear-history ruleset

#### Scenario: Repository administrators cannot bypass linear history

- **WHEN** a repository administrator attempts to push a merge commit to `main`
- **THEN** the push is rejected because the linear-history ruleset has no bypass actors

### Requirement: Merges into main require a pull request review

The repository SHALL require pull request reviews before merging into `main` via an active branch ruleset targeting
`refs/heads/main`: at least one approving review SHALL be required, and the allowed merge method SHALL be squash merge
only. The repository owner SHALL be an always-bypass actor for this ruleset.

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

The repository SHALL prevent deletion of `main` via an active branch ruleset targeting `refs/heads/main` with NO bypass
actors.

#### Scenario: Branch deletion is rejected

- **WHEN** an actor attempts to delete the `main` branch via the UI or the git references API
- **THEN** the deletion is rejected by the deletion-protection ruleset

#### Scenario: Repository administrators cannot delete main

- **WHEN** a repository administrator attempts to delete the `main` branch
- **THEN** the deletion is rejected because the deletion-protection ruleset has no bypass actors

### Requirement: Pull requests run the fast quality gate

A pull request to the repository SHALL run the Docker-free quality tiers as a single CI check named `build`: the
standard `check` task with `-PskipITs` (unit tests, component tests, and all static gates — formatting, checkstyle,
SpotBugs, ErrorProne), an OpenSpec validation of changes and specs, and a verification that the OpenSpec configuration's
declared list surfaces — its per-artifact rules and its operation guidance — are readable by the CLI, so a list the CLI
would silently ignore fails the check. The coverage gate SHALL NOT run on the pull-request path, because it is
calibrated on integration-test coverage that only the `main` gate provides. The check SHALL run on `ubuntu-latest` with
a Temurin JDK 21 and SHALL NOT require Docker.

#### Scenario: Pull request triggers the fast gate

- **WHEN** a pull request is opened or updated
- **THEN** the `build` check runs `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` (without the coverage gate)
  and `openspec validate --all` against the pull request head, followed by the OpenSpec configuration check

#### Scenario: Fast gate failure blocks merging

- **WHEN** a pull request's `build` check fails (style, unit, component, or OpenSpec validation)
- **THEN** the pull request is not mergeable until the check passes

#### Scenario: A rule set the CLI cannot read fails the fast gate

- **WHEN** `openspec/config.yaml` declares a per-artifact rule in a shape the CLI cannot read (for example an unquoted
  scalar containing `: `, which YAML parses as a mapping rather than a string)
- **THEN** the OpenSpec configuration check fails and reports the defect, instead of the CLI silently ignoring that
  artifact's rules

#### Scenario: An operation guidance set the CLI cannot read fails the fast gate

- **WHEN** `openspec/config.yaml` declares an operation guidance item in a shape the CLI cannot read (for example an
  unquoted scalar containing `: `)
- **THEN** the OpenSpec configuration check fails and reports the defect, instead of the CLI silently ignoring that
  operation's guidance

### Requirement: Pushes to main run the full quality gate

A push to `main` SHALL run the full quality gate as the same `build` check: the complete `check` task including the
integration tier (Testcontainers — PostgreSQL, Kafka, OpenSearch) and the JaCoCo coverage gate, plus an OpenSpec
validation and the same verification that the OpenSpec configuration's declared list surfaces — its per-artifact rules
and its operation guidance — are readable by the CLI. The full gate SHALL require Docker.

#### Scenario: Push to main triggers the full gate

- **WHEN** a commit is pushed to `main`
- **THEN** the `build` check runs `./gradlew check` (with integration tests and coverage), `openspec validate --all`,
  and the OpenSpec configuration check

#### Scenario: Integration-tier failure fails the main gate

- **WHEN** the `build` check on `main` fails in the integration tier
- **THEN** the check is reported as failed and the commit does not pass the quality gate

### Requirement: Merge protection requires the CI check for everyone

The repository SHALL require the `build` status check before any commit merges into `main`, enforced by a branch ruleset
targeting `refs/heads/main` with NO bypass actors — the check SHALL bind repository administrators as well as ordinary
contributors.

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

The end-to-end test suite SHALL run automatically on a nightly schedule and be manually triggerable, as the same `e2e`
job in a dedicated workflow separate from the merge gate. It SHALL build all four service images and boot the full
pipeline (PostgreSQL, Kafka, OpenSearch, and the four services) via the existing `:showcase-api-gateway:e2eTest` task,
SHALL run on `ubuntu-latest` with a Temurin JDK 21, and SHALL NOT be part of the merge-gate `build` check or a required
check for merging into `main`.

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

The dependency security scan SHALL run automatically on a schedule and be manually triggerable, as the same `snyk` job
in a dedicated workflow separate from the merge gate. It SHALL install the Snyk CLI, SHALL run the existing
`:dependencySecurityCheck` task (Snyk `test --all-sub-projects --policy-path=.snyk`), SHALL authenticate with the
`SNYK_TOKEN` secret, SHALL run on `ubuntu-latest`, and SHALL NOT be part of the merge-gate `build` check or a required
check for merging into `main`.

#### Scenario: Scheduled trigger runs the dependency security scan

- **WHEN** the scheduled trigger fires
- **THEN** the `snyk` job runs `./gradlew dependencySecurityCheck`, scanning all sub-projects with the root `.snyk`
  policy applied

#### Scenario: Manual trigger runs the dependency security scan

- **WHEN** a maintainer dispatches the snyk workflow manually
- **THEN** the `snyk` job runs the same scan against the current `main`

#### Scenario: The dependency security scan is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the snyk run is not required, because it is not part of the merge-gate `build` check and no ruleset requires
  it

### Requirement: Dependency update report runs on a schedule and on demand

The dependency update report SHALL run automatically on a schedule and be manually triggerable, as the same
`dependency-updates` job in a dedicated workflow separate from the merge gate. It SHALL run the existing
`dependencyUpdates` Gradle task (catalog-owned coordinates, majors for deferred groups suppressed) and SHALL surface the
result by opening or updating a GitHub issue, SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN` granted
`issues: write`, and SHALL NOT be part of the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the dependency update report

- **WHEN** the scheduled trigger fires
- **THEN** the `dependency-updates` job runs `./gradlew dependencyUpdates` and opens or updates the "Dependency updates"
  issue with the available stable catalog updates and the Gradle wrapper section (the actionable sections of the report
  only), and posts a new comment mentioning the repository owner so they are notified

#### Scenario: Manual trigger runs the dependency update report

- **WHEN** a maintainer dispatches the dependency-updates workflow manually
- **THEN** the `dependency-updates` job runs the same report against the current `main` and updates the issue

#### Scenario: The dependency update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the dependency-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

#### Scenario: No stable updates are available

- **WHEN** the report contains no stable catalog updates (no "dependencies have newer versions" section)
- **THEN** the issue states that no stable catalog updates are available, without listing the non-actionable milestone
  sections, and no notification comment is posted

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds actionable updates (stable dependency updates or a
  newer Gradle wrapper)
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the previous
  bot-authored comment, keeping at most one bot comment on the issue

### Requirement: Helm update report runs on a schedule and on demand

The Helm update report SHALL run automatically on a schedule and be manually triggerable, as the same `helm-updates` job
in a dedicated workflow separate from the merge gate. It SHALL run an existing Gradle check (e.g. `helmUpdates`) that
reports, for the pinned Helm CLI version and each pinned Helm chart coordinate in the version catalog, the latest
available version from the Helm CLI's release channel and the charts' repositories, and SHALL surface the result by
opening or updating a GitHub issue, SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN` granted `issues: write`, and
SHALL NOT be part of the merge-gate `build` check or a required check for merging into `main`.

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
  diverges from the test-surface `*-image-tag` pins; a same-major (minor or patch) chart update SHALL still be reported

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds available Helm updates
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the previous
  bot-authored comment, keeping at most one bot comment on the issue

### Requirement: Buildpack update report runs on a schedule and on demand

The Paketo buildpack update report SHALL run automatically on a schedule and be manually triggerable, as the same
`buildpack-updates` job in a dedicated workflow separate from the merge gate. It SHALL run the `buildpackUpdates` Gradle
check that reports, for the pinned Paketo builder and each pinned buildpack in the version catalog, the latest available
version from the registry, and SHALL surface the result by opening or updating a GitHub issue, SHALL run on
`ubuntu-latest` with the `GITHUB_TOKEN` granted `issues: write`, and SHALL NOT be part of the merge-gate `build` check
or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the buildpack update report

- **WHEN** the scheduled trigger fires
- **THEN** the `buildpack-updates` job runs the buildpack update check and opens or updates the "Buildpack updates"
  issue listing each pinned Paketo builder and buildpack coordinate with an available newer version, and posts a new
  comment mentioning the repository owner so they are notified

#### Scenario: Manual trigger runs the buildpack update report

- **WHEN** a maintainer dispatches the buildpack-updates workflow manually
- **THEN** the `buildpack-updates` job runs the same check against the current `main` and updates the issue

#### Scenario: The buildpack update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the buildpack-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

#### Scenario: No buildpack updates are available

- **WHEN** every pinned Paketo builder and buildpack coordinate is current
- **THEN** the issue states that no buildpack updates are available, and no notification comment is posted

#### Scenario: Repeated runs notify the owner without accumulating comments

- **WHEN** the workflow runs again on an existing issue and finds an available update
- **THEN** it posts a new comment mentioning the repository owner (so the owner is notified) and removes the previous
  bot-authored comment, keeping at most one bot comment on the issue

### Requirement: Pinned-tool update report runs on a schedule and on demand

The pinned-tool update report SHALL run automatically on a schedule and be manually triggerable, as its own
`tooling-updates` job in a dedicated workflow separate from the merge gate. It SHALL run a Gradle check that reports,
for each declared pinned-tool check — a tool whose version is pinned in a workflow rather than in the version catalog —
the latest available version, so a pin that lags is visible, and SHALL surface the result by opening or updating a
GitHub issue, updated silently when nothing is actionable. It SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN`
granted `issues: write`, and SHALL NOT be part of the merge-gate `build` check or a required check for merging into
`main`.

#### Scenario: Scheduled trigger runs the pinned-tool update report

- **WHEN** the scheduled trigger fires
- **THEN** the `tooling-updates` job runs the tool-pin check and opens or updates the "Tooling updates" issue listing
  each pinned tool with an available newer version, mentioning the repository owner so they are notified

#### Scenario: Manual trigger runs the pinned-tool update report

- **WHEN** a maintainer dispatches the tooling-updates workflow manually
- **THEN** the `tooling-updates` job runs the same check against the current `main` and updates the issue

#### Scenario: A current pin reports no update

- **WHEN** every checked tool's pin matches the latest available version
- **THEN** the report carries no actionable update, and the run updates the issue without mentioning the repository
  owner

#### Scenario: Repeated runs replace the notification rather than stack it

- **WHEN** the workflow runs again on an existing issue with an available update
- **THEN** it replaces the previous owner-mention comment rather than adding another

#### Scenario: The pinned-tool update report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the tooling-updates run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it

### Requirement: Repository audits run on a schedule and on demand

The repository audits SHALL run automatically on a schedule and be manually triggerable, as a dedicated `audit` workflow
separate from the merge gate. The workflow SHALL invoke the audits unattended through the OpenCode GitHub action's
scheduled-run mechanism (a `prompt` input, which that trigger requires, and a single batched run rather than one
workflow per audit), SHALL report the findings into a reviewable artifact — a pull request opened from the branch the
run commits the report to, since the scheduled path has no issue to comment on and produces a pull request only when the
run leaves commits — and SHALL commit nothing when the audits report nothing. It SHALL notify the repository owner of a
report pull request by mentioning them in that pull request's body, so the report is not left unread. That mention SHALL
be placed in the body rather than leading the agent's response, because the action derives the pull request title by
summarising the response (a response dominated by the mention tends to summarise into a title about it rather than the
report). It SHALL run on `ubuntu-latest` with `id-token: write` (the action authenticates by OIDC by default),
`contents: write`, and `pull-requests: write` (the scopes the no-actor scheduled path needs to open a branch and a pull
request), and SHALL NOT be part of the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the audits

- **WHEN** the scheduled trigger fires
- **THEN** the `audit` job runs the three audits in one batched agent run, writes their findings in the shared report
  contract to a dated file, and commits it so the action opens a pull request carrying the report

#### Scenario: The owner is notified of a report pull request

- **WHEN** an audit run opens a pull request carrying its report
- **THEN** the pull request body mentions the repository owner, so they are notified the report is available for review,
  and the agent's response does not lead with the mention, so its substance — and therefore the generated title — tends
  to concern the report rather than the mention

#### Scenario: A clean audit run makes no artifact

- **WHEN** the scheduled run finds nothing across the three audits
- **THEN** it commits nothing, so the action opens no pull request and a quiet week produces no noise

#### Scenario: Manual trigger runs the audits

- **WHEN** a maintainer dispatches the audit workflow manually
- **THEN** the `audit` job runs the same audits against the current `main` through the same scheduled-run mechanism

#### Scenario: The scheduled audit is not a merge gate

- **WHEN** the merge-gate `build` check runs on a pull request
- **THEN** it does not include the audit workflow, which is triggered only by its schedule and by manual dispatch

### Requirement: Upstream-reference report runs on a schedule and on demand

The upstream-reference report SHALL run automatically on a schedule and be manually triggerable, as its own
`upstream-references` job in a dedicated workflow separate from the merge gate. It SHALL run a Gradle check that scans
the repository's durable artifacts (`AGENTS.md`, `README.md`, `docs/adr/`, and `docs/ideas.md`) for each
`owner/repo#NNN` reference, resolves its state through the GitHub API, and reports every reference with that state and
the file and line it is cited at. Because a reference is cited for several reasons — a gap awaiting an upstream fix, a
documentation pointer, or a counter-example — a closed reference is surfaced as a _trigger to check_ rather than
declared actionable: the report lists the state, and the run SHALL surface it by opening or updating a GitHub issue,
mentioning the repository owner when any reference has closed or failed to resolve and updating the issue silently when
every reference is open. It SHALL run on `ubuntu-latest` with the `GITHUB_TOKEN` granted `issues: write` and
`contents: read`, and SHALL NOT be part of the merge-gate `build` check or a required check for merging into `main`.

#### Scenario: Scheduled trigger runs the upstream-reference report

- **WHEN** the scheduled trigger fires
- **THEN** the `upstream-references` job scans the corpus, resolves each reference, and opens or updates the "Upstream
  references" issue, mentioning the repository owner when any reference has closed or does not resolve

#### Scenario: Manual trigger runs the upstream-reference report

- **WHEN** a maintainer dispatches the upstream-references workflow manually
- **THEN** the job runs the same check against the current `main` and updates the issue

#### Scenario: Every reference open reports nothing actionable

- **WHEN** every reference the corpus cites resolves to an open issue
- **THEN** the run updates the issue without mentioning the repository owner

#### Scenario: Repeated runs replace the notification rather than stack it

- **WHEN** the workflow runs again on an existing issue with a closed or unresolved reference
- **THEN** it replaces the previous owner-mention comment rather than adding another

#### Scenario: A reference cited for a reason other than waiting is still listed by state

- **WHEN** the corpus cites a reference as a documentation pointer or a counter-example rather than a gap awaiting a fix
- **THEN** the report lists it with its state, so its closure is visible for the owner to judge rather than suppressed

#### Scenario: The upstream-reference report is not a merge gate

- **WHEN** a pull request or push to `main` is evaluated for merging
- **THEN** the upstream-references run is not required, because it is not part of the merge-gate `build` check and no
  ruleset requires it
