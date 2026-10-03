# Proposal

## Why

The `code-quality` requirement "GitHub workflows are linted by the build" carries a scenario whose WHEN conditions on "a
shell error **that shellcheck reports**", but `shellcheck` appears nowhere in the repository: `workflowLint` invokes
`actionlint` alone, `ci.yml` installs only actionlint, no prerequisite documents shellcheck, and actionlint catches
`run:` script errors only through its opportunistic shellcheck integration — which does nothing when shellcheck is
absent (as it is on CI and a fresh clone). The scenario is therefore unfalsifiable as written: it asserts an outcome the
gate cannot produce in the environment it runs in.

## What Changes

- `openspec/specs/showcase/quality/code-quality/spec.md`: correct both the requirement body's "`run:` script errors
  surface locally" clause and the "Broken run script fails the build" scenario to the error classes `actionlint` catches
  natively — a workflow-structure `syntax-check` error or an `expression` error (an undefined context variable) — so the
  spec no longer promises `run:` shell-error coverage the gate cannot deliver without shellcheck.
- `docs/ideas.md`: remove the implemented idea.

`AGENTS.md` needs no edit: its two shellcheck passages already state that the repository installs and documents no
shellcheck and that `workflowLint` runs actionlint alone, so they describe the gate correctly. This change is the
second-instance fix the config-read-path gotcha already anticipates ("two artifacts asserted 'actionlint + shellcheck' —
the `add-actionlint-gate` proposal that named a spec scenario, and a later change's verification story"); the spec
scenario was the one still stale.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `showcase/quality/code-quality`: the "GitHub workflows are linted by the build" requirement's body clause ("`run:`
  script errors surface locally") and its "Broken run script fails the build" scenario are both corrected to the errors
  the gate produces without shellcheck.

## Impact

- **Files**: `openspec/specs/showcase/quality/code-quality/spec.md` (via the delta) and `docs/ideas.md`. `AGENTS.md`
  needs no edit (its shellcheck passages already describe the gate correctly).
- **Build / tests / services**: no code change — the gate's behavior is unchanged; only the spec's description of it is
  corrected to what it actually does.
- **Verification**: the corrected scenario is reproduced against `actionlint` with shellcheck disabled (a `syntax-check`
  error and an undefined-context-variable `expression` error each exit non-zero; a shell-syntax error exits zero,
  confirming shellcheck is what would be needed and is not shipped).
