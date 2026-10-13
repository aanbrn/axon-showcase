# Design

## Context

See `proposal.md` — Why. The evidence is the 2026-10-13 retrospective's review: eleven rounds (the session's own count;
the rounds are recorded nowhere before unit 3), where the recurring `contradiction` class was raised across several
rounds, and the owner eventually called the loop rather than let it converge. Unit 2 has since made the review
**exhaust** a class on the round that first raises it, so a later round's _new instance of an already-raised class_ now
means the exhaustion was incomplete — not that the artifact has more independent defects.

Three things differ about a **record artifact** (a retrospective, a load-test record) and make the bound safe there,
which is what keeps it a scoped rule rather than a general loop cap:

- It has no shipped behavior to converge on: no shipped behavior consumes it, so an unfound claim cannot break a build
  or a service. The digest and the `baselineTrend` view do read it, but what they report is the record's own content — a
  defect in a claim changes no consumer's behavior, only the figure the view prints.
- The digest (unit 1) derives its countable claims, so the round that would re-check them is redundant for the classes
  the block covers.
- The record's review rounds are now themselves recorded (unit 3), so stopping early is visible and measurable rather
  than a silent gap.

## Goals / Non-Goals

**Goals:**

- Bound the loop for a record artifact at the point a class has already been raised and a later round only adds its
  instances, so the owner decides whether to continue rather than the loop deciding by running.
- Keep the bound from weakening the loop for shipped behavior, where a round's coverage is the point.

**Non-Goals:**

- A general loop cap (a fixed round count): the flap the bound prevents is class-shaped, not count-shaped, and a count
  would stop a productive loop.
- Weakening the class-exhaustion rule (unit 2) or the root-cause re-derivation: the bound is what happens when
  exhaustion was incomplete, and the escalation names it.
- Automating the stop: the review reports the shape; the calling agent stops and escalates.

## Decisions

**Bound the loop at a class-shaped signal, not a round count.** A count stops a loop that is still finding independent
defects; the signal — a later round raising only new instances of an already-raised class — is exactly the
non-convergence the loop's own rule already names, so the bound reuses it rather than adding a second, competing
threshold.

**Scope it to a record artifact, and escalate rather than abandon.** The trade (a claim may ship unverified) is
acceptable only where no shipped behavior consumes the artifact; elsewhere the loop keeps its mandate to converge.
Escalation leaves the decision with the owner, matching how the loop's other non-convergence outcomes already end (a
revert, a re-derived root cause, or an abandoned unit).

**The bound is only safe because the derivation landed first.** The pre-write pass (unit 1's digest) derives the claims
a record's rounds would re-check, so the bound trades a bounded coverage loss for a bounded cost; the spec clause states
the bound beside the exhaustion and repeat-marking clauses it depends on.

## Risks / Trade-offs

- A record could ship a genuine defect the bound stopped before raising → it is a record, consumed by no shipped
  behavior, and the escalation names the class so the owner can choose to continue; the per-round record makes the stop
  visible.
- The "record artifact" scope is a judgment → the delta names the type (a retrospective, a load-test record) rather than
  a path, and a shipped artifact is out of scope by the same test (something consumes it).
- The bound could be read as licensing a sloppy first round → the class-exhaustion clause still requires the whole class
  on the round that raises it, so the bound fires only when that was missed, and the escalation says so.

## Migration Plan

None: the bullet gains one clause; no behavior is removed.
