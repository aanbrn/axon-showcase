# Run the Snyk dependency security scan on GitHub

## Why

The `dependencySecurityCheck` (Snyk) scan exists as a Gradle task but is never run automatically. The
`merge-governance` and `e2e-in-ci` changes deferred it as a credentialed follow-up: nothing catches newly introduced
vulnerable dependencies or policy drift between human-triggered runs. The root `.snyk` policy now makes the scan
green locally (21 version-pinned ignores, quarterly expiry), so the gate is ready to run on a schedule without
failing.

## What Changes

- Add a new `.github/workflows/snyk.yml` workflow (separate from `ci.yml` and `e2e.yml`):
  - **Triggers**: `workflow_dispatch` (manual) and a scheduled cron — not `pull_request` and not `push` to `main`,
    consistent with the heavy/credentialed gates being observational rather than merge blockers.
  - **Job**: single `snyk` job on `ubuntu-latest`; installs the Snyk CLI via `snyk/actions/setup`, then runs
    `./gradlew dependencySecurityCheck` (which invokes `snyk test --all-sub-projects --policy-path=.snyk`).
  - **Credentials**: authenticates with the `SNYK_TOKEN` repository secret (set by the repo owner); no other secrets.
  - **Cache**: `gradle/actions/setup-gradle` restores the Gradle User Home, never the workspace `build/` directories.
- No changes to the merge gate: the Snyk scan is observational (schedule/manual only), never a required check.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `showcase/quality/merge-governance`: adds the requirement that the dependency security scan runs on a schedule and
  on demand, and that it is not part of the merge gate.

## Impact

- **New files**: `.github/workflows/snyk.yml`.
- **GitHub config**: new `SNYK_TOKEN` repository secret must be set before the workflow can authenticate; no ruleset
  changes (the scan is observational, never a required check).
- **Build/test**: no Gradle task changes — the workflow invokes the existing `dependencySecurityCheck` task.
- **Secrets**: `SNYK_TOKEN` (the only secret); the Snyk CLI reads it from the environment.
- **Rate limit**: the policy-suppressed scan reports zero identified vulnerabilities, so it consumes no Snyk test
  quota per the "what counts as a test" policy (tests count only for manifests with identified findings).