# Design — review capture proposals against their evidence

## Context

**The verified gap.** `review-quick`'s capture check asks two questions — whether a new rule governs a decision, and
whether it extends an existing rule rather than restating one — and neither has an operational answer in its own text.
The check does not name the existing obligation to compare against, does not say that a restatement keeps its action,
and says nothing about the evidence, the remedy's boundary, the lesson's home, or the file's growth. The spec's
quick-review scenario carries the same two questions as its only capture clause. So the reviewer can echo "governs a
decision, extends an existing rule" without the materials that would establish either.

**The motivating example, from the unit's own record.** `add-extraction-procedure`'s capture proposed two candidates and
the calling agent reassessed both: a claim-sourcing clause whose action the source-derivation rule already required, and
a recurring-class candidate that inferred a drafting cause from a repeated review-class label. The record's own account
is that the first changed no action and the second's cause was not established; the retained rule is a rewrite
conditioned on a confirmed shared cause. Both rejections happened in the calling agent's judgment, not in the review
loop.

**The producer was already sharpened; the reviewer was not.** `sharpen-lesson-capture-gate` added the decision
comparison, the observation/cause separation, the preservation bound, and the supplied-evidence rule to
`.opencode/agent/lesson-capture.md` and to the capture requirement. It left `review-quick`'s capture check as it was, so
the reviewer still cannot check the gate's own outcomes against a proposal. This change closes that half.

**Single-source check.** The promotion gate's criteria, the evidence threshold, the `already covered` and `split`
dispositions, and the decision-difference test already live in `lesson-capture.md` and the `agent-skills` spec. This
change points at them rather than copying them: the reviewer applies the contract's gate and names the subjects it must
screen, the definition carries only the reviewer-specific residue (what a finding states, and the limitation), and the
spec states the reviewer's duty plus the supplied-materials and limitation outcomes.

**No parked design.** `docs/ideas.md` carries no entry about screening a capture proposal; the entries that mention
`lesson-capture` concern recording its rejected proposals and making it confirm a rule excludes its motivating incident,
which are different subjects, so neither is adopted or duplicated here.

## Goals / Non-Goals

**Goals:**

- The reviewer can establish, from supplied materials, whether a capture proposal adds a decision, is a restatement, is
  supported by the evidence, keeps what it must, belongs where it is put, and justifies its growth.
- The caller supplies those materials rather than the reviewer searching for them.
- An unavailable comparison or causal claim is reported as that specific limitation.

**Non-Goals:**

- No repository-wide search, no exhaustive reproduction of the promotion gate, and no new finding class.
- No change to `lesson-capture.md`, its gate, or the motivating capture's record.
- No verdict about the motivating example is encoded in the reviewer's instructions.

## Decisions

- **D1 — replace the existing capture check, not add a parallel checklist.** The check becomes one bullet that applies
  the contract's gate, names the subjects it screens, and keeps the reviewer-specific residue.
- **D2 — point at the gate rather than copy it.** The reviewer applies `lesson-capture.md`'s promotion gate and
  dispositions; the threshold and each criterion stay there, and the spec states the reviewer's duty and its supplied-
  materials and limitation outcomes rather than restating the criteria.
- **D3 — the comparison is conditional on supplied materials.** The hand-over and the review gate's run step name the
  existing rules, the round rows, and the change record; without them the reviewer reports a limitation instead of
  searching, and says whether the missing material is the capture's defect or the screen's scope.
- **D4 — the delta is a `MODIFIED` block.** The capture-review behavior is already part of the quick-review scenario;
  refining it costs one delta, while a new requirement would enter the corpus permanently for the same subject.
- **D5 — the example is a validation case, not text.** The four assessments below are run when the change is reviewed;
  no finding or verdict from them is written into the reviewer's instructions.

## Validation

| Case                                                                        | Supplied evidence                                                                                                                                                                                            | Result                                                                                                                                                                                                                                   |
| --------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| The pointer-source clause (`derive a claim from a pointer, not its target`) | The source-derivation bullet requires deriving a claim about another artifact by grepping the surface it summarizes; the pointer-target bullet restricts what a pointer may claim, the opposite direction    | **`already covered`** — the same action under the same condition; no decision difference                                                                                                                                                 |
| The recurring-class candidate                                               | `docs/review-rounds.tsv` records `claim-unreproducible` for rounds 1, 2, 7, and 8; the capture record states the candidate inferred a drafting cause; no supplied material confirms the instances shared one | **recurrence reported, cause withheld** — the label evidences the recurrence only, so the blanket rewrite is not justified on it; the conditioned rewrite the record retains is a distinct preventive action and remains acceptable      |
| A distinct preventive action with evidence                                  | The retained failure-handling bullet conditions a section rewrite on a confirmed shared cause, bounds what it may remove, and preserves warnings, exceptions, and identifiers                                | **acceptable** — a different action under an explicit condition, backed by the loop's existing non-converging rule and the unit's recurrence evidence                                                                                    |
| A proposal supplied without its existing rule or round rows                 | The caller supplies the proposed text only                                                                                                                                                                   | **reported limitation** — the specific comparison is unavailable; the reviewer states whether the missing material is one the capture contract requires or a limit of the screen, and reports no confirmed defect from uncertainty alone |

