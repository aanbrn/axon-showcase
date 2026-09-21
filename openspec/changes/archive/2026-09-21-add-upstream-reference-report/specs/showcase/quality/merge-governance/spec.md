## ADDED Requirements

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
