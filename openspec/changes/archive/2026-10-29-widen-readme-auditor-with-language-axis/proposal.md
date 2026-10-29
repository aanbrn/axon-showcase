## Why

`readme-auditor` cannot report a grammar error as a defect. Its three axes are accuracy, design intent, and coverage,
and its report contract sends every prose concern to the advisory class — the rule that protects the README's
hand-curated style also buries a sentence that does not parse. Two merged units on the file made the gap concrete:
`proofread-readme-language` (#737) landed a proofread pass whose merged description is "a set of prose corrections (21
hunks against `origin/main`)", and `correct-readme-mechanism-claims` (#738) corrected three statements about how the
system works. A language error reaches the owner today only because a human reads the file: no auditor axis and no gate
reports the class.

## What Changes

- `.opencode/agent/readme-auditor.md` gains a **language** axis: a provable grammar, agreement, punctuation, or
  sentence-structure error becomes a **finding** carrying the passage, what is wrong, and a minimal rewrite that
  preserves the meaning; a debatable phrasing, a diction preference, or an unclear authorial intent stays **advisory**.
- The same definition states the third case: a language correction that would change a technical claim is an accuracy
  question, verified against the repository, never applied as "mere grammar".
- The frontmatter `description` names the language axis beside the three it already names.
- The report contract groups findings by four axes and keeps its verdict line shape (`<n> findings, <n> advisory`).
- `.opencode/commands/audit-readme.md` names four axes in its step 1 and in its frontmatter description.
- `AGENTS.md`'s readme-auditor bullet names the language axis (it currently says "on three axes") and keeps its pointer
  to the `agent-skills` spec for the boundary; `README.md`'s agent-table row, `/audit-readme` row, and the Self-Learning
  Loop bullet that describes the audit name it too.
- `openspec/specs/showcase/quality/agent-skills/spec.md`: the README-audit requirement is retitled to name four axes
  (REMOVED + ADDED, carrying every existing scenario) and gains the language scenarios.
- The capability `## Purpose` mentions the audit's scope and is refreshed in the archive commit — a delta cannot carry a
  Purpose.

## Capabilities

### New Capabilities

- none

### Modified Capabilities

- `showcase/quality/agent-skills`: the README audit covers language beside accuracy, design intent, and coverage, and
  the requirement states the finding/advisory boundary that keeps a hand-curated style out of the defect list.

## Impact

- `.opencode/agent/readme-auditor.md` — the axis, the boundary rule, the report contract, the frontmatter description.
- `.opencode/commands/audit-readme.md` — the axis count in step 1, the description, and the advisory decision point.
- `AGENTS.md` and `README.md` — four descriptions of the audit: the `AGENTS.md` bullet, the agent-table row, the
  `/audit-readme` row, and the Self-Learning Loop bullet.
- `openspec/specs/showcase/quality/agent-skills/spec.md` — the retitled requirement and its scenarios; the Purpose is
  refreshed at archive.
- No code, API, dependency, build, or deployment change. No new subagent, no new command, no gate: the audit stays on
  demand, and applying its findings stays a docs unit under the review gate.
- A run's report may grow: the language axis adds findings the contract previously could not carry, and a longer report
  is the price of the axis rather than a defect in it.
