# Proposal

## Why

The CI `build` gate's OpenSpec configuration check greps `openspec new change` output only for
`ignoring this artifact's rules|could not parse`, so a malformed item on the config's other declared list surface —
`operations.apply.guidance` — is dropped with a different warning
(`Guidance for operation 'apply' must be an array of strings, ignoring this operation's guidance`) that the gate does
not match, and the CLI silently loses that operation's guidance. The guard exists to catch exactly this class on the
`rules` surface; widening it to every declared list surface closes the gap, and the `/opsx-tool-update` re-verification
that mirrors it must widen with it.

## What Changes

- `.github/workflows/ci.yml`: widen the OpenSpec configuration probe's grep to the generic list-shape warning
  (`must be an array of strings`) so it matches every declared list surface (`rules` and `operations.*.guidance`),
  update the `::error::` messages to name a declared list item rather than a rule, and rename the probe to
  `openspec-config-probe` (it no longer covers only rules).
- `.opencode/commands/opsx-tool-update.md`: widen the re-verification to the same pattern, check the `operations`
  guidance surface as well as the four artifacts' `rules`, and add the guidance warning to its positive controls.
- `openspec/specs/showcase/quality/merge-governance/spec.md`: the fast-gate and full-gate requirements now say the check
  verifies the config's declared artifact rules and operation guidance are readable; add a scenario for the guidance
  surface.
- `AGENTS.md`: update the config read-path gotcha — the probe now covers every declared list surface, retiring the "put
  a rule on `rules:` unless the guard is widened" clause — and the CI summary's "rule sets" to the declared list
  surfaces.
- `README.md`: the CI summary's "rule sets" now reads the declared list surfaces.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/merge-governance`: the OpenSpec configuration check now covers the config's operation guidance as
  well as its per-artifact rules (the fast-gate and full-gate requirements, plus a new scenario for the guidance
  surface).

## Impact

- **CI**: `.github/workflows/ci.yml`'s OpenSpec configuration step (the `build` gate).
- **Agent tooling**: `.opencode/commands/opsx-tool-update.md`.
- **Specs**: `showcase/quality/merge-governance`.
- **Docs**: `AGENTS.md`, `README.md`.
- **Build / tests / deployment**: none.
