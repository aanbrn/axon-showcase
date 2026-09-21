# Notify the owner from the audit report

## Why

The `audit` workflow opens its report PR with no notification: PRs #321 and #323 carried no reviewer, no assignee, and
no mention, so a report could sit unread until someone happened to look. The four update-check workflows already solve
this by mentioning the repository owner (`cc @<owner>`), and the audit report should do the same — the report only has
value once the owner knows it exists.

## What Changes

- **`.github/workflows/audit.yml`** — the prompt asks the agent to begin its final response with the owner mention, so
  the mention lands in the pull request body (the action builds that body from the agent's own response, so no workflow
  step, no PR discovery, and no extra permission is needed).
- **`openspec/changes/notify-owner-from-the-audit-report/specs/showcase/quality/merge-governance/spec.md`** — a
  `MODIFIED` block: the audit requirement gains the notification behavior, mirroring the sibling checks' `cc @<owner>`
  mention.
- **`AGENTS.md`** and **`README.md`** — refresh the two descriptions of the audit workflow's output to include the
  notification.

## Impact

- **Build**: none — a prompt line and documentation.
- **Tests**: none; the workflow is observational.
- **Verification**: only a dispatched run exercises the mention, since the body it lands in is produced by the action.
- **Capability**: `showcase/quality/merge-governance`'s audit requirement gains the behavior; its `## Purpose` needs the
  archive-commit refresh if the scope statement is affected (checked in the tasks).

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/merge-governance` — the "Repository audits run on a schedule and on demand" requirement gains the
  owner notification on the report PR.
