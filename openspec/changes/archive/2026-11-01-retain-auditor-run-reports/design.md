# Design

## Context

See `proposal.md` — Why for the motivation. The constraints that shape the approach:

- The workflow is one agent step (`anomalyco/opencode/github`) whose only durable output is the combined dated report,
  committed from inside the run; the action opens the pull request from the commits the run leaves.
- The run is unattended: a permission prompt it cannot answer hangs or ends it, so a write outside the workspace is a
  risk rather than a convenience.
- The job holds `id-token: write` (the action authenticates by OIDC), `contents: write`, and `pull-requests: write`;
  triggers, schedule, and permissions are out of scope.
- The Spotless markdown target covers `docs/**/*.md`, so a report kept under `docs/` would be reformatted by the
  prompt's own `./gradlew spotlessApply` before it is published.
- `actions/upload-artifact` excludes hidden files and hidden directories unless `include-hidden-files: true`, and its
  `if-no-files-found` defaults to `warn` — a plan that ignores either publishes nothing and still reads green.
- Verified while drafting this change (`57f416a0`; the later refresh changed none of the files these facts rest on):
  both 2026-10-31 runs invoked `agents-auditor` as a subagent (`mode: "subagent"` in the run logs), no run of the
  workflow has any artifact (`gh api .../actions/runs/<id>/artifacts` → `total_count: 0` for both), and the subagent's
  own text never reaches the log — only the combined report's `Write` payload does.

## Goals / Non-Goals

**Goals:**

- One retained copy of each auditor's report per run, carrying that auditor's name and the input revision.
- Retention on a clean run too, since a clean verdict is the case a later miss has to be explained against.
- The repository's committed artifacts and the clean-run rule unchanged.

**Non-Goals:**

- Changing an auditor's definition, method, or the shared report contract.
- Publishing the reports in the pull request body or as committed files.
- Changing the workflow's triggers, permissions, schedule, or model pin.

## Decisions

1. **The retained reports live in `build/audit-reports/`.**
   - The root `.gitignore` already ignores `build/`, so neither `git add` nor `git add -A` can sweep them into the run's
     commit, and no new ignore rule is needed.
   - `build/` is outside the Spotless markdown target, so the prompt's `spotlessApply` cannot rewrite an auditor's text
     into a formatter's rendering of it.
   - It is not a hidden path, so `actions/upload-artifact`'s default file selection includes it.
   - Alternatives: `docs/audits/.raw/` fails on two counts above — hidden (excluded from the artifact unless
     `include-hidden-files: true`) and inside the markdown target; `$RUNNER_TEMP` sits outside the workspace, so
     producing a file there through the agent's normal write path raises an `external_directory` prompt an unattended
     run cannot answer (a shell write would not, but the write tool is the path this prompt implies); a committed file
     would break "a clean run leaves the repository unchanged".
2. **A completeness step guards the set, and the upload publishes whatever exists.**
   - `if-no-files-found` on the upload cannot carry the guarantee: it answers only "no file at all", while the
     requirement is one report **per auditor**. One report present and two missing would upload cleanly and read green.
   - So a step after the agent step runs `scripts/audit-report-completeness.sh build/audit-reports ${{ github.sha }}`,
     which asserts, per auditor, a non-empty report whose first line names that auditor and which carries the run's
     input revision, exiting non-zero and naming what is wrong; the run is red, which is the point — the retained set is
     this change's own outcome, and the workflow is not a required check, so a red run blocks no pull request.
   - The stamp is checked, not trusted: non-emptiness alone would pass a report the agent wrote without the revision,
     losing the one machine-checkable link between a report and the tree it audited — which is why the check takes the
     SHA, and why decision 3 forbids the agent from deriving it itself.
   - The check is a committed script rather than an inline `run:` block, so its own test —
     `scripts/test-audit-report-completeness.sh`, wired into `check` as `testAuditReportCompleteness` — carries the
     known-bad cases (a missing report, an empty one, a missing stamp, an unnamed first line, an absent directory)
     instead of a task note.
   - The upload then runs `if: always()` with the default `warn`, because the completeness step carries the failure;
     `warn` there still publishes a partial set, which is what a failed run needs for diagnosis.
   - One artifact holding every auditor's report, named for the run (`audit-reports-<run_id>`); one upload serves the
     three reports and keeps the step list flat.
