# Design

## Context

See `proposal.md` — Why. The evidence is the 2026-10-13 retrospective's review: eleven rounds (the session's own count;
the rounds are recorded nowhere), with the recurring `contradiction` class raised across several of them — each round
reporting the instance it found, the fix addressing it, and the next round finding another of the same shape (a
`captured:`-output mismatch, a wrong prerequisite unit, an exclusion claim contradicting its own derivation, an archive
scoping that did not resolve the same boundary). The reviewer flagged the recurrence itself on the later rounds and
asked for the root cause to be re-derived rather than the instance patched — but by then the class had already been
split across rounds.

`review-quick.md` today classifies each finding and, **when the caller supplies a prior round's list**, marks a finding
whose class a prior round raised as a repeat. That mechanism is comparative: it can only fire once a prior round exists,
so round 1 — which has no list — reports co-classed findings as independent items, and the class surfaces one instance
per round thereafter. The parked idea (2026-10-12) names exactly this gap.

## Goals / Non-Goals

**Goals:**

- Make a class surface **whole** on the round that first raises it, so a co-classed pair is one finding, not two rounds.
- Keep the existing per-finding class label and the repeat-marking mechanism, which stay correct and become a backstop.

**Non-Goals:**

- Changing what counts as a finding, or adding a class taxonomy — the exhaustion is over the artifact, not a new schema.
- Automating the search: the review is an agent pass, and the class shapes vary (a count, an enumeration, a mechanism
  claim), so the search is described rather than scripted.
- The other three review-cost units (the derived-facts digest, the per-round record, the loop bound), which are
  separate.

## Decisions

**Exhaust the class by searching the artifact, not by grouping the items already found.** "Cluster the findings you
have" would not have helped: the round had not found the second instance yet. The rule is to search the artifact for
every instance of the class's **shape** — grep a count against its set, an enumeration against the corpus it summarizes,
a mechanism claim against the tool — and report the set as one finding. That is the same remedy the review-loop bullet
already prescribes to the **caller** on a repeated class, moved upstream to the review that raises it.

**Apply on any round, the first included.** The comparative repeat-marking fires only from round 2; the exhaustion is
unconditional, so it is what makes round 1 useful rather than merely cheap.

## Risks / Trade-offs

- An exhaustion search can over-collect (a class shape that matches unrelated text) → the finding states the search it
  ran and the set it returned, so an over-collection is visible and the caller can narrow it.
- A clustered finding can hide a distinct sub-defect → the finding still lists each instance with its `file:line`, so
  the cluster is one class, not one line.
- The instruction is prose the review may under-apply → the unit's own review is the smoke-run: it must report a planted
  co-classed pair as one clustered finding, which is the check that the rule reached the definition — and it must run
  **after** OpenCode reloads the edited definition, since an edited subagent is served from the pre-change copy until
  then.

## Migration Plan

None: the definition gains one clause; no existing behavior is removed.
