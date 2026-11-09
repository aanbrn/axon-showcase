- [contradiction] docs/ideas.md:81 — "only the pull_request/push CI gates kept running" is false: opencode.yml and
  release.yml are reported active as well.
- [premise] docs/units/pause-scheduled-jobs-2026-11-09/unit.md:17-19 — the no-delta decision rests on "the workflow
  definitions are unchanged", while the specs state an outcome (ten requirements requiring a scheduled run).
- [docs-residue] AGENTS.md:1490,1506,1531 — "The scheduled variant runs unattended in the `audit` workflow" asserts a
  run while `audit.yml` is disabled.
- [claim-missing] pr-body-draft:15 — "the setting takes effect once this lands on the default branch" has no
  claims-table row.
- [wording] pr-body-draft:23 — the `./gradlew check` test-plan box is unticked although the hand-over records that run
  green.
