# Proposal

## Why

`openspec/config.yaml`'s per-artifact rules are the text the CLI shows an agent at drafting time, and no gate reads
their content — `ci.yml`'s probe gates only that each item is a string. Three defects sit in that text: `rules.specs`
names `DEPRECATED` as a delta operation where the CLI's fourth operation is `RENAMED`, so a change following the config
gets a warning and its requirement is dropped at archive while `validate` still exits 0; `rules.proposal`'s count clause
and `rules.design`'s state the same rule in different words, so a proposal is drafted under the weaker one; and two
clauses `rules.design` carries are missing where their failures were recorded — the single-sourcing clause from
`rules.proposal` and `rules.tasks`, and the count clause from `rules.tasks`.

## What Changes

- `openspec/config.yaml`, `rules.specs` — `DEPRECATED` becomes `RENAMED`, naming the `FROM:`/`TO:` form the CLI reads.
- `openspec/config.yaml`, `rules.proposal` — the count clause takes `rules.design`'s wording; the single-sourcing clause
  is added; a set-widening clause is added; the `Impact` rule widens past build/tests/deployment.
- `openspec/config.yaml`, `rules.tasks` — the single-sourcing clause and the count clause are added.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None — no capability spec covers the config's rules' content. `merge-governance` owns the probe that the rules are
_readable_ (a malformed list item), not what they say, and that shape is unchanged, so this change sets
`skip_specs: true`.

## Impact

- **Build / tests / services**: no code, build, dependency, or deployment change — the config file, its change dir, this
  unit's records, and the docs the unit itself causes (`AGENTS.md`'s capture and the `docs/ideas.md` sweep).
- **Verification**: `openspec validate --all`, the CI probe's own commands, and `spotlessApply`/`spotlessCheck`.
