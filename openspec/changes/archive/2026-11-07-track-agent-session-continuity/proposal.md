# Record and reuse a unit's deep-review and advisor sessions

## Why

Only the quick reviewer's session has a recorded home. The deep review's and the advisor's identifiers live in the
caller's memory or in hand-over prose, so a follow-up pass after a pause or a context loss restarts them, and the only
per-unit session record is the quick ledger — which two units bent to carry a deep pass as a quick round with
`definition-changed`, a reason their diff does not evidence. The probe this unit ran shows the harness refuses to
continue a child session from another main session, so a resumed unit loses those sessions outright: nothing records
that forced replacement, and nothing offers the one route that keeps them, resuming the same main session.

## What Changes

- `docs/units/<unit>/sessions.tsv` — a new append-only register for the stateful agents the quick ledger does not hold:
  one row per invocation carrying its agent, scope, opinion, pass, requested and returned session identifiers,
  transition, reason, hand-over section and outcome.
- `showcase/quality/commit-hygiene` (delta) — the register's format and continuity rule, its admission to the unit
  layout, its pre-commit and `check` verification, and `parent-session-changed` added to the quick ledger's reasons.
- `showcase/quality/agent-skills` (delta) — a requirement recording the deep-review and unit-bound advisor sessions and
  gating their replacement; the advisor's decision-plus-opinion scope; the quick loop's permitted recovery cases, with
  the record format owning the recorded values; and the resumed unit's session rule.
- `.opencode/skills/review-gate/SKILL.md` — the session preflight generalizes to every registered invocation, and the
  deep pass's identifier is resolved from the register with its hand-over written before the call.
- `.opencode/commands/review-thorough.md` — resolve, continue, or replace the pass's session from the register, and
  record the pass.
- `.opencode/commands/consult.md` — the decision-plus-opinion key and the unit-bound register, while an outside-unit
  consultation stays unrecorded.
- `.opencode/skills/pause-resume-unit/SKILL.md` — the checkpoint names the register rows the next step continues; the
  pause report offers the same main session or a new one; the resume continues them or records the forced replacement.
- `scripts/commit-hygiene.py` — an `--agent-sessions` mode (format, per-key continuity, hand-over link) and
  `parent-session-changed` on the quick ledger.
- `scripts/test-commit-hygiene.py` — a known-bad and a known-good case per rule the mode adds.
- `scripts/claim-scan.py` — the new record is data rather than prose, so its rows are not claim candidates.
- `build.gradle.kts` — `verifyAgentSessionContinuity`, a member of `check`.
- `AGENTS.md` — the unwrappable-record exception names both record files, since a register row cannot be wrapped.
- `docs/contracts/process-bindings.md` — the `agent-session-record` role and the consumer-trace rows for it.
- `README.md` — the review, pause/resume, and check-list prose.
- `docs/units/<unit>/review-handover.md` — one section per registered invocation, written before the invocation runs,
  or, in the unit whose change introduces it, from its reports when the invocation predates the mechanism; an earlier
  unit's invocations are not reconstructed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `showcase/quality/agent-skills` — records the deep-review and unit-bound advisor sessions, gates their replacement,
  and states what a resumed unit does with them.
- `showcase/quality/commit-hygiene` — adds the agent-session record's format and continuity verification, admits it to
  the unit layout, and adds the fourth replacement reason to the quick ledger.

## Impact

- **Records**: one new per-unit file kind, `docs/units/<unit>/sessions.tsv`; the existing `rounds.tsv` gains a fourth
  allowed replacement reason.
- **Build**: one new `check` member (`verifyAgentSessionContinuity`) and the Python tests behind it; no dependency,
  runtime, JVM, web-UI, or deployment change.
- **Documents and specs**: the two capability deltas, the review-gate and pause-resume skills, the two commands, the
  bindings declaration, and the README's prose about review and resume.
- **Behavior**: a deep-review or unit-bound advisor invocation is recorded and reused within its main session; a
  replacement requires an evidenced reason, is reported to the owner when forced, and needs the owner's approval when
  discretionary; a resumed unit continues the sessions when the owner resumes the same main session, and records the
  forced replacement otherwise.
