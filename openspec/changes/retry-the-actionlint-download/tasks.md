# Tasks

## 1. Retry the install

- [x] 1.1 In `.github/workflows/ci.yml`, wrap the "Install actionlint" step's work in a bounded retry — up to three
      attempts — so a transient failure of either the installer-script fetch or the script's inner release-asset
      download is retried before the step fails. Keep everything else identical: the same script URL, the same `latest`
      argument, `mkdir -p "$RUNNER_TEMP/actionlint"`, and the `$GITHUB_PATH` export (which must run only on success, so
      a failed attempt does not add a path to a binary that was not installed). Verify: read the step as shell and
      confirm the loop's structure — attempt, on failure retry after a short pause, and exit non-zero once the attempts
      are exhausted with the error visible.
- [x] 1.2 Prove the loop's behaviour without CI: run the step's shell locally with the installer command replaced by a
      command that fails a fixed number of times and then succeeds, and confirm the step succeeds; then with one that
      always fails, and confirm the step exits non-zero after the bounded attempts. Do this in the approved scratch
      directory, not against the workflow's real URL. Verify: both runs' exit codes and output, recorded.
- [x] 1.3 Confirm the step still succeeds against the real installer on this machine (the healthy path is unchanged).
      Verify: run the step's shell verbatim with the real URL in a scratch directory and confirm actionlint installs and
      `"$RUNNER_TEMP/actionlint/actionlint" --version` reports a version.

## 2. Verification

- [x] 2.1 Run `./gradlew workflowLint` (actionlint over the workflows — it is the only linter the gate runs) and confirm
      the edited step lints clean — the retry's shell is the thing being linted. Verify: the task succeeds.
- [x] 2.2 Run the Docker-free gate `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` and confirm green. Verify:
      the task completes successfully.
- [x] 2.3 Run `openspec validate --changes` and confirm the delta validates with the requirement's four existing
      scenarios preserved in main-spec order plus the new one. Verify: exit 0, `1 passed`, the count read from the
      delta.
- [x] 2.4 Run `./gradlew spotlessApply` after the final edit to a Spotless-owned file, then `./gradlew spotlessCheck`.
      `ci.yml` is YAML, which the Spotless markdown/JSON targets do not own — check it against the manual 120-character
      rule instead. Verify: the Gradle task succeeds and no over-120 line was added.
- [x] 2.5 Read the step once more as content, not as a green run: confirm the retry cannot loop forever, that a failed
      attempt does not export a path to a missing binary, and that the exhausted case fails rather than passing. Record
      the reading in the report, together with the two other unretried fetches this change deliberately leaves out of
      scope (`npm install --global @fission-ai/openspec` and the config probe's
      `curl -fsSL https://opencode.ai/install | bash`) and the upstream gap (the installer script honouring no retry
      flag), so the scope decision and the gap reference are recorded rather than reconstructed at review.
- [x] 2.6 Run the `lesson-capture` subagent over the diff, apply the durable proposals the main agent judges worth
      keeping, and record the applied net `AGENTS.md` delta on this task. Verify: the verdict is recorded and any
      applied edit is visible in `git diff AGENTS.md`.
