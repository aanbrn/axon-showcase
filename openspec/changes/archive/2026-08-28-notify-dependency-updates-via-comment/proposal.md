# Notify on dependency updates via a new issue comment

## Why

The `dependency-updates` workflow opens or updates the "Dependency updates" issue and prefixes its body with
`cc @<owner>` to notify the repository owner. GitHub notifies on **new issue creation** and on **new comments**, but
**not on body edits**. The first run (create) notified the owner; every subsequent run (body edit via
`gh issue edit`) silently updates the issue without a notification — so the owner learns about new stable updates
only by manually checking. The last run demonstrated this: the issue was updated but no email arrived.

## What Changes

- In `.github/workflows/dependency-updates.yml`, after opening/updating the issue, the workflow posts a **new
  comment** on the issue that includes the `cc @<owner>` mention and the actionable report summary (stable catalog
  updates + Gradle wrapper status), so GitHub notifies the owner — but **only when there is something actionable**
  (stable dependency updates present or a newer Gradle wrapper). When there are no updates, the issue body is updated
  silently and no comment/notification is sent.
- To avoid comment accumulation, the workflow first deletes the previous bot-authored comment on the issue (the
  standard "one rolling comment" pattern), so at most one bot comment exists at a time.
- The issue body continues to hold the same filtered summary; the comment carries the notification-triggering
  mention.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance`: the dependency-updates requirement's scheduled-run scenario now states that the
  owner is notified via a new comment on the issue (not only on creation).

## Impact

- **Changed file**: `.github/workflows/dependency-updates.yml` (the issue step).
- **Behavior**: the owner receives an email notification when a run finds actionable updates (new comment with a
  mention); runs with no updates update the issue silently; at most one bot comment accumulates.
- **No secrets, no Gradle task changes**: the report file and `dependencyUpdates` task are untouched.