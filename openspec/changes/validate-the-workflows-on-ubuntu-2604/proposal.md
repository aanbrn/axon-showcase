# Proposal: Validate the workflows on Ubuntu 26.04 before the label migrates

## Why

GitHub is rolling `ubuntu-latest` onto Ubuntu 26.04 between 2026-10-19 and 2026-11-19 (`actions/runner-images#14748`),
and all 12 jobs across the 11 workflow files run on that label. The migration's own "possible impact" is software the
image carries — system libraries, package versions, prebuilt binaries — and this repo's most image-dependent job is the
deployment smoke, which drives `kind`, `kubectl`, `docker`, and `sudo tee /etc/hosts`. Keeping `ubuntu-latest` is the
right default (pinning would owe nine `merge-governance` deltas and a bump-on-a-schedule process nothing tracks), but
the repo has no evidence either way about the new image. The label's meaning also changes underneath it mid-rollout, so
a red run could be the image rather than the change under test.

## What Changes

- `.github/workflows/deployment-smoke.yml` — gains a `workflow_dispatch` input selecting the runner image, so the
  nightly default stays `ubuntu-latest` while a dispatch can run the whole smoke on `ubuntu-26.04`; the input is passed
  to `runs-on` and named in the run, so a failure during the migration window is attributable.
- `AGENTS.md` — the CI note records that the workflows track `ubuntu-latest` deliberately, and how to validate a
  dispatch against a newer image before the label moves.
- `docs/ideas.md` — remove the implemented "Pin or validate the CI runners" idea.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — `skip_specs: true`. No requirement names a runner image's version: `merge-governance` requires that each job
"SHALL run on `ubuntu-latest`", which this change keeps, and the dispatch input does not change any job's outcome.

## Impact

- **CI**: one workflow gains an optional input; every job's default runner is unchanged.
- **Validation**: the deployment smoke (and any other workflow, once it opts in) can be run against Ubuntu 26.04 on
  demand, before the label moves — the check that the decision to keep `ubuntu-latest` rests on.
- **Deployment**: none.
