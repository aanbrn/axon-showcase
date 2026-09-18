## Why

The tooling audit's conciseness axis already names "duplicated or near-duplicate entries worth merging" and "one-off
trivia", but a normal run does not surface them: the first audit this session reported one stale count, and both the
merge pass and the deletion pass only appeared when separate runs were explicitly scoped to them. So the counterweight
to accretion exists in the brief and not in practice, and the file grows until someone thinks to ask. Two tests make
both analyses decidable: a merge must carry every anchor and evidence the originals held, and a removal must clear the
decision test — a rule governing no decision is trivia — the same filter the capture side now uses.

## What Changes

- **Merge candidates are a standing analysis.** The audit reports overlapping or complementary entries with the merged
  text, under the rule that a merge preserves every anchor and piece of evidence either side carried and never blends
  two distinct lessons into one.
- **Removal candidates are a standing analysis.** The audit reports rules that govern no decision, naming what would be
  lost and whether git preserves it — candidates only; the owner decides.
- **The verdict line names both counts**, so a run that found three merge candidates is distinguishable from one that
  found none.

## New Capabilities

None.

## Modified Capabilities

- `showcase/quality/agent-skills`: the tooling-audit requirement's conciseness coverage gains the two standing analyses
  and their tests, and its report gains the counts.

## Impact

`.opencode/agent/agents-auditor.md` (its conciseness axis, the finding labels, the verdict line),
`.opencode/commands/audit-agents.md` (the output description), `AGENTS.md` (the auditor bullet), and `README.md` (the
two agent/command table rows and the auditor prose). No code change.
