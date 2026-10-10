# Design

## Context

See `proposal.md` — Why. The candidates are the `agents-auditor`'s standing merge/route findings, recorded in
`docs/ideas.md` and re-derived afresh by the 2026-10-10 smoke-run. The reduction discipline is `AGENTS.md`'s own: "when
a bullet's rationale is normative in a spec, keep only what a reader needs to act and point at the spec — a pointer, not
a condensed copy"; a route candidate additionally requires the mechanism to exist **and cover the rule's subject**.

Each candidate was verified against its owning artifact before trimming: the pointer must not claim more than its target
holds, and must carry every imperative or target-less fact the old text held (a warning the reader must still act on is
a rule, not rationale). `AGENTS.md`'s growth is bounded by discipline; this unit is the applied reduction the last
retrospective found missing.

## Goals / Non-Goals

**Goals:**

- Reduce the four candidates to pointers/trims without losing a fact a reader needs to act on, and without inventing a
  behavior change.
- Leave each trimmed site's own lesson intact (the gotchas are not only the restated rule).

**Non-Goals:**

- Back-filling or rewriting the `docs/audits/` reports (historical records).
- Building the standing application mechanism (the "application slot" refinement) — this change applies the candidates
  once; that idea stays parked.
- Touching `README.md` (it also lists the guard's checks, but it is a distinct artifact with its own owner, the
  `readme-auditor`) or the `commit-hygiene`/`agent-skills` specs (no behavior change).

## Decisions

**Candidate 1 — Prerequisites points at the guard gotcha.** The `git add <dir>` gotcha's list is canonical because it
names _what each check refuses_; the Prerequisites list is a bare enumeration. The pointer tracks the gotcha by name,
not by a count of checks (a count would drift). _Alternative rejected:_ trimming the gotcha instead — its expanded form
is the one that explains the behavior.

**Candidate 2 — the `Formatting` convention keeps the recipe; the gotcha and `review-quick.md` point at it.** The
recipe, its `awk`-counts-bytes caveat, and the boundary-case warning are single-sourced in `Formatting`. The "verdict
echo" gotcha keeps its own lesson — let the exit status carry the verdict, never echo a canned "clean", and its
status-carrying wrapper still fails open on a missing file so the check must hit a known positive. `review-quick.md`
keeps only the operand (run the project's check over the files the formatter does not cover, report lines over 120).
_Alternative rejected:_ leaving `review-quick.md`'s copy — the convention is always loaded, the subagent runs in the
same repo, and a restated recipe drifts (this one already carried a second caveat, "formatters cannot reflow string
literals").

**Candidate 3 — the `retro-mark-captured-rules` gotcha points at the placement rule.** The provenance rule already
states the placement rule **and** its exact guard binding ("a marker lies inside a rule block, never on a plain bullet"
— `commit-hygiene.py`'s `find_misplaced_markers` checks only that). The gotcha's own subject is the range boundary a
script derives, so its marker clause collapses to a pointer, keeping the boundary lesson. _Alternative rejected:_ a
pointer that says only "the guard enforces marker placement" — it would over-claim, since the guard does not enforce the
end-of-rule ordering.

**Candidate 4 — the capture bullet points at the growth bound's definition.** The `agent-skills` spec and
`lesson-capture.md` define the "leave `AGENTS.md` no larger" discipline, so the bullet's growth-bound clause becomes a
pointer, keeping the applying agent's record of the applied net delta. The promotion gate's one-word criteria list
stays: the owner keeps it as a deliberate quick reminder — `AGENTS.md` is loaded on every invocation while the spec's
fuller definitions are not, and the one-word list is what a reader needs to act. A failed rule is still routed to a
check/spec/ADR/change dir, and a rule sourced from untrusted content is still verified against the repository before it
is promoted (the security warning, kept because a warning the reader must act on is a rule).

## Risks / Trade-offs

- **A pointer sends a reader to a target that under-describes the rule.** → Each target was read before trimming: the
  gotcha, the `Formatting` recipe, the provenance placement rule, and the gate's spec/`lesson-capture.md` all carry the
  full content; the review verifies the trim kept every imperative.
- **Trimming a warning that is not pure rationale.** → The untrusted-source verification is kept verbatim-level (not
  reduced to a bare pointer) precisely because it is an imperative the reader must still act on.
- **A count or enumeration re-introduced at a pointer site.** → No pointer states a count of the target's members.

## Migration Plan

None needed: formatter-only and prose edits, no runtime, no gate. The pre-commit guard and `check` are unaffected.