3. **The input revision is the workflow's `${{ github.sha }}`, interpolated into the prompt.**
   - The agent must not sample `git rev-parse HEAD` itself: it commits the dated report first, so its own `HEAD` is the
     report commit rather than the tree it audited.
   - A dispatch can audit another ref, so the run's SHA — not the branch name — is the revision the report describes.
4. **The reports are retained verbatim.**
   - The combined dated report is a condensation by design, so a paraphrase would re-create the gap this change closes.
   - The prompt says so explicitly, and the shared report contract already bounds each item, so no second budget is
     invented.
5. **Only the reports are retained, not the whole subagent transcript.**
   - A transcript adds cost and a credential-exposure surface for no diagnostic gain the reports do not already give.

## Claims table

Rows describing the state before this change are read at `0734891b`, the `origin/main` commit this planning was written
against — `git show <sha>:<path>` or `git grep <pattern> <sha>` — never from the working tree, since one row records the
absence of the step this change adds.

- Both 2026-10-31 runs invoked the auditor as a subagent — `gh run view <id> --log` (scheduled `41238953011`, dispatch
  `41214442301`), grepping `agent: "agents-auditor"`: a `mode: "subagent"` stream and a session object in each
  (scheduled `ses_e6c5a0c60…`, dispatch `ses_e6d7192a0…`).
- Neither run carries any artifact — `gh api repos/aanbrn/axon-showcase/actions/runs/<id>/artifacts --jq .total_count`:
  `0` for both.
- The auditor's own text never reaches the run log — grepping both logs for `Instruction-budget ledger` and `accreted`
  matches only the combined report's `Write` payload, never a subagent message.
- The scheduled report's verdict is `0 findings (0 merge, 0 removal, 0 route)` — `docs/audits/2026-10-31.md:9`; the
  dispatch run's structural finding exists only on the closed PR #781's branch.
- The workflow has no artifact step — `grep -rn 'upload-artifact' .github/` at `0734891b`: no match.
- The job's permissions are `id-token: write`, `contents: write`, `pull-requests: write` — `.github/workflows/audit.yml`
  lines 8–11.
- The Spotless markdown target covers `docs/**/*.md` and excludes nothing under `docs/audits/` — `build.gradle.kts`'s
  `markdownTarget` / `markdownTargetExclude`; `build/` appears in neither list.
- `actions/upload-artifact`'s latest release is `v7.0.2` and it excludes hidden paths by default —
  `gh api repos/actions/upload-artifact/releases/latest --jq .tag_name`, and its `action.yml` `include-hidden-files`
  description.
- The requirement being modified carries five scenarios, all retained in the delta —
  `sed -n '443,484p' openspec/specs/showcase/quality/merge-governance/spec.md` at `0734891b`, and
  `openspec validate retain-auditor-run-reports` passes with the delta present.

## Risks / Trade-offs

- **Artifact retention expires (the action's default).** → Accepted: the dated report stays the durable record; the
  artifact answers "what did the auditor say in this run" while the run is recent.
- **A Gradle `clean` between the write and the upload would delete the reports.** → The workflow runs no `clean` and the
  agent runs only `spotlessApply`; the completeness step fails the run, so a lost directory is red rather than silent.
- **The agent may leave the set incomplete.** → The completeness step names the missing auditor and fails the run; a
  partial set is still uploaded, so the failure is diagnosable rather than empty.
- **A partial set could pass unnoticed if the check itself regressed.** → The check is covered by
  `testAuditReportCompleteness`, a committed test wired into `check` that fails on a missing report, an empty one, and
  an absent directory.
- **A new workflow step is a new failure mode.** → It runs after the deliverable is committed, so a failure there fails
  the run without losing the report pull request.

## Migration Plan

None — no repository state to migrate. The first run after the change produces the first artifact, and reverting the
workflow edit is the rollback.
