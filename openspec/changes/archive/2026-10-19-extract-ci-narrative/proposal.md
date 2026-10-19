# Proposal

## Why

The 2026-10-19 agent-tooling audit's second extraction opportunity is the `Continuous Integration` section: `AGENTS.md`
974-1225, **252 lines and 23,620 bytes** — the report's ~252-line size measured exact, unlike the first opportunity's
range; its ~26KB is a rounded estimate. Unlike the first, it is a **partial split**, and the report named the
precondition honestly: the section's gate-placement rule is a workflow rule `showcase/quality/merge-governance` does not
state and must stay at root, so the boundary between what stays and what moves had to be drawn before any content moved.
The owner confirmed the boundary on 2026-10-19, including the one genuinely mixed paragraph (the
rebuild-cost/test-timing lesson and the cache mechanics stay; the cache detail is deleted, with the workflow YAML as the
mechanism's authoritative configuration), and this change implements it.

## What Changes

- `AGENTS.md`: the section is trimmed to its rules and pointers — the gate-placement rule with its retry-not-tolerate
  corollary, the two workflow procedures, the schedule-placement and `ubuntu-latest` rules, and the CI-only-failure
  diagnostic lesson stay; the per-workflow outcome inventories move; the `setup-gradle` cache detail is deleted, with
  the mechanics pointing at the workflow YAML; and the dependabot/CODEOWNERS narrative is condensed to its rules and
  homes.
- No spec delta: every moved narrative's outcome is already stated — `showcase/quality/merge-governance` owns the gates
  and the observational scheduled workflows, and `showcase/quality/releases` owns the release workflow — so the moved
  content is deleted with a pointer and no capability spec changes (`skip_specs: true`). The `setup-gradle` cache
  mechanics stay at root as a pointer to the workflow YAML: they are mechanism, and changing them changes no scenario's
  outcome.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None: `skip_specs: true`. `showcase/quality/merge-governance` already owns the gate composition and the observational
scheduled workflows' outcomes, and `showcase/quality/releases` owns the release workflow's — the trigger and version
guards, the five images and the chart published to the registry, the tag created only after they publish, and the
dry-run and backfill paths — so the trim deletes prose with a pointer and changes no capability spec. No spec specifies
the section's prose (`grep -rn 'Continuous Integration' openspec/specs/` finds none).

## Impact

- Files: `AGENTS.md` only; no spec change, since the moved narratives are deletions with a pointer.
- No code, build, or deployment impact: guidance prose.
- The first extraction opportunity shipped separately as `extract-load-test-commands`.
