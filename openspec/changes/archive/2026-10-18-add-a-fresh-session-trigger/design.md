# Design

## Context

The review loop's session rule has two triggers: the session has expired, or the reviewer's definition changed. The
parked `docs/ideas.md` entry ("Add a third fresh-session trigger to the review loop's `review-quick` session rule") is
the design this change adopts rather than re-derives: it named the observation, the two sites that state the rule, and
the fix — a `MODIFIED` delta on the requirement plus the `AGENTS.md` bullet. The premise was interrogated before
designing: the idea is the parked design, `docs/adr/` holds no decision on the review loop's session mechanics, and the
requirement it names is the live one (`openspec/specs/showcase/quality/agent-skills/spec.md`, the per-unit quality-gate
requirement's quick-review scenario).

The evidence for the third trigger is four observed instances of the same failure, each recovered the same way:

| Unit                         | Instances                                                   |
| ---------------------------- | ----------------------------------------------------------- |
| `sharpen-architecture-audit` | two, the second reused across five rounds                   |
| `make-the-capture-selective` | two — the reviewer session hung twice, after rounds 4 and 9 |

The owner confirmed the promotion at `make-the-capture-selective`'s merge.

## Goals / Non-Goals

**Goals:**

- A stalled reviewer session is a named trigger, so a caller starts a fresh one instead of waiting on a session that
  will never answer.
- The rule stays single-sourced in the requirement that owns it, with `AGENTS.md`'s bullet as its pointer.

**Non-Goals:**

- Changing the hand-over obligation. A fresh session still receives the prior findings' instances, which the requirement
  already requires on every round regardless of the reviewer's session.
- Diagnosing _why_ a session stalls. The change names the trigger; the cause is the model runtime's, not this
  repository's.
- Editing any agent definition.

## Decisions

### Decision: the trigger lives in the requirement, and the `AGENTS.md` bullet is synced to it

`AGENTS.md`'s bullet already declares the spec the rule's home ("the session-reuse rule's rationale is in
`showcase/quality/agent-skills`, not restated here"), so the delta states the outcome and the bullet's trigger list is
synced to match — a one-line edit, not a second rule.

- **Alternative — add the trigger to the bullet alone:** it would duplicate the requirement's rule and leave the
  requirement stale, the drift the "single-source a claim, a rule set, or a record" decision point warns against.

### Decision: the trigger's wording names the symptom, not a timeout

The clause reads "it stops responding mid-round (the loop has observed a session stall with no reply, which no wait
resolves)". Naming a timeout would invent a duration the repository has no measurement for; the observable is the
symptom itself, which is what the caller acts on.

## Risks / Trade-offs

A caller could start a fresh session on a slow-but-alive response, losing the reviewer's accumulated context. The
mitigation is the hand-over the requirement already requires on every round: a fresh session resumes from the caller's
record of the prior findings' instances, so the cost of an unnecessary restart is bounded to the context the hand-over
restores.

## Claims table

| Claim                                                                      | Source / deriving command                                                                                                                                                                          |
| -------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The rule has two triggers (expired, or the definition changed)             | `git show HEAD:AGENTS.md`, the session bullet; the requirement's session clause                                                                                                                    |
| Four observed instances, each recovered by a fresh session + the hand-over | `git show HEAD:docs/ideas.md`, the parked entry (naming `sharpen-architecture-audit`'s two); `docs/review-rounds.tsv`'s `make-the-capture-selective` rows 1–9                                      |
| The requirement owns the rule and `AGENTS.md` points at it                 | `AGENTS.md`'s bullet ("the session-reuse rule's rationale is in `showcase/quality/agent-skills`, not restated here")                                                                               |
| No other live site states the rule                                         | `git grep -nE "session id\|fresh session\|reviewer's session" -- AGENTS.md README.md .opencode/agent .opencode/commands openspec/specs openspec/config.yaml` → the bullet and the requirement only |
| No `## Purpose` refresh is owed                                            | `sed -n '3,22p' openspec/specs/showcase/quality/agent-skills/spec.md \| grep -ci session` → 0                                                                                                      |
| No ADR covers the review loop's session mechanics                          | `grep -rln "review-quick\|session id" docs/adr/` → no files                                                                                                                                        |

`scripts/claim-scan.py --base origin/main` also returns candidates that are not claims about evidence, dismissed by
kind: the change's own normative text and the sites it names (the delta's verbatim carry of the requirement block
included); this unit's own appended `docs/review-rounds.tsv` rows; and the pre-existing lines the formatter reflowed.
The evidence table's round references are resolved by the four-instances row above.
