# Proposal

## Why

`ci.yml`'s `build` job installs actionlint with a single unretried fetch, so a transient failure of GitHub's
release-asset download fails the whole **merge gate**. It happened on `main` at `2011915`: the download returned HTTP
**504** for actionlint, so `tar` exited 2 with `gzip: stdin: not in gzip format`; the same commit passed minutes earlier
and succeeded again once the 504 cleared, and the release asset itself was intact throughout. The failure is invisible
to `workflowLint` (actionlint lints the YAML, not the download) and costs a red `main` plus a manual re-run.

The retry has to wrap the **whole step**, not just the script fetch: the 504 hit the installer script's _inner_ asset
download (`curl -L "${url}" | tar xvz`), which honours no retry flag or environment variable — verified against the
script — so `--retry` on the outer `curl …download-actionlint.bash` would absorb nothing.

## What Changes

- `.github/workflows/ci.yml`: wrap the "Install actionlint" step's work in a bounded retry (a small shell loop), so a
  transient failure of either the installer-script fetch or the asset download is retried before the step fails. Keep
  the step's behaviour otherwise identical: the same script, the same `latest` argument, the same `$GITHUB_PATH` export
  of the installed directory.
- The `showcase/quality/code-quality` spec's workflow-lint requirement gains the CI-side obligation: the lint tool the
  merge gate installs SHALL be installed resiliently, so a transient download failure does not fail the gate.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/code-quality`: "GitHub workflows are linted by the build" gains the CI-install resilience clause —
  the requirement says the gate fails when actionlint is _absent_, which is silent on a _transient_ fetch failure, and
  that is the distinction the fix rests on.

## Impact

- Edited files: `.github/workflows/ci.yml` (the retry loop), and the change's delta spec.
- No behaviour change on a healthy run: the step installs the same actionlint and exports the same path.
- The workflow file is gated by `workflowLint` (actionlint, part of `check`) and by no test — so the retry logic's
  correctness rests on reading it, which the change's tasks make explicit rather than assumed. actionlint may invoke
  `shellcheck` opportunistically when it is on `PATH`, but this repository neither installs nor documents it, so the
  gate is actionlint alone and the loop's exit-code logic is covered only by that reading.
- CI cost: the retry only loops when a failure occurs, so a normal run is unchanged.
- `.github/workflows/ci.yml` is a path-gated trigger for the OpenSpec-config CI probe, so this change exercises that
  step too; the probe is unchanged.
