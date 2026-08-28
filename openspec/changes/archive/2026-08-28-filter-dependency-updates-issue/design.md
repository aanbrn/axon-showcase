# Filter the dependency-updates issue to actionable sections — Design

## Context

See proposal.md — Why. The `dependencyUpdates` report (`build/dependencyUpdates/report.txt`) has four sections:
"using the latest milestone version" (current deps — noise), "have newer versions" (stable, actionable), "have
later milestone versions" (non-stable candidates + build-environment noise), and "Gradle CURRENT updates" (wrapper).
The `/dependency-updates` command already documents that only "have newer versions" and the Gradle section are
actionable. The workflow currently posts the whole file.

## Goals / Non-Goals

**Goals:**
- The issue body contains only the actionable sections (stable updates + Gradle wrapper status), or an explicit
  "no stable updates" line.
- Keep the issue title, in-place update, and owner notification unchanged.

**Non-Goals:**
- Changing the `dependencyUpdates` task or report generation — filtering happens in the workflow's issue step.
- Adding new sections or reformatting beyond extracting the existing actionable ones.

## Decisions

### D1: Extract the actionable sections with `awk` between report section headers

The report uses consistent section headers. The issue step extracts the block between "The following dependencies
have newer versions:" and the next section header (when present), plus the "Gradle CURRENT updates:" block. If the
"have newer versions" section is absent, the body is "No stable catalog updates available" (plus the Gradle
section). This is a pure text extraction — no Gradle task change, no extra dependencies on the runner.

### D2: Keep the notification and in-place-update behavior

The `cc @<owner>` body prefix and the update-in-place logic (stable title, `gh issue edit`) are unchanged, so the
owner still receives the email and at most one issue exists.

## Risks / Trade-offs

- **Filtering hides context** → the removed sections are non-actionable by the repo's own documented policy
  (`/dependency-updates`); the actionable signal is preserved, and the report file itself remains available in the
  Actions run.
- **Section header drift in a future ben-manes release** → the extraction keys on the stable report headers; a
  format change would surface as an empty/odd issue body, caught by the weekly run and the notification.