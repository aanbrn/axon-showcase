# Notify on dependency updates via a new issue comment — Design

## Context

See proposal.md — Why. GitHub delivers notifications for **new issue creation** and **new comments**, but not for
**body edits** (confirmed against GitHub docs). The workflow's `cc @<owner>` mention in the issue body only notifies
on the first (create) run; `gh issue edit` on later runs updates the body silently. The issue step also already
filters the report to actionable sections (filter-dependency-updates-issue).

## Goals / Non-Goals

**Goals:**
- The owner is notified on every run that has actionable updates, and only then.
- At most one bot comment accumulates on the issue.
- Keep the filtered issue body and the in-place update behavior.

**Non-Goals:**
- Changing the report filter or the `dependencyUpdates` task.
- Multi-comment history — the rolling single-comment pattern keeps the issue clean.
- Notifying when there are no updates — a no-op run updates the issue body silently.

## Decisions

### D1: Post a new comment (with the mention) and delete the previous bot comment — only when there are updates

After the issue body is updated, the step determines whether anything is actionable: stable catalog updates present
(the "dependencies have newer versions" section is non-empty) or a newer Gradle wrapper (the Gradle line is not
`UP-TO-DATE`). Only when there is something actionable does it list the issue's comments, find the previous one
authored by `github-actions[bot]`, delete it, and post a fresh comment that includes `cc @<owner>` plus the summary.
When there are no updates, the issue body is still updated (with "No stable catalog updates available.") but no
comment is posted — so the owner is not notified about a no-op run.

Alternatives rejected:
- **Commenting on every run** — notifies even when there is nothing to act on (noise).
- **Keeping the mention in the body only** — doesn't notify on edits (the observed failure).

### D2: Comment body mirrors the filtered report summary

The comment carries the same filtered content as the issue body (stable updates + Gradle section, or the
no-updates line) so the notification email itself contains the actionable summary, not just "an issue was updated".

## Risks / Trade-offs

- **Deleting the previous comment loses history** → the issue body retains the latest full filtered summary, so the
  current state is always in the body; only old bot comments are pruned.
- **Comment author detection** → the step identifies the bot comment by author login (`github-actions[bot]`) and a
  marker in the body; a genuine maintainer comment is never deleted.