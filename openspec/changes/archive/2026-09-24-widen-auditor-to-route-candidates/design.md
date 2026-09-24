# Design

## Context

See `proposal.md` — Why. Two current facts shape the approach.

The audit's conciseness axis reports two standing analyses: **merge candidates** and **removal candidates** (a rule that
governs no decision — the same test the capture filter applies). It has no lever for a rule whose content is real
guidance but whose _enforcement_ a mechanism already provides.

The capture-side promotion gate already draws that boundary: a candidate rule that "is automatable as a lint/test/CI
check at reasonable cost" is routed to a check, a spec, or an ADR instead of `AGENTS.md`. The gate runs at capture time,
so a rule that predates it — or whose subject a later change made enforceable — is never re-checked, and the growth
bound has nothing to point at it.

The user proposed the natural extension: treat a rule that is "easy to find and fix on review" as a removal candidate.
That is the unsafe version, for two reasons the design rejects below.

## Goals / Non-Goals

**Goals:**

- Give the audit a standing analysis for a rule whose subject a deterministic mechanism already enforces, so the
  always-loaded file's budget has a counterweight beyond merge and removal.
- State the boundary — deterministic mechanism, never review — so the class is decidable and the unsafe extension is
  excluded by construction.
- Keep the existing contract: report, don't edit; verify against the repository; candidate for the owner, not an action.

**Non-Goals:**

- Re-scoping what the audit may edit (it edits nothing) or the provenance partition (unchanged).
- A new output class or subagent: route is another conciseness finding, like merge and removal.
- Automating the audit or reconciling the file: the analysis only surfaces the candidate.

## Decisions

**A route candidate is a finding, not a new class.** The severity vocabulary already holds it (`redundant`), and merge
and removal are findings with a label the verdict line counts, not separate classes. Route follows the same shape: a
finding carrying a `route` label, counted in the verdict alongside `merge` and `remove`. A fourth class would imply a
different kind of object; this is the same kind — a conciseness finding. _Alternative rejected:_ a separate "routing"
section, which would duplicate the findings machinery for no distinction.

**The test is a named, in-place deterministic mechanism — the capture gate's own boundary.** The mechanism set is a
build or CI gate, a lint or test, a CLI validation, or a deterministic workflow step such as the archive-time spec sync.
The auditor must name the mechanism and confirm it exists and covers the rule's subject, so the candidate is verifiable
and not an impression. _Alternative rejected:_ "any mechanism" — vague, and would let an editor warning that no gate
reads count as enforcement, which is no enforcement at all. _Alternative rejected:_ rely on the capture gate alone — the
gate is capture-time and never revisits existing rules, so the accretion it exists to bound survives it.

**Review-detectability is rejected as a criterion, and encoded as a bound.** A rule is not a removal or route candidate
because "review would catch it": the rule is often what makes the reviewer look, so removing it can remove the detection
it is judged by (circular), and review here is not a gate — `review-quick` runs only on a change, is nondeterministic,
and the repository states a clean quick review is not a substitute for the manual pass. The bound is stated as a
scenario so the criterion cannot be re-introduced silently. _Alternative rejected:_ accepting review as a detector — it
would trade always-loaded budget for a class the corpus already covers as a `remove`-of-duplicate, and weaken the file's
own safety net to do it.

**Route and removal are distinct, and both name what would be lost.** Removal deletes a rule that governs no decision
(git preserves it). Route keeps the rule's decision but proposes reducing its _enforcement copy_ to a pointer, or moving
its content to the spec or ADR that owns it. A route candidate must therefore name the mechanism, what would be lost,
and whether the mechanism covers the subject — else it reads as a deletion in disguise. _Alternative rejected:_ folding
route into removal — the dispositions differ (one deletes, one delegates), and the owner decides differently on each.

**Candidates, not actions.** The audit proposes; the owner decides; the main agent applies what is approved. This is the
existing contract for merge and removal and carries over unchanged.

## Risks / Trade-offs

- **A route candidate can be wrong about "what would be lost"** (a rule's preventive value the mechanism only catches
  after the fact) → the candidate must name the loss, and it is a candidate for the owner, not an action; a wrong
  candidate is a line the owner rejects, not a bad edit.
- **The mechanism set could be stretched** (calling an editor inspection or a documentation note "enforcement") → the
  test names the set and requires the auditor to confirm the mechanism exists and covers the subject, and the review
  bound states the one thing that never counts.
- **A longer report** → each item is budgeted to its location and one line of evidence; the audit already reads the file
  in full.

## Migration Plan

Not applicable — no behavior, code, or deployment change. The definition takes effect on the next OpenCode reload; the
change is verified by reading the definition and the spec plus a smoke-run seeded with a mechanism-enforced rule and a
review-only rule.
