## Why

`agent-skills` states which agents run unattended on a schedule and records `readme-auditor`'s deliberate on-demand-only
status, but says nothing about `experience-analyzer` — so a reader cannot tell whether the retrospective is unscheduled
by oversight or by design. This records the decision: it stays on-demand.

## What Changes

- Extend the `agent-skills` scheduling requirement to name `experience-analyzer` as on-demand only, with the reasoning.
- Extend the `experience-analyzer` bullet in `AGENTS.md` with the on-demand rule, a one-line rationale, and a pointer to
  the spec; the full reasoning and the rejected alternatives are recorded in the delta and the design, so the
  always-loaded file does not carry a second copy of them.

- Fold the worked technique into `AGENTS.md`'s growth-discipline clause: when a bullet's rationale is normative in a
  spec, keep a pointer rather than a condensed copy — this change's bullet is the example, written as a rule and a
  pointer rather than the thirteen-line draft it briefly held.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills`: two `MODIFIED` blocks — the "on-demand audits are also available on a schedule"
  requirement gains the `experience-analyzer` on-demand-only clause (alongside the existing `readme-auditor` one), and
  "per-change quality-gate and analysis subagents are available" gains the pointer-technique clause in its "Lessons are
  captured after implementation" scenario.

## Impact

- **Specs**: two `MODIFIED` blocks on `showcase/quality/agent-skills` — the scheduling requirement gains the
  `experience-analyzer` on-demand-only clause, and the per-change-subagents requirement gains the pointer-technique
  clause in its "Lessons are captured after implementation" scenario.
- **Docs**: the `experience-analyzer` bullet extended with a rule + one-line rationale + pointer; a sentence folded into
  the growth-discipline clause; and a `docs/ideas.md` entry disambiguated (it listed `/retrospective` among shiftable
  "unattended work", which read as scheduled). Net `AGENTS.md` growth is **+6 lines** (the `experience-analyzer` bullet
  gains 2, the growth-discipline sentence 4) — a justified decision: the bullet carries a rule and a pointer rather than
  the thirteen lines of duplicated rationale an earlier draft held, and the folded sentence is the durable technique
  that keeps future bullets flat.
- **Build / tests / deployment**: none — no code, workflow, or configuration changes; the `audit` workflow and the
  `/retrospective` trigger are untouched.
