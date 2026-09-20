## ADDED Requirements

### Requirement: The on-demand audits are also available on a schedule

The repository's audits of its agent tooling, its spec corpus, and its architecture SHALL be runnable unattended on a
schedule, in addition to their on-demand `/audit-*` triggers, so the reconciliation they perform does not depend on a
human asking. The scheduled run SHALL perform the audits through the OpenCode GitHub action's scheduled path — which
requires a `prompt` input, authenticates by OIDC (`id-token: write`), and produces a branch or pull request rather than
an issue — and SHALL batch them into one run whose findings land in a single committed report the action carries into a
pull request, so the pro-model cost is one run per period rather than one per audit. Its cadence SHALL serve the
reconciliation the audits exist for (the consolidation that counters the accretion the capture loop produces) rather
than being a bare reminder: a run with nothing to report SHALL commit nothing and so produce no artifact.

#### Scenario: The audits run unattended on their schedule

- **WHEN** the scheduled audit workflow fires
- **THEN** it runs the agents-auditor, specs-auditor, and architecture-auditor audits in one unattended agent run and
  reports their findings together

#### Scenario: The scheduled audits do not replace the on-demand triggers

- **WHEN** a maintainer invokes an audit on demand (e.g. `/audit-agents`)
- **THEN** the on-demand trigger still runs that single audit, and the scheduled workflow's existence does not change it
