## MODIFIED Requirements

### Requirement: The on-demand audits are also available on a schedule

The repository's audits of its agent tooling, its spec corpus, and its architecture SHALL be runnable unattended on a
schedule, in addition to their on-demand `/audit-*` triggers, so the reconciliation they perform does not depend on a
human asking. The scheduled run SHALL use the scheduled-audit mechanism `showcase/quality/merge-governance` specifies
(the "Repository audits run on a schedule and on demand" requirement). Its cadence SHALL serve the reconciliation the
audits exist for (the consolidation that counters the accretion the capture loop produces) rather than being a bare
reminder. The `readme-auditor` SHALL remain on-demand only rather than joining the scheduled set: it verifies the
human-facing `README.md` against the repository, an accuracy-and-coverage check distinct from the reconciliation of the
agent's own machinery the schedule exists for. The `experience-analyzer` SHALL likewise remain on-demand only: its
retrospective is a narrative judgment about a period rather than a reconciliation verifiable against the repository, and
its richest input — what went wrong that no diff captures — is available only in the session that lived it, so a
scheduled run could not supply it. A scheduled run that _produces_ the retrospective SHALL NOT be used in its place,
because it automates the decision rather than the trigger and still lacks the session-only context; a scheduled check
that only _reports_ what has accumulated since the newest retrospective, and otherwise stays silent, is not such a run —
it surfaces the trigger while leaving the decision and the analysis to the on-demand invocation.

#### Scenario: The audits run unattended on their schedule

- **WHEN** the scheduled audit workflow fires
- **THEN** it runs the agents-auditor, specs-auditor, and architecture-auditor audits

#### Scenario: The scheduled audits do not replace the on-demand triggers

- **WHEN** a maintainer invokes an audit on demand (e.g. `/audit-agents`)
- **THEN** the on-demand trigger still runs that single audit, and the scheduled workflow's existence does not change it
