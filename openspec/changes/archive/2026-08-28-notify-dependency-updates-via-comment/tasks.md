# Tasks

## 1. Post a notifying comment on the issue

- [x] 1.1 Update the issue step in `.github/workflows/dependency-updates.yml` so that, after updating the issue body,
      it deletes any previous `github-actions[bot]` comment on the issue and posts a new comment containing
      `cc @<owner>` and the filtered report summary — but only when there are actionable updates (stable dependency
      updates or a newer Gradle wrapper), so no notification is sent on a no-op run
- [x] 1.2 Verify the YAML validates (YAML 1.2) and that the step still updates the issue in place and uses only the
      `GITHUB_TOKEN`

## 2. Verify the workflow

- [x] 2.1 Run the workflow twice via `workflow_dispatch` and confirm that a run with actionable updates posts a new
      comment with the mention (and notifies the owner) while a run with no updates does not post a comment, and that
      only one bot comment remains on the issue after an update run
- [x] 2.2 Run `openspec validate notify-dependency-updates-via-comment` and confirm the change is valid with all
      artifacts consistent

## 3. Docs refresh

- [x] 3.1 Update `AGENTS.md` and `README.md` only if the workflow description changes meaningfully (notification via
      a new comment), and verify the edited files respect the 120-character line limit