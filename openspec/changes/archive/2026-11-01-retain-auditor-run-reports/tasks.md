# Tasks — retain-auditor-run-reports

Workspace: unit=retain-auditor-run-reports; path=/Users/afanasyev/Development/axon-showcase-retain-auditor-run-reports;
branch=retain-auditor-run-reports; writer=ses_e695b53b9ffeCh1sn44PGihjod

## 1. The workflow change

- [x] 1.1 `.github/workflows/audit.yml` — the prompt requires each auditor's own report **verbatim** at
      `build/audit-reports/<auditor>.md`, stamped with the auditor's name and the input SHA `${{ github.sha }}`
      interpolated into the prompt, on every run including a clean one; it states that those files are never committed.
- [x] 1.2 `scripts/audit-report-completeness.sh` — the per-auditor check: a non-empty report whose first line names its
      auditor and which carries the run's input revision, exiting non-zero and naming what is wrong; the workflow runs
      it after the agent step, so an incomplete or unstamped set is red.
- [x] 1.3 `scripts/test-audit-report-completeness.sh` and `build.gradle.kts` — the check's committed test covering a
      complete set, a missing report, an empty one, a missing stamp, an unnamed report, and an absent directory,
      registered as `testAuditReportCompleteness` and wired into `check`.
- [x] 1.4 `.github/workflows/audit.yml` — the upload step after the completeness check (`actions/upload-artifact@v7`,
      always running, the default `warn`), uploading `build/audit-reports/` under an artifact name that carries the run
      identifier.

## 2. The delta

- [x] 2.1 Write the `MODIFIED` block for "Repository audits run on a schedule and on demand" from the current block: the
      retention clause names a **workflow artifact** and the verbatim requirement; the "Each auditor's report is
      retained with the run" scenario is added; the clean-run scenario's body is narrowed to repository artifacts; all
      five existing scenarios are retained in their relative order and none is renamed.

## 3. Verification and delivery

- [x] 3.1 `./gradlew workflowLint` and `./gradlew spotlessCheck` pass after the final edit.
- [x] 3.2 `openspec validate --all` passes.
- [x] 3.3 The unit's process-defect record is written — `docs/process-defects/retain-auditor-run-reports.md`, or it
      states that none was observed.
- [x] 3.4 The review gate, per `.opencode/skills/review-gate/SKILL.md`; consult the advisor if a decision has moved
      since the proposal stage. **Routing** — re-derived on the completed implementation: `REQUIRED`
      (`infrastructure-or-deployment`, `.github/workflows/audit.yml`). **Deep review, pass 1** — 1 should-fix (the
      completeness check verified non-emptiness, not the name/revision stamp the delta requires) and 1 nitpick (the
      test's case count); **pass 2** — both `FIXED_AND_VERIFIED`, nothing remains. The earlier `RECOMMENDED` skip
      covered only the proposal stage.
- [x] 3.5 Commit, push, and open the pull request, per `.opencode/skills/ship-change/SKILL.md`. **Done** — pull
      request #797.
- [x] 3.6 Dispatch check on the pushed branch — `gh workflow run audit.yml --ref <branch>` — then, on that run:
  - download the artifact (`gh run download <run-id> -n audit-reports-<run-id>`) and confirm it holds the three reports,
    none empty, each opening with its auditor's name and the run's input SHA;
  - confirm the files are the auditors' own reports rather than the combined report's condensation — the agents-auditor
    file carries the instruction-budget ledger section the dated report summarises;
  - record that the clean-run path is covered by the completeness step's no-findings instruction rather than by a forced
    clean run, since a dispatch cannot make the audits find nothing. **Done** — run `41305432952` (success) published
    one artifact, `audit-reports-41305432952` (8,799 bytes) holding `agents-auditor.md` (13,432 B),
    `architecture-auditor.md` (5,851 B), and `specs-auditor.md` (93 B); each opens with
    `<auditor> — input revision 02d86de…` (the dispatched ref's SHA) and the agents report carries its
    `## Instruction-budget ledger` and extraction sections. The run also opened the audit's own report pull request from
    its `opencode/dispatch-…` branch; closed, since it carries this unmerged branch and reports 0 findings.
- [x] 3.7 Archive and merge, per `.opencode/skills/ship-change/SKILL.md`. **Done** — archived in this pull request;
      the merge follows its `build` check.
