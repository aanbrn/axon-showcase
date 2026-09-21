## MODIFIED Requirements

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
