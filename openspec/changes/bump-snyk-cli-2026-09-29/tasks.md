# Tasks

## 1. Apply the bump

- [x] 1.1 Bump the Snyk CLI pin in `.github/workflows/dependency-security.yml` (`snyk-version`) from `v1.1307.3` to
      `v1.1307.4`. Verify by reading the step.

## 2. Verification

- [x] 2.1 Confirm the pinned tag exists (`gh api repos/snyk/cli/releases/tags/v1.1307.4`), run `./gradlew workflowLint`
      (the YAML lints), and run `./gradlew toolingUpdates` (the check that triggered the bump now reports no tooling
      updates). Record the results. Done: `v1.1307.4` published 2026-09-23; `workflowLint` green; `toolingUpdates`
      reports "No tooling updates available.".
- [x] 2.2 Run the implementation `review-quick` loop over the diff; fix its findings and re-run until it reports nothing
      new.
- [x] 2.3 Run the per-unit `lesson-capture` over this change and apply its durable proposals, recording the applied net
      `AGENTS.md` delta on this change's record. Done: the workflow-dispatch route (`--ref <branch>`) merged into the CI
      and verification bullets, +2 lines.
- [x] 2.4 Run `./gradlew spotlessApply` after the last edit and confirm `spotlessCheck` passes; run the manual
      120-character check over the lines this change introduces in `.github/workflows/dependency-security.yml` and the
      change dir's `.openspec.yaml`.
- [x] 2.5 Request the user's manual review pass — the step before committing.
- [ ] 2.6 After the approval pushes the branch (before the archive commit), dispatch
      `gh workflow run dependency-security.yml --ref <branch>` and confirm the pinned Snyk CLI installs and the scan
      runs — the credentialed run is the first real execution that installs the pinned version, which neither
      `workflowLint` nor a local `dependencySecurityCheck` (it uses the developer's own `snyk`) can prove. Running it
      against the pushed branch keeps the task from archiving unticked.
