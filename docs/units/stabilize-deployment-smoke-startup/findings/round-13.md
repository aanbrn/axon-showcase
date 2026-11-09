- [test-control] tasks.md:34-38 — the task 2.2 note attributes the first draft's `curl | grep -q` failure to the
  runner's `-o pipefail`, but the step declares no `shell:` and so runs `bash -e` without pipefail; the harness ran
  under `set -euo pipefail`, so the defect was the harness's shell rather than the step's.
- [missing-task] tasks.md:98-104 — the deep-review routing re-derivation has run and is recorded in
  `docs/units/stabilize-deployment-smoke-startup/deep-review.md`, but task 4.5 is unticked, so the plan and the record
  disagree and the archive guard would refuse the change.
