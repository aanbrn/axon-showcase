# Tasks

## 1. Add the probe to the build gate

- [x] 1.1 Add a probe step to `.github/workflows/ci.yml`'s `build` job, after the quality-gate step, with
      `if: github.event_name == 'pull_request'` and `shell: bash`. The step fetches the base with
      `git fetch --no-tags --depth=1 origin "${{ github.base_ref }}"`, exits 0 when the two-dot changed-file check
      `git diff --name-only FETCH_HEAD HEAD -- '.opencode/opencode.json*' .github/workflows/ci.yml` lists nothing,
      installs the action's consumer with `curl -fsSL https://opencode.ai/install | bash` (adding `$HOME/.opencode/bin`
      to PATH), and runs `opencode debug config`, failing when its exit status is non-zero. Verify with
      `./gradlew workflowLint`.

## 2. Capture the behavior in the spec

- [x] 2.1 Write the delta at
      `openspec/changes/probe-opencode-config-read-path/specs/showcase/quality/merge-governance/spec.md`: one `MODIFIED`
      requirement carrying the main spec's `Pull requests run the fast quality gate` block verbatim (all four existing
      scenarios, in order, byte-identical header), with the OpenCode-config verification added to the description, the
      OpenCode case added to the trigger scenario's THEN and to the failure scenario's WHEN, and the new
      `An OpenCode config the action's consumer cannot load fails the fast gate` scenario. Verify with
      `openspec validate --changes`.

## 3. Docs the change owns

- [x] 3.1 Update `AGENTS.md`: add the probe to the PR-gate enumeration in the Continuous Integration section, qualify
      the `main`-gate sentence that says it runs the same OpenSpec validation and config probe as the pull-request path
      (the OpenCode probe is PR-scoped), and correct the V1-shape bullet's "no in-repo gate loads the config with the
      action's v1 binary" absence claim: a config-changing pull request now probes the load, while a behavioural change
      (a new grant, model, or server) still needs a dispatch.
- [x] 3.2 Update `README.md`'s Continuous Integration section to name the probe.

## 4. Verification

- [x] 4.1 Known-good / known-bad controls for the probe's load, run before the manual review. Install the V1 binary with
      the action's line into a temporary `HOME`, assert its reported version matches the tag `anomalyco/opencode`'s
      `releases/latest` resolves to, then run it by absolute path with that same `HOME` (so it does not read the host's
      global config) against (a) the repository's `.opencode/opencode.json` from the repository root, expecting exit 0,
      and (b) a scratch project whose config carries a V2 `permissions` array, expecting a non-zero exit. Done: the V1
      binary installed at 1.18.33, matching `releases/latest`; (a) exit 0; (b) exit 1, the output naming the unsupported
      V2 `permissions` setting.
- [x] 4.2 Run the implementation `review-quick` loop over the diff against this change's planning artifacts; fix its
      findings and re-run until it reports nothing new.
- [x] 4.3 Run the per-unit `lesson-capture` over this change and apply its durable proposals. Done: three additions (a
      workflow two-dot/gate-includes-its-own-definition gotcha; an exit-status-vs-message clause merged into the
      config-read-path bullet; an installed-binary clause merged into the wrong-resolution-context sub-bullet) and one
      docs correction (the `openspec`-grant sentence), 0 retirements.
- [x] 4.4 Run `./gradlew spotlessApply` after the last edit to a Spotless-owned file and confirm `spotlessCheck` passes;
      run the manual 120-character check over every changed file a formatter does not own — `.github/workflows/ci.yml`
      and the change dir's `.openspec.yaml`.
- [x] 4.5 Request the user's manual review pass — the step before committing.
- [x] 4.6 At the merge stage, after the approval commits and pushes the branch, prove the CI gate end-to-end and record
      each run's step output: on the pushed head the step runs and passes — this change edits
      `.github/workflows/ci.yml`, so the gate includes it; after pushing a temporary V2 `permissions` key to
      `.opencode/opencode.json` the step runs and fails; after reverting it the step runs and passes again. This
      exercises the run-and-pass and run-and-fail paths end-to-end, beyond the local controls in 4.1. Done: run
      36518332085 the step ran and passed; run 36518497562 with the temporary key failed
      (`V2 permissions are not supported by OpenCode V1`); run 36518633126 passed after the revert.