## Risks / Trade-offs

- **A longer capture check could drift toward a thorough pass.** The checks are bounded by the supplied materials and
  point at the gate; the criteria stay in `lesson-capture.md`, and the screen never expands its own scope.
- **A caller that omits the materials gets a limitation instead of a verdict.** That is the intended trade: an explicit
  limitation is reviewable, while an inferred comparison is not.
- **A definition edit is served from the pre-change copy until OpenCode reloads.** The edited definition's smoke-run is
  a precondition for the archive.

## Claims table

| Claim                                                                                                                                                 | Source / deriving command                                                                                                                                                                                                            |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| The reviewer's capture check asks only whether a rule governs a decision and extends an existing rule                                                 | `git show origin/main:.opencode/agent/review-quick.md`, the "A capture" bullet                                                                                                                                                       |
| The spec's quick-review scenario carries that durability clause as its only capture check                                                             | `git show origin/main:openspec/specs/showcase/quality/agent-skills/spec.md`; the scenario's "challenges each new rule's durability" clause                                                                                           |
| The capture proposed a claim-sourcing clause already covered by source derivation and inferred a drafting cause from a class label                    | `openspec/changes/archive/2026-10-19-add-extraction-procedure/tasks.md`, task 4.1                                                                                                                                                    |
| The producer's gate already carries the decision comparison, the observation/cause separation, the preservation bound, and the supplied-evidence rule | `openspec/changes/archive/2026-10-19-sharpen-lesson-capture-gate/proposal.md`, What Changes                                                                                                                                          |
| Both candidates were reassessed by the calling agent, not rejected inside the review loop                                                             | `git show origin/main:AGENTS.md`, "Workflow: Capture lessons", procedure step 3; the capture record's task 4.1                                                                                                                       |
| `claim-unreproducible` recurs in rounds 1, 2, 7, and 8 for that unit                                                                                  | `awk -F'\t' '$2=="add-extraction-procedure" && $4 ~ /claim-unreproducible/ {print $3}' docs/review-rounds.tsv` → `1 2 7 8`                                                                                                           |
| The source-derivation bullet requires re-deriving a claim from the surface it summarizes                                                              | `git show origin/main:AGENTS.md`, "Failure handling — re-deriving a non-converging loop", the `**A claim about another artifact …**` bullet                                                                                          |
| The pointer-target bullet governs what a pointer written into `AGENTS.md` may claim                                                                   | `git show origin/main:AGENTS.md`, the growth-bound bullet ("must not claim more than its target holds")                                                                                                                              |
| The failure handling conditions a section rewrite on a confirmed shared cause                                                                         | `git show origin/main:AGENTS.md`, the class-level bullet under "Failure handling — re-deriving a non-converging loop"                                                                                                                |
| The capture contract owns the decision-difference test, the observation/cause separation, and the evidence threshold                                  | `git show origin/main:.opencode/agent/lesson-capture.md`; the `agent-skills` capture scenario                                                                                                                                        |
| The review gate's run step is the caller's input site                                                                                                 | `git show origin/main:AGENTS.md`, "Workflow: The review gate", step 3's **Run** bullet                                                                                                                                               |
| The capability's Purpose names the per-unit review agents without describing their method                                                             | `git show origin/main:openspec/specs/showcase/quality/agent-skills/spec.md`, `## Purpose`                                                                                                                                            |
| The parked `lesson-capture` ideas concern other subjects (pre-change)                                                                                 | `git show origin/main:docs/ideas.md \| grep -niE "capture.{0,60}review\|review.{0,60}capture"` → no output; `git show origin/main:docs/ideas.md \| grep -n "lesson-capture"` → the rejected-proposals and incident-exclusion entries |
| The capture applied no `AGENTS.md` line                                                                                                               | `tasks.md` task 6.1's capture record (`nothing durable (net 0)`); `git diff -U0 origin/main -- AGENTS.md` shows the only hunk is the unit's Run-step clause                                                                          |

**Dismissals** (the scan's candidates resolved to neither a row nor a fact, one reason per subject): **file, command,
and symbol names** — a path, an invocation, or a slash-joined phrase — name an artifact rather than assert a fact about
the repository; **this unit's own records and results** — the ledger rows' fields, the task numbering, a task Done
note's quantifiers and recorded command outcomes, the round sequence a claims-table row's command reproduces, and the
capture's applied delta (a row above) — are the unit's own record or result, not a claim about the corpus; **copied spec
text** is the current requirement body the delta must reproduce whole, not authored prose; **modal and structural
wording inside a rule** is normative phrasing, not a claim about a corpus.
