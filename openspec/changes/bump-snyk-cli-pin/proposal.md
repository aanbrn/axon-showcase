# Bump the pinned Snyk CLI to the current release

## Why

`.github/workflows/snyk.yml` pins the Snyk CLI one release behind the published latest: `v1.1307.2` against `v1.1307.3`,
re-resolved from both npm and `snyk/cli`'s latest release at bump time. The check this repository added
(`toolingUpdates`) detected it on its first real run and opened the "Tooling updates" issue (#308), so the stale pin is
now reported on every run while the credentialed weekly scan keeps executing the older CLI.

## What Changes

- **`.github/workflows/snyk.yml`** — the `snyk-version` input, from `v1.1307.2` to `v1.1307.3`. That is the only line
  this change moves.

## Impact

- **Build**: none — the pin is not build input. `toolingUpdates` reads it from the workflow file, declared as
  `@InputFiles`, so the check picks the movement up without a build-logic change.
- **Tests**: none, and the pin cannot be verified locally: `workflowLint` (actionlint) proves only that the YAML lints,
  not that the tag is installable. The credentialed weekly run — or a local `dependencySecurityCheck` given `SNYK_TOKEN`
  — is the first real execution, as the update-check gotcha records.
- **Deployment**: none.
- **Tracker**: #308 is referenced, not closed. The update workflows look their tracker up with `is:issue is:open` and
  open a fresh one when none is open, so a closing reference would orphan its history and the next weekly run would open
  a duplicate; the next run updates #308 silently now that the pin is current.

## New Capabilities

None — a workflow tool pin belongs to no capability's requirements.

## Modified Capabilities

None — no spec statement changes, so this change is `skip_specs`.
