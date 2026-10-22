# Proposal

## Why

The `toolingUpdates` report named three rows: `openspec-cli: 1.14.2 -> 1.14.3` (in `ci.yml`, `opencode.yml` and
`audit.yml`), `snyk-cli: v1.1308.1 -> v1.1308.2`, and `pack-cli: 0.40.9 -> 0.40.10` (in `e2e.yml`, `release.yml` and
`deployment-smoke.yml`). Each target was re-resolved against its own lookup — `npm view @fission-ai/openspec version`,
`gh api repos/snyk/cli/releases/latest`, and `gh api repos/buildpacks/pack/releases/latest` — and all three still point
at those versions. Tracker issue #308 named only the OpenSpec row: its report predates the other two releases.

Each pin is load-bearing. The OpenSpec CLI generates the `opsx-*` commands and `openspec-*` skills and validates the
corpus in the `build` gate; the Snyk CLI is what the weekly scan installs; `pack` builds the web UI image.

## What Changes

- **`.github/workflows/ci.yml`, `opencode.yml`, `audit.yml`** — the OpenSpec pin `1.14.2` → `1.14.3` in each workflow's
  install step.
- **`.github/workflows/dependency-security.yml`** — `snyk-version` `v1.1308.1` → `v1.1308.2`. The action ref itself
  (`snyk/actions/setup@master`) is a deliberate floating ref Dependabot does not manage, and stays.
- **`.github/workflows/e2e.yml`, `deployment-smoke.yml`, `release.yml`** — `pack-version` `0.40.9` → `0.40.10`.
- **`.opencode/commands/opsx-explore.md`, `opsx-propose.md`, and the six `.opencode/skills/openspec-*/SKILL.md` files**
  — the regeneration `npx @fission-ai/openspec@1.14.3 update` produces. Eight files change, all of them modifications,
  so the generated command and skill inventory is unchanged and no inventory-driven `AGENTS.md`/README edit is owed.
  Four carry a template change — the output-path rule (choose the concrete path inside `changeRoot` before creating
  anything, and verify it survives symlink resolution) in both `opsx-*.md` commands and in `openspec-propose/SKILL.md`
  and `openspec-explore/SKILL.md`; the other four skills change only their `generatedBy` stamp.

No `build.gradle.kts` change is needed: the `toolingUpdates` check already declares one entry per file for each of these
pins, and every file it reads is registered.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: a tooling-pin bump, which no capability spec covers (`skip_specs`). Verified rather than assumed — a grep of
`openspec/specs/` for the pin coordinates (`openspec@`, `snyk-version`, `pack-version`) returns no match, so no spec
states these values.

## Impact

- **CI**: the `build` gate installs the pinned OpenSpec CLI and validates the corpus with it, so the pull request is the
  OpenSpec pin's own end-to-end verification.
- **The credentialed scan and the image builds**: the Snyk and `pack` pins can only be verified by a run that installs
  them, so they rest on a dispatched scan and the next image build rather than on this pull request's checks.
- **No behavior change in the repository's own tooling**: the regenerated files are generated artifacts, and the pins
  are the CLI versions CI, the weekly scan, and the image builds install.
