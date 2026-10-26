# Proposal

## Why

`AGENTS.md` tells an agent that a subagent added or edited mid-session "is not registered until OpenCode reloads its
agent list", and requires a restart before smoke-testing one. That came from a 2026-09-12 incident that needed a restart
(the archived `add-agents-auditor-agent` record), which predates the 2026-09-24 V2 migration. On the installed V2 line
(v2.1.4) the definitions reload live, which a probe in this change confirmed in one running session: the same subagent
answered `NONE`, then the marker added to its definition, then the marker that replaced it, and a newly created agent
file was invocable while a deleted one stopped being. The rule therefore prescribes a restart nothing needs, and — worse
for a rule about verification — it names the wrong evidence: a run echoing new text "is no proof of pickup", which is
true, but the remedy it gives is a restart rather than a check that can fail.

## What Changes

- **`AGENTS.md`** — the "A subagent added or edited mid-session is not registered …" bullet becomes several: the rule
  states current behaviour only (the dated incident stays in the change record, per `design.md` Decision 4), the V2
  mechanism and the conditions under which it does not reload replace the restart requirement, the restart becomes a
  fallback rather than the verification, and the confirmation practice is a numbered probe whose false-success channel
  is closed. The obligations the old bullet lumped under one lead become one bullet each (`design.md` Decision 5).
- **`AGENTS.md`** — the neighbouring frontmatter bullet's cross-reference ("a reload does not fix it, unlike the
  not-registered `Unknown agent type` failure") drops the now-vacuous contrast.
- The bullet's retained obligations keep their content and become one bullet each; the rule is their list, so this
  proposal does not repeat it.
- No spec delta (`skip_specs`), no code, build, dependency, or deployment change.

## Capabilities

### New Capabilities

none

### Modified Capabilities

none — no requirement states when a definition edit takes effect (claims row 6 in `design.md`). The bullet's other
obligations are unchanged in this unit, so no requirement's outcome moves. `showcase/quality/agent-skills` owns the
knowledge-authoring standard and the capture rules, and no scenario changes.

## Impact

- `AGENTS.md`: one bullet replaced by several (a numbered probe sequence plus one bullet per remaining obligation), one
  clause deleted, and one capture clause added to the durable-claims bullet (a rule recording a tool's behavior names
  the version probed and the surfaces it did not establish); the measured net delta is in `design.md`'s claims table
  (row 7), so the growth bound is checked rather than asserted.
- `.opencode/skills/review-gate/SKILL.md` §2 step 2 gains the capture's other addition: a fix is verified against the
  finding's own claim before the next round and before any record reports it fixed.
- No code, API, dependency, build task, or deployment change. `spotlessCheck` and `check` must stay green, and the
  markdown formatter owns the bullet's wrapping.
