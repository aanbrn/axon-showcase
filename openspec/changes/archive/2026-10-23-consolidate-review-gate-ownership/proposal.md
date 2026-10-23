# Proposal

## Why

The review gate is the stage every unit runs, and its procedure lives in two copies: `AGENTS.md`'s
`### Workflow: The review gate` (202 lines) and the `agent-skills` requirement "Per-change quality-gate and analysis
subagents are available" (437 lines, 20 scenarios). Twelve scenarios cover the review loop, and the first alone is 129
lines whose 35 `AND` clauses restate the caller's steps, so one step change has to be made twice and no `check` member
pairs them. The 2026-10-19 agent-tooling report's two findings are both copy drift.

That requirement also owns the capture and the diagram/vision scenarios, so one requirement answers to three subjects.
Five commands name `AGENTS.md` as the owner of the steps, and `AGENTS.md`'s `## Conventions` cites the single-sourcing
rule by that section, so the move has six sites to retarget.

The owner's objective is a consolidated, internally consistent process; this change is its first unit.

## What Changes

- **Add** `.opencode/skills/review-gate/SKILL.md`: the calling agent's complete review-gate procedure — trigger, stages,
  decision points, failure handling, completion criteria, and the identifiers its steps name — carrying a `Source:` line
  that quotes the `AGENTS.md` heading it replaces.
- **Reduce** `AGENTS.md`'s `### Workflow: The review gate` to the stage entry point: the trigger, the stage names with
  their owners, the authorization stops that must stay always-loaded (a clean quick review is not approval; no commit,
  push, archive, or merge before the owner's pass), the instruction to load the skill before the first stage, and the
  stop-and-report rule for a missing skill.
- **Keep in `AGENTS.md`** the rules that serve authoring and every stage rather than this one procedure: the
  single-sourcing rule (referenced from `## Conventions`) and the lifecycle invariants that already name the review
  gate.
- **Retarget** the five command definitions and `AGENTS.md`'s single-sourcing cross-reference to name the skill as the
  owner of the steps.
- **Replace** the `agent-skills` requirement "Per-change quality-gate and analysis subagents are available" with focused
  requirements that each own one subject: the quick loop, the routed deep review (four requirement blocks in place of
  one), the post-implementation capture, and the diagram/vision agents.
- **Add** two requirements that state the new ownership: the caller's procedure lives in the project skill, and the root
  carries only the stage entry point.
- **Leave unchanged** every gate's membership, order, and obligation, every machine check
  (`verifyReviewSessionContinuity`, `verifyUniqueReviewRounds`, `verifyCapturedMarkers`, the ledger contract in
  `showcase/quality/commit-hygiene`), and the reviewer contracts in `review-quick.md` and `review-thorough.md`.

## Capabilities

### New Capabilities

none

### Modified Capabilities

- `showcase/quality/agent-skills`: the review gate's procedure ownership, the requirement decomposition, and the root
  entry point

## Impact

- `AGENTS.md`: the review-gate section shrinks from 202 lines to an entry point; one cross-reference is retargeted.
- `.opencode/skills/review-gate/SKILL.md`: new project-authored skill (created).
- `.opencode/commands/{audit-agents,audit-architecture,audit-readme,audit-specs,apply-audit-reductions}.md`: the owner
  named in each review-gate reference changes from `AGENTS.md` to the skill.
- `openspec/specs/showcase/quality/agent-skills/spec.md`: one requirement removed, four added in its place, two
  ownership requirements added (at archive, through the delta).
- No code, API, dependency, build, test, or deployment change. The gates that read the moved text already include
  `.opencode/skills/*/SKILL.md` — the Markdown format target, `verifyCapturedMarkers`, and `upstreamReferences` — so the
  build is not expected to change; any exception is a task, not an assumption.
- `docs/ideas.md`: the follow-on consolidation units this pilot defers are parked there.
