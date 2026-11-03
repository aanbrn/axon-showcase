# Process defects — probe-pause-resume-reuse

Process defects observed while working this unit. Recorded under `AGENTS.md`'s "Workflow: Record process defects".

### probe-pause-resume-reuse#1 — a sandbox deny blocked a read-only command

- **Recorded** — 2026-10-31, the main agent.
- **Stage** — implementation (pilot setup).
- **Expected** — the fixture's `git worktree *` deny blocks worktree creation and mutation.
- **Observed** — it also blocked a read-only `git worktree list --porcelain` inside a compound
  `git status … && git worktree list …` command; the continuation session was denied and stopped.
- **Evidence** — the session export carries `{"type":"permission.rejected","message":"Permission denied: shell"}` for
  the compound command; the deny pattern was `git worktree *`.
- **Consequence** — one invocation was wasted; the deny was narrowed to `git worktree add|remove|move|prune` and the
  `-C` variants. No file was affected.
- **Knowledge boundary** — observed on OpenCode v2.2.5. The per-node matching is the documented behavior; every compound
  form was not tested.

- **Decision** — proposed: applied as the sandbox rule's mutating-subcommand denial, with the read-only `list` left
  allowed and the restriction scoped to the sandbox's own config; it ships in
  https://github.com/aanbrn/axon-showcase/pull/794.

### probe-pause-resume-reuse#2 — the session-move deny is accepted and not enforced

- **Recorded** — 2026-10-31, the main agent.
- **Stage** — implementation (sandbox control).
- **Expected** — `permission.session_move: deny` in the fixture config blocks the `opencode.session_move` tool.
- **Observed** — the rule normalized as `{"action":"session_move","resource":"*","effect":"deny"}`, and the tool call
  succeeded, moving the session to `/tmp`.
- **Evidence** — `opencode debug config` prints the normalized rule; the control's tool output carries
  `{"sessionID":"ses_e701c34b4ffevvM1CxvpM0gsbr","directory":"/tmp"}`.
- **Consequence** — the sandbox's isolation was incomplete on that axis. The session moved but wrote no files; no
  worktree, remote, or escape directory was created. The pilot's behavioral results are unaffected; the control is
  recorded as failed.
- **Knowledge boundary** — observed on OpenCode v2.2.5. The mechanism that would enforce the rule is unknown, and the v1
  line was not tested.

- **Decision** — proposed: applied as the `tools`-map remedy the permission-action bullet now names (the same rule
  `lesson-capture-reuse#1` applies); it ships in https://github.com/aanbrn/axon-showcase/pull/794.

### probe-pause-resume-reuse#3 — a mutable review count in the pull-request body went stale

- **Recorded** — 2026-10-31, the main agent.
- **Stage** — review.
- **Expected** — the pull-request body's review line states the routing decision and points at `docs/review-rounds.tsv`
  for the round rows.
- **Observed** — the line restated the round count and the repeated-class count; each appended ledger row falsified it,
  and round 7 raised a `contradiction` against the ledger.
- **Evidence** — round 7's finding names the body's "5 rounds (6, 2, 1, 1, 0 classes)" and "3 rows repeat a prior class"
  against the six-row ledger with four repeated rows.
- **Consequence** — one wasted round (7) and one confirmation round (8); the line now points at the ledger and states no
  count.
- **Knowledge boundary** — observed in this unit. The pattern is the review-gate skill's "self-records" failure
  handling, not a new mechanism.

- **Decision** — no-change: the documented-numbers rule already forbids a mutable count in a durable artifact.
