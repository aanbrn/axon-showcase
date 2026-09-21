# Tasks — notify the owner from the audit report

## 1. The prompt

- [x] 1.1 Extend `.github/workflows/audit.yml`'s prompt so the agent's final response (which the action uses as the pull
      request body) **begins with the owner mention** — `cc @<repository-owner> — the audit report is ready for review`
      — mirroring the `cc @<owner>` idiom the four update-check workflows use on their issues.
- [x] 1.2 State in the prompt why: the report's value is that the owner knows it exists, and the body is built from this
      response, so the mention belongs here rather than in a workflow step (which would need to discover the PR the
      action opens).

## 2. Spec capture

- [x] 2.1 Write the `MODIFIED` block into
      `openspec/changes/notify-owner-from-the-audit-report/specs/showcase/quality/merge-governance/spec.md`, copying the
      whole "Repository audits run on a schedule and on demand" requirement (all four scenarios) and adding the
      notification — per the repo rule that a `MODIFIED` block replaces the requirement and must carry every scenario.
- [x] 2.2 Checked: the capability's `## Purpose` describes the merge governance surface, not this workflow's output, so
      no refresh is owed.

## 3. Docs

- [x] 3.1 Refresh every description of the audit workflow's output — `AGENTS.md`'s sentence and `README.md`'s **two**
      copies (the CI paragraph and the "audits also run unattended" bullet) — so each states the owner notification.

## 4. Verification

- [x] 4.1 `./gradlew workflowLint` and `spotlessCheck` green; no line over 120.
- [x] 4.2 `openspec validate --all` with the delta accepted (the `MODIFIED` block valid and scenario-complete).
- [ ] 4.3 **Deferred past the merge, by necessity**: `workflow_dispatch` is exposed only once the file is on the default
      branch, so dispatching `audit.yml` and confirming the resulting PR body carries the owner mention happens after
      this PR merges. It is the only real verification, since no local session or CI job produces that body.
