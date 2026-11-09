# Proposal

## Why

The scheduled `tooling-updates` report named issue #308: the OpenSpec CLI pin, declared in three workflows, and the Snyk
CLI pin each lag the latest release. The check exists to surface exactly that drift, and a lagging pin is what the next
run re-reports until it is bumped.

## What Changes

- **`.github/workflows/ci.yml`, `.github/workflows/opencode.yml`, `.github/workflows/audit.yml`** — the OpenSpec CLI pin
  `@fission-ai/openspec@1.16.0` → `1.16.1` in each workflow's install step.
- **`.github/workflows/dependency-security.yml`** — `snyk-version: v1.1308.3` → `v1.1308.4`. The action ref
  (`snyk/actions/setup@master`) is a deliberate floating ref Dependabot does not manage, and stays.
- **`.opencode/skills/openspec-{apply-change,archive-change,explore,propose,sync-specs,update-change}/SKILL.md`** — the
  regeneration `openspec update` produces with `1.16.1`; a stamp-only change is the expected outcome. IF a template body
  changed, THEN its new instruction is read and reported before the gate runs.
- **`scripts/verify-openspec-delta-guard.py`, `scripts/test-verify-openspec-delta-guard.py`, and the descriptions of the
  guard's behavior** — the pinned CLI now rejects a delta section it does not recognise with its own error, so the probe
  must recognise that diagnostic while it keeps guarding the stray-text warning, and the `ci.yml` step comment and the
  `AGENTS.md` bullet must stop describing both shapes as warnings with a zero exit. The spec needs no change: its
  requirement's outcome — the change fails the check even when it also carries valid content — holds for both shapes,
  and its scenario still fires for the stray-text shape.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true` is set in `.openspec.yaml`. No capability spec states either pin —
`git grep -n -e "@fission-ai/openspec" -e "snyk-version" -- openspec/specs` returns no match — and
`showcase/quality/merge-governance`'s tooling-updates requirement describes the report mechanism without naming a pinned
version, so no scenario's outcome changes.

## Impact

- The `build` gate installs the bumped OpenSpec CLI and runs the corpus validation and the configuration probe under it.
- The `opencode` workflow's `/oc` runs and the scheduled `audit` workflow install the same OpenSpec CLI, so both run the
  bumped version. The audit prompt invokes no `openspec` command, so there the bump changes only the installed CLI; the
  regenerated instruction files reach every workflow through the checkout, not through the pin.
- The weekly credentialed Snyk workflow installs the bumped Snyk CLI.
- No application code, API, dependency catalog, image, chart, or deployment surface changes.
- The Snyk bump is not locally provable: `workflowLint` proves the YAML lints, not that the release tag installs.
