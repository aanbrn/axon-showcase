# Process defects — lesson-capture-reuse

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### lesson-capture-reuse#1 — the `execute` deny did not remove the pinned session-move tool

- **Recorded** — 2026-10-31, the main agent.
- **Stage** — implementation (trial setup).
- **Expected** — the trial agent's `permission: execute: deny` removes the `opencode.session_move` tool, so the
  confinement holds through tool exposure.
- **Observed** — the agent still called `opencode_session_move` and moved the session to `/tmp`; only the agent-level
  `tools` map (`opencode_session_move: false`) removed the tool.
- **Evidence** — the first session-move attempt returned `{"directory": "/tmp"}`; after the `tools` map, the agent
  reported no such tool. The normalized config carried the `execute` deny in both cases.
- **Consequence** — one invocation moved the session; the confinement was re-established with the `tools` map, and no
  file was written.
- **Knowledge boundary** — observed on OpenCode v2.2.5; the pinned-tool exposure is not governed by the `execute`
  action, and the `tools` map is the mechanism that worked.

### lesson-capture-reuse#2 — the synthetic record cited evidence the fixture did not have

- **Recorded** — 2026-10-31, the main agent.
- **Stage** — implementation (fixture build).
- **Expected** — the fixture's `docs/process-defects/unit-b.md` cites a round-2 report and a ledger row that exist.
- **Observed** — the fixture had only `unit-b-round-1.md` and no unit-b row in `docs/reviews.md`; the trial agent
  withheld the candidate because its recurrence evidence was absent.
- **Evidence** — the S1 report named the absent `docs/reviews/unit-b-round-2.md` and the missing unit-b row.
- **Consequence** — the first S1 run did not exercise the novel branch; the fixture was repaired with the row and the
  round-2 report, and the second run then found the candidate covered by the first slice's skill.
- **Knowledge boundary** — observed in this unit; the repair was made by the main agent, not the builder, so the fixture
  is builder-blind but not slice-blind.
