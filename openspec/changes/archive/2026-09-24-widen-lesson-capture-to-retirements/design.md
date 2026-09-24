# Design

## Context

See `proposal.md` — Why. Two existing mechanisms bound the design, so the premise was interrogated before it was
drafted.

The archived `bound-agents-growth` change already identified retirement as unowned — "a bullet can be added in every
change while retirement happens only when someone happens to look" — and made the periodic `/audit-agents` pass the
control; this change supplies the in-flight reader that change did not add, and does not disturb that control.

The `Docs refresh on change` convention already requires the main agent to refresh `AGENTS.md` on every change. It is a
**state** refresh ("commands, config, conventions, gotchas" the change alters): it reaches a fact the change
_falsifies_, but not a rule that stays true yet is no longer needed.

The `agents-auditor` already reports removal candidates (a rule governing no decision) and route candidates (a rule an
in-place deterministic mechanism now enforces), and those standing analyses exist precisely because the brief alone did
not surface them.

The gap is the in-flight counterpart: the change that _causes_ a rule's obsolescence is the latest and cheapest moment
to retire it, and the capture is the reader best placed to report it — it already reads `AGENTS.md` in full to name the
bullet each addition extends.

## Goals / Non-Goals

**Goals:**

- Report the rules a change makes obsolete or redundant as retirement/replacement candidates, at the moment the change
  causes it.
- Keep every retirement a candidate the main agent applies, under the existing capture contract.
- Let a capture shrink the file, so the growth discipline ("prefer a merge or a replacement over an addition") gains its
  missing direction.

**Non-Goals:**

- Replacing the auditor's periodic removal/route analyses — they remain the whole-file counterweight.
- Replacing the docs-refresh convention — facts a change falsifies stay with the main agent's sweep.
- A new subagent or a new output class: the retirement analysis is a second output of the existing capture.

## Decisions

**A retirement analysis on the capture, not a widened docs-refresh obligation.** The docs-refresh is a main-agent state
refresh; this is a change-scoped redundancy read by the `AGENTS.md` specialist. Folding it into the convention would
grow an always-loaded rule and still not name a reader. _Alternative rejected:_ do nothing and rely on the audit — the
whole point is timeliness, and the auditor runs weeks later.

**Retirements are the capture's second output, labelled and counted.** Mirroring the auditor's merge/removal/route
findings, the verdict names the retirement count alongside the durable-proposal count, so a capture that retired two
rules is distinguishable from one that retired none. _Alternative rejected:_ mixing retirements into the additions list
— the two go opposite ways (the file shrinks versus grows) and the owner decides each differently.

**The analysis is nearly free because the read already happens.** The capture must name the bullet each addition
extends, so it already forms a view of `AGENTS.md`; reporting an obsolete sibling adds no read. _Alternative rejected:_
a separate pass — the audit already is one, and a second read of the same file at capture time is waste.

**Candidates, not actions.** The capture proposes; the main agent verifies and applies; the user approves. Unchanged.

## Risks / Trade-offs

- **A false retirement** (a rule that looks redundant but still governs a decision the mechanism does not) → mitigated
  by the candidate status and by requiring the candidate to name _why_ the change makes the rule obsolete or redundant;
  the auditor's route analysis carries the same risk and the same mitigation.
- **Overlap with the docs-refresh sweep** where a change both falsifies a fact and obsoletes a rule → acceptable: a
  duplicate is a line the owner dismisses, and the capture's target (the redundancy the sweep does not reach) is
  disjoint.
- **Growth of the capture definition** → bounded: one analysis and one verdict count, no new machinery.
- **The claim rests on a narrow gap** (the docs-refresh covers falsified facts; the auditor covers redundancy
  periodically) → the change is justified only by the in-flight redundancy case; if that is judged covered, the change
  should be dropped rather than shipped.

## Migration Plan

Not applicable — agent tooling takes effect on the next OpenCode reload. Verified by reading the definition and the
spec, plus a smoke-run seeded with both controls: a change that obsoletes a rule (it must yield a retirement candidate)
and one that does not (it must not).
