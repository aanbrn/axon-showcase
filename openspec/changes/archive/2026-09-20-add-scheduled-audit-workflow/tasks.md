# Tasks — add a scheduled audit workflow

## 1. The workflow

- [x] 1.1 Add `.github/workflows/audit.yml`: `schedule` (`0 5 * * 1`, after the update checks) plus `workflow_dispatch`,
      `permissions: id-token: write` (the action's OIDC default), `contents: write` and `pull-requests: write`, a
      checkout (`persist-credentials: false`), and the `anomalyco/opencode/github@latest` step with the
      `OPENCODE_API_KEY` secret, a pro model, and a `prompt` that runs the three audits and reports their findings.
- [x] 1.2 Write the prompt as the deliverable: run `agents-auditor`, `specs-auditor`, and `architecture-auditor` in
      sequence; report in the shared subagent report contract; write the findings to a dated file under `docs/audits/`
      and commit it (the action opens the PR from the commit), and if the audits are clean, print that and commit
      nothing.
- [x] 1.3 Confirm the workflow's shape against the action's documented schedule example (the `prompt` input is required;
      output is a PR, not an issue) and against the sibling workflows' pinning (actions versions, secret name).

## 2. Spec capture

- [x] 2.1 Write the `ADDED` requirement into
      `openspec/changes/add-scheduled-audit-workflow/specs/showcase/quality/agent-skills/spec.md` (scheduled audits run
      unattended, batched, reporting into one reviewable artifact).
- [x] 2.2 Write the `ADDED` requirement for `showcase/quality/merge-governance` (the scheduled audit workflow; it stays
      never-a-merge-gate) — `ADDED`, not `MODIFIED`, because the capability models each scheduled workflow as its own
      requirement.
- [ ] 2.3 Record a task for each capability's `## Purpose` refresh in the archive commit (a delta cannot carry one).

## 3. The parked riders

- [x] 3.1 Remove the implemented riders from `docs/ideas.md`: the three scheduled-auditor variants and the
      consolidation-cadence entry (with its measured finding folded into the workflow's design).
- [x] 3.2 Re-park the upstream-reference watcher with the corrected premise: its mechanism is a `gh`-based issue
      workflow like the four update checks, not an OpenCode schedule run (the schedule path outputs to logs/PR, not
      issues).

## 4. Verification

- [x] 4.1 `actionlint` (`./gradlew workflowLint`) green on the new workflow, and `spotlessCheck` green.
- [x] 4.2 `openspec validate --all` with both deltas accepted.
- [ ] 4.3 **Dispatch the workflow** (`gh workflow run audit.yml`) — the only way to exercise the schedule path, since no
      CI job runs it — and confirm it runs green and produces the expected artifact (a PR, or a clean-run message).
      Record the outcome; if the run cannot be dispatched before merge (it requires the file on the default branch),
      state that as the deferred verification rather than claiming it.
- [ ] 4.4 Confirm the run did not need `issues: write` (the schedule path does not fire issues) and that the permissions
      are the minimum the mechanism requires.
