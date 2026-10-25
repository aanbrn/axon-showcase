# Proposal

## Why

The paths that open a pull request do not agree on how to title or describe one, and nothing states which is right. A
body template exists (`.github/PULL_REQUEST_TEMPLATE.md`), but `gh pr create --body-file` bypasses it, so an
agent-authored body follows it only by hand, and the template states no title rule at all. Titles are therefore habit:
as of 2026-10-25 only 55 of the 660 merged titles carry a `:`, 49 of them a leading kebab change name — and that form,
22 of the 28 merged since #650, was never written down. Merged titles become the public release notes, so the
inconsistency ships.

## What Changes

- **`.github/PULL_REQUEST_TEMPLATE.md`** — the header comment becomes the repository's pull-request standard: a title
  rule (one imperative English phrase naming the change, no prefix) and the two required body sections (`## Summary`
  then `## Test plan`), with further sections allowed after them. The cloud path's response carries the sections and its
  generated title is renamed when it deviates; a report pull request's body is the report. The existing checklist is
  unchanged.
- **`.opencode/skills/ship-change/SKILL.md`** — "Ship a change" step 1 points at that standard, since a `--body-file`
  body bypasses the template; the cloud-agent pull-request section gains a decision point checking an action-derived
  title against the standard and renaming it when it deviates; the "How the action derives the pull request" section
  points at that check instead of leaving the generated title unaddressed.
- **`AGENTS.md`** — the `--body-file` gotcha states that the bypassed template's title rule travels through `--title`,
  beside the clause that already sends its sections into the body file.
- **`docs/ideas.md`** — one parked entry recording the deferred title lint and its revisit condition.
- **`.opencode/skills/review-gate/SKILL.md` and `openspec/config.yaml`** — the capture this unit's review loop earned:
  the pre-write pass's universal-claim clause names the absence form and a two-part subject, and the config's mirrored
  rule follows, since this change's own claims row was re-patched across four rounds for exactly that gap.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None, and the skip is deliberate rather than incidental. `showcase/quality/merge-governance` owns the audit workflow's
report-body requirement (the owner mention in the body, and the reason it is not in the title); this change leaves that
obligation intact and adds a conditional rename after creation, which alters no scenario's outcome.
`showcase/quality/agent-skills` requires each project skill to carry its procedure: the `ship-change` requirement names
the steps, decision branches, and identifiers, so step 1 naming the standard and the cloud path naming a check and a
conditional rename make the existing step set more complete without changing what its scenarios assert; and the
`review-gate` requirement states that "a correction to a step SHALL be made in the skill", so the capture's refinement
of the pre-write claim clause is the correction that requirement mandates, not a new obligation. No capability spec
covers a pull-request title or body format.

## Impact

- **Process**: every pull request follows one title shape and one body shape; on the cloud path the response carries the
  sections and the generated title is renamed when it deviates, and a report pull request's body is the report. Existing
  titles stand — the rule is forward-looking, and it reverses the `<change-name>: …` prefix most pull requests merged
  since #650 use.
- **Release notes**: merged titles are the notes' text, so the shape lands there too.
- **No gate**: nothing validates a title or a body, so a deviation is caught by review, not by CI.
- **Docs**: the standard is stated once, in the template; the skill and `AGENTS.md` point at it rather than restating
  it.
