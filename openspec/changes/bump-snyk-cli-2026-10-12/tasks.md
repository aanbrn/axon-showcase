# Tasks

## 1. The pin

- [x] 1.1 Re-resolve the Snyk CLI's latest release (`gh api repos/snyk/cli/releases/latest`) and set `snyk-version` in
      `.github/workflows/dependency-security.yml` to it. (Resolved `v1.1308.0`, published 2026-10-08 — the issue's
      target was not stale.)
- [x] 1.2 Run `./gradlew workflowLint` (actionlint) and confirm the YAML lints; run `./gradlew toolingUpdates` and
      confirm the `snyk-cli` row is gone. (Lint clean; the report now reads "No tooling updates available.")

## 2. Verification

- [ ] 2.1 **After the owner's approval pushes the branch** (before the archive commit), dispatch the credentialed
      workflow against it (`gh workflow run dependency-security.yml --ref bump-snyk-cli-2026-10-12`) and watch the run:
      it must install the pinned CLI and complete the scan. A red run attributed to the pin means the tag does not
      install; a red run attributed to a finding means the policy, not the pin.
- [x] 2.2 Run `./gradlew spotlessApply` then `./gradlew spotlessCheck` and `openspec validate --all`; run the manual
      120-character check over the changed set (`git diff --name-only`) — here
      `.github/workflows/dependency-security.yml` and the change dir's `.openspec.yaml`, both formatter-uncovered — and
      confirm clean.
- [x] 2.3 Run a `review-quick` pass over the proposal and the implementation, repeating until clean.

## 3. Capture

- [x] 3.1 Run the `lesson-capture` subagent over the diff, apply its durable proposals, and record the applied net
      `AGENTS.md` delta on this task. Applied: **net +2 rendered lines** — the unanswered-approval sentence in the
      auto-review bullet now says the request **outlives its unit**: when a reply moves the thread to the next unit,
      re-ask before starting it, because the new branch forked from `main` excludes the unmerged one (so its edits read
      pre-change there — expected, not a revert). The capture judged the `formatting` repeat, the push-conditioned
      dispatch, the stale-target re-resolve and the pin-location claim as **compliance misses of existing rules**, not
      gaps — nothing re-captured. No retirements proposed.
- [x] 3.2 Ask the owner for the manual review pass, after 2.3 is clean and 3.1 is done.

## Workflow follow-up

- Archive the change after the owner approves; its archive commit rides this PR.
