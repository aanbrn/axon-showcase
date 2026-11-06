# Record the unit as the process's base unit of work

## Why

ADR-0029 places a unit's record outside the change directory, but it never says why a unit is broader than an OpenSpec
change. `AGENTS.md` scopes its rules to units and says to read "change" as "unit of work", yet neither sentence names
the unit as the base unit of work, so an agent that reads only the always-loaded guidance can conclude the opposite —
that a change is the base and the unit a side record.

## What Changes

- Add a dated `## Amendment` to `docs/adr/0029-unit-state-lives-outside-the-change-plan.md` recording the layering
  between a unit and an OpenSpec change and the declined alternative of carrying every unit as a change, with a pointer
  at the Context sentence the Decision supersedes.
- Replace the paragraph at `AGENTS.md:127-128` with the definition: a unit is the process's base unit of work, one
  branch intended for one pull request; an OpenSpec change is one form it takes and every other form the section names
  is equally a unit; the rules govern a unit whether or not it carries a change, so a rule that says "change" is read as
  the unit of work.
- Change no record location, field, check, capability-spec requirement, or code path.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None, for two reasons. No capability spec states the amendment convention the first edit follows: `docs/adr/README.md`
owns it, and `git grep -ni "amendment" origin/main -- openspec/specs` finds no match. The second edit states terminology
at the process entry point rather than behavior: the behavior it orients is already specified by `agent-skills`'s
continuation requirement and `commit-hygiene`'s requirements for the unit record, the commit assignment, and review
readiness, so a spec copy would be a second statement of rules those requirements own.

## Impact

- `docs/adr/0029-unit-state-lives-outside-the-change-plan.md` gains one amendment section; its Status stays `Accepted`.
- `AGENTS.md`'s "OpenSpec Workflow Agreement" paragraph is replaced at roughly its current size.
- No build, dependency, API, deployment, or test change.
