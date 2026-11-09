# Unit: pause-scheduled-jobs-2026-11-09

Workspace:
`unit=pause-scheduled-jobs-2026-11-09; path=/Users/afanasyev/Development/axon-showcase; branch=pause-scheduled-jobs-2026-11-09; writer=ses_e3f76a918ffe0COcr75OQjMoLE`

State: closed

Stop the repository's recurring automation while active work on the project pauses: disable the nine schedule-triggered
workflows in the repository's Actions settings, and stop Dependabot's weekly GitHub Actions version updates by lowering
`open-pull-requests-limit` in `.github/dependabot.yml`. The `pull_request`/`push` CI gates are untouched, and nothing in
the workflow files changes.

## Decisions

- The scheduled workflows are disabled through repository Actions state (`gh workflow disable`) rather than by editing
  their `schedule:` blocks: it leaves no diff to revert on resume, and the owner chose it over the two YAML routes.
- No spec delta is written. The specs state an outcome rather than a file: `merge-governance`'s nine "… run on a
  schedule and on demand" requirements and `agent-skills`' "The on-demand audits are also available on a schedule" each
  claim a scheduled run, and the disablement suspends that outcome while the pause lasts. A delta would have to be
  reverted at resume, so the pause is recorded here and in the pull request as out-of-band enablement state, and the
  resume action in `docs/ideas.md` re-checks those ten requirements before the workflows are re-enabled.
- The per-workflow "on a weekly/nightly schedule" sentences in `README.md` are left as the workflows' declared triggers:
  one note at the head of that block states the pause, so no reader acts on a false premise there. In `AGENTS.md` the CI
  section carries the suspension sentence, the dependabot paragraph states its own suspension, and the three auditor
  bullets say the scheduled variant runs "when it is enabled" rather than asserting a run.
