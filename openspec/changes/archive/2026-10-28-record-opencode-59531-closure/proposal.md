# Proposal

## Why

`anomalyco/opencode#59531` is the repository's own report that an agent cannot see its remaining context budget. It
closed `not_planned` on 2026-10-27, so no harness signal is coming from it, and the maintainer points at a plugin
instead. The repository cites that issue in `pause-resume-unit`'s guidance, and a reference closed `not_planned` owes
its disposition beside the note and a retirement condition that can fire. The plugin work the closure implies has no
home yet: a squashed pull-request body and a closed issue are not records.

## What Changes

- `.opencode/skills/pause-resume-unit/SKILL.md` — the bullet citing the issue states the `not_planned` closure and the
  maintainer's plugin answer, and re-derives the reference's retirement condition to one that can fire.
- `docs/ideas.md` — a dated entry parks the plugin work with the channels the running runtime's plugin surface offers,
  the caution upstream stated, and the condition that reopens the idea.
- `docs/process-defects/record-opencode-59531-closure.md` — the unit's process-defect record.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. The closure changes a note beside an upstream reference, not the behaviour the `showcase/quality/agent-skills`
requirement states (an unmeasured limitation stays the agent's own assessment), so the change declares
`skip_specs: true` and touches no main spec.

## Impact

Skill guidance, `docs/ideas.md`, and the unit's process-defect record. No code, build, spec, dependency, or deployment
surface changes; the weekly `upstreamReferences` report renders the new state from the API without an edit.
