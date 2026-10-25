# Design

## Context

`ship-change` carries two procedures. The ban on closing a long-lived tracker appears only in the second one's decision
points (`.opencode/skills/ship-change/SKILL.md`, "Complete a cloud (`/oc`) agent pull request"), where it explains that
the action's closing reference is right for a one-shot work item and wrong for a tracker the weekly workflow looks up
with `is:issue is:open`. The reason for the ban belongs to the workflow's lookup, not to the pull request's origin, so a
locally-opened pull request — which never enters that procedure — is governed by a section it has no reason to read.

The hazard is live in five workflows — `.github/workflows/{dependency,helm,buildpack,tooling}-updates.yml` and
`.github/workflows/upstream-references.yml` — each opening a fresh tracker when none is open.

The technical advisor's consultation (2026-10-25) settled two things: the rule itself is sound and already written, so
the defect is its placement; and a **post-merge check** does not clear the promotion gate — the one candidate incident
was a success (the merged pull request #690 kept tracker #308 open by its author's wording), and the existing rule
already covers the hazard. That candidate is recorded here so it is not re-litigated.

## Goals / Non-Goals

**Goals:**

- Keep exactly one live statement of the tracker branch, in the procedure every shipping path reads.
- Preserve the one-shot exception: an issue that the pull request completes may close.
- Keep the cloud section's cloud-specific provenance — the action writes the closing reference — and point it at the
  shared branch.

**Non-Goals:**

- A mandatory post-merge check of the tracker's state: declined on the advisor's review, since no confirmed incident
  exists and the rule already governs the decision.
- Automating the check: the advisor classed its cost as unmeasured, so it is not decided here.
- Any change to the shipping stage's root entry point in `AGENTS.md`: the capture's addition lands in the capture
  workflow, and the root keeps only that stage's entry point.

## Decisions

1. **Move the branch, do not copy it.** Two copies drift, and the single-sourcing rule makes the second one the defect.
2. **Scope the branch by the tracker's lifetime, not by who opened the pull request.** The reason the ban exists — the
   workflow's `is:issue is:open` lookup opening a duplicate — is independent of the pull request's origin, so the
   condition reads "a tracker that outlives the pull request", and the rule alone names the tracker issues it covers.
3. **Keep the cloud provenance as a pointer, in both places it is needed.** The cloud section still records that the
   action writes the closing reference; it no longer decides what to do with it, naming the shared branch instead. Its
   completion criterion delegates to the shipping procedure's, which gains the tracker outcome — the procedure that
   makes a decision owns its check, so the outcome is not asserted in one procedure and never verified in the other.
4. **Record the declined post-merge check in this design.** A later reader who asks why the shipping procedure does not
   verify the tracker after merging gets the answer here rather than re-proposing it.
5. **Apply one capture addition, decline the other, and decline its replacement.** The placement rule passes the gate's
   first branch on two independent occurrences, each a rule unavailable to the cases it governs: the tracker ban — one
   rule, placed twice (`#243` into `AGENTS.md`'s cloud section, `#651` into the skill's cloud procedure), which is one
   occurrence's history rather than a second case — while five workflows look a tracker up, and
   `reduce-agents-md-loaded-surface`'s first design putting five search, audit, review, and debugging rules behind a
   check-authoring trigger, caught by the owner. **Decision difference:** the existing guidance places a rule only
   inside the extraction procedure's split, so it acts when a source section is being divided and no other capture is
   asked whether the section it writes into reaches the rule's cases; the new rule makes that coverage a question at
   every capture and at a move, and names the consequence — a narrower trigger leaves the rest ungoverned with nothing
   reporting the gap. **Growth:** +5 net lines, after the extraction clause's placement sentence becomes a pointer in
   the same change, and it restates none of that clause's split, keep-at-root, or don't-broaden obligations. The review
   gate's guidance-edit check is declined: it has one occurrence (this unit's round-2 `impact-omission`) and no severe
   incident, so it meets neither branch. The capture's proposed replacement of the extraction clause is declined as
   proposed too: a whole-clause pointer would drop the obligations the addition does not carry, so only the placement
   sentence is reduced.
6. **`skip_specs: true`.** `showcase/quality/agent-skills` requires the procedures to live in the skill and to carry
   their decision branches, and neither states which procedure holds this one nor the tracker rule's wording; the
   repo-wide grep for `is:issue`/`closing keyword` under `openspec/specs/` returns no match. The capture's applied
   addition is the same kind of guidance: the spec delegates a capture's destination to the workflow, and the placement
   rule contradicts no scenario.

## Risks / Trade-offs

- **A reader of the cloud section must follow the pointer.** The pointer names the shared branch explicitly, so the cost
  is one hop within the same file; the alternative — restating the rule there — is the duplication this change removes.
- **The shared bullet must not absorb the cloud explanation.** The provenance (the action writes the reference) stays in
  the cloud section, so the shared branch reads as a rule about trackers rather than about that action.

## Verification

- `git grep` for the rule's tokens (`is:issue is:open`, `closing keyword`) shows one live statement, in the shipping
  procedure's decision points.
- `./gradlew spotlessApply` then `spotlessCheck` — the skill markdown is a Spotless target.
- `openspec validate --all` — the change validates with `skip_specs`.
- `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` — the `build` gate's definition-inventory probe resolves
  every agent, command, and skill definition with its metadata intact.
- A read-through of both procedures against their `Source:` lines and their step order.

## Claims

| Claim                                                           | Source                                                                                                                                                                                                                                              |
| --------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The rule has exactly one live statement, in the cloud procedure | `git grep -n -e "is:issue is:open" -e "closing keyword" -- . ':!openspec/changes/archive'` at branch base: the five tracker-opening workflows plus the cloud procedure's own bullet, which carried the only statement of the remedy before the move |
| No capability spec states the rule or its procedure             | `git grep -n -e "is:issue" -e "closing keyword" openspec/specs/` → no match                                                                                                                                                                         |
| The workflows open a fresh tracker when none is open            | `.github/workflows/{dependency,helm,buildpack,tooling}-updates.yml` and `.github/workflows/upstream-references.yml`, each searching `in:title "<title>" is:issue is:open` before creating one                                                       |
| The hazardous case is a success, not a reproduced failure       | Merged pull request #690 and tracker #308's state (OPEN after the merge), as the advisor's review recorded                                                                                                                                          |
| The post-merge-check candidate was declined                     | The advisor's consultation report of 2026-10-25 (session with the calling agent), recorded in this design's Context                                                                                                                                 |
| The shipping stage's root entry point needs no edit             | `AGENTS.md` "Workflow: Ship a change" carries only the trigger, the load instruction, the boundary, the ordering, and failure handling; the rule it names is the skill's                                                                            |
| The change validates with no delta                              | `openspec validate move-tracker-guard-to-shipping-path --type change` → valid with `skip_specs`                                                                                                                                                     |
| The skill definitions still resolve                             | `./gradlew check -PskipITs -Pcoverage.gate.enabled=false` → the definition-inventory probe passes                                                                                                                                                   |

Seeded at `7f76216b` and re-checked at `3456bcb1`, the base task 3.7's refresh names; every row still reproduces there.

**Dismissals.** `scripts/claim-scan.py` fires on shape rather than on claimhood, so each kind carries one class-level
reason: a `[path]` hit is a file, directory, or command mention that the gate reading it or a run of the command
resolves; a `[count]` hit is a numeral the line stating it derives; an `[absolute]` hit is a quantifying word that a
claims row carries when it spans a corpus and a task imperative does otherwise; and a `[cross-ref]` hit is a repository
path, issue number, or package scope that its tracker or lookup resolves.
