# Proposal

## Why

The weekly `agents-auditor` reports three standing reduction candidates (the 2026-10-04 reports, re-confirmed by the
2026-10-10 smoke-run), plus a fourth the dispatch audit flagged: `AGENTS.md` restates rules a spec or a deterministic
mechanism already owns, so the file carries condensed copies that drift from their sources. The parked idea "Trim the
AGENTS.md rules the 2026-10-04 audits found restated" (and its refinement, "Give parked reduction candidates an
application slot") notes the reduction half of the capture loop has no engine — the audit reports, but applying is a
separate owner-gated unit. This change is that unit for the current candidates.

## What Changes

- **Candidate 1 (merge)** — the pre-commit guard's check enumeration: `AGENTS.md`'s Prerequisites list and the
  `git add <dir>` gotcha state the same six checks; the gotcha's expanded form is canonical, so Prerequisites becomes a
  pointer to it.
- **Candidate 2 (merge)** — the 120-character check recipe: single-sourced in the `Formatting` convention, it is also
  restated in the "verdict echo" gotcha and in `.opencode/agent/review-quick.md`; both become pointers, each keeping its
  own lesson.
- **Candidate 3 (route)** — the `captured:` marker placement rule: the `retro-mark-captured-rules` gotcha restates a
  rule already stated (with the guard's exact binding) in the provenance rule; the gotcha's clause becomes a pointer,
  its range-boundary lesson kept.
- **Candidate 4 (merge)** — the growth-bound discipline: the capture bullet restates the "leave the file no larger" rule
  that the `agent-skills` spec and `lesson-capture.md` define; that clause becomes a pointer, keeping the promotion
  gate's one-word criteria list (a deliberate quick reminder the owner retained), the routing of a failed rule, the
  untrusted-source verification, and the applying agent's instruction to record the net delta.
- `docs/ideas.md`: remove the applied "Trim the AGENTS.md rules…" idea; leave the "application slot" refinement parked
  (this change applies the candidates, it does not build the standing mechanism).

## Capabilities

### New Capabilities

None — this is a docs/tooling trim with no spec-level behavior change (`skip_specs: true`).

### Modified Capabilities

None.

## Impact

- Files: `AGENTS.md`, `.opencode/agent/review-quick.md`, `docs/ideas.md`.
- No code, build, test, or deployment impact: every edit removes a restatement of a rule that a spec or a mechanism
  still owns; no behavior changes, and the pre-commit guard and the `check` members are untouched. `README.md` needs no
  change: it also lists the guard's checks, but it is a distinct artifact with its own owner (the `readme-auditor`), so
  its listing is out of scope here.
