## Why

`openspec/config.yaml` declares per-artifact rules for four artifacts, but the CLI silently ignores two of them:
`proposal`'s and `tasks`' items each contain an unquoted `: `, so YAML parses those items as mappings and the whole
artifact's rules fail the "array of strings" check. The only signal is a warning on stderr at every
`openspec new change`, which reads as noise — so rules meant to prevent, for example, a proposal's missing
`New Capabilities` subsection have never been applied.

## What Changes

- Quote the two `openspec/config.yaml` rule items whose unquoted `: ` makes YAML parse them as mappings, so all four
  artifacts' rules are read.
- Extend the CI `build` check's OpenSpec step with a verification that the CLI reads every declared rule set, so a
  silently ignored rule set fails the gate instead of only warning.
- Add the same check to `/opsx-tool-update` — with a positive control — so a local CLI upgrade re-verifies it.
- Record in `AGENTS.md` the YAML pitfall and the lesson that a warning dismissed as noise was reporting a live defect.
- Audit the `openspec/config.yaml` `context:` block against the project facts it duplicates, since it is the un-gated
  second copy the docs-refresh convention names.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/merge-governance`: the pull-request and main quality gates gain a verification that the OpenSpec
  configuration's declared artifact rules are readable by the CLI.

## Impact

- `openspec/config.yaml` — the two rule items quoted, and the `context:` block audited.
- `.github/workflows/ci.yml` — the OpenSpec step extended.
- `.opencode/commands/opsx-tool-update.md` — the same check with a positive control.
- `AGENTS.md` and `README.md` — the pitfall and the lesson, plus the CI-gate enumerations the new step makes stale.
- `openspec/specs/showcase/quality/merge-governance/spec.md` — synced at archive.
- No application behavior changes, and the generated OpenSpec instruction files are untouched. (Regenerating those
  against the installed CLI is a separate `/opsx-tool-update` unit; this change must hold under the CI-pinned CLI too.)
