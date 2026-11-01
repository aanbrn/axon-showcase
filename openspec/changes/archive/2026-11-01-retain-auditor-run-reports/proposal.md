## Why

The `audit` workflow retains only the combined report: each auditor's own answer lives inside the run and is discarded,
so a verdict cannot be checked against what that auditor actually said. On 2026-10-31 the scheduled run reported 0
findings on `AGENTS.md` while the same week's dispatch run reported a structural finding on the same material, and
neither run left the auditor's text behind — the difference cannot be attributed to detection or to report assembly.

## What Changes

- `.github/workflows/audit.yml` — the prompt requires each auditor's own report, verbatim, at
  `build/audit-reports/<auditor>.md`, stamped with the auditor's name and the workflow run's input SHA, on every run
  including a clean one; those files are never committed.
- `.github/workflows/audit.yml` — a step after the agent step publishes `build/audit-reports/` as a workflow artifact.
- `openspec/specs/showcase/quality/merge-governance/spec.md` gains a `MODIFIED` delta for "Repository audits run on a
  schedule and on demand": the run retains each auditor's report as a workflow artifact; one scenario is added and the
  clean-run scenario's body is narrowed to repository artifacts.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance` — the audit workflow retains each auditor's report as a workflow artifact of the
  run.

## Impact

- Workflow: `.github/workflows/audit.yml` — the prompt, a completeness step that runs
  `scripts/audit-report-completeness.sh`, and the upload step; triggers, permissions, and the schedule are unchanged.
- Build: `build.gradle.kts` gains one verification task, `testAuditReportCompleteness`, wired into `check`.
- Scripts: two new files under `scripts/` — the completeness check the audit job runs, and its test.
- Repository: no new tracked data path. `build/audit-reports/` sits under the root `.gitignore`'s existing `build/` rule
  and outside the Spotless markdown target, and a clean run still commits nothing and opens no pull request.
- Specs: a `MODIFIED` delta on `showcase/quality/merge-governance`; the main spec changes at archive.
- No application code, API, dependency, image, chart, or deployment change.
