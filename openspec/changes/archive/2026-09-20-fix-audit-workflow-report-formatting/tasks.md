# Tasks — fix the scheduled audit workflow's unformatted report

## 1. The fix

- [x] 1.1 Add `actions/setup-java@v6` (Temurin 21) and `gradle/actions/setup-gradle@v6` steps before the action step,
      mirroring the sibling Gradle-running workflows, so the run can invoke Gradle.
- [x] 1.2 Restore the prompt's `./gradlew spotlessApply` instruction (with the reason: CI runs `build` on the audit PR
      and an unformatted report fails it), replacing the earlier "the PR is reviewed rather than merged, so formatting
      is not a gate" rationale, which was wrong.

## 2. Verification

- [ ] 2.1 `./gradlew workflowLint` and `./gradlew spotlessCheck` green; no line over 120 in the workflow.
- [ ] 2.2 The PR's own `build` check passes — the verification the first attempt skipped (it must be checked on the PR,
      not only on the branch and `main`).
- [ ] 2.3 Dispatch `audit.yml` after the merge and confirm its resulting PR's `build` passes (or that it commits nothing
      when the audits are clean), so the fix is verified end to end rather than by inspection.
- [ ] 2.4 Close the superseded PR #321 (open, `build` red — the unformatted report the fix addresses): the next dispatch
      produces a fresh, formatted PR, so #321 is closed rather than reformatted. Its report content is not lost — the
      next run regenerates it — but if its findings are wanted before then, read them from the PR.
