# Proposal

## Why

The rule that a site named for a later sweep is identified by its text, never by a line number, governs how `design.md`
and `tasks.md` are written — but it lives in `.opencode/skills/review-gate/SKILL.md`, which `AGENTS.md` loads only when
the review gate runs, after those artifacts exist. `AGENTS.md`'s placement rule — "a rule lives under the trigger that
fires before every case it governs … not where its incident was read" — is the general statement of the gap; this change
applies it to that one rule with a pointer where authoring passes.

## What Changes

- **`openspec/config.yaml`** — `rules.design` and `rules.tasks` each gain one pointer item (exact text in `design.md`
  decision 1) sending the author to the anchoring rule in `.opencode/skills/review-gate/SKILL.md` §1 step 2 before
  naming a site for a later sweep.
- **The anchor rule itself is unchanged** and stays single-sourced in the skill: the config lists carry a pointer, not a
  second copy of the rule.
- No `AGENTS.md`, skill, command, agent, spec, code, build, test, or deployment change.

## Capabilities

### New Capabilities

none

### Modified Capabilities

none — no requirement states where the anchoring rule is delivered; `showcase/quality/agent-skills` owns the review
gate's procedure _ownership_ (the rule stays in the skill) and no scenario's outcome changes. `skip_specs: true`.

## Impact

- `openspec/config.yaml`: two added rule items, one per list; `design.md` row 3 carries the before/after counts and the
  commands that reproduce them.
- No code, API, dependency, build task, or deployment change. `check` must stay green; the config's shape is probed by
  the CI `build` job's quality-gates step (`.github/workflows/ci.yml`), which runs `openspec new change` and fails on a
  list-shape warning — no local Gradle task reads the config.
