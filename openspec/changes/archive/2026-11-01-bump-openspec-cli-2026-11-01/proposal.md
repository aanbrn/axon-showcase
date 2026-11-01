# Proposal

## Why

The weekly tooling check (issue #308) reports one coordinate across three rows — `openspec-cli: 1.15.0 -> 1.16.0` in
`ci.yml`, `opencode.yml` and `audit.yml`. Re-resolved on 2026-11-01 with `npm view @fission-ai/openspec version` →
`1.16.0`, so the target has not moved since the issue's report. The pin is load-bearing: the `build` gate installs that
CLI and validates the corpus with it, and the other two workflows install it for their OpenSpec steps.

## What Changes

- **`.github/workflows/ci.yml`, `opencode.yml`, `audit.yml`** — the OpenSpec pin `1.15.0` → `1.16.0` in each workflow's
  install step.
- **`.opencode/skills/openspec-{apply-change,archive-change,explore,propose,sync-specs,update-change}/SKILL.md`** — the
  regeneration `openspec update` under `1.16.0` produces: six files, each only its `generatedBy` stamp. No command or
  skill body changes, so the generated inventory is unchanged and no inventory-driven `AGENTS.md`/README edit is owed.
- **`.github/workflows/ci.yml`** — the config probe drops its `could not parse` arm. Under `1.16.0`
  `openspec validate --all` exits 1 on a config it cannot parse, so the gate fails that shape before the probe runs; the
  grep keeps the list-shape warning, which `validate` does not fail on.
- **`AGENTS.md`** — the config-guard bullets record the retired arm and the mechanism that replaced it, and the
  pattern-history sentence stops naming the retired alternative.
- **`.opencode/commands/opsx-tool-update.md`** — step 2 and step 3 follow the retired arm: the probe's grep is verified
  on the two list-shape controls, and the whole-file control now asserts `openspec validate --all` exits non-zero.
- **`.opencode/skills/check-and-test-authoring/SKILL.md`** — the incident sentence that said the probe "was widened to
  catch both" records the arm's later retirement, so the worked example still describes shipped behavior.
- **`docs/ideas.md`** — the 2026-09-30 entry's first watch clause is marked met; the entry stays live for
  `Fission-AI/OpenSpec#1894`.

No `build.gradle.kts` change is needed: the `toolingUpdates` check already declares one entry per file for this pin, and
every file it reads is registered.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: a tooling-pin bump, which no capability spec covers (`skip_specs`). Verified rather than assumed — a grep of
`openspec/specs/` for the pin coordinate and the old version returns no match, so no spec states the value.
`showcase/quality/merge-governance` specifies the config check's outcomes (a rules or guidance item the CLI cannot read
fails the gate), and both stay met: the list-shape arm still catches both surfaces, and `openspec validate` now fails
the whole-file case.

## Impact

- **CI**: the `build` gate installs the pinned CLI and validates the corpus, so this pull request is the pin's own
  end-to-end verification. The config probe is replayed against `1.16.0` (healthy, both list-shape controls, one
  unparseable config) because the retired arm is the gate's own behavior.
- **The two cloud workflows**: `opencode.yml` and `audit.yml` install the same pin, and their next comment-triggered or
  scheduled run is its first execution there; this pull request cannot exercise them.
- **No behavior change in the repository's own tooling** beyond the retired probe arm: the regenerated files are
  generated artifacts.
