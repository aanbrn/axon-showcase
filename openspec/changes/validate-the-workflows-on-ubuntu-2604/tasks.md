# Tasks

## 1. The runner-image input

- [x] 1.1 `.github/workflows/deployment-smoke.yml` — add a `workflow_dispatch` input `runner` (a string, defaulting to
      `ubuntu-latest`), set the job's `runs-on` to it (`${{ inputs.runner || 'ubuntu-latest' }}`), and report the
      resolved OS version in the run. Note in the workflow why the fallback is mandatory: on the schedule the input is
      empty, so a bare `inputs.runner` would fail the job. Verify with `./gradlew workflowLint` (actionlint) green and
      by reading the workflow — the scheduled trigger still resolves to `ubuntu-latest`, and a dispatch may name
      `ubuntu-26.04`. — verified: `workflowLint` passes; the input carries `default: ubuntu-latest`, the job keeps the
      label as the fallback, and the first step prints `runner.os`, the requested label, and `/etc/os-release`'s
      `NAME`/`VERSION`.
- [x] 1.2 `AGENTS.md` — extend the CI note: the workflows track `ubuntu-latest` deliberately (a pin owes the
      `merge-governance` deltas and a bump process), and the smoke's `runner` input dispatches the job onto a newer
      image to validate one before the label moves (`actions/runner-images#14748` covers the 2026-10-19 → 2026-11-19
      rollout). Remove the implemented idea from `docs/ideas.md` (the "Pin or validate the CI runners" entry). Verify by
      reading the paragraph, that the reference resolves, and that the idea is gone — while the unrelated entries stay.
      — verified: the CI note carries the rationale and the dispatch recipe; the idea is removed (only its own 10
      lines), with its now-empty dated heading.

## 2. Validation and close-out

- [ ] 2.1 Prove the default is unchanged: read the workflow's resolved `runs-on` for the scheduled path (no input), and
      confirm by dispatching the smoke **without** the input — the run must execute on `ubuntu-latest` exactly as
      before. Record the run URL. (A dispatch of the workflow as it stands on this branch, before the change lands on
      the default branch, is the only way to run it; `--ref` the branch.)
- [ ] 2.2 Prove the new image works: dispatch the smoke with `runner=ubuntu-26.04` and confirm it completes green — the
      in-repo evidence that keeping `ubuntu-latest` is safe for the repo's heaviest job. Record the run URL; if it
      fails, report the failure and the attributable step rather than proceeding (this is the check the whole change
      exists for).
- [x] 2.3 `./gradlew spotlessApply`, then `./gradlew check -PskipITs -Pcoverage.gate.enabled=false`; confirm green.
      Re-run `spotlessApply` after the last edit to a Spotless-owned file (a task tick included). — verified: `check`
      BUILD SUCCESSFUL and `spotlessCheck`/`verifyCapturedMarkers` green; `workflowLint` green.
- [x] 2.4 Run the `lesson-capture` subagent over the diff, review findings, and change dir; apply its durable
      `AGENTS.md` proposals and record the applied net `AGENTS.md` delta on this task. — applied: one merge into the "A
      CLI's `--help` and its docs are not its contract" bullet — "reproduce the _condition_, not the message": a tool
      error names only what it rejected in that invocation, so a limitation derived from it is about the input, not the
      tool (the `actionlint` case: it rejects a bare `inputs.runner` only when the input is **undeclared**, so the
      fallback the message seemed to mandate was a runtime need). The rule **grew the bullet by +8 `AGENTS.md` lines,
      with no offsetting trim** (from that hunk: `+25/−17`); the capture's proposed trim was not applied. This unit's
      own `ubuntu-latest` doc note is a separate hunk (`+10/−0`); the file's total is `+35/−17`. Rejected as already
      covered: the step-outran-its-purpose case (the control-perturbs-the-surface bullet) and the three-artifact sweep
      (the doc-consistency-sweep bullet). The capture also flagged a **future retirement**: when the rollout completes
      (2026-11-19), the `ubuntu-latest` note's validation recipe becomes stale and should be replaced with the outcome.
