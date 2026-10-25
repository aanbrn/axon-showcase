# Proposal

## Why

The `toolingUpdates` report names two rows: `openspec-cli: 1.14.3 -> 1.15.0` (in `ci.yml`, `opencode.yml` and
`audit.yml`) and `snyk-cli: v1.1308.2 -> v1.1308.3`. Tracker issue #308 names exactly these rows, and each target was
re-resolved against its own lookup — `npm view @fission-ai/openspec version` and `gh api repos/snyk/cli/releases/latest`
— so neither has moved since the issue's report.

Each pin is load-bearing. The OpenSpec CLI generates the `opsx-*` commands and `openspec-*` skills and validates the
corpus in the `build` gate; the Snyk CLI is what the weekly credentialed scan installs.

## What Changes

- **`.github/workflows/ci.yml`, `opencode.yml`, `audit.yml`** — the OpenSpec pin `1.14.3` → `1.15.0` in each workflow's
  install step.
- **`.github/workflows/dependency-security.yml`** — `snyk-version` `v1.1308.2` → `v1.1308.3`. The action ref itself
  (`snyk/actions/setup@master`) is a deliberate floating ref Dependabot does not manage, and stays.
- **`.opencode/skills/openspec-{apply-change,archive-change,explore,propose,sync-specs,update-change}/SKILL.md`** — the
  regeneration `openspec update` produces with the pinned CLI. Six files change, each only its `generatedBy` stamp
  (`1.14.3` → `1.15.0`). No command or skill body changes, so the generated inventory is unchanged and no
  inventory-driven `AGENTS.md`/README edit is owed.
- **`.opencode/commands/opsx-tool-update.md`** — the implementation capture's applied delta, narrowed on the advisor's
  review: step 7 points the command at the standing 2026-09-30 `docs/ideas.md` entry, whose own watch condition is read
  when the CLI is bumped, and notes that the step retires with that entry. No `AGENTS.md` rule and no general obligation
  over "the records a bump activates" ships: one bump's record is silent on the check while the one before it ran it,
  and the incident is not severe enough for the promotion gate's second branch either.

No `build.gradle.kts` change is needed: the `toolingUpdates` check already declares one entry per file for each of these
pins, and every file it reads is registered.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: a tooling-pin bump, which no capability spec covers (`skip_specs`). Verified rather than assumed — a grep of
`openspec/specs/` for the pin coordinates (`openspec@`, `snyk-version`, `1.1308`, and either version string) returns no
match, so no spec states these values. `showcase/quality/merge-governance` specifies the `tooling-updates` workflow's
behavior and its issue's report, not which versions the pin declares.

## Impact

- **CI**: the `build` gate installs the pinned OpenSpec CLI and validates the corpus with it, so the pull request is the
  OpenSpec pin's own end-to-end verification.
- **The credentialed scan**: the Snyk pin takes effect only in a run that installs it, so it rests on a dispatch of
  `dependency-security.yml` from the branch rather than on this pull request's checks.
- **No behavior change in the repository's own tooling**: the regenerated files are generated artifacts, and the pins
  are the CLI versions CI and the weekly scan install.
