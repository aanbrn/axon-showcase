- [contradiction] openspec/changes/track-agent-session-continuity/design.md:119-122 — the replacement reason set exists
  in three inconsistent forms: the design states one shared set, the register format enumerates four and claims
  ownership, and the quick-loop clause points at a set that includes the violation label `caller-restarted`
- [contradiction] openspec/changes/track-agent-session-continuity/specs/showcase/quality/commit-hygiene/spec.md:278 — a
  non-report row's pass and transition fields are not derivable: the pass is contiguous per key while a
  `stalled`/`failed` row is said to count as no pass, and the `mismatch` row's no-transition rule appears only in a
  scenario
- [coherence] openspec/changes/track-agent-session-continuity/specs/showcase/quality/agent-skills/spec.md:233-236 — the
  discretionary replacement's approval must be recorded but neither the register's thirteen fields nor the hand-over's
  field set carries it
- [incomplete-enumeration]
  openspec/changes/track-agent-session-continuity/specs/showcase/quality/commit-hygiene/spec.md:237-238 — the
  registered-agent set has no owner that names its members, while the checker tasks require the set and fail an
  unregistered agent
- [missing-task] openspec/changes/track-agent-session-continuity/tasks.md:90-91 — the unit's own registered invocations
  are unrecorded: the deep pass task and the unit record's Plan-stage consultation write no `sessions.tsv` row or
  hand-over section
- [test-control] openspec/changes/track-agent-session-continuity/tasks.md:19-25 — three rules the mode adds have no
  known-bad case: a `report` row without a returned identifier, a `stalled`/`failed` row carrying a transition, and a
  `mismatch` row carrying one, and the append-only rule's rewritten half has no detection
- [impact-omission] scripts/claim-scan.py:37 — the new TSV kind is not exempted from the claim scan, so every future
  unit's `sessions.tsv` lines become claim candidates
- [impact-omission] AGENTS.md:1358 — the 120-character exception names only `rounds.tsv`, which a thirteen-field
  `sessions.tsv` row falsifies
- [single-sourcing] openspec/changes/track-agent-session-continuity/specs/showcase/quality/agent-skills/spec.md:86-89 —
  the quick-loop clause keeps the enumeration after pointing at the set's owner, and the amended-clause table omits the
  retained `caller-restarted` qualifier
- [claim-missing] openspec/changes/track-agent-session-continuity/proposal.md:6-7 — three absence claims have neither a
  claims-table row nor a dismissal: the quick ledger is the only per-unit session record, the deep review and advisor
  have no record or verification, and no unit holds a register today
- [claim-unreproducible] openspec/changes/track-agent-session-continuity/design.md:230 — the idea row names the
  persistent-session rejection as the entry's fourth lever where it is the first
- [structured-writing] openspec/changes/track-agent-session-continuity/design.md:117-126 — D4 hides three independently
  actionable rules in one paragraph
- [formatting] openspec/changes/track-agent-session-continuity/tasks.md:18,32,34 — inline code spans carry runs of
  spaces where the formatter joined a wrapped span
